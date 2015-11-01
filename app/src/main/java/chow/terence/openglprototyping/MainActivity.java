package chow.terence.openglprototyping;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.widget.RelativeLayout;

import chow.terence.openglprototyping.GLSurfaceImplementation.MyGLSurfaceView;
import chow.terence.openglprototyping.TextureViewImplementation.MySurfaceView;
import chow.terence.openglprototyping.TextureViewImplementation.MyMainRenderer;

public class MainActivity extends Activity {

    private MySurfaceView mMySurfaceView;
    private TextureView surface;
    private MediaPlayer player;
    private MyMainRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout mRelativeLayout = (RelativeLayout) findViewById(R.id.main);

        //UNCOMMENT BELOW THREE LINES TO USE GLSURFACE VIEW IMPLEMENTATION
//        MyGLSurfaceView mMyGLSurfaceView = new MyGLSurfaceView(this);
//        mRelativeLayout.addView(mMyGLSurfaceView);
//    }

        //COMMENT BELOW TO USE TEXTURE VIEW IMPLEMENTATION, NOTE: GL SURFACE VIEW HAS FLICKERING ISSUE. Double Buffer?
        mMySurfaceView = new MySurfaceView(this);
        surface = mMySurfaceView.getSurface();
        mRelativeLayout.addView(surface);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (surface.isAvailable())
            mMySurfaceView.startPlaying();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if ((player = mMySurfaceView.getPlayer()) != null)
            player.release();
        if ((renderer = mMySurfaceView.getRenderer()) != null)
            renderer.onPause();
    }
}
