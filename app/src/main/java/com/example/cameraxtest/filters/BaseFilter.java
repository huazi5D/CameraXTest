package com.example.cameraxtest.filters;

import android.opengl.GLES20;

import com.example.cameraxtest.utils.BufferUtil;
import com.example.cameraxtest.utils.GLUtil;

import java.nio.FloatBuffer;

public class BaseFilter {

    protected int program;

    protected int verCoordinateHandle;
    protected int texCoordinateHandle;
    protected int texHandle;

    protected float[] verCoordinate = {-1, -1, -1, 1, 1, -1, 1, 1};
    protected float[] texCoordinate = {0, 1, 0, 0, 1, 1, 1, 0};

    protected FloatBuffer verBuffer;
    protected FloatBuffer texBuffer;

    private final String verShader;
    private final String fraShader;

    protected int textureId;
    private boolean isInitialized = false;

    protected static final String NO_FILTER_VERTEX_SHADER = "attribute vec4 position;\n" +
            "attribute vec4 inputTexCoordinate;\n" +
            "varying vec2 texCoordinate;\n" +
            "\n" +
            "void main () {\n" +
            "    texCoordinate = inputTexCoordinate.xy;\n" +
            "    gl_Position = position;\n" +
            "}";

    protected static final String NO_FILTER_FRAGMENT_SHADER = "precision mediump float;\n" +
            "uniform sampler2D texture;\n" +
            "varying vec2 texCoordinate;\n" +
            "\n" +
            "void main () {\n" +
            "    vec4 color = texture2D(texture, texCoordinate);\n" +
            "    gl_FragColor = color;\n" +
            "}";

    public BaseFilter() {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
    }

    public BaseFilter(String verShader, String fraShader) {
        this.verShader = verShader;
        this.fraShader = fraShader;
    }

    private void init() {
        program = GLUtil.createProgram(verShader, fraShader);
        GLES20.glUseProgram(program);
        verCoordinateHandle    = GLES20.glGetAttribLocation(program, "position");
        texCoordinateHandle    = GLES20.glGetAttribLocation(program, "inputTexCoordinate");
        texHandle              = GLES20.glGetUniformLocation(program, "texture");

        onInitialized();

        verBuffer = BufferUtil.convertToFloatBuffer(verCoordinate);
        texBuffer = BufferUtil.convertToFloatBuffer(texCoordinate);
        isInitialized = true;
    }

    protected void onInitialized() {

    }

    public void draw() {
        if (!isInitialized) {
            init();
        }
        GLES20.glUseProgram(program);
        verBuffer.position(0);
        GLES20.glVertexAttribPointer(verCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, verBuffer);
        GLES20.glEnableVertexAttribArray(verCoordinateHandle);

        texBuffer.position(0);
        GLES20.glVertexAttribPointer(texCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer);
        GLES20.glEnableVertexAttribArray(texCoordinateHandle);

        onDrawArraysPre();

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, verCoordinate.length / 2);

        GLUtil.checkError("glDrawArrays");
        GLES20.glDisableVertexAttribArray(verCoordinateHandle);
        GLES20.glDisableVertexAttribArray(texCoordinateHandle);
        GLUtil.checkError("glDisableVertexAttribArray");
    }

    protected void onDrawArraysPre() {

    }

    public void updateTextureId(int textureId) {
        this.textureId = textureId;
    }
}
