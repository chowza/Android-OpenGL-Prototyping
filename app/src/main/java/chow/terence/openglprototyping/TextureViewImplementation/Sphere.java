package chow.terence.openglprototyping.TextureViewImplementation;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import chow.terence.openglprototyping.Helpers;
import chow.terence.openglprototyping.R;


public class Sphere {
    final int BYTES_PER_VERTEX = 4 * 3;
    final int BYTES_PER_NORMAL = 4 * 3;
    final int BYTES_PER_TEXTURE_COORD = 4 * 2;
    final int BYTES_PER_TRIANGLE_INDEX = 2 * 6;

    public FloatBuffer vertexBuffer;
    public FloatBuffer normalBuffer;
    public FloatBuffer textureCoordBuffer;
    public ShortBuffer drawListBuffer;
    public int drawListOrderLength;
    private Context ctx;
    private int vertexShaderHandle;
    private int fragmentShaderHandle;
    private int shaderProgram;
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    public Sphere(Context ctx, float radius, int stacks, int slices)
    {

        this.ctx = ctx;
        int vertexCount = (stacks + 1) * (slices + 1);
        vertexBuffer        = ByteBuffer.allocateDirect(vertexCount * BYTES_PER_VERTEX).order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer        = ByteBuffer.allocateDirect(vertexCount * BYTES_PER_NORMAL).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureCoordBuffer  = ByteBuffer.allocateDirect(vertexCount * BYTES_PER_TEXTURE_COORD).order(ByteOrder.nativeOrder()).asFloatBuffer();
        drawListBuffer = ByteBuffer.allocateDirect(vertexCount * BYTES_PER_TRIANGLE_INDEX).order(ByteOrder.nativeOrder()).asShortBuffer();
        drawListOrderLength = 0;

        for (int stackNumber = 0; stackNumber <= stacks; ++stackNumber)
        {
            for (int sliceNumber = 0; sliceNumber <= slices; ++sliceNumber)
            {
                float theta = (float) (stackNumber * Math.PI / stacks);
                float phi = (float) (sliceNumber * 2 * Math.PI / slices);
                float sinTheta = (float) Math.sin(theta);
                float sinPhi = (float) Math.sin(phi);
                float cosTheta = (float) Math.cos(theta);
                float cosPhi = (float) Math.cos(phi);

                float nx = cosPhi * sinTheta;
                float ny = cosTheta;
                float nz = sinPhi * sinTheta;


                float x = radius * nx;
                float y = radius * ny;
                float z = radius * nz + 10;

                float u = 1.f - ((float)sliceNumber / (float)slices);
                float v = (float)stackNumber / (float)stacks * 0.5f; //take half because over under video
//                float v2 = (float)stackNumber / (float)stacks / 0.5f;



                normalBuffer.put(nx);
                normalBuffer.put(ny);
                normalBuffer.put(nz);

                vertexBuffer.put(x);
                vertexBuffer.put(y);
                vertexBuffer.put(z);

                textureCoordBuffer.put(u);
                textureCoordBuffer.put(v);
            }
        }

        for (int stackNumber = 0; stackNumber < stacks; ++stackNumber)
        {
            for (int sliceNumber = 0; sliceNumber < slices; ++sliceNumber)
            {
                int second = (sliceNumber * (stacks + 1)) + stackNumber;
                int first = second + stacks + 1;

                //int first = (stackNumber * slices) + (sliceNumber % slices);
                //int second = ((stackNumber + 1) * slices) + (sliceNumber % slices);

                drawListBuffer.put((short) first);
                drawListBuffer.put((short) second);
                drawListBuffer.put((short) (first + 1));

                drawListBuffer.put((short) second);
                drawListBuffer.put((short) (second + 1));
                drawListBuffer.put((short) (first + 1));

                drawListOrderLength += 6;
            }
        }

        vertexBuffer.rewind();
        normalBuffer.rewind();
        drawListBuffer.rewind();
        textureCoordBuffer.rewind();

    }

    public void loadShaders(){
        String vertexShaderCode = Helpers.readTextFileFromRawResource(ctx, R.raw.sphere_vertex_shader);
        String fragmentShaderCode = Helpers.readTextFileFromRawResource(ctx, R.raw.sphere_fragment_shader);
        vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode);
        GLES20.glCompileShader(vertexShaderHandle);
        Helpers.checkGlError("Sphere Vertex shader compile");

        fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShaderHandle);
        Helpers.checkGlError("Sphere Pixel shader compile");

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
        GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
        GLES20.glLinkProgram(shaderProgram);
        Helpers.checkGlError("Shader program compile");

        int[] status = new int[1];
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(shaderProgram);
            Log.e("SurfaceTest", "Error while linking sphere program:\n" + error);
        }
    }

    public void initGLComponents()
    {
        loadShaders();
    }

    public void deinitGLComponents()
    {
        GLES20.glDeleteProgram(shaderProgram);
    }

    public void draw(float[] mMVPMatrix){
        GLES20.glUseProgram(shaderProgram);
        int positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        int uMVPHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        int mColorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor");

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, vertexBuffer);

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        GLES20.glUniformMatrix4fv(uMVPHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawListOrderLength, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);


    }

//    Sphere sphere = new Sphere().setVertexBuffer(vertexBuffer)
//            .setNormalBuffer(normalBuffer)
//            .setIndexBuffer(drawListBuffer)
//            .setTexture(R.drawable.earth)
//            .setTextureCoordBuffer(textureCoordBuffer)
//            .setDiffuseLighting(-3f, 2.3f, 2f);
//    return sphere;
}
