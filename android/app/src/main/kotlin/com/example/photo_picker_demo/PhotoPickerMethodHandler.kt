package com.example.photo_picker_demo

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.*
import java.lang.ref.WeakReference

class PhotoPickerMethodChannelHandler(
    private val activity: WeakReference<Activity>,
) : MethodChannel.MethodCallHandler {

    private lateinit var pendingResult: MethodChannel.Result

    private val pickMedia = (activity.get() as ComponentActivity).registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val path = getPathFromUri(activity.get()!!.applicationContext,uri)
            pendingResult.success(path)
        } else {
            pendingResult.success(null)
        }
    }

    private val pickMultipleMedia = (activity.get() as ComponentActivity).registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val path = getPathFromUri(activity.get()!!.applicationContext,uri)
            pendingResult.success(path)
        } else {
            pendingResult.success(null)
        }
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "pickMedia" -> pickMedia(call,result)
            "pickMultipleMedia" -> pickMultipleMedia(call,result)
            else -> result.notImplemented()
        }
    }

    private fun pickMultipleMedia(call: MethodCall, result: MethodChannel.Result) {
        this.pendingResult = result
        val items = call.argument<Int>("items") ?: 1

        when(call.argument<String>("file_type") ?: "image"){
            "media" -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            "image" -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            "video" -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }
    }

    private fun pickMedia(call: MethodCall, result: MethodChannel.Result) {
        this.pendingResult = result

        when(call.argument<String>("file_type") ?: "image"){
            "media" -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            "image" -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            "video" -> pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }
    }

    private fun getPathFromUri(context: Context, uri: Uri): String? {
        var file: File? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var success = false
        try {
            val extension = getImageExtension(context, uri)
            inputStream = context.contentResolver.openInputStream(uri)
            file = File.createTempFile("image_picker", extension, context.cacheDir)
            file.deleteOnExit()
            outputStream = FileOutputStream(file)
            if (inputStream != null) {
                copy(inputStream, outputStream)
                success = true
            }
        } catch (ignored: IOException) {
        } finally {
            try {
                inputStream?.close()
            } catch (ignored: IOException) {
            }
            try {
                outputStream?.close()
            } catch (ignored: IOException) {
                success = false
            }
        }
        return if (success) file!!.path else null
    }

    private fun getImageExtension(context: Context, uriImage: Uri): String {
        var extension: String? = null
        extension = try {
            val imagePath = uriImage.path
            if (uriImage.scheme == ContentResolver.SCHEME_CONTENT) {
                val mime = MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(context.contentResolver.getType(uriImage))
            } else {
                MimeTypeMap.getFileExtensionFromUrl(
                    Uri.fromFile(File(uriImage.path)).toString()
                )
            }
        } catch (e: Exception) {
            null
        }
        if (extension == null || extension.isEmpty()) {
            extension = "jpg"
        }
        return ".$extension"
    }

    @Throws(IOException::class)
    private fun copy(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(4 * 1024)
        var bytesRead: Int
        while (`in`.read(buffer).also { bytesRead = it } != -1) {
            out.write(buffer, 0, bytesRead)
        }
        out.flush()
    }

}