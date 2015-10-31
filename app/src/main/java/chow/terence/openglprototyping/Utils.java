package chow.terence.openglprototyping;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Terence on 2015-10-31.
 */
public class Utils {
    public static String readTextFileFromRawResource(final Context context, final int resourceId){
        final InputStream mInputStream  = context.getResources().openRawResource(resourceId);
        final InputStreamReader mReader = new InputStreamReader(mInputStream);
        final BufferedReader mBufferedReader = new BufferedReader(mReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();
        try {
            while((nextLine = mBufferedReader.readLine())!= null){
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return body.toString();
    }

}
