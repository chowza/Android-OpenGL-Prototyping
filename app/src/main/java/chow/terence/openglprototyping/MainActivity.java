package chow.terence.openglprototyping;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout mRelativeLayout = (RelativeLayout) findViewById(R.id.main);
        MyGLSurfaceView mMyGLSurfaceView = new MyGLSurfaceView(this);
        mRelativeLayout.addView(mMyGLSurfaceView);

    }
}
