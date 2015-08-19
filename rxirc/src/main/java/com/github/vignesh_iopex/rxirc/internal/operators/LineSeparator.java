package com.github.vignesh_iopex.rxirc.internal.operators;

import rx.Observable;
import rx.Subscriber;

public final class LineSeparator implements Observable.Operator<String, String> {

  @Override public Subscriber<? super String> call(final Subscriber<? super String> subscriber) {
    return new Subscriber<String>(subscriber) {
      @Override public void onCompleted() {
        subscriber.onCompleted();
      }

      @Override public void onError(Throwable e) {
        subscriber.onError(e);
      }

      @Override public void onNext(String s) {
        String[] lines = s.split("\n");
        for (String line : lines) {
          subscriber.onNext(line);
        }
      }
    };
  }
}
