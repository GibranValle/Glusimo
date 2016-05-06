package app.proyectoterminal.upibi.glusimo.classes;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by Gibran on 06/05/2016.
 */
public class Audio
{
    public static MediaPlayer mediaPlayer;
    public static boolean isplayingAudio=false;
    public static final String TAG = "Medicion";

    public void load(Context c, int id, boolean loop)
    {
        Log.e(TAG,"cargando datos de sonido estado: "+isplayingAudio);
        if(isplayingAudio)
        {
            Log.e(TAG,"en efecto esta sonando");
            mediaPlayer.stop();
            isplayingAudio = false;
        }
        mediaPlayer = MediaPlayer.create(c,id);
        mediaPlayer.setLooping(loop);
    }

    public void play()
    {

        if(mediaPlayer.isPlaying())
        {
            Log.e(TAG,"estaba sonando, la para y empieza");
            mediaPlayer.stop();
            isplayingAudio=true;
            mediaPlayer.start();
        }
        else
        {
            Log.e(TAG,"empezando sonido");
            isplayingAudio=true;
            mediaPlayer.start();
        }
    }

    public void stop()
    {
        isplayingAudio=false;
        mediaPlayer.stop();
    }
}
