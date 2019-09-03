package com.example.cameraxtest

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES11Ext
import java.nio.FloatBuffer


class CameraXGLSrefaceView :GLSurfaceView, SurfaceTexture.OnFrameAvailableListener {

    var mContext: Context? = null
    var textureId: Int = 0

    private var mCameraTexture: SurfaceTexture? = null


    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setEGLContextClientVersion(2)
        setRenderer(Render())
        renderMode = RENDERMODE_CONTINUOUSLY
        mContext = context
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        requestRender()
    }

    fun setSurfaceTexture(surfaceTexture: SurfaceTexture?) {
        mCameraTexture = surfaceTexture
        queueEvent(Runnable {

            mCameraTexture?.attachToGLContext(textureId)
        })

    }

    inner class Render : Renderer {

        private var mProgram: Int = 0

        private var uPosHandle: Int = 0
        private var aTexHandle: Int = 0
        private var mMVPMatrixHandle: Int = 0

        private var mPosCoordinate = floatArrayOf(-1f, -1f, -1f, 1f, 1f, -1f, 1f, 1f)
        private var mTexCoordinate = floatArrayOf(0f, 1f, 0f, 0f, 1f, 1f, 1f, 0f)

        private var mPosBuffer: FloatBuffer? = null
        private var mTexBuffer: FloatBuffer? = null

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            mCameraTexture?.updateTexImage()
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mPosCoordinate.count() / 2)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)

            mProgram = ShaderUtils.createProgram(mContext, "vertex_texture.glsl", "fragment_texture.glsl")
            GLES20.glUseProgram(mProgram)

            createAndBindVideoTexture()

            uPosHandle           = GLES20.glGetAttribLocation (mProgram, "position")
            aTexHandle           = GLES20.glGetAttribLocation (mProgram, "inputTextureCoordinate")
            mMVPMatrixHandle    = GLES20.glGetUniformLocation(mProgram, "textureTransform")

            mPosBuffer = BufferUtil.convertToFloatBuffer(mPosCoordinate)
            mTexBuffer = BufferUtil.convertToFloatBuffer(mTexCoordinate)

            GLES20.glVertexAttribPointer(uPosHandle, 2, GLES20.GL_FLOAT, false, 0, mPosBuffer)
            GLES20.glVertexAttribPointer(aTexHandle, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer)

            GLES20.glEnableVertexAttribArray(uPosHandle)
            GLES20.glEnableVertexAttribArray(aTexHandle)
        }

        private fun createAndBindVideoTexture() {
            val texture = IntArray(1)
            GLES20.glGenTextures(1, texture, 0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
            GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
            textureId = texture[0]
        }

    }
}