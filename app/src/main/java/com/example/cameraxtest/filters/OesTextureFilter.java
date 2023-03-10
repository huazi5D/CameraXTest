package com.example.cameraxtest.filters;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

public class OesTextureFilter extends BaseFilter {

    private static final String verShader = "attribute vec4 position;\n" +
            "uniform mat4 textureTransform;\n" +
            "attribute vec4 inputTexCoordinate;\n" +
            "varying   vec2 texCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = position;\n" +
            "    texCoordinate = inputTexCoordinate.xy;\n" +
            "}";

    private static final String fraShader = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES videoTex;\n" +
            "varying vec2 texCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(videoTex, texCoordinate);\n" +
            "}";

    public OesTextureFilter() {
        super(verShader, fraShader);
    }

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(texHandle, 0);
    }

}
