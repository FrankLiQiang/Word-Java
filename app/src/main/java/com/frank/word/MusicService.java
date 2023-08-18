package com.frank.word;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class MusicService extends Service {
    private MediaPlayer player;
    private Handler thisHandler;
    private Timer timer;
    private TimerTask task;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicControl();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
    }

    public void addTimer() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (player != null && player.isPlaying()) {
                        thisHandler.sendEmptyMessage(0);
                    }
                } catch (Exception e) {
                    //TODO
                }
            }
        };
        timer.schedule(task, 0, 100);
    }

    class MusicControl extends Binder {

        public void play(Handler handler, Uri uri, boolean isMute) {
            try {
                if (player != null) {
                    player.setOnPreparedListener(null);
                    player.stop();
                    player.reset();
                    player.release();
                    player = null;
                }
                thisHandler = handler;
                player = new MediaPlayer();
                addTimer();
                player.setAudioAttributes(new AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
                player.setLooping(true);
                player.setDataSource(getApplicationContext(), uri);
                player.prepare();
                if (isMute) {
                    mute();
                } else {
                    Volume();
                }
                player.setOnErrorListener(null);
                player.setOnPreparedListener(player -> {
                    if (thisHandler != null) {
                        int duration = player.getDuration();
                        Message msg = thisHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putInt("duration", duration);
                        msg.setData(bundle);
                        thisHandler.sendMessage(msg);
                    }
                });
                player.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void continuePlay() {
            player.start();
        }

        public void pausePlay() {
            player.pause();
        }

        public void seekTo(int Start) {
            player.seekTo(Start);
        }

        public void speedTo(float progress) {
            PlaybackParams params = player.getPlaybackParams();
            params.setSpeed(progress);
            player.setPlaybackParams(params);
        }

        public int getCurrentPosition() {
            return player.getCurrentPosition();
        }

        public void mute() {
            player.setVolume(0f, 0f);
        }

        public void Volume() {
            player.setVolume(1, 1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player == null) return;
        if (player.isPlaying()) player.stop();
        player.reset();
        player.release();
        player = null;
    }
}
