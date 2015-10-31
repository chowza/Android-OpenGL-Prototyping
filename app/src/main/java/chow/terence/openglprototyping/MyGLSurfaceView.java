package chow.terence.openglprototyping;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.util.Log;

/**
 * Created by Terence on 2015-10-31.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    MyGLSurfaceViewRenderer mRenderer;
    MediaPlayer mMediaPlayer;

    public MyGLSurfaceView(Context context) {
        super(context);
        Log.d("MyGLSurfaceViewRenderer", "myGlSurfaceView constructor");
        initGLSurfaceView();
    }

    public void initGLSurfaceView(){
        setEGLContextClientVersion(2);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mRenderer = new MyGLSurfaceViewRenderer(getContext());
        setRenderer(mRenderer);

        mMediaPlayer = mRenderer.getMediaPlayer();
    }

    //Lifecycle activities
    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
    }
    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        if (mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }
}
