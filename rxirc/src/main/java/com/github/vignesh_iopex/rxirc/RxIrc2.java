package com.github.vignesh_iopex.rxirc;

import com.github.vignesh_iopex.rxirc.internal.operators.LineSeparator;
import com.github.vignesh_iopex.rxirc.internal.operators.LoginOperator;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;

import static com.github.vignesh_iopex.rxirc.IrcCommands.commandify;
import static com.github.vignesh_iopex.rxirc.IrcCommands.privmsg;

public class RxIrc2 {
  static final String NEWLINE = "\r\n";
  private final IOAction ioAction;

  RxIrc2(IOAction ioAction) {
    this.ioAction = ioAction;
  }

  public static Observable<RxIrc2> connect(String host, int port) {
    return connect(new IrcConnector(), host, port);
  }

  static Observable<RxIrc2> connect(IOAction ioAction, String host, int port) {
    return Observable.create(new Observable.OnSubscribe<RxIrc2>() {
      @Override
      public void call(Subscriber<? super RxIrc2> subscriber) {
        try {
          subscriber.onStart();
          ioAction.connect(host, port);
          if (subscriber.isUnsubscribed()) {
            return;
          }
          subscriber.onNext(new RxIrc2(ioAction));
          subscriber.onCompleted();
        } catch (IOException e) {
          subscriber.onError(e);
        }
      }
    });
  }

  private Observable.Operator<String, String> playPingPong() {
    return new Observable.Operator<String, String>() {
      @Override
      public Subscriber<? super String> call(Subscriber<? super String> subscriber) {
        return new Subscriber<String>() {
          @Override
          public void onCompleted() {
            subscriber.onCompleted();
          }

          @Override
          public void onError(Throwable e) {
            subscriber.onError(e);
          }

          @Override
          public void onNext(String incoming) {
            subscriber.onNext(incoming);
            if (incoming.toLowerCase().startsWith("ping ")) {
              try {
                writeln("PONG " + incoming.substring(5));
              } catch (IOException e) {
                subscriber.onError(e);
              }
            }
          }
        };
      }
    };
  }

  public Observable<String> incoming() {
    return ioAction.reader()
        .lift(new LineSeparator())
        .lift(new LoginOperator())
        .lift(playPingPong());
  }

  Observable.Operator<Void, String> writeOperator() {
    return new Observable.Operator<Void, String>() {
      @Override
      public Subscriber<? super String> call(Subscriber<? super Void> subscriber) {
        return new Subscriber<String>() {
          @Override
          public void onCompleted() {
            subscriber.onCompleted();
          }

          @Override
          public void onError(Throwable e) {
            subscriber.onError(e);
          }

          @Override
          public void onNext(String message) {
            try {
              writeln(message);
              subscriber.onNext(null);
            } catch (IOException e) {
              subscriber.onError(e);
            }
          }
        };
      }
    };
  }

  void writeln(String message) throws IOException {
    ioAction.write(message + NEWLINE);
  }

  public void outgoing(Observable<String> outgoingMessage) {
    outgoingMessage.lift(writeOperator()).subscribe();
  }

  public static class OutgoingMessageProcessor implements Observable.Operator<String, String> {
    @Override
    public Subscriber<? super String> call(Subscriber<? super String> subscriber) {
      return new Subscriber<String>() {
        @Override
        public void onCompleted() {
          subscriber.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
          subscriber.onError(e);
        }

        @Override
        public void onNext(String s) {
          if (s == null) {
            return;
          }
          if (s.startsWith("/")) {
            subscriber.onNext(commandify(s));
          } else {
            subscriber.onNext(privmsg(s));
          }
        }
      };
    }
  }
}
