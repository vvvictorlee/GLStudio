package com.benhero.glstudio.l2;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import com.benhero.glstudio.util.LoggerConfig;
import com.benhero.glstudio.util.ShaderHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AirHockeyRenderer2 implements Renderer {
    private static final String VERTEX_SHADER = "" +
            "uniform mat4 u_Matrix;\n" +
            "attribute vec4 a_Position;\n" +
            "attribute vec4 a_Color;\n" +
            "varying vec4 v_Color;\n" +
            "void main()\n" +
            "{\n" +
            "    v_Color = a_Color;\n" +
            "    gl_Position = u_Matrix * a_Position;\n" +
            "}";
    private static final String FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying vec4 v_Color;\n" +
            "void main()\t\t\n" +
            "{\n" +
            "    gl_FragColor = v_Color;\n" +
            "}";
    private static final String A_POSITION = "a_Position";
    private static final String U_MATRIX = "u_Matrix";
    private static final String A_COLOR = "a_Color";
    private final Context mContext;
    private int mProgram;
    private final FloatBuffer mVertexData;
    private int aColorLocation;
    private int aPositionLocation;

    private static final float[] POINT_DATA = {
            -0.5f, -0.5f, 1f, 1f, 1f,
            0.5f, -0.5f, 1f, 0f, 1f,
            -0.5f, 0.5f, 0f, 1f, 1f,
            0.5f, 0.5f, 1f, 1f, 0f,
    };
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int BYTES_PER_FLOAT = 4;

    /**
     * 数据数组中每个顶点起始数据的间距：数组中每个顶点占的Byte值
     */
    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private int uMatrixLocation;
    private final float[] projectionMatrix = new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1,
    };

    public AirHockeyRenderer2(Context context) {
        mContext = context;

        mVertexData = ByteBuffer
                .allocateDirect(POINT_DATA.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        mVertexData.put(POINT_DATA);
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        int vertexShader = ShaderHelper.compileVertexShader(VERTEX_SHADER);
        int fragmentShader = ShaderHelper.compileFragmentShader(FRAGMENT_SHADER);

        mProgram = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(mProgram);
        }

        GLES20.glUseProgram(mProgram);

        aPositionLocation = GLES20.glGetAttribLocation(mProgram, A_POSITION);
        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, U_MATRIX);
        aColorLocation = GLES20.glGetAttribLocation(mProgram, A_COLOR);

        mVertexData.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation,
                POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT, false, STRIDE, mVertexData);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        mVertexData.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aColorLocation,
                COLOR_COMPONENT_COUNT, GLES20.GL_FLOAT, false, STRIDE, mVertexData);
        GLES20.glEnableVertexAttribArray(aColorLocation);
    }

    /**
     * onSurfaceChanged is called whenever the surface has changed. This is
     * called at least once when the surface is initialized. Keep in mind that
     * Android normally restarts an Activity on rotation, and in that case, the
     * renderer will be destroyed and a new one created.
     *
     * @param width  The new width, in pixels.
     * @param height The new height, in pixels.
     */
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        GLES20.glViewport(0, 0, width, height);

        final float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;

        if (width > height) {
            // Landscape
            Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
            // Portrait or square
            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
    }

    /**
     * OnDrawFrame is called whenever a new frame needs to be drawn. Normally,
     * this is done at the refresh rate of the screen.
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {
        // Clear the rendering surface.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Assign the matrix
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        // Draw the table.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}