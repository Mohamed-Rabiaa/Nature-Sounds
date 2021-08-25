    package com.relaxation.naturesounds;

    import android.content.Context;
    import android.media.MediaPlayer;
    import android.net.Uri;
    import android.os.PowerManager;
    import java.io.IOException;


    public class LoopMediaPlayer {

        private static final String PACKAGE_NAME = "com.relaxation.naturesounds";

        public final String TAG = LoopMediaPlayer.class.getSimpleName();

        private Context context;

        private String resourceName = "";

        private float nextPlayerVolume;

        private MediaPlayer currentPlayer;

        private MediaPlayer nextPlayer;

        private String componentName = "";

        public static LoopMediaPlayer create(Context context, String resourceName) {

            return new LoopMediaPlayer(context, resourceName);
        }

        public LoopMediaPlayer(Context context, String resourceName) {

            this.context = context;

            this.resourceName = resourceName;

            currentPlayer = new MediaPlayer();

            try {
                currentPlayer.setDataSource(context, Uri.parse("android.resource://" +PACKAGE_NAME+ "/raw/"
                + resourceName));

                currentPlayer.prepare();

                currentPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {

                        currentPlayer.start();
                    }
                });
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }

            componentName = getComponentName();

            if(componentName.equals("SoundsPlayingService"))
            {
                currentPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            }

            createNextMediaPlayer();
        }

        void createNextMediaPlayer() {

            nextPlayer = new MediaPlayer();

            try
            {
                nextPlayer.setDataSource(context, Uri.parse("android.resource://" +PACKAGE_NAME+ "/raw/"
                + resourceName));

                nextPlayer.prepare();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }

            currentPlayer.setNextMediaPlayer(nextPlayer);

            currentPlayer.setOnCompletionListener(onCompletionListener);
        }

        private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {

                mp.release();

                nextPlayerVolume = getNextPlayerVolume();

                nextPlayer.setVolume(nextPlayerVolume, nextPlayerVolume);

                componentName = getComponentName();

                if(componentName.equals("SoundsPlayingService"))
                {
                    nextPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
                }
                currentPlayer = nextPlayer;

                createNextMediaPlayer();
            }
        };


        public MediaPlayer getCurrentPlayer() {
            return this.currentPlayer;
        }

        public void setNextPlayervolume(float volume) {
            this.nextPlayerVolume = volume;
        }

        public float getNextPlayerVolume() {
            return this.nextPlayerVolume;
        }

        public void releasePlayers()
        {
            currentPlayer.release();

            nextPlayer.release();
        }

        public void setComponentName(String componentName)
        {
           this.componentName = componentName;
        }

        public String getComponentName()
        {
            return this.componentName;
        }
    }

