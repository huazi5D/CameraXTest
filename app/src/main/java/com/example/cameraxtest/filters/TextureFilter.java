package com.example.cameraxtest.filters;

import android.opengl.GLES20;

public class TextureFilter extends BaseFilter {

    public TextureFilter() {

    }

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
    }

}
