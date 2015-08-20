package com.github.vignesh_iopex.rxirc.sample;

import com.github.vignesh_iopex.rxirc.RxIrc;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import rx.Subscriber;
import rx.functions.Func1;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;

public class RxIrcSample {

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
        doAfterConnect(rxIrc, channel);
      }
    });
    System.out.println("Done");
    while (true) {
      // keep it alive
    }
  }

  private static void doAfterConnect(RxIrc rxIrc, final String channel) {

    // incoming read and outgoing write should happen on different threads
    rxIrc.subscribeOutgoingMessages(StringObservable
        .from(new BufferedReader(new InputStreamReader(System.in)))
        .map(new Func1<String, String>() {
          @Override public String call(String s) {
            return "PRIVMSG " + channel + " : " + s;
          }
        }).subscribeOn(Schedulers.io()));

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
  }
}
