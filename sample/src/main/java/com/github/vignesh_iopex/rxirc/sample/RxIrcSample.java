package com.github.vignesh_iopex.rxirc.sample;

import com.github.vignesh_iopex.rxirc.RxIrc2;

import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

import rx.Observer;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import static com.github.vignesh_iopex.rxirc.IrcCommands.join;
import static com.github.vignesh_iopex.rxirc.IrcCommands.login;

public class RxIrcSample {
  static final String IRC_HOST = "irc.freenode.net";
  static final int IRC_PORT = 6667;

  public static void main(String[] args) throws Exception {
    CountDownLatch countDownLatch  = new CountDownLatch(1);
    PublishSubject<String> outgoingPublisher = PublishSubject.create();

    RxIrc2.connect(IRC_HOST, IRC_PORT).subscribe(new Observer<RxIrc2>() {
      @Override
      public void onCompleted() {
        log("RxIrc connect process completed");
      }

      @Override
      public void onError(Throwable e) {
        log(e);
      }

      @Override
      public void onNext(RxIrc2 rxIrc2) {
        listenIncoming(rxIrc2);
        listenOutgoing(rxIrc2, outgoingPublisher);
        performLogin(outgoingPublisher, "bot_1", "#junkhack");
      }
    });
    countDownLatch.await();
  }

  private static void listenOutgoing(RxIrc2 rxIrc2, Subject<String, String> outgoingPublisher) {
    StringObservable.from(new InputStreamReader(System.in)).subscribeOn(Schedulers.io())
        .lift(new RxIrc2.OutgoingMessageProcessor())
        .subscribe(outgoingPublisher);
    rxIrc2.outgoing(outgoingPublisher);
  }

  private static void performLogin(Observer<String> observer, String username, String channel) {
    observer.onNext(login(username, username));
    observer.onNext(join(channel));
  }

  private static void listenIncoming(RxIrc2 rxIrc2) {
    rxIrc2.incoming().subscribeOn(Schedulers.io()).subscribe(new Observer<String>() {
      @Override
      public void onCompleted() {
        log("Incoming listening is done");
      }

      @Override
      public void onError(Throwable e) {
        log(e);
      }

      @Override
      public void onNext(String s) {
        log(s);
      }
    });
  }

  static void log(String msg) {
    System.out.println(msg);
  }

  static void log(Throwable e) {
    e.printStackTrace();
  }
}
