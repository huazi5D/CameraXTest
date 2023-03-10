package com.example.cameraxtest.filters;

import android.opengl.GLES20;
import android.util.Log;

import com.example.cameraxtest.utils.BufferUtil;

import java.nio.FloatBuffer;

public class MoveDelayFilter extends BaseFilter {

    private static final String verShader = "uniform mat3 _affine2D_or_homography3D;\n" +
            "uniform   int   _method;\n" +
            "uniform   vec2  _wid_hei;\n" +
            "attribute vec4  position;\n" +
            "attribute vec4  inputTexCoordinate;\n" +
            "varying   vec2  _outUV;\n" +
            "varying   vec2  _outIsH3D;\n" +
            "varying   mat3  _outH2D3D;\n" +
            "varying   vec2  _outWidHei;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "   _outH2D3D   =   _affine2D_or_homography3D;\n" +
            "   _outIsH3D.x =   float(_method);\n" +
            "   _outWidHei  =   _wid_hei;\n" +
            "   _outUV      =   inputTexCoordinate.xy;\n" +
            "   gl_Position =   position;\n" +
            "}";

    private static final String fraShader = "precision mediump float;\n" +
            "uniform    sampler2D   _texture;\n" +
            "varying    vec2        _outUV;\n" +
            "varying    vec2        _outWidHei;\n" +
            "varying    vec2        _outIsH3D;\n" +
            "varying    mat3        _outH2D3D;\n" +
            "\n" +
            "void main () {\n" +
            "   float sx, sy;\n" +
            "   vec3 dst1 = vec3(_outUV[0] * _outWidHei[0], _outUV[1] * _outWidHei[1], 1.0);\n" +
            "   vec3 src  = _outH2D3D * dst1;\n" +
            "   if (_outIsH3D.x > 1.0){\n" +
            "       sx  = src[0] / _outWidHei[0] / src[2];\n" +
            "       sy  = src[1] / _outWidHei[1] / src[2];\n" +
            "   }else{\n" +
            "       sx  = src[0] / _outWidHei[0];\n" +
            "       sy  = src[1] / _outWidHei[1];\n" +
            "   }\n" +
            "   if (_outUV.s < 0.0 || _outUV.t < 0.0 || _outUV.s > 1.0 || _outUV.t > 1.0) {\n" +
            "      gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);\n" +
            "   } else {\n" +
            "      gl_FragColor = texture2D(_texture, vec2(sx,sy));\n" +
            "   }\n" +
//            "      gl_FragColor = texture2D(_texture, _outUV);\n" +
            "}";

    private int _textureHandle;
    private int _affine2D_or_homography3DHandle;
    private int _wid_heiHandle;
    private int _methodHandle;
    private FloatBuffer algorithmResults;
    private FloatBuffer whBuffer;

    public MoveDelayFilter() {
        super(verShader, fraShader);
    }

    @Override
    protected void onInitialized() {
        verCoordinate = new float[]{-1, 1, -1, -1, 1, 1, 1, -1};
        texCoordinate = new float[]{0, 1, 0, 0, 1, 1, 1, 0};
        super.onInitialized();
        _textureHandle = GLES20.glGetUniformLocation(program, "_texture");

        // 2D 放射变换参数
        _affine2D_or_homography3DHandle = GLES20.glGetUniformLocation(program, "_affine2D_or_homography3D");
        _wid_heiHandle = GLES20.glGetUniformLocation(program, "_wid_hei");
        _methodHandle  = GLES20.glGetUniformLocation(program, "_method");
    }

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        GLES20.glUniform1i(_textureHandle, 0);
        algorithmResults.rewind();
        GLES20.glUniformMatrix3fv(_affine2D_or_homography3DHandle, 1, true, algorithmResults);
        GLES20.glUniform1i(_methodHandle, 2);
        GLES20.glUniform2fv(_wid_heiHandle,  1, whBuffer);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
    }

    public void setAffine2DInvOrHomography3D(float[] affine2D_inv_OR_homography3D) {
        Log.d("zhx", "setAffine2DInvOrHomography3D: " + affine2D_inv_OR_homography3D.length);
        if (algorithmResults == null) {
            algorithmResults = BufferUtil.convertToFloatBuffer(affine2D_inv_OR_homography3D);
        } else {
            algorithmResults.rewind();
            algorithmResults.put(affine2D_inv_OR_homography3D);
        }
    }

    public void setVideoSize(int w, int h) {
        if (whBuffer == null) {
            float[] wh = new float[]{w, h};
            whBuffer = BufferUtil.convertToFloatBuffer(wh);
        }
    }
}
