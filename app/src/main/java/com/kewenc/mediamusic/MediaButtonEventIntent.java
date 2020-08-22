package com.kewenc.mediamusic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

public class MediaButtonEventIntent extends AbstractMediaButtonEvent {

    private long mFirstTime = 0;
    private long mSecondTime = 0;
    private static final int MSG_PERFORM_OPERATION = 2;
    private static final int MSG_MUSIC_NEXT = 3;
    private static final int MSG_MUSIC_PREVIOUS = 4;
    private static int mNumber = 0;//点击次数
    private static long mSaveTime = 0;//
    private int SPACE_TIME = 800;//两次点击之前的最大间隔时间
    private long COMPLY_TIME = 1000L;//每次判断完次数后执行的延迟发送处理消息时间，必须比间隔时间大

    private MediaButtonIntent mediaButtonIntent;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PERFORM_OPERATION:
                    if (mNumber == 0) {
                        if (mediaButtonIntent != null) {
                            mediaButtonIntent.onOneTap();
                        }
                    } else if (mNumber == 1) {
                        if (mediaButtonIntent != null) {
                            mediaButtonIntent.onDoubleTap();
                        }
                    } else if (mNumber == 2) {
                        if (mediaButtonIntent != null) {
                            mediaButtonIntent.onThreeTap();
                        }
                    }
                    break;
                case MSG_MUSIC_NEXT:
                    if (mediaButtonIntent != null) {
                        mediaButtonIntent.onNextAction();
                    }
                    break;
                case MSG_MUSIC_PREVIOUS:
                    if (mediaButtonIntent != null) {
                        mediaButtonIntent.onPreviousAction();
                    }
                    break;
            }
        }
    };

    @Override
    protected void setCallBack(MediaButtonIntent mediaButtonIntent) {
        this.mediaButtonIntent = mediaButtonIntent;
    }

    @Override
    protected boolean handleIntent(Intent mediaButtonEvent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(mediaButtonEvent.getAction())) {
            KeyEvent event = (KeyEvent) mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) return false;
            int keycode = event.getKeyCode();
            Log.i("TAGF","keycode="+keycode);
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                if (keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                    mHandler.sendEmptyMessage(MSG_MUSIC_PREVIOUS);
                }
                else if (keycode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                    mHandler.sendEmptyMessage(MSG_MUSIC_NEXT);
                }
                else if (keycode == KeyEvent.KEYCODE_MEDIA_PLAY) {

                }
                else if (keycode == KeyEvent.KEYCODE_MEDIA_PAUSE) {

                }
                else if (keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                    if (mediaButtonIntent != null) {
                        mediaButtonIntent.onPlayPause();
                    }
                }
                else {
                    buttonClick();
                }
                return true;
            } else if (action == KeyEvent.ACTION_UP) {
                return false;
            }
        }
        return false;
    }

    @Override
    protected void buttonClick() {
        mSecondTime = mSaveTime;
        mFirstTime = System.currentTimeMillis();
        mSaveTime = mFirstTime;
        long time = mFirstTime - mSecondTime;
        int number = mNumber;
        if (time > 0 && time < SPACE_TIME) {//点击间隔小于800ms判定为一次操作
            mHandler.removeMessages(MSG_PERFORM_OPERATION);
            if (number == 0) {
                mNumber = 1;
            } else if (number == 1) {
                mNumber = 2;
            } else if (number == 2) {
                mNumber = 0;
            }
        } else {
            mNumber = 0;
        }
        mHandler.sendEmptyMessageDelayed(MSG_PERFORM_OPERATION, COMPLY_TIME);
    }

}
