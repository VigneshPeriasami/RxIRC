package com.github.vignesh_iopex.rxirc;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import rx.Subscriber;
import rx.functions.Func1;
import rx.observables.StringObservable;

public class RxMain {
  public static void main(String[] args) throws Exception {
    RxIrc rxIrc = RxIrc.connect("irc.freenode.net", 6667);
    final String channel = "#junkhack";
    rxIrc.login("jjjb", channel).subscribe(new Subscriber<String>() {
      @Override public void onCompleted() {
        System.out.println("completed");
      }

      @Override public void onError(Throwable e) {
        e.printStackTrace();
      }

      @Override public void onNext(String s) {
        System.out.println("=>" + s);
      }
    });

    rxIrc.readOutgoingMessageFrom(StringObservable
        .from(new BufferedReader(new InputStreamReader(System.in)))
        .map(new Func1<String, String>() {
          @Override public String call(String s) {
            return "PRIVMSG " + channel + " : " + s;
          }
        }));
  }
}
