package chow.terence.openglprototyping;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Terence on 2015-10-31.
 */
public class MyGLSurfaceViewRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    //constants
    final private String TAG = "MyGLSurfaceViewRenderer";
    final private int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 3 * FLOAT_SIZE_BYTES;
    private static final int TEXTURE_VERTICES_DATA_STRIDE_BYTES = 2 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 0;
    private final float[] mSquareCoordinatesData =
            {
                    -1.0f, -1.0f,  0, // bottom left
                     1.0f, -1.0f,  0, // bottom right
                    -1.0f,  1.0f,  0, // top left
                     1.0f,  1.0f,  0, // top right
            };

    private final float[] mTextureVerticesData =
            {
                    0.f,    0.0f, // bottom left
                    1.0f,    0.f, // bottom right
                    0.0f,    1.f,  // top left
                    1.0f,    1.0f // top right
            };

    private FloatBuffer mTriangleVertices;
    private FloatBuffer mTextureVertices;



    private final Context mContext;
    private SurfaceTexture videoSurfaceTexture;
    private boolean update = false;


    //Open GL handles
    private int mProgram;
    private int mVideoFrameTextureId;
    private int uSTHandle;
    private int uMVPHandle;
    private int mTextureHandle;
    private int mPositionHandle;

    // matrices
    private float[] uSTMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];

    //media player
    MediaPlayer mMediaPlayer;

    public MyGLSurfaceViewRenderer(Context context){
        Log.d(TAG, TAG + " constructor");
        mContext = context;
        initializeBuffers();
        Matrix.setIdentityM(uSTMatrix, 0);
    }

    private void initializeBuffers(){

        //create a float buffer for the vertices
        ByteBuffer bb = ByteBuffer.allocateDirect(mSquareCoordinatesData.length * FLOAT_SIZE_BYTES);
        bb.order(ByteOrder.nativeOrder());
        mTriangleVertices = bb.asFloatBuffer();

        //insert the
        mTriangleVertices.put(mSquareCoordinatesData);
        mTriangleVertices.position(0);

        // extra
        mTextureVertices = ByteBuffer
                .allocateDirect(
                        mTextureVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureVertices.put(mTextureVerticesData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        Log.i(TAG,"onSurfaceCreated");
        final String mVertexShaderSource = Utils.readTextFileFromRawResource(mContext,R.raw.vertex_shader);
        final String mFragmentShaderSource = Utils.readTextFileFromRawResource(mContext, R.raw.fragment_shader);

        int mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderSource);
        checkForGLError("Create & Compile Vertex Shader");

        int mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderSource);
        checkForGLError("Create & Compile Fragment Shader");
        mProgram = createProgram(mVertexShader,mFragmentShader);

        connectHandlesToShader(mProgram);
        createVideoFrameTextures();
    }

    private int loadShader(int shaderType, String shaderSource){

        //create and compile shader
        final int shader = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shader, shaderSource);
        GLES20.glCompileShader(shader);

        return shader;
    }

    private int createProgram(int vertexShader,int fragmentShader){
        final int GLProgram = GLES20.glCreateProgram();;
        GLES20.glAttachShader(GLProgram, vertexShader);
        checkForGLError("Attach Vertex Shader");

        GLES20.glAttachShader(GLProgram, fragmentShader);
        checkForGLError("Attach Fragment Shader");

        GLES20.glLinkProgram(GLProgram);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(GLProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG,"GL Link Program Error");
            Log.e(TAG,GLES20.glGetProgramInfoLog(GLProgram));
            GLES20.glDeleteProgram(GLProgram);
            throw new RuntimeException("GL Link Program Error");
        }
        return GLProgram;
    }

    private void connectHandlesToShader(int GLProgram){

        mPositionHandle = GLES20.glGetAttribLocation(GLProgram, "aPosition");
        checkForGLError("Position Handle");

        mTextureHandle = GLES20.glGetAttribLocation(GLProgram, "aTexCoords");
        checkForGLError("Texture Handle");

        uMVPHandle = GLES20.glGetUniformLocation(GLProgram, "uMVPMatrix");
        checkForGLError("MVP Matrix Handle");

        uSTHandle = GLES20.glGetUniformLocation(GLProgram, "uSTMatrix");
        checkForGLError("ST Matrix Handle");
    }

    private void createVideoFrameTextures (){
        int [] videoFrameTextures = new int[1];
        GLES20.glGenTextures(1, videoFrameTextures, 0);
        mVideoFrameTextureId = videoFrameTextures[0];

        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES,mVideoFrameTextureId);
        checkForGLError("Bind Texture");

//        used for handling when one pixels maps to less than 1 texture element.
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

//        used for handling when one pixels maps to more than 1 texture element.
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);

        videoSurfaceTexture = new SurfaceTexture(mVideoFrameTextureId);
        videoSurfaceTexture.setOnFrameAvailableListener(this);

        Surface mVideoSurface = new Surface(videoSurfaceTexture);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mContext,  Uri.parse("android.resource://chow.terence.openglprototyping/" + R.raw.big_buck_bunny));
        } catch (IOException e) {
            Log.e(TAG, "Media Player set data source failed" + e.getMessage());
        }
        mMediaPlayer.setSurface(mVideoSurface);
        mVideoSurface.release();

//        try{
//            mMediaPlayer.prepare();
//        } catch (IOException e) {
//            Log.e(TAG, "Media Player prepare failed");
//        }

//        mMediaPlayer.start();

        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });


    }

    protected MediaPlayer getMediaPlayer (){
        return mMediaPlayer;
    }

    private void checkForGLError(String operation){
        //check if error on shader
        int error;
        while((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR){
            Log.e(TAG, operation +" GL Error: " + GLUtils.getEGLErrorString(error));
            throw new RuntimeException(operation +" GL Error: " + GLUtils.getEGLErrorString(error));
        }
    }



    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.d(TAG,"on surface changed");
        GLES20.glViewport(0,0,width,height);
        Matrix.frustumM(mProjectionMatrix, 0, -1.0f, 1.0f, -1.0f, 1.0f,
                1.0f, 10.0f);
    }


    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surfaceTexture) {

        update = true;
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        synchronized (this){
            if (update){
                videoSurfaceTexture.updateTexImage();
                videoSurfaceTexture.getTransformMatrix(uSTMatrix);
                update = false;
            } else{
                return;
            }
        }

        GLES20.glClearColor(255.0f, 255.0f, 255.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkForGLError("Use Program");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mVideoFrameTextureId);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                mTriangleVertices);
        checkForGLError("glVertexAttribPointer mPositionHandle");

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        checkForGLError("glEnableVertexAttribArray mPositionHandle");

        mTextureVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(mTextureHandle, 2, GLES20.GL_FLOAT,
                false, TEXTURE_VERTICES_DATA_STRIDE_BYTES, mTextureVertices);

        checkForGLError("glVertexAttribPointer mTextureHandle");
        GLES20.glEnableVertexAttribArray(mTextureHandle);
        checkForGLError("glEnableVertexAttribArray mTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(uMVPHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(uSTHandle, 1, false, uSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkForGLError("glDrawArrays");
        GLES20.glFinish();

    }

}
