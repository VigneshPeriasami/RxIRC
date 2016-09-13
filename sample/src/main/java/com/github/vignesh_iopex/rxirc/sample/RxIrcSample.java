package com.github.vignesh_iopex.rxirc.sample;

import com.github.vignesh_iopex.rxirc.RxIrc;

import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static com.github.vignesh_iopex.rxirc.IrcCommands.join;
import static com.github.vignesh_iopex.rxirc.IrcCommands.login;

public class RxIrcSample {
  static final String IRC_HOST = "irc.freenode.net";
  static final int IRC_PORT = 6667;
  static final String CHANNEL_NAME = "#junkhack";

  public static void main(String[] args) throws Exception {
    CountDownLatch countDownLatch  = new CountDownLatch(1);
    PublishSubject<String> outgoingPublisher = PublishSubject.create();

    RxIrc rxIrc = RxIrc.create(IRC_HOST, IRC_PORT);
    RxIrc.inputStream(rxIrc).subscribeOn(Schedulers.io()).subscribe(new Subscriber<String>() {
      @Override
      public void onCompleted() {
        log("incoming read completed");
      }

      @Override
      public void onError(Throwable e) {
        log(e);
      }

      @Override
      public void onNext(String msg) {
        log(msg);
      }
    });

    outgoingStream().subscribe(outgoingPublisher);
    RxIrc.outputStream(rxIrc, outgoingPublisher);

    RxIrc.observeConnection(rxIrc).subscribe(new Action1<RxIrc>() {
      @Override
      public void call(RxIrc rxIrc) {
        performLogin(outgoingPublisher, "bot_1", CHANNEL_NAME);
      }
    });

    countDownLatch.await();
  }

  private static Observable<String> outgoingStream() {
    return StringObservable.from(new InputStreamReader(System.in)).subscribeOn(Schedulers.io())
        .lift(new RxIrc.OutgoingMessageProcessor(CHANNEL_NAME));
  }

  private static void performLogin(Observer<String> observer, String username, String channel) {
    observer.onNext(login(username, username));
    observer.onNext(join(channel));
  }

  static void log(String msg) {
    System.out.println(msg);
  }

  static void log(Throwable e) {
    e.printStackTrace();
  }
}
