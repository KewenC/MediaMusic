package com.kewenc.mediamusic;

import android.app.Notification;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaNotificationManager mMediaNotificationManager;
    private MediaPlayer mMediaPlayer;
    private MediaMetadataCompat mCurrentMedia;
    private ServiceManager mServiceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, "MusicService");
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        mediaSession.setCallback(new MediaSessionCallback());
//        mediaSession.setMediaButtonReceiver();
        setSessionToken(mediaSession.getSessionToken());

        mMediaNotificationManager = new MediaNotificationManager(this);


        mServiceManager = new ServiceManager();

    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MusicLibrary.getRoot(), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(MusicLibrary.getMediaItems());
    }

//    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
//
//    // Defined elsewhere...
//    private AudioManager.OnAudioFocusChangeListener afChangeListener;
//    private BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
//    private MediaStyleNotification myPlayerNotification;
//    private MediaSessionCompat mediaSession;
//    private MediaBrowserService service;
//    private SomeKindOfPlayer player;
//
//    private AudioFocusRequest audioFocusRequest;

    private class MediaSessionCallback extends MediaSessionCompat.Callback implements MediaButtonIntent {

//        private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
//        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;
        private MediaButtonEventIntent mediaButtonEventIntent = new MediaButtonEventIntent();

        MediaSessionCallback() {
            mediaButtonEventIntent.setCallBack(this);
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return mediaButtonEventIntent.handleIntent(mediaButtonEvent);
//            Log.i("TAGF","onMediaButtonEvent");
//            KeyEvent keyEvent = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
//            if (keyEvent != null) {
//                int keyCode = keyEvent.getKeyCode();
//                KeyEvent.KEYCODE_MEDIA_PREVIOUS
//                int count = keyEvent.getRepeatCount();
//                Log.i("TAGF", "keyCode="+keyCode + "_count="+count);
//
////                keyEvent.getAction();
//            }
//
//
//            boolean gg = super.onMediaButtonEvent(mediaButtonEvent);//headsethook
//            Log.i("TAGF", "gg="+ gg);
//            return gg;
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        }

        @Override
        public void onPlay() {
            Log.i("TAGF", "onPlay");
//            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            // Request audio focus for playback, this registers the afChangeListener
//            AudioAttributes attrs = new AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .build();
//            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//                    .setOnAudioFocusChangeListener(afChangeListener)
//                    .setAudioAttributes(attrs)
//                    .build();
//            int result = am.requestAudioFocus(audioFocusRequest);
//
//            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                // Start the service
//                startService(new Intent(context, MediaBrowserService.class));
//                // Set the session active  (and update metadata and state)
//                mediaSession.setActive(true);
//                // start the player (custom call)
//                player.start();
//                // Register BECOME_NOISY BroadcastReceiver
//                registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
//                // Put the service in the foreground, post notification
//                service.startForeground(id, myPlayerNotification);
//            }
            if (mPreparedMedia == null) {
                onPrepare();
            }

            playFromMedia(mPreparedMedia);//jazz_in_paris.mp3
        }

        @Override
        public void onPrepare() {
            Log.i("TAGF", "onPrepare");
            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();

            Bitmap albumArt = BitmapFactory.decodeResource(MediaPlaybackService.this.getResources(),
                    R.drawable.album_jazz_blues);

            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "Jazz_In_Paris")
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Jazz & Blues")
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Media Right Productions")
                    .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Jazz")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Jazz in Paris")
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                            TimeUnit.MILLISECONDS.convert(103, TimeUnit.SECONDS))
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);

            mPreparedMedia = builder.build();
            mediaSession.setMetadata(mPreparedMedia);

            if (!mediaSession.isActive()) {
                mediaSession.setActive(true);
            }
        }

        @Override
        public void onPause() {
            Log.i("TAGF", "onPause");
//            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            // Update metadata and state
//            // pause the player (custom call)
//            player.pause();
//            // unregister BECOME_NOISY BroadcastReceiver
//            unregisterReceiver(myNoisyAudioStreamReceiver);
//            // Take the service out of the foreground, retain the notification
//            service.stopForeground(false);
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                setNewState(PlaybackStateCompat.STATE_PAUSED);
            }
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            Log.i("TAGF", "onSeekTo");
        }

        @Override
        public void onStop() {
            Log.i("TAGF", "onStop");
//            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            // Abandon audio focus
//            am.abandonAudioFocusRequest(audioFocusRequest);
//            unregisterReceiver(myNoisyAudioStreamReceiver);
//            // Stop the service
//            service.stopSelf();
//            // Set the session inactive  (and update metadata and state)
//            mediaSession.setActive(false);
//            // stop the player (custom call)
//            player.stop();
//            // Take the service out of the foreground
//            service.stopForeground(false);
            setNewState(PlaybackStateCompat.STATE_STOPPED);
            release();
        }

        @Override
        public void onOneTap() {
            Log.i("TAGF", "onOneTap");
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                onPause();
            } else {
                onPlay();
            }
        }

        @Override
        public void onDoubleTap() {
            Log.i("TAGF", "onDoubleTap");
        }

        @Override
        public void onThreeTap() {
            Log.i("TAGF", "onThreeTap");
        }

        @Override
        public void onPlayPause() {
            Log.i("TAGF", "onPlayPause");
        }

        @Override
        public void onNextAction() {
            Log.i("TAGF", "onNextAction");
        }

        @Override
        public void onPreviousAction() {
            Log.i("TAGF", "onPreviousAction");
        }

    }

    private void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void playFromMedia(MediaMetadataCompat mPreparedMedia) {
        mCurrentMedia = mPreparedMedia;
        final String mediaId = mCurrentMedia.getDescription().getMediaId();
        playFile("jazz_in_paris.mp3");
    }

    private void playFile(String filename) {
        initializeMediaPlayer();
//        Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//        new String[] {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA}, null,null,null);
//        String data = null;
//        long id = 55 ;
//        if (cursor != null) {
//            if (cursor.moveToLast()) {
//                id  = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
//                data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
//                Log.e("TAGF", "id="+id+"_data="+data);
//            }
//            cursor.close();
//        }
//        Log.e("TAGF", "_data="+data);
//
////        val parcelFileDescriptor: ParcelFileDescriptor? = contentResolver?.openFileDescriptor(uri, "r")
////        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
//        try {
//            ParcelFileDescriptor fileDescriptor = this.getContentResolver().openFileDescriptor(
//                    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id), "r");
//            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            Log.e("TAGF", "mFilename="+filename);
            AssetFileDescriptor assetFileDescriptor = this.getAssets().openFd(filename);
            mMediaPlayer.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength());
        } catch (Exception e) {
            Log.e("TAGF", "e="+e.getMessage());
        }

