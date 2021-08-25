package com.relaxation.naturesounds;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  implements SeekBar.OnSeekBarChangeListener
, TimerDialogFragment.TimerDialogFragmentListener
{
    private static final String PACKAGE_NAME = "com.relaxation.naturesounds";

    private static final int PLAY_Icon = R.drawable.main_activity_layout_play_icon;

    private static final int PAUSE_Icon = R.drawable.main_activity_layout_pause_icon;

    private static  final String PLAYING_SOUNDS_NOTIFICATION_CHANNEL_ID = "Playing_Sounds_Notification_Channel_Id ";

    private static final String ADAPTIVE_BANNER_AD_UNIT_ID = "ca-app-pub-9662811358205324/1807507745";

    private int runningSoundsCount;

    private float seekBarProgress;

    private boolean playAll = true;

    private boolean timerRunning ;

    private boolean runningSounds;

    private boolean onNewIntentImplemented ;

    private boolean comingFromService;

    private int seconds;

    private int h;

    private int m;

    private int s;

    private int which;

    private int frameLayoutHeightInPx;

    private int frameLayoutHeightInDp;

    String time = "";

    ImageButton rainButton;

    ImageButton rainOnGlassButton;

    ImageButton oceanWavesButton;

    ImageButton thunderButton;

    ImageButton windButton;

    ImageButton windInTreesButton;

    ImageButton waterfallsButton;

    ImageButton birdsButton;

    SeekBar rainSeekBar;

    SeekBar rainOnGlassSeekBar;

    SeekBar oceanWavesSeekBar;

    SeekBar thunderSeekBar;

    SeekBar windSeekBar;

    SeekBar windInTreesSeekBar;

    SeekBar waterfallsSeekBar;

    SeekBar birdsSeekBar;

    Button playAllButton;

    Button runTimerButton;

    FrameLayout frameLayout;

    AdView adView;

    TimerDialogFragment timerDialogFragment;

    Handler handler = new Handler();

    TimerRunnable timerRunnable ;

    Intent soundsPlayingServiceIntent;

    ComponentName serviceComponentName;

    SharedPreferences sharedPreferences;

    SharedPreferences.Editor editor;

    LoopMediaPlayer loopMediaPlayer;

    HashMap<ImageButton,Integer> buttonClicksCountMap = new HashMap<>();

    HashMap<String,SeekBar> seekBarsMap = new HashMap<>();

    HashMap<ImageButton,Boolean> runSoundsMap = new HashMap<>();

    HashMap<ImageButton,String> resourcesNamesMap = new HashMap<>();

    HashMap<String,ImageButton> buttonsMap = new HashMap<>();

    HashMap<String,LoopMediaPlayer> loopMediaPlayerMap = new HashMap<>();

    ArrayList<String> runningResourcesNamesList = new ArrayList<>();

    Bundle playersVolumeBundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        frameLayout = findViewById(R.id.adviewContainer);

        final ViewTreeObserver viewTreeObserver = frameLayout.getViewTreeObserver();

        if(viewTreeObserver.isAlive())
        {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    frameLayoutHeightInPx = frameLayout.getHeight();

                    if(frameLayoutHeightInPx != 0)
                    {
                        frameLayoutHeightInDp = pxToDp(frameLayoutHeightInPx);

                        if(frameLayoutHeightInDp >= 50)
                        {
                            MobileAds.initialize(getApplicationContext(), new OnInitializationCompleteListener() {
                                @Override
                                public void onInitializationComplete(InitializationStatus initializationStatus) {

                                }
                            });

                            displayAds(1);
                        }

                        frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        playAllButton = findViewById(R.id.runAllButton);

        runTimerButton = findViewById(R.id.runTimerButton);

        /*Instantiating the ImageButtons*/
        instantiatingImageButtons();

        /*Instantiating the SeekBars*/
        instantiatingSeekBars();

        /*Filling the HashMaps*/
        fillButtonClicksCountMap();

        fillRunSoundsMap();

        fillResourcesNamesMap();

        fillButtonsMap();

        fillSeekBarsMap();

        sharedPreferences = getSharedPreferences(PACKAGE_NAME + ".SoundsPlayingService.values", MODE_PRIVATE);

        editor = sharedPreferences.edit();

        timerDialogFragment = new TimerDialogFragment();

        soundsPlayingServiceIntent = new Intent(this, SoundsPlayingService.class);

        Intent intent = getIntent();

        stopService(soundsPlayingServiceIntent);

        comingFromService = intent.getBooleanExtra(PACKAGE_NAME +
        ".SoundsPlayingService.comingFromService",false);

        if(comingFromService)
        {
            runningSounds = intent.getBooleanExtra(PACKAGE_NAME +
            ".SoundsPlayingService.runningSounds",false);

            if(runningSounds)
            {
                runningResourcesNamesList = intent.getStringArrayListExtra(PACKAGE_NAME +
                 ".SoundsPlayingService.runningResourcesNames");

                playersVolumeBundle = intent.getBundleExtra(PACKAGE_NAME +
                 ".SoundsPlayingService.playersVolume");

                for(int i = 0; i< runningResourcesNamesList.size(); i++)
                {
                    String resourceName = runningResourcesNamesList.get(i);

                    ImageButton button = buttonsMap.get(resourceName);

                    float progress = playersVolumeBundle.getFloat(resourceName);

                    seekBarsMap.get(resourceName).setProgress((int)(progress * 100));

                    createPlayer(resourceName);

                    startPlayer(resourceName);

                    buttonClicksCountMap.put(button,1);
                }

                playAll = sharedPreferences.getBoolean(PACKAGE_NAME + ".MainActivity.runAll",true);

                if(playAll)
                {
                    playAllButton.setText(getText(R.string.PLAY_ALL_BUTTON_LABEL));
                }
                else
                {
                    playAllButton.setText(getText(R.string.PAUSE_ALL_BUTTON_LABEL));
                }
            }

            timerRunning = intent.getBooleanExtra(PACKAGE_NAME +
            ".SoundsPlayingService.timerRunning",false);

            if(timerRunning)
            {
                seconds = intent.getIntExtra(PACKAGE_NAME +
                ".SoundsPlayingService.seconds",0);

                startTimer();
            }
        }
        else
           {
             runningSounds = sharedPreferences.getBoolean(PACKAGE_NAME +
            ".SoundsPlayingService.runningSounds",false);

             if(runningSounds)
             {
                runningResourcesNamesList.addAll(sharedPreferences.getStringSet(PACKAGE_NAME +
                ".SoundsPlayingService.runningResourcesNamesSet",new LinkedHashSet<String>()));

                for(int i = 0; i< runningResourcesNamesList.size(); i++)
                {
                    String resourceName = runningResourcesNamesList.get(i);

                    ImageButton button = buttonsMap.get(resourceName);

                    float progress = sharedPreferences.getFloat(PACKAGE_NAME + ".SoundsPlayingService."+
                    resourceName + "Volume",0.5f);

                    seekBarsMap.get(resourceName).setProgress((int) (progress * 100));

                    createPlayer(resourceName);

                    startPlayer(resourceName);

                    buttonClicksCountMap.put(button,1);
                }
            }

            playAll = sharedPreferences.getBoolean(PACKAGE_NAME + ".MainActivity.runAll",true);

            if(playAll)
            {
                playAllButton.setText(getText(R.string.PLAY_ALL_BUTTON_LABEL));
            }
            else
            {
                playAllButton.setText(getText(R.string.PAUSE_ALL_BUTTON_LABEL));
            }

            timerRunning = sharedPreferences.getBoolean(PACKAGE_NAME + ".SoundsPlayingService.timerRunning", false);

            if (timerRunning)
            {
                seconds = sharedPreferences.getInt(PACKAGE_NAME + ".SoundsPlayingService.seconds", 0);

                startTimer();
            }
        }

        /*Creating the Playing Sounds Notification Channel Id */
        createNotificationChannel();
    }

    @Override
    protected void onPause() {

        if(runningSoundsCount > 0)
        {
            for(int i=0; i<runningResourcesNamesList.size(); i++)
            {
                String resourceName = runningResourcesNamesList.get(i);

                ImageButton button = buttonsMap.get(resourceName);

                buttonClicksCountMap.put(button, 0);

                if(loopMediaPlayerMap.get(resourceName) != null)
                {
                 MediaPlayer player = loopMediaPlayerMap.get(resourceName).getCurrentPlayer();

                 if(!player.isPlaying())
                 {
                   runningResourcesNamesList.remove(resourceName);
                 }
               }
            }

            releasePlayers();

            runningSoundsCount = 0;

            if(timerRunning)
            {
                soundsPlayingServiceIntent.putExtra(PACKAGE_NAME + ".MainActivity.timerRunning",timerRunning);

                soundsPlayingServiceIntent.putExtra(PACKAGE_NAME + ".MainActivity.seconds",seconds);

                timerRunning = false;

                seconds = -1;
            }
            else
            {
                soundsPlayingServiceIntent.putExtra(PACKAGE_NAME + ".MainActivity.timerRunning",timerRunning);
            }

            soundsPlayingServiceIntent.putStringArrayListExtra(PACKAGE_NAME
            + ".MainActivity.runningResourcesNames", runningResourcesNamesList);

            soundsPlayingServiceIntent.putExtra(PACKAGE_NAME
            +".MainActivity.playersVolume", playersVolumeBundle);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                serviceComponentName =  startForegroundService(soundsPlayingServiceIntent);
            }

            else
            {
                serviceComponentName =  startService(soundsPlayingServiceIntent);
            }
        }
        else
        {
            editor.putBoolean(PACKAGE_NAME + ".SoundsPlayingService.runningSounds", false);

            editor.putBoolean(PACKAGE_NAME + ".SoundsPlayingService.timerRunning", false);

            editor.putStringSet(PACKAGE_NAME + ".SoundsPlayingService.runningResourcesNamesSet",
            new LinkedHashSet<String>());

            editor.commit();
        }

        editor.putBoolean(PACKAGE_NAME + ".MainActivity.runAll", playAll);

        editor.commit();

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {

        stopService(soundsPlayingServiceIntent);

        runningSounds = intent.getBooleanExtra(PACKAGE_NAME + ".SoundsPlayingService.runningSounds",false);

        if(runningSounds)
        {
            recreatePlayers();
        }
        else
        {
          for(int i=0; i<runningResourcesNamesList.size();i++)
          {
                String resourceName = runningResourcesNamesList.get(i);

                ImageButton button = buttonsMap.get(resourceName);

                if(!runSoundsMap.get(button))
                {
                    runSoundsMap.put(button,true);

                    button.setImageResource(PLAY_Icon);
                }
            }

            if(runningSoundsCount == 0)
            {
                playAll = true;

                playAllButton.setText(getText(R.string.PLAY_ALL_BUTTON_LABEL));
            }
        }

        timerRunning = intent.getBooleanExtra(PACKAGE_NAME +".SoundsPlayingService.timerRunning",false);

        if(timerRunning)
        {
            seconds = intent.getIntExtra(PACKAGE_NAME +".SoundsPlayingService.seconds",0);

            startTimer();
        }

        onNewIntentImplemented = true;
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (serviceComponentName != null)
        {
            stopService(soundsPlayingServiceIntent);

            if (!onNewIntentImplemented) {

                runningSounds = sharedPreferences.getBoolean(PACKAGE_NAME + ".SoundsPlayingService.runningSounds", false);

                if (runningSounds)
                {
                    recreatePlayers();
                }
                else
                    {
                    for (int i = 0; i < runningResourcesNamesList.size(); i++)
                    {
                        String resourceName = runningResourcesNamesList.get(i);

                        ImageButton button = buttonsMap.get(resourceName);

                        if (!runSoundsMap.get(button)) {

                            runSoundsMap.put(button, true);

                            button.setImageResource(PLAY_Icon);
                        }
                    }

                    if (runningSoundsCount == 0)
                    {
                        playAll = true;

                        playAllButton.setText(getText(R.string.PLAY_ALL_BUTTON_LABEL));
                    }
                }

                timerRunning = sharedPreferences.getBoolean(PACKAGE_NAME + ".SoundsPlayingService.timerRunning", false);

                if (timerRunning) {

                    seconds = sharedPreferences.getInt(PACKAGE_NAME + ".SoundsPlayingService.seconds", 0);

                    startTimer();
                }
            }
        }

        onNewIntentImplemented = false;

        if(frameLayoutHeightInDp >= 50)
        {
            displayAds(10);
        }
    }

    @Override
    protected void onDestroy() {

        releasePlayers();

        super.onDestroy();
    }

    public void onClick(View view) {

        ImageButton button = (ImageButton)view;

        String resourceName = resourcesNamesMap.get(button);

        Integer buttonClicksCount = buttonClicksCountMap.get(button);

        Boolean runSound = runSoundsMap.get(button);

        buttonClicksCount++;

        if(buttonClicksCount == 1 && runSound)
        {
            createPlayer(resourceName);

            startPlayer(resourceName);
        }

        if(buttonClicksCount >1 && runSound)
        {
            startPlayer(resourceName);
        }

        if(!runSound)
        {
            pausePlayer(resourceName);
        }

        buttonClicksCountMap.put(button,buttonClicksCount);
    }

    public void runSound(String resourceName)
    {
        ImageButton button = buttonsMap.get(resourceName);

        if(buttonClicksCountMap.get(button) == 0)
        {
            createPlayer(resourceName);

            startPlayer(resourceName);

            buttonClicksCountMap.put(button,1);
        }

        MediaPlayer player = loopMediaPlayerMap.get(resourceName).getCurrentPlayer();

        if(buttonClicksCountMap.get(button) > 0 && !player.isPlaying())
        {
            startPlayer(resourceName);
        }
    }

    public void runAllSounds(View view)
    {
        if(playAll)
        {
            runSound("rain");
            runSound("rainonglass");
            runSound("oceanwaves");
            runSound("thunder");
            runSound("wind");
            runSound("windintrees");
            runSound("waterfalls");
            runSound("birds");

            ((Button)view).setText(getText(R.string.PAUSE_ALL_BUTTON_LABEL));
        }
        else{
            pausePlayer("rain");
            pausePlayer("rainonglass");
            pausePlayer("oceanwaves");
            pausePlayer("thunder");
            pausePlayer("wind");
            pausePlayer("windintrees");
            pausePlayer("waterfalls");
            pausePlayer("birds");

            ((Button)view).setText(getText(R.string.PLAY_ALL_BUTTON_LABEL));
        }
    }

    public void createPlayer(final String resourceName) {

        loopMediaPlayer = LoopMediaPlayer.create(this, resourceName);

        loopMediaPlayer.setComponentName("MainActivity");

        loopMediaPlayerMap.put(resourceName,loopMediaPlayer);
    }

    public void startPlayer(String resourceName)
    {
        if(loopMediaPlayerMap.get(resourceName) != null) {

            MediaPlayer currentPlayer = loopMediaPlayerMap.get(resourceName).getCurrentPlayer();

            setPlayerVolume(resourceName);

            if(!currentPlayer.isPlaying())
            {
                currentPlayer.start();
            }

            ImageButton button = buttonsMap.get(resourceName);

            button.setImageResource(PAUSE_Icon);

            runSoundsMap.put(button, false);

            if (!runningResourcesNamesList.contains(resourceName))
            {
                runningResourcesNamesList.add(resourceName);
            }

            if (runningSoundsCount < 8)
            {
                runningSoundsCount++;
            }

            if (runningSoundsCount == 8)
            {
                playAll = false;

                playAllButton.setText(getText(R.string.PAUSE_ALL_BUTTON_LABEL));
            }
        }
    }

    public void pausePlayer(String resourceName)
    {
        if(loopMediaPlayerMap.get(resourceName) != null)
        {
         MediaPlayer currentPlayer = loopMediaPlayerMap.get(resourceName).getCurrentPlayer();

         if(currentPlayer.isPlaying())
           {
                currentPlayer.pause();

                ImageButton button = buttonsMap.get(resourceName);

                button.setImageResource(PLAY_Icon);

                runSoundsMap.put(button,true);

                runningResourcesNamesList.remove(resourceName);

                if(runningSoundsCount > 0)
                {
                    runningSoundsCount--;
                }

                if(runningSoundsCount == 0)
                {
                    playAll = true;

                    playAllButton.setText(getText(R.string.PLAY_ALL_BUTTON_LABEL));
                }
            }
        }
    }

    public void releasePlayers()
    {
        for(int i=0; i<runningResourcesNamesList.size(); i++)
        {
            String resourceName = runningResourcesNamesList.get(i);

            if(loopMediaPlayerMap.get(resourceName) != null)
            {
             loopMediaPlayerMap.get(resourceName).releasePlayers();

             loopMediaPlayerMap.remove(resourceName);
           }
       }
    }

    public void recreatePlayers()
    {
        for(int i=0; i<runningResourcesNamesList.size();i++)
        {
            String resourceName = runningResourcesNamesList.get(i);

            ImageButton button = buttonsMap.get(resourceName);

            if(!runSoundsMap.get(button))
            {
                createPlayer(resourceName);

                startPlayer(resourceName);

                buttonClicksCountMap.put(button,1);
            }
        }
    }

    public void setPlayerVolume(String resourceName)
    {
        seekBarProgress = getSeekBarProgress(seekBarsMap.get(resourceName));

        if(loopMediaPlayerMap.get(resourceName) != null)
       {
         MediaPlayer currentPlayer = loopMediaPlayerMap.get(resourceName).getCurrentPlayer();

         currentPlayer.setVolume(seekBarProgress,seekBarProgress);

         loopMediaPlayerMap.get(resourceName).setNextPlayervolume(seekBarProgress);

         playersVolumeBundle.putFloat(resourceName,seekBarProgress);
       }
    }

    public float getSeekBarProgress(SeekBar seekBar)
    {
        float seekBarProgress =(float) seekBar.getProgress()/100;

        return seekBarProgress;
    }

    public void instantiatingImageButtons()
    {
        rainButton = findViewById(R.id.rainImageButton);

        rainOnGlassButton = findViewById(R.id.rainOnGlassImageButton);

        oceanWavesButton = findViewById(R.id.oceanWavesImageButton);

        thunderButton = findViewById(R.id.thunderImageButton);

        windButton  = findViewById(R.id.windImageButton);

        windInTreesButton = findViewById(R.id.windInTreesImageButton);

        waterfallsButton = findViewById(R.id.waterfallsImageButton);

        birdsButton = findViewById(R.id.birdsImageButton);
    }

    public void instantiatingSeekBars()
    {
        rainSeekBar = findViewById(R.id.rainSeekBar);

        rainSeekBar.setOnSeekBarChangeListener(this);

        rainOnGlassSeekBar = findViewById(R.id.rainOnGlassSeekBar);

        rainOnGlassSeekBar.setOnSeekBarChangeListener(this);

        oceanWavesSeekBar = findViewById(R.id.oceanWavesSeekBar);

        oceanWavesSeekBar.setOnSeekBarChangeListener(this);

        thunderSeekBar = findViewById(R.id.thunderSeekBar);

        thunderSeekBar.setOnSeekBarChangeListener(this);

        windSeekBar = findViewById(R.id.windSeekBar);

        windSeekBar.setOnSeekBarChangeListener(this);

        windInTreesSeekBar = findViewById(R.id.windInTreesSeekBar);

        windInTreesSeekBar.setOnSeekBarChangeListener(this);

        waterfallsSeekBar = findViewById(R.id.waterfallsSeekBar);

        waterfallsSeekBar.setOnSeekBarChangeListener(this);

        birdsSeekBar = findViewById(R.id.birdsSeekBar);

        birdsSeekBar.setOnSeekBarChangeListener(this);
    }

    public void fillButtonsMap()
    {
        buttonsMap.put("rain",rainButton);

        buttonsMap.put("rainonglass",rainOnGlassButton);

        buttonsMap.put("oceanwaves",oceanWavesButton);

        buttonsMap.put("thunder",thunderButton);

        buttonsMap.put("wind",windButton);

        buttonsMap.put("windintrees",windInTreesButton);

        buttonsMap.put("waterfalls",waterfallsButton);

        buttonsMap.put("birds",birdsButton);
    }

    public void fillSeekBarsMap()
    {
        seekBarsMap.put("rain",rainSeekBar);

        seekBarsMap.put("rainonglass",rainOnGlassSeekBar);

        seekBarsMap.put("oceanwaves",oceanWavesSeekBar);

        seekBarsMap.put("thunder",thunderSeekBar);

        seekBarsMap.put("wind",windSeekBar);

        seekBarsMap.put("windintrees",windInTreesSeekBar);

        seekBarsMap.put("waterfalls",waterfallsSeekBar);

        seekBarsMap.put("birds",birdsSeekBar);
    }

    public void fillButtonClicksCountMap()
    {
        buttonClicksCountMap.put(rainButton,0);

        buttonClicksCountMap.put(rainOnGlassButton,0);

        buttonClicksCountMap.put(oceanWavesButton,0);

        buttonClicksCountMap.put(thunderButton,0);

        buttonClicksCountMap.put(windButton,0);

        buttonClicksCountMap.put(windInTreesButton,0);

        buttonClicksCountMap.put(waterfallsButton,0);

        buttonClicksCountMap.put(birdsButton,0);
    }

    public void fillRunSoundsMap()
    {
        runSoundsMap.put(rainButton,true);

        runSoundsMap.put(rainOnGlassButton,true);

        runSoundsMap.put(oceanWavesButton,true);

        runSoundsMap.put(thunderButton,true);

        runSoundsMap.put(windButton,true);

        runSoundsMap.put(windInTreesButton,true);

        runSoundsMap.put(waterfallsButton,true);

        runSoundsMap.put(birdsButton,true);
    }

    public void fillResourcesNamesMap()
    {
        resourcesNamesMap.put(rainButton,"rain");

        resourcesNamesMap.put(rainOnGlassButton,"rainonglass");

        resourcesNamesMap.put(oceanWavesButton,"oceanwaves");

        resourcesNamesMap.put(thunderButton,"thunder");

        resourcesNamesMap.put(windButton,"wind");

        resourcesNamesMap.put(windInTreesButton,"windintrees");

        resourcesNamesMap.put(waterfallsButton,"waterfalls");

        resourcesNamesMap.put(birdsButton,"birds");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if(fromUser)
        {
            for(int i =0; i<runningResourcesNamesList.size();i++)
            {
                String resourceName = runningResourcesNamesList.get(i);

                if (seekBar.equals(seekBarsMap.get(resourceName))) {

                    setPlayerVolume(resourceName);
                }
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void singleChoiceItemsOnClick(DialogFragment dialogFragment,int which)
    {
        setWhich(which);
    }

    @Override
    public void positiveButtonOnClick(DialogFragment dialogFragment)
    {
        if(which == 0)
        {
            seconds = -1;

            timerRunning = false;

            editor.putBoolean(PACKAGE_NAME + ".SoundsPlayingService.timerRunning",timerRunning);

            editor.commit();
        }

        else
        {
            timerRunning = true;

            if(which == 1)
            {
                seconds = 10 * 60;
            }

            if(which == 2)
            {
                seconds = 20 * 60;
            }

            if(which == 3)
            {
                seconds = 30 * 60;
            }

            if(which == 4)
            {
                seconds = 40 * 60;
            }

            if(which == 5)
            {
                seconds = 50 * 60;
            }

            if(which == 6)
            {
                seconds = 1 * 60 * 60;
            }

            if(which == 7)
            {
                seconds = 2 * 60 * 60;
            }

            if(which == 8)
            {
                seconds = 4 * 60 * 60;
            }

            if(which == 9)
            {
                seconds = 8 * 60 *60;
            }
        }

        startTimer();
    }

    @Override
    public void  negativeButtonOnclick(DialogFragment dialogFragment)
    {
        dialogFragment.dismiss();
    }

    public void runTimerOnClick(View view)
    {
        timerDialogFragment.show(getSupportFragmentManager(),"TimerDialogFragment");
    }

    public void startTimer()
    {
        if(timerRunnable != null)
        {
            handler.removeCallbacks(timerRunnable);
        }

        timerRunnable = new TimerRunnable();

        handler.post(timerRunnable);
    }

    public  void setWhich(int which)
    {
        this.which = which;
    }

    public  int getWhich()
    {
        return which;
    }

    public int pxToDp(int px)

    {
        DisplayMetrics displayMetrics =  getResources().getDisplayMetrics();

        int dp = Math.round(px/(displayMetrics.xdpi/DisplayMetrics.DENSITY_DEFAULT));

        return dp;
    }

    public AdSize getAdSize()
    {
        Display display = getWindowManager().getDefaultDisplay();

        DisplayMetrics outMetrics = new DisplayMetrics();

        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;

        float density = outMetrics.density;

        int adWidth = (int) (widthPixels/density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this,adWidth);
    }

    public void displayAds(int adRequestLoadingTimes)
    {
        adView = new AdView(getApplicationContext());

        AdSize adSize = getAdSize();

        adView.setAdSize(adSize);

        adView.setAdUnitId(ADAPTIVE_BANNER_AD_UNIT_ID);

        adView.setAdListener(new AdViewListener());

        frameLayout.addView(adView);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT);

        params.gravity = Gravity.BOTTOM;

        adView.setLayoutParams(params);

        for (int i = 0; i < adRequestLoadingTimes; i++)
        {
            AdRequest adRequest = new AdRequest.Builder().build();

            adView.loadAd(adRequest);
        }
    }
    public void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = getString(R.string.PLAYING_SOUNDS_NOTIFICATION_NAME);

            String description = getString(R.string.PLAYING_SOUNDS_NOTIFICATION_DESCRIPTION);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(PLAYING_SOUNDS_NOTIFICATION_CHANNEL_ID,
            name, importance);

            notificationChannel.setSound(null,null);

            notificationChannel.setDescription(description);

            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    class  TimerRunnable implements  Runnable{

        @Override
        public void run() {

            if(timerRunning)
            {
                h = seconds / 3600;

                m = (seconds % 3600) / 60;

                s = seconds % 60;

                time = String.format(Locale.getDefault(),"%02d:%02d:%02d",h,m,s);

                runTimerButton.setText(time);

                seconds--;

                if(seconds == -1)
                {
                    for(int i =0; i< runningResourcesNamesList.size(); i++)
                    {
                        String resourceName = runningResourcesNamesList.get(i);

                        ImageButton button = buttonsMap.get(resourceName);

                        MediaPlayer player = loopMediaPlayerMap.get(resourceName).getCurrentPlayer();

                        if(player != null)
                        {
                            runSoundsMap.put(button,true);

                            buttonClicksCountMap.put(button,0);

                            if(player.isPlaying())
                            {
                                button.setImageResource(PLAY_Icon);
                            }
                        }
                    }

                    releasePlayers();

                    runningResourcesNamesList.clear();

                    timerRunning = false;

                    runTimerButton.setText(getText(R.string.PLAY_TIMER_BUTTON_LABEL));

                    runningSoundsCount = 0;

                    playAll = true;

                    playAllButton.setText(getText(R.string.PLAY_ALL_BUTTON_LABEL));
                }
            }

            if(seconds >= 0 )
            {
                handler.postDelayed(this,1000);
            }

            if(!timerRunning)
            {
                runTimerButton.setText(getText(R.string.PLAY_TIMER_BUTTON_LABEL));

                handler.removeCallbacks(this);
            }
        }
    }

    class AdViewListener extends AdListener{

        @Override
        public void onAdFailedToLoad(LoadAdError loadAdError)
        {
        }
        @Override
        public void onAdLoaded() {}
        @Override
        public void onAdOpened() { }
        @Override
        public void onAdClicked() { }
        @Override
        public void onAdLeftApplication() { }
        @Override
        public void onAdClosed() { }
    }
}
