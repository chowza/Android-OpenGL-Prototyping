package chow.terence.openglprototyping.TextureViewImplementation;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;

import chow.terence.openglprototyping.Helpers;

/**
 * Created by Terence on 2015-10-31.
 */
public class MyMainRenderer extends TextureSurfaceRenderer  {

    Context ctx;
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private boolean adjustViewport = false;

    VideoTextureRenderer mVideoTextureRenderer;
    Sphere mSphere;

    public MyMainRenderer (Context context, SurfaceTexture texture, int width, int height)
    {
        super(texture, width, height);
        this.ctx = context;

        mVideoTextureRenderer = new VideoTextureRenderer(context, width, height);
        mSphere = new Sphere(context,1,10,10);
    }


    @Override
    protected boolean draw() {
//        if (adjustViewport)
//            adjustViewport();

        //use width and height for regular objects. Use video width and video height for videos

        //videos have a double buffer, a frame shown in front, and a frame behind which will show up next.
        // If you clear the GL before a new frame is available, you see the back frame, which is 1 frame behind.
        // this appears visually like a jitter, so you only clear if the frame is available
        if (mVideoTextureRenderer.shouldDraw()) {

            //clear color
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            //apply matrix transformation needed for video
            applyMatrixTransformations(mVideoTextureRenderer.getVideoHeight(), mVideoTextureRenderer.getVideoWidth());

            //draw video left
            GLES20.glViewport(0, 0, width / 2, height);
            mVideoTextureRenderer.draw(mMVPMatrix);

            //draw video right
            GLES20.glViewport(width / 2, 0, width / 2, height);
            mVideoTextureRenderer.draw(mMVPMatrix);

            //apply matrix transformation needed for sphere
            applyMatrixTransformations(width, height);

            //draw right sphere
            mSphere.draw(mMVPMatrix);

            //draw left sphere
            GLES20.glViewport(0, 0, width / 2, height);
            mSphere.draw(mMVPMatrix);

        } else {
            return false;
        }
        //right eye
//        GLES20.glViewport(width / 2, 0, width / 2, height);
//        applyMatrixTransformations();
//            mSphere.draw(mMVPMatrix);

        return true;
    }
    private void applyMatrixTransformations(int width, int height){

        float aspectRatio =  (width/2) / (float)height;

        // left, right, bottom, top, near, far
        Matrix.frustumM(mProjectionMatrix, 0, -aspectRatio, aspectRatio, -1.0f, 1.0f, 3.0f, 50.0f);

        // camera position, focus of camera, and up direction relative to camera
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    private void adjustViewport()
    {
//        GLES20.glViewport(0, 0, width, height);

        adjustViewport = false;
    }

    @Override
    protected void initGLComponents() {
        mVideoTextureRenderer.initGLComponents();
        mSphere.initGLComponents();
    }

    @Override
    protected void deinitGLComponents() {
        mVideoTextureRenderer.deinitGLComponents();
        mSphere.deinitGLComponents();
    }

    protected SurfaceTexture getVideoTexture(){
        return mVideoTextureRenderer.getVideoTexture();
    }

    protected void setVideoSize(int width, int height){
        mVideoTextureRenderer.setVideoSize(width, height);
    }
}
