package com.relaxation.naturesounds;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import androidx.media.app.NotificationCompat.MediaStyle;

public class SoundsPlayingService extends Service  implements
MediaPlayer.OnErrorListener {

    private static final String PACKAGE_NAME = "com.relaxation.naturesounds";

    private static final String PLAYING_SOUNDS_NOTIFICATION_CHANNEL_ID = "Playing_Sounds_Notification_Channel_Id ";

    private static final String ACTION_PLAY = PACKAGE_NAME + ".broadcast.PLAY";

    private static final String ACTION_PAUSE = PACKAGE_NAME + "broadcast.PAUSE";

    private static final String ACTION_STOP = PACKAGE_NAME + "broadcast.FINISH";

    private static final String ACTION_CANCEL_TIMER = PACKAGE_NAME + "broadcast.CANCELTIMER";

    private static final int PLAYING_SOUNDS_NOTIFICATION_ID = 1;

    private static final int PLAY_ICON = R.drawable.play;

    private static final int PAUSE_ICON = R.drawable.pause;

    private static final int STOP_ICON = R.drawable.stop;

    private static final int TIMEROFF_ICON = R.drawable.timeroff;

    private float playerVolume;

    private boolean runningSounds = true;

    private boolean timerRunning ;

    private boolean comingFromService = true;

    private int seconds;

    private int h;

    private int m;

    private int s;

    private String time = "";

    Intent intent;

    Intent mainActivityIntent;

    Intent playReceiverIntent;

    Intent pauseReceiverIntent;

    Intent stopReceiverIntent;

    PendingIntent mainActivityPendingIntent;

    PendingIntent playPendingIntent;

    PendingIntent pausePendingIntent;

    PendingIntent stopPendingIntent;

    NotificationCompat.Builder notificationBuilder;

    NotificationManager notificationManager;

    Notification notification;

    Receiver receiver;

    SharedPreferences sharedPreferences;

    SharedPreferences.Editor editor;

    PlayersRunnable playersRunnable;

    TimerRunnable timerRunnable;

    Handler handler = new Handler();

    LoopMediaPlayer loopMediaPlayer;

    Bundle playersVolumeBundle;

    ArrayList<String> runningResourcesNamesList = new ArrayList<>();

    Set<String> runningResourcesNamesSet = new LinkedHashSet<>();

    HashMap<String, LoopMediaPlayer> loopMediaPlayerMap = new HashMap<>();

    @Override
    public void onCreate() {

        playersRunnable = new PlayersRunnable();

        receiver = new Receiver();

        IntentFilter filter = new IntentFilter();

        filter.addAction(ACTION_PLAY);

        filter.addAction(ACTION_PAUSE);

        filter.addAction(ACTION_STOP);

        filter.addAction(ACTION_CANCEL_TIMER);

        registerReceiver(receiver, filter);

        sharedPreferences = getSharedPreferences(PACKAGE_NAME + ".SoundsPlayingService.values", MODE_PRIVATE);

        editor = sharedPreferences.edit();

        editor.putBoolean(PACKAGE_NAME + ".SoundsPlayingService.runningSounds", runningSounds);

        editor.commit();

        createNotification(PLAYING_SOUNDS_NOTIFICATION_CHANNEL_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null)
        {
            this.intent = intent;

            handler.post(playersRunnable);
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        releasePlayers();

        handler.removeCallbacks(playersRunnable);

        if (timerRunnable != null)
        {
            handler.removeCallbacks(timerRunnable);
        }

        notificationManager.cancel(PLAYING_SOUNDS_NOTIFICATION_ID);

        unregisterReceiver(receiver);
    }

    public void createAllPlayers()
    {
        for(int i=0; i<runningResourcesNamesList.size(); i++)
        {
            String resourceName = runningResourcesNamesList.get(i);

            loopMediaPlayer = LoopMediaPlayer.create(this,resourceName);

            loopMediaPlayer.setComponentName("SoundsPlayingService");

            loopMediaPlayerMap.put(resourceName,loopMediaPlayer);

            setPlayerVolume(resourceName);
        }
    }

    public void startAllPlayers()
    {
        for(int i = 0; i< runningResourcesNamesList.size(); i++)
        {
            String resourceName = runningResourcesNamesList.get(i);

            MediaPlayer currentPlayer = loopMediaPlayerMap.get(resourceName).getCurrentPlayer();

            if(!currentPlayer.isPlaying())
            {
                currentPlayer.start();
            }
        }
    }

    public void pauseAllPlayers()
    {
        for(int i = 0; i< runningResourcesNamesList.size(); i++)
        {
            String resourceName = runningResourcesNamesList.get(i);

            MediaPlayer currentPlayer = loopMediaPlayerMap.get(resourceName).getCurrentPlayer();

            if(currentPlayer.isPlaying())
            {
                currentPlayer.pause();
            }
        }
    }

    public void releasePlayers()
    {
        for(int i = 0; i< runningResourcesNamesList.size(); i++)
        {
            String resourceName = runningResourcesNamesList.get(i);

            loopMediaPlayerMap.get(resourceName).releasePlayers();
        }
    }

    public  void setPlayerVolume(String resourceName)
    {
        playerVolume = playersVolumeBundle.getFloat(resourceName);

        MediaPlayer currentPlayer = loopMediaPlayerMap.get(resourceName).getCurrentPlayer();

        currentPlayer.setVolume(playerVolume, playerVolume);

        loopMediaPlayerMap.get(resourceName).setNextPlayervolume(playerVolume);

        editor.putFloat(PACKAGE_NAME + ".SoundsPlayingService." + resourceName + "Volume",
        playerVolume);

        editor.commit();
    }

    public void startTimer()
    {
        timerRunnable = new TimerRunnable();

        handler.post(timerRunnable);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        mp.reset();

        return true;
    }

    public void createNotification(String channelId)
    {
        mainActivityIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());

        mainActivityIntent.putExtra(PACKAGE_NAME + ".SoundsPlayingService.comingFromService",comingFromService);

        mainActivityIntent.putExtra(PACKAGE_NAME + ".SoundsPlayingService.runningSounds",runningSounds);

        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mainActivityPendingIntent = PendingIntent.getActivity(this,0,
        mainActivityIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        pauseReceiverIntent = new Intent();

        pauseReceiverIntent.setAction(ACTION_PAUSE);

        pausePendingIntent = PendingIntent.getBroadcast(this,1,
        pauseReceiverIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        stopReceiverIntent = new Intent();

        stopReceiverIntent.setAction(ACTION_STOP);

        stopPendingIntent = PendingIntent.getBroadcast(this,2,
        stopReceiverIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(this,channelId)

                .setSmallIcon(R.drawable.my_notification_icon)

                .setContentTitle(getText(R.string.app_name))

                .setContentIntent(mainActivityPendingIntent)

                .addAction(PAUSE_ICON,getText(R.string.PAUSE_BUTTON_LABEL),pausePendingIntent)

                .addAction(STOP_ICON,getText(R.string.STOP_BUTTON_LABEL),stopPendingIntent)

                .setStyle(new MediaStyle().setShowActionsInCompactView(0,1))

                .setContentText("")

                .setSound(null)

                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                .setAutoCancel(true);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.largeicon);

            notificationBuilder.setLargeIcon(largeIcon);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            notificationBuilder.setColor(Color.parseColor("#4CAF50"));
        }

        notification = notificationBuilder.build();

        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(PLAYING_SOUNDS_NOTIFICATION_ID,notification);

        startForeground(PLAYING_SOUNDS_NOTIFICATION_ID,notification);
    }

    class PlayersRunnable implements Runnable{

        @Override
        public void run()
        {
            runningResourcesNamesList = intent.getStringArrayListExtra(PACKAGE_NAME
            + ".MainActivity.runningResourcesNames");

            playersVolumeBundle = intent.getBundleExtra(PACKAGE_NAME + ".MainActivity.playersVolume");

            createAllPlayers();

            timerRunning = intent.getBooleanExtra(PACKAGE_NAME + ".MainActivity.timerRunning", false);

            if (timerRunning)
            {
                seconds = intent.getIntExtra(PACKAGE_NAME + ".MainActivity.seconds", 0);

                startTimer();

                Intent cancelTimerIntent = new Intent();

                cancelTimerIntent.setAction(ACTION_CANCEL_TIMER);

                PendingIntent cancelTimerPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
               3, cancelTimerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                notificationBuilder.addAction(TIMEROFF_ICON,
                getString(R.string.CANCEL_TIMER_LABEL), cancelTimerPendingIntent);
            }
            else
            {
                editor.putBoolean(PACKAGE_NAME + ".SoundsPlayingService.timerRunning", timerRunning);

                editor.commit();
            }

            mainActivityIntent.putExtra(PACKAGE_NAME +
            ".SoundsPlayingService.runningResourcesNames", runningResourcesNamesList);

            mainActivityIntent.putExtra(PACKAGE_NAME +
            ".SoundsPlayingService.playersVolume", playersVolumeBundle);

            mainActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
            mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.setContentIntent(mainActivityPendingIntent);

            notification = notificationBuilder.build();

            notificationManager.notify(PLAYING_SOUNDS_NOTIFICATION_ID,notification);

            runningResourcesNamesSet.addAll(runningResourcesNamesList);

            editor.putStringSet(PACKAGE_NAME +
            ".SoundsPlayingService.runningResourcesNamesSet",runningResourcesNamesSet);

            editor.commit();
        }
    }

    class TimerRunnable implements Runnable{

        @Override
        public void run() {

            if(timerRunning) {

                h = seconds / 3600;

                m = (seconds % 3600) / 60;

                s = seconds % 60;

                time = String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);

                mainActivityIntent.putExtra(PACKAGE_NAME +".SoundsPlayingService.timerRunning",timerRunning);

                mainActivityIntent.putExtra(PACKAGE_NAME +".SoundsPlayingService.seconds",seconds);

                mainActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(),
               0,mainActivityIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                notificationBuilder.setContentIntent(mainActivityPendingIntent);

                notificationBuilder.setContentText( time);

                notification = notificationBuilder.build();

                notificationManager.notify(PLAYING_SOUNDS_NOTIFICATION_ID, notification);

                editor.putBoolean(PACKAGE_NAME + ".SoundsPlayingService.timerRunning",timerRunning);

                editor.putInt(PACKAGE_NAME + ".SoundsPlayingService.seconds",seconds);

                editor.commit();

                seconds--;

                if (seconds == -1)
                {
                    timerRunning = false;

                    runningSounds = false;

                    editor.putBoolean(PACKAGE_NAME + ".SoundsPlayingService.timerRunning",timerRunning);

                    editor.putBoolean(PACKAGE_NAME +".SoundsPlayingService.runningSounds",runningSounds);

                    editor.putBoolean(PACKAGE_NAME + ".MainActivity.runAll",true);

                    editor.commit();

                    stopSelf();
                }
            }

            if(seconds >=  0 )
            {
                handler.postDelayed(this,1000);
            }
        }
    }

    class Receiver extends BroadcastReceiver {

        @SuppressLint("RestrictedApi")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if(action.equals(ACTION_PLAY))
            {
                startAllPlayers();

                runningSounds = true;

                mainActivityIntent.putExtra(PACKAGE_NAME + ".SoundsPlayingService.runningSounds",runningSounds);

                mainActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(),0,
                mainActivityIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                notificationBuilder.mActions.remove(0);

                notificationBuilder.mActions.add(0,new NotificationCompat.Action(PAUSE_ICON,
                getString(R.string.PAUSE_BUTTON_LABEL), pausePendingIntent));

                notificationBuilder.setContentIntent(mainActivityPendingIntent);

                notification = notificationBuilder.build();

                notificationManager.notify(PLAYING_SOUNDS_NOTIFICATION_ID,notification);

                editor.putBoolean(PACKAGE_NAME+".SoundsPlayingService.runningSounds",runningSounds);

                editor.commit();
            }

            if(action.equals(ACTION_PAUSE))
            {
                pauseAllPlayers();

                runningSounds = false;

                mainActivityIntent.putExtra(PACKAGE_NAME + ".SoundsPlayingService.runningSounds",runningSounds);

                mainActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(),0,
                mainActivityIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                playReceiverIntent = new Intent();

                playReceiverIntent.setAction(ACTION_PLAY);

                playPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),1,
                playReceiverIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                notificationBuilder.mActions.remove(0);

                notificationBuilder.mActions.add(0,new NotificationCompat.Action(PLAY_ICON,
                getString(R.string.PLAY_BUTTON_LABEL), playPendingIntent));

                notificationBuilder.setContentIntent(mainActivityPendingIntent);

                notification = notificationBuilder.build();

                notificationManager.notify(PLAYING_SOUNDS_NOTIFICATION_ID,notification);

                editor.putBoolean(PACKAGE_NAME+".SoundsPlayingService.runningSounds",runningSounds);

                editor.commit();
            }

            if(action.equals(ACTION_STOP))
            {
                runningSounds = false;

                if(timerRunning)
                {
                    timerRunning = false;

                    seconds = -1;
                }

                editor.putBoolean(PACKAGE_NAME+".SoundsPlayingService.runningSounds",runningSounds);

                editor.putStringSet(PACKAGE_NAME + ".SoundsPlayingService.runningResourcesNamesSet",
                new LinkedHashSet<String>());

                editor.putBoolean(PACKAGE_NAME + ".SoundsPlayingService.timerRunning",timerRunning);

                editor.putBoolean(PACKAGE_NAME + ".MainActivity.runAll",true);

                editor.commit();

                stopSelf();
            }

            if(action.equals(ACTION_CANCEL_TIMER))
            {
                timerRunning = false;

                handler.removeCallbacks(timerRunnable);

                notificationBuilder.setContentText("")
                .mActions.remove(2);

                mainActivityIntent.putExtra(PACKAGE_NAME +".SoundsPlayingService.timerRunning",timerRunning);

                mainActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(),
               0,mainActivityIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                notificationBuilder.setContentIntent(mainActivityPendingIntent);

                notification = notificationBuilder.build();

                notificationManager.notify(PLAYING_SOUNDS_NOTIFICATION_ID,notification);

                editor.putBoolean(PACKAGE_NAME + ".SoundsPlayingService.timerRunning",timerRunning);

                editor.commit();
            }
        }
    }
}
