package com.github.vignesh_iopex.rxirc;

import com.github.vignesh_iopex.rxirc.internal.operators.LineSeparator;
import com.github.vignesh_iopex.rxirc.internal.operators.LoginOperator;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.github.vignesh_iopex.rxirc.IrcCommands.commandify;
import static com.github.vignesh_iopex.rxirc.IrcCommands.privmsg;

public class RxIrc {
  static final String NEWLINE = "\r\n";
  private final IOAction ioAction;

  RxIrc(IOAction ioAction) {
    this.ioAction = ioAction;
  }

  public static RxIrc create(String host, int port) {
    return new RxIrc(new IrcConnector(host, port));
  }

  public static Observable<RxIrc> observeConnection(RxIrc rxIrc) {
    return safeConnect(rxIrc.ioAction);
  }

  public static Observable<String> inputStream(RxIrc rxIrc) {
    return safeConnect(rxIrc.ioAction).flatMap(new Func1<RxIrc, Observable<String>>() {
      @Override
      public Observable<String> call(RxIrc rxIrc) {
        return rxIrc.incoming();
      }
    });
  }

  public static void outputStream(RxIrc rxIrc, Observable<String> outgoing) {
    safeConnect(rxIrc.ioAction).subscribe(new Action1<RxIrc>() {
      @Override
      public void call(RxIrc rxIrc) {
        rxIrc.outgoing(outgoing);
      }
    });
  }

  @Deprecated
  public static Observable<RxIrc> safeConnect(String host, int port) {
    return safeConnect(new IrcConnector(host, port));
  }

  static Observable<RxIrc> safeConnect(IOAction ioAction) {
    return Observable.create(new Observable.OnSubscribe<RxIrc>() {
      @Override
      public void call(Subscriber<? super RxIrc> subscriber) {
        try {
          subscriber.onStart();
          ioAction.safeConnect();
          if (subscriber.isUnsubscribed()) {
            return;
          }
          subscriber.onNext(new RxIrc(ioAction));
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

  Observable<String> incoming() {
    return ioAction.reader()
        .lift(new LineSeparator())
        .lift(new LoginOperator())
        .lift(playPingPong());
  }

  Observable.Operator<String, String> writeOperator() {
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
          public void onNext(String message) {
            try {
              writeln(message);
              subscriber.onNext(message);
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

  void outgoing(Observable<String> outgoingMessage) {
    outgoingMessage.lift(writeOperator()).subscribe();
  }

  public static class OutgoingMessageProcessor implements Observable.Operator<String, String> {
    private final String channelName;

    public OutgoingMessageProcessor(String channelName) {
      this.channelName = channelName;
    }

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
        public void onNext(String message) {
          if (message == null) {
            return;
          }
          if (message.startsWith("/")) {
            subscriber.onNext(commandify(message));
          } else {
            subscriber.onNext(privmsg(channelName, message));
          }
        }
      };
    }
  }
}
