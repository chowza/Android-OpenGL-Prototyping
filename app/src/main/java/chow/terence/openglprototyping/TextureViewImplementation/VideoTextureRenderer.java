package chow.terence.openglprototyping.TextureViewImplementation;

/**
 * Created by Terence on 2015-10-31.
 */
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import chow.terence.openglprototyping.R;
import chow.terence.openglprototyping.Helpers;

public class VideoTextureRenderer implements SurfaceTexture.OnFrameAvailableListener
{
//    private static float squareSize = 1.0f;
//    private static float squareCoords[] = { -squareSize,  squareSize, 0.0f,   // top left
//            -squareSize, -squareSize, 0.0f,   // bottom left
//            squareSize, -squareSize, 0.0f,   // bottom right
//            squareSize,  squareSize, 0.0f }; // top right
//
//    private static short squareDrawOrder[] = { 0, 1, 2, 0, 2, 3};

    private Context ctx;

    // Texture to be shown in backgrund
//    private FloatBuffer videoTextureBuffer;
//    private float textureCoords[] = { 0.0f, 1.0f, 0.0f, 1.0f,
//            0.0f, 0.0f, 0.0f, 1.0f,
//            1.0f, 0.0f, 0.0f, 1.0f,
//            1.0f, 1.0f, 0.0f, 1.0f };
    private int[] videoFrameTextures = new int[1];

    private int vertexShaderHandle;
    private int fragmentShaderHandle;
    private int shaderProgram;
//    private FloatBuffer squareVertexBuffer;
//    private ShortBuffer squareDrawListBuffer;

    private SurfaceTexture videoTexture;
    private boolean frameAvailable = false;


    private float[] uSTMatrix = new float[16];

    private int videoWidth;
    private int videoHeight;
    protected int width;
    protected int height;


    Sphere mSphere;

    public VideoTextureRenderer(Context context, int width, int height)
    {
        this.width = width;
        this.height = height;
        this.ctx = context;
        mSphere = new Sphere(context,5,45,45);
    }

    private void loadShaders()
    {
        String vertexShaderCode = Helpers.readTextFileFromRawResource(ctx, R.raw.video_vertex_shader);
        String fragmentShaderCode = Helpers.readTextFileFromRawResource(ctx, R.raw.video_fragment_shader);
        vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderHandle, vertexShaderCode);
        GLES20.glCompileShader(vertexShaderHandle);
        Helpers.checkGlError("Video Vertex shader compile");

        fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShaderHandle);
        Helpers.checkGlError("Video Pixel shader compile");

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShaderHandle);
        GLES20.glAttachShader(shaderProgram, fragmentShaderHandle);
        GLES20.glLinkProgram(shaderProgram);
        Helpers.checkGlError("Video Shader program compile");

        int[] status = new int[1];
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(shaderProgram);
            Log.e("SurfaceTest", "Error while linking Video program:\n" + error);
        }

    }


//    private void setupSquareVertexBuffer()
//    {
//        // Draw list buffer
//        ByteBuffer dlb = ByteBuffer.allocateDirect(squareDrawOrder.length * 2);
//        dlb.order(ByteOrder.nativeOrder());
//        squareDrawListBuffer = dlb.asShortBuffer();
//        squareDrawListBuffer.put(squareDrawOrder);
//        squareDrawListBuffer.position(0);
//
//        // Initialize the texture holder
//        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
//        bb.order(ByteOrder.nativeOrder());
//
//        squareVertexBuffer = bb.asFloatBuffer();
//        squareVertexBuffer.put(squareCoords);
//        squareVertexBuffer.position(0);
//    }


    private void setupVideoTexture()
    {
//        ByteBuffer texturebb = ByteBuffer.allocateDirect(textureCoords.length * 4);
//        texturebb.order(ByteOrder.nativeOrder());
//
//        videoTextureBuffer = texturebb.asFloatBuffer();
//        videoTextureBuffer.put(textureCoords);
//        videoTextureBuffer.position(0);

        // Generate the actual texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, videoFrameTextures, 0);
        Helpers.checkGlError("Video Texture generate");

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoFrameTextures[0]);
        Helpers.checkGlError("Video Texture bind");

        //        used for handling when one pixels maps to less than 1 texture element. (texture needs to be magnified to fit polygon)
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //        used for handling when one pixels maps to more than 1 texture element. (texture needs to be shrunk to fit polygon)
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

        videoTexture = new SurfaceTexture(videoFrameTextures[0]);
        videoTexture.setOnFrameAvailableListener(this);
    }

    protected boolean shouldDraw(){
        synchronized (this)
        {
            if (frameAvailable)
            {
                videoTexture.updateTexImage();
                videoTexture.getTransformMatrix(uSTMatrix);

                // at the current orientation, we see the back of the image.
                // Therefore to see the image's front, we need to rotate it around the up axis
                // since we are watching in landscape mode, the X axis is the up direction

                //invert matrix along X axis. (Recall android matrices are column wise, not row wise)
                uSTMatrix[0] = - uSTMatrix[0];
                uSTMatrix[12] = 1.0f - uSTMatrix[12];

                //also invert on Y axis because video is showing upside down
                uSTMatrix[5] = - uSTMatrix[5];
                uSTMatrix[13] = 1.0f - uSTMatrix[13];

                frameAvailable = false;
                return true;
            }
            else
            {
                return false;
            }

        }
    }

    protected boolean draw(float[] mMVPMatrix)
    {
        // Draw texture
        GLES20.glUseProgram(shaderProgram);
        int textureParamHandle = GLES20.glGetUniformLocation(shaderProgram, "sTexture");
        int textureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "vTexCoordinate");
        int positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
        int uSTHandle = GLES20.glGetUniformLocation(shaderProgram, "uSTMatrix");
        int uMVPHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");

        GLES20.glEnableVertexAttribArray(positionHandle);
//        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, squareVertexBuffer);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, mSphere.vertexBuffer);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, videoFrameTextures[0]);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(textureParamHandle, 0);

        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
//        GLES20.glVertexAttribPointer(textureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, videoTextureBuffer);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mSphere.textureCoordBuffer);

        GLES20.glUniformMatrix4fv(uSTHandle, 1, false, uSTMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPHandle, 1, false, mMVPMatrix, 0);

//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, squareDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, squareDrawListBuffer);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mSphere.drawListOrderLength, GLES20.GL_UNSIGNED_SHORT, mSphere.drawListBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);

        return true;
    }

    protected void initGLComponents()
    {
//        setupSquareVertexBuffer();
        setupVideoTexture();
        loadShaders();
    }

    protected void deinitGLComponents()
    {
        GLES20.glDeleteTextures(1, videoFrameTextures, 0);
        GLES20.glDeleteProgram(shaderProgram);
        videoTexture.release();
        videoTexture.setOnFrameAvailableListener(null);
        mSphere.deinitGLComponents();
    }

    public void setVideoSize(int width, int height)
    {
        this.videoWidth = width;
        this.videoHeight = height;
    }
    public int getVideoWidth(){
        return this.videoWidth;
    }
    public int getVideoHeight(){
        return this.videoHeight;
    }

    public SurfaceTexture getVideoTexture()
    {
        return videoTexture;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture)
    {
        synchronized (this)
        {
            frameAvailable = true;
        }
    }
}