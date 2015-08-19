package com.github.vignesh_iopex.rxirc.internal.operators;

import rx.Observable;
import rx.Subscriber;

public final class LoginOperator implements Observable.Operator<String, String> {

  @Override public Subscriber<? super String> call(final Subscriber<? super String> child) {
    return new Subscriber<String>(child) {
      private boolean isVerified;

      @Override public void onCompleted() {
        child.onCompleted();
      }

      @Override public void onError(Throwable e) {
        child.onError(e);
      }

      @Override public void onNext(String s) {
        // always forward whatever received from source.
        child.onNext(s);
        if (isVerified)
          return;
        if (s.contains("433")) {
          isVerified = true;
          child.onNext("Username already in use");
          child.onError(new IllegalStateException("Pick another username"));
        } else if (s.contains("004")) {
          isVerified = true;
        }
      }
    };
  }
}
