package com.example.cameraxtest.filters;

import android.graphics.SurfaceTexture;

import com.example.cameraxtest.utils.GLUtil;

public class CameraFilter extends BaseFilter{

//    private static final String verShader =
//            "#version 300 es\n" +
//            "in vec4 position;\n" +
//            "in vec4 inputTexCoordinate;\n" +
//            "out vec2 texCoordinate;\n" +
//            "\n" +
//            "void main() {\n" +
//            "    gl_Position = position;\n" +
//            "    texCoordinate = inputTexCoordinate.xy;\n" +
//            "}";

    private static final String verShader = "attribute vec4 position;\n" +
            "attribute vec4 inputTexCoordinate;\n" +
            "varying   vec2 texCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = position;\n" +
            "    texCoordinate = inputTexCoordinate.xy;\n" +
            "}";

//             yuv转换的一个例子
//             #version 300 es
//             #extension GL_EXT_YUV_target: require
//             precision mediump float;
//             in vec2 v_texCoord;
//             layout(yuv) out vec4 outColor;
//             uniform sampler2D s_texture;
//             void main()
//             {
//                 //色彩空间标准公式
//                 yuvCscStandardEXT conv_standard = itu_601_full_range;

//                 vec4 rgbaColor = texture(s_texture, v_texCoord);
//                 //dealwith rgba

//                 vec3 rgbColor = rgbaColor.rgb;
//                 vec3 yuv = rgb_2_yuv(rgbColor, conv_standard);//实现 RGB 到 YUV 的格式转换
//                 outColor = vec4(yuv, 1.0);
//             }

//    private static final String fraShader =
//            "#version 300 es\n" +
//            "#extension GL_OES_EGL_image_external_essl3 : require\n" +
//            "precision mediump float;\n" +
//            "uniform samplerExternalOES texture;\n" +
//            "in vec2 texCoordinate;\n" +
////            "layout(yuv) out vec4 gl_FragColor;" +
//            "out vec4 gl_FragColor;" +
//            "\n" +
//            "void main() {\n" +
//            "    gl_FragColor = texture(texture, texCoordinate);\n" +
//            "}";

    private static final String fraShader = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES texture;\n" +
            "varying vec2 texCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(texture, texCoordinate);\n" +
            "}";

    private SurfaceTexture surfaceTexture;

    public CameraFilter() {
        super(verShader, fraShader);
    }

    @Override
    protected void onInitialized() {
        verCoordinate = new float[]{-1, -1, -1, 1, 1, -1, 1, 1};
        texCoordinate = new float[]{1, 1, 0, 1, 1, 0, 0, 0};
        super.onInitialized();
    }

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
        if (surfaceTexture != null) {
            surfaceTexture.updateTexImage();
            GLUtil.checkError("updateTexImage");
        }
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }
}
