package com.example.cameraxtest

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.cameraxtest.filters.CameraFilter
import com.example.cameraxtest.filters.OesTextureFilter
import com.example.cameraxtest.filters.TextureFilter
import com.example.cameraxtest.utils.GLUtil
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class CameraXGLSrefaceView :GLSurfaceView, SurfaceTexture.OnFrameAvailableListener {

    interface OnViewReadyListener {
        fun onReady(surfaceTexture: SurfaceTexture);
    }

    var mContext: Context? = null

    private var surfaceTexture: SurfaceTexture? = null
    private var onViewReadyListener: OnViewReadyListener? = null


    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setEGLContextClientVersion(2)
        setRenderer(Render())
        renderMode = RENDERMODE_WHEN_DIRTY
        mContext = context
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        requestRender()
    }

    fun setOnViewReadyListener(listener: OnViewReadyListener) {
        onViewReadyListener = listener
    }

    inner class Render : Renderer {
        private var oesId = -1
        private var frameBufferId = -1
        private var texture2d = 0
        private var oesTexture = 0
        private val camera_width = 1080
        private var camera_height = 1920
        private val cameraFilter: CameraFilter = CameraFilter()
        private val textureFilter: TextureFilter = TextureFilter()
        private val oesTextureFilter: OesTextureFilter = OesTextureFilter()

        private var mGLCopyJni: GLCopyJni? = null

        var w = 0
        var h = 0
        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//            GLUtil.bindFBO(frameBufferId, texture2d)
            GLUtil.bindOesFBO(frameBufferId, oesTexture)

            GLES20.glViewport(0, 0, camera_width, camera_height)
            cameraFilter.draw()
            GLUtil.checkError("cameraFilter.draw()")
            GLUtil.unbindFBO()
            GLUtil.checkError("unbindFBO")

            var array = mGLCopyJni?.buffer
            GLES20.glViewport(0, 0, w, h)
//            textureFilter.updateTextureId(texture2d)
//            textureFilter.draw()
            oesTextureFilter.updateTextureId(oesTexture)
            oesTextureFilter.draw()
            GLUtil.checkError("oesTextureFilter.draw()")
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            w = width
            h = height
//            texture2d = GLUtil.createTexture2D(camera_width, camera_height)
//            mGLCopyJni = GLCopyJni(1080, 1920, texture2d)
//            frameBufferId = GLUtil.createFrameBuffer(texture2d)

            oesTexture = GLUtil.createOESTexture(camera_width, camera_height)
            // 必须先绑定 否则下一步创建framebuffer会失败
            mGLCopyJni = GLCopyJni(1080, 1920, oesTexture)
            frameBufferId = GLUtil.createOesFrameBuffer(oesTexture)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            oesId = GLUtil.createOESTexture(camera_width, camera_height)
            surfaceTexture = SurfaceTexture(oesId)
            surfaceTexture?.setOnFrameAvailableListener(this@CameraXGLSrefaceView)
            onViewReadyListener?.onReady(surfaceTexture!!)
            cameraFilter.setSurfaceTexture(surfaceTexture)
        }

    }
}