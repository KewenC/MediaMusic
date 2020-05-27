package com.kewenc.mediamusic;

import android.content.Intent;

public abstract class AbstractMediaButtonEvent {

    protected abstract void setCallBack(MediaButtonIntent mediaButtonIntent);

    protected abstract boolean handleIntent(Intent mediaButtonEvent);

    protected abstract void buttonClick();

}
