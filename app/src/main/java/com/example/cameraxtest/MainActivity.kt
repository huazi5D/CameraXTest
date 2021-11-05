package com.example.cameraxtest

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import androidx.activity.ComponentActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.example.cameraxtest.databinding.ActivityMainBinding
import java.lang.IllegalStateException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity(),View.OnClickListener, View.OnTouchListener {

    private lateinit var binding: ActivityMainBinding
    private var imagePreview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var faceLens: Int = CameraSelector.LENS_FACING_BACK
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF
    private var cameraControl: CameraControl? = null
    private var touchTime = 0L
    private var surfaceTexture: SurfaceTexture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()

//        binding.cameraView.post {
//            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//            cameraProviderFuture.addListener(Runnable {
//                cameraProvider = cameraProviderFuture.get()
//
//                bindCamera()
//            }, ContextCompat.getMainExecutor(this))
//        }

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

        binding.switchCamera.setOnClickListener(this);
        binding.capture.setOnClickListener(this)
        binding.flashMode.setOnClickListener(this)
        binding.cameraView.setOnTouchListener(this)
    }

    private fun bindCamera(surfaceTexture: SurfaceTexture) {
        var cameraProvider = cameraProvider?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(faceLens).build()
        imagePreview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

//        binding.cameraView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//        imagePreview?.setSurfaceProvider(binding.cameraView.surfaceProvider)
        surfaceTexture.setDefaultBufferSize(1920, 1080)
        imagePreview?.setSurfaceProvider(MyGlSurfaceProvider(surfaceTexture))

        cameraProvider.unbindAll()
        var camera = cameraProvider.bindToLifecycle(this, cameraSelector, imagePreview, imageCapture)
        cameraControl = camera.cameraControl
        // open flash
        // cameraControl?.enableTorch(true)
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.switch_camera -> {
                faceLens = if (faceLens == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                bindCamera(surfaceTexture!!)
            }

            R.id.capture -> {
                imageCapture?.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {

                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        Log.d("zhx111", "onCaptureSuccess: ")
                        image.close()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        Log.d("zhx111", "onError: ")
                    }
                })
            }

            R.id.flash_mode -> {
                var currentMode = ++ flashMode % 3
                imageCapture?.flashMode = currentMode
                binding.flashMode.setImageResource(
                    if (currentMode == ImageCapture.FLASH_MODE_ON)
                        R.drawable.ic_baseline_flash_on_24
                    else if (currentMode == ImageCapture.FLASH_MODE_OFF)
                        R.drawable.ic_baseline_flash_off_24
                    else R.drawable.ic_baseline_flash_auto_24
                )
            }
        }
    }

    private fun touchFocus(w:Float, h:Float, x: Float, y: Float) {
        // TODO: 2021/11/5 计算聚焦点坐标
        val factory = SurfaceOrientedMeteringPointFactory(w, h)
//        val factory = binding.cameraView.meteringPointFactory
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()

        val future = cameraControl?.startFocusAndMetering(action)
        future?.addListener( Runnable {
            val result = future.get()
            // process the result
            if (result.isFocusSuccessful) {
                Log.d("zhx", "touchFocus: focus success!!!")
            } else {
                Log.d("zhx", "touchFocus: focus failed!!!")
            }
        } , cameraExecutor)
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        when(p1?.action) {
            MotionEvent.ACTION_DOWN -> touchTime = System.currentTimeMillis()
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - touchTime < 500) {
                    var x = p1.x / p0?.width!!
                    var y = p1.y / p0.height
                    Log.d("zhx111", "onTouch: ${x}  ${y}")
                    touchFocus(p0.width.toFloat(), p0.height.toFloat(), p1.x, p1.y)
                }
            }
        }
        return true
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
