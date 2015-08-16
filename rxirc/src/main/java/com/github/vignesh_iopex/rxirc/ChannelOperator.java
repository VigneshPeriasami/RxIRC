package com.github.vignesh_iopex.rxirc;

import java.io.BufferedReader;

import rx.Observable;
import rx.Subscriber;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;

public class ChannelOperator implements Observable.Operator<String, String> {
  private final BufferedReader reader;

  public ChannelOperator(BufferedReader reader) {
    this.reader = reader;
  }

  @Override public Subscriber<? super String> call(Subscriber<? super String> subscriber) {
    StringObservable.from(reader).subscribeOn(Schedulers.io())
        .subscribe(subscriber);
    return subscriber;
  }
}
