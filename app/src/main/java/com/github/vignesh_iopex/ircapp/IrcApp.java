package com.github.vignesh_iopex.ircapp;

import android.app.Application;
import android.content.Context;

public class IrcApp extends Application {
  public static Context context;

  @Override public void onCreate() {
    super.onCreate();
    context = getApplicationContext();
  }
}
