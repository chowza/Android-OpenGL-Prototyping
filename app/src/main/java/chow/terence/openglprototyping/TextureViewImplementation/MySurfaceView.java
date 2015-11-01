package chow.terence.openglprototyping.TextureViewImplementation;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;


import java.io.IOException;

import chow.terence.openglprototyping.R;

public class MySurfaceView implements TextureView.SurfaceTextureListener
{
    private static final String LOG_TAG = "SurfaceTest";

    private TextureView surface;
    private MediaPlayer player;
    private MyMainRenderer renderer;

    Context ctx;
    private int surfaceWidth;
    private int surfaceHeight;

    public MySurfaceView(Context ctx){
        this.ctx = ctx;
        surface = new TextureView(ctx);
        surface.setSurfaceTextureListener(this);
    }

    public TextureView getSurface(){
        return surface;
    }

    public MyMainRenderer getRenderer(){
        return renderer;
    }
    public MediaPlayer getPlayer(){
        return player;
    }

    public void startPlaying()
    {
        renderer = new MyMainRenderer(ctx, surface.getSurfaceTexture(), surfaceWidth, surfaceHeight);
        player = new MediaPlayer();

        try
        {
            player.setDataSource(ctx,  Uri.parse("android.resource://chow.terence.openglprototyping/" + R.raw.big_buck_bunny));
//            player.setSurface(new Surface(renderer.getVideoTexture()));
            player.setLooping(true);
            player.prepare();
//            renderer.setVideoSize(player.getVideoWidth(), player.getVideoHeight());
            player.start();

        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not open input video!");
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height)
    {
        surfaceWidth = width;
        surfaceHeight = height;
//        startPlaying();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}