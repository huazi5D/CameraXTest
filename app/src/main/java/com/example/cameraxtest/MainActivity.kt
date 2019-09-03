package com.example.cameraxtest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start.setOnClickListener({v: View? ->
            val previewConfig = PreviewConfig.Builder().build()
            val preview = Preview(previewConfig)
            preview.setOnPreviewOutputUpdateListener {
                texture_view.setSurfaceTexture(it.surfaceTexture)
            }
            CameraX.bindToLifecycle(this as LifecycleOwner, preview)
        })
    }
}
