package com.github.vignesh_iopex.ircapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.github.vignesh_iopex.rxirc.RxIrc;

import java.io.IOException;

public class IrcClientService extends Service {
  private RxIrc rxIrc;
  private final IrcBinder ircBinder = new IrcBinder();

  @Override public void onCreate() {
    super.onCreate();
  }

  private RxIrc connect() {
    try {
      return RxIrc.using("irc.freenode.net", 6667);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return ircBinder;
  }

  public class IrcBinder extends Binder {
    public RxIrc connectAndGetIrc() {
      return connect();
    }
  }
}
