package com.example.cameraxtest.filters;

import android.opengl.GLES20;

import com.example.cameraxtest.utils.BufferUtil;
import com.example.cameraxtest.utils.GLUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class PictureFilter {

    public interface OnPictureListener {
        void onPicture(ByteBuffer buffer, int w, int h);
    }

    private int mShaderProgram;
    private int mTextureCoordinateHandle;
    private int mPositionHandle;
    private int mTextureHandle;

    private float[] mTextureCoords = {0, 0, 0, 1, 1, 0, 1, 1};
    private float[] sSquareCoords = {-1, -1, -1, 1, 1, -1, 1, 1};
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private int frameBufferId = -1;
    private int textureId = -1;
    private OnPictureListener pictureListener;
    private int w, h;

    private String vertexSharder = "attribute vec4 vPosition;\n" +
            "attribute vec4 vTexCoordinate;\n" +
            "uniform mat4 textureTransform;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "\n" +
            "void main () {\n" +
            "    v_TexCoordinate = vTexCoordinate.xy;\n" +
            "    gl_Position = vPosition;\n" +
            "}";
    private String fragmentSharder = "precision mediump float;\n" +
            "uniform sampler2D texture;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "\n" +
            "void main () {\n" +
            "    vec4 color = texture2D(texture, v_TexCoordinate);\n" +
            "    gl_FragColor = color;\n" +
            "}";

    public PictureFilter(int w, int h) {
        this.w = w;
        this.h = h;
        mShaderProgram = GLUtil.createProgram(vertexSharder, fragmentSharder);
        GLES20.glUseProgram(mShaderProgram);
        mTextureCoordinateHandle    = GLES20.glGetAttribLocation(mShaderProgram, "vTexCoordinate");
        mPositionHandle             = GLES20.glGetAttribLocation(mShaderProgram, "vPosition");
        mTextureHandle              = GLES20.glGetUniformLocation(mShaderProgram, "texture");

        mVertexBuffer = BufferUtil.convertToFloatBuffer(sSquareCoords);
        mTextureBuffer = BufferUtil.convertToFloatBuffer(mTextureCoords);

        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        textureId = GLUtil.createTexture2D(w, h);
        frameBufferId = GLUtil.createFrameBuffer(textureId);
    }

    long time;
    public int draw(int texture2d) {
        GLES20.glUseProgram(mShaderProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2d);

        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
//
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glUniform1i(mTextureHandle, 0);
        GLUtil.bindFBO(frameBufferId, textureId);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sSquareCoords.length / 2);
//        Log.d("zhx1", "draw: " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
//        if (pictureListener != null) {
//            ByteBuffer buf = ByteBuffer.allocateDirect(w * h * 4);
//            buf.order(ByteOrder.LITTLE_ENDIAN);
//            GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
//            buf.rewind();
//            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//            bitmap.copyPixelsFromBuffer(buf);
//            pictureListener.onPicture(buf, w, h);
//        }

        GLUtil.unbindFBO();
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
        return textureId;
    }

    public void setPictureListener(OnPictureListener listener) {
        pictureListener = listener;
    }

    public void release() {
        if (textureId != -1) {
            GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
            textureId = -1;
        }

        if (frameBufferId != -1) {
            GLES20.glDeleteFramebuffers(1, new int[]{frameBufferId}, 0);
            frameBufferId = -1;
        }
    }
}
