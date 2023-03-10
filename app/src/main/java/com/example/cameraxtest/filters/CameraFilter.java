package com.example.cameraxtest.filters;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.example.cameraxtest.utils.GLUtil;

public class CameraFilter extends BaseFilter{

    private static final String verShader = "attribute vec4 position;\n" +
            "uniform mat4 textureTransform;\n" +
            "attribute vec4 inputTexCoordinate;\n" +
            "varying   vec2 texCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = position;\n" +
            "    texCoordinate = inputTexCoordinate.xy;\n" +
            "}";

    private static final String fraShader =
            "#version 300 es\n" +
            "#extension GL_OES_EGL_image_external_essl3 : require\n" +
            "#extension GL_EXT_YUV_target : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES texture;\n" +
            "in vec2 texCoordinate;\n" +
            "layout(yuv) out vec4 gl_FragColor;" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = texture(texture, texCoordinate);\n" +
            "}";

    private SurfaceTexture surfaceTexture;
    private float[] texTransform = new float[16];
    private int texTransformHandle;

    public CameraFilter() {
        super(verShader, fraShader);
    }

    @Override
    protected void onInitialized() {
        verCoordinate = new float[]{-1, -1, -1, 1, 1, -1, 1, 1};
        texCoordinate = new float[]{1, 1, 0, 1, 1, 0, 0, 0};
        super.onInitialized();
        texTransformHandle = GLES20.glGetUniformLocation(program, "textureTransform");
    }

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        if (surfaceTexture != null) {
            surfaceTexture.updateTexImage();
            GLUtil.checkError("updateTexImage");
            surfaceTexture.getTransformMatrix(texTransform);
            GLUtil.checkError("getTransformMatrix");
            GLES20.glUniformMatrix4fv(texTransformHandle, 1, false, texTransform, 0);
            GLUtil.checkError("glUniformMatrix4fv");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(texHandle, 0);
        }
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }
}
