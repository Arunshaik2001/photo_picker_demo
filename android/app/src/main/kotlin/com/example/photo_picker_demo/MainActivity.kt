package com.example.photo_picker_demo

import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.lang.ref.WeakReference


class MainActivity: FlutterFragmentActivity() {
    companion object{
        private const val PHOTO_PICKER_METHOD_CHANNEL: String = "photo_picker_method_channel"
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        handlePhotoPickerHandler(flutterEngine)
    }

    private fun handlePhotoPickerHandler(flutterEngine: FlutterEngine) {
        val photoPickerMethodChannelHandler = PhotoPickerMethodChannelHandler(WeakReference(this))
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, PHOTO_PICKER_METHOD_CHANNEL)
            .setMethodCallHandler(photoPickerMethodChannelHandler)
    }
}
