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

//    VideoTextureRenderer mVideoTextureRenderer;
    Sphere mSphere;

    public MyMainRenderer (Context context, SurfaceTexture texture, int width, int height)
    {
        super(texture, width, height);
        this.ctx = context;

//        mVideoTextureRenderer = new VideoTextureRenderer(context, width, height);
        mSphere = new Sphere(context,1,10,10);
    }


    @Override
    protected boolean draw() {
//        if (adjustViewport)
//            adjustViewport();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //left Eye
        GLES20.glViewport(0, 0, width / 2, height);
//        if (mVideoTextureRenderer.shouldDraw()){
//            mVideoTextureRenderer.draw(mMVPMatrix);
        applyMatrixTransformations();
            mSphere.draw(mMVPMatrix);

        //right eye
        GLES20.glViewport(width / 2, 0, width / 2, height);
        applyMatrixTransformations();
            mSphere.draw(mMVPMatrix);

        return true;
    }
    private void applyMatrixTransformations(){
        // left, right, bottom, top, near, far
        float surfaceAspect =  (width/2) / (float)height;
        Matrix.frustumM(mProjectionMatrix, 0, -surfaceAspect, surfaceAspect, -1.0f, 1.0f, 3.0f, 50.0f);

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
//        mVideoTextureRenderer.initGLComponents();
        mSphere.initGLComponents();
    }

    @Override
    protected void deinitGLComponents() {
//        mVideoTextureRenderer.deinitGLComponents();
//        mSphere.deinitGLComponents();
    }

//    protected SurfaceTexture getVideoTexture(){
//        return mVideoTextureRenderer.getVideoTexture();
//    }
//
//    protected void setVideoSize(int width, int height){
//        mVideoTextureRenderer.setVideoSize(width, height);
//    }
}
