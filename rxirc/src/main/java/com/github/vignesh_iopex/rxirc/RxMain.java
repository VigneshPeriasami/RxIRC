package com.github.vignesh_iopex.rxirc;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;

public class RxMain {

  public static void main(String[] args) throws Exception {
    RxIrc rxIrc = RxIrc.using("irc.freenode.net", 6667);
    final String channel = "#junkhack";
    rxIrc.connect().subscribeOn(Schedulers.io()).subscribe(new Subscriber<RxIrc>() {
      @Override public void onCompleted() {
        System.out.println("Completed");
      }

      @Override public void onError(Throwable e) {
        System.out.println(e);
      }

      @Override public void onNext(RxIrc rxIrc) {
        System.out.println("Connected " + rxIrc.isConnected());
        doAfterLogin(rxIrc, channel);
      }
    });
    System.out.println("Done");
    StringObservable.from(new InputStreamReader(System.in)).subscribe(new Action1<String>() {
      @Override public void call(String s) {
        System.out.println("Yes still listening: " + s);
      }
    });
  }

  private static void doAfterLogin(RxIrc rxIrc, final String channel) {
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

    rxIrc.subscribeOutgoingMessages(StringObservable
        .from(new BufferedReader(new InputStreamReader(System.in)))
        .map(new Func1<String, String>() {
          @Override public String call(String s) {
            return "PRIVMSG " + channel + " : " + s;
          }
        }));
  }
}
