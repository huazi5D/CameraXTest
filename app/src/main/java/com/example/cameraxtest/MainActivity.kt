package com.example.cameraxtest

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.View
import androidx.activity.ComponentActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.example.cameraxtest.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity(),View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private var imagePreview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var faceLens: Int = CameraSelector.LENS_FACING_FRONT
    private lateinit var cameraExecutor: ExecutorService
    private var surfaceTexture: SurfaceTexture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.cameraView.setOnViewReadyListener(object : CameraXGLSrefaceView.OnViewReadyListener {
            override fun onReady(surfaceTexture: SurfaceTexture) {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(baseContext)
                cameraProviderFuture.addListener(Runnable {
                    cameraProvider = cameraProviderFuture.get()

                    this@MainActivity.surfaceTexture = surfaceTexture
                    bindCamera(this@MainActivity.surfaceTexture!!)
                }, ContextCompat.getMainExecutor(baseContext))
            }
        })
    }

    private fun bindCamera(surfaceTexture: SurfaceTexture) {
        val cameraProvider = cameraProvider?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(faceLens).build()
        imagePreview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        surfaceTexture.setDefaultBufferSize(1920, 1080)
        imagePreview?.setSurfaceProvider(MyGlSurfaceProvider(surfaceTexture))

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this, cameraSelector, imagePreview)
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {

        }
    }

    inner class MyGlSurfaceProvider(texture: SurfaceTexture) : Preview.SurfaceProvider {

        private val surfaceTexture = texture

        override fun onSurfaceRequested(request: SurfaceRequest) {
            request.provideSurface(Surface(surfaceTexture), cameraExecutor, object : Consumer<SurfaceRequest.Result> {
                override fun accept(t: SurfaceRequest.Result?) {
                    Log.d("zhx", "accept: ${t?.resultCode}")
                }
            })
        }
    }
}