//        AssetFileDescriptor assetFileDescriptor = null;
//        try {
//            Log.e("TAGF","GGG1");
//            assetFileDescriptor = getAssets().openFd("jazz_in_paris.mp3");
//        } catch (IOException e) {
//            Log.e("TAGF","GGG2");
//            Log.e("TAGF",e.getMessage());
//            e.printStackTrace();
//        }
//        try {
//            mMediaPlayer.setDataSource(
//                        assetFileDescriptor.getFileDescriptor(),
//                        assetFileDescriptor.getStartOffset(),
//                        assetFileDescriptor.getLength());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            mMediaPlayer.prepare();
        } catch (Exception e) {
            Log.e("TAGF", "e="+e.getMessage());
        }

        play();
    }

    private void play() {
        onPlay();
    }

    protected void onPlay() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            setNewState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    private void initializeMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
//            mMediaPlayer = MediaPlayer.create(this, R.raw.aa);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
//                    mPlaybackInfoListener.onPlaybackCompleted();
                    setNewState(PlaybackStateCompat.STATE_PAUSED);
                }
            });
        }
    }

    private int mState;
    // This is the main reducer for the player state machine.
    private void setNewState(@PlaybackStateCompat.State int newPlayerState) {
        mState = newPlayerState;

//        // Whether playback goes to completion, or whether it is stopped, the
//        // mCurrentMediaPlayedToCompletion is set to true.
//        if (mState == PlaybackStateCompat.STATE_STOPPED) {
//            mCurrentMediaPlayedToCompletion = true;
//        }
//
//        // Work around for MediaPlayer.getCurrentPosition() when it changes while not playing.
        final long reportPosition;
//        if (mSeekWhileNotPlaying >= 0) {
//            reportPosition = mSeekWhileNotPlaying;
//
//            if (mState == PlaybackStateCompat.STATE_PLAYING) {
//                mSeekWhileNotPlaying = -1;
//            }
//        } else {
            reportPosition = mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
//        }

        Log.i("TAGF", "reportPosition="+reportPosition);
        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(getAvailableActions());
        stateBuilder.setState(mState,
                reportPosition,
                1.0f,
                SystemClock.elapsedRealtime());

        onPlaybackStateChange(stateBuilder.build());
    }

    public void onPlaybackStateChange(PlaybackStateCompat state) {
        // Report the state to the MediaSession.
        mediaSession.setPlaybackState(state);

        // Manage the started state of this service.
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                mServiceManager.moveServiceToStartedState(state);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mServiceManager.updateNotificationForPause(state);
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                mServiceManager.moveServiceOutOfStartedState(state);
                break;
        }
    }
    private boolean mServiceInStartedState;
    class ServiceManager {

        private void moveServiceToStartedState(PlaybackStateCompat state) {
            Log.i("TAGF", "moveServiceToStartedState");
            Notification notification =
                    mMediaNotificationManager.getNotification(
                            mCurrentMedia, state, getSessionToken());

            if (!mServiceInStartedState) {
                ContextCompat.startForegroundService(
                        MediaPlaybackService.this,
                        new Intent(MediaPlaybackService.this, MediaPlaybackService.class));
                mServiceInStartedState = true;
            }

            startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
        }

        private void updateNotificationForPause(PlaybackStateCompat state) {
            Log.i("TAGF", "updateNotificationForPause");
            stopForeground(false);
            Notification notification =
                    mMediaNotificationManager.getNotification(
                            mCurrentMedia, state, getSessionToken());
            mMediaNotificationManager.getNotificationManager()
                    .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
        }

        private void moveServiceOutOfStartedState(PlaybackStateCompat state) {
            Log.i("TAGF", "moveServiceOutOfStartedState");
            stopForeground(true);
            stopSelf();
            mServiceInStartedState = false;
        }
    }


    /**
     * Set the current capabilities available on this session. Note: If a capability is not
     * listed in the bitmask of capabilities then the MediaSession will not handle it. For
     * example, if you don't want ACTION_STOP to be handled by the MediaSession, then don't
     * included it in the bitmask that's returned.
     */
    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        switch (mState) {
            case PlaybackStateCompat.STATE_STOPPED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                actions |= PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            default:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }
}
