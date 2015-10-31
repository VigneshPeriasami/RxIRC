package com.github.vignesh_iopex.rxirc;

import com.github.vignesh_iopex.rxirc.internal.operators.LineSeparator;
import com.github.vignesh_iopex.rxirc.internal.operators.LoginOperator;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class RxIrc {
  private static final String NEWLINE = "\r\n";
  private IOAction ioAction;

  RxIrc(IOAction ioAction) {
    this.ioAction = ioAction;
  }

  public static RxIrc create() {
    return new RxIrc(new IrcConnector());
  }

  public Observable<RxIrc> connect(final String host, final int port) {
    return Observable.create(new Observable.OnSubscribe<RxIrc>() {
      @Override public void call(Subscriber<? super RxIrc> subscriber) {
        try {
          ioAction.connect(host, port);
          subscriber.onNext(RxIrc.this);
          subscriber.onCompleted();
        } catch (IOException e) {
          subscriber.onError(e);
        }
      }
    });
  }

  /**
   * @return if connection is alive
   */
  public boolean isConnected() {
    return ioAction.isConnected();
  }

  private Action0 performLogin(final String username, final String channelName) {
    return new Action0() {
      @Override public void call() {
        String login = "NICK %s " + NEWLINE;
        login += "USER %s 8 * : RxIrc login" + NEWLINE;
        login += "JOIN %s" + NEWLINE;
        login = String.format(login, username, username, channelName);
        try {
          writeln(login);
        } catch (Exception e) {
          throw new RuntimeException("Session closed");
        }
      }
    };
  }

  /**
   * @return flatmap this to keep the session alive
   */
  private Func1<String, Observable<String>> playPingPong() {
    return new Func1<String, Observable<String>>() {
      @Override public Observable<String> call(final String incoming) {
        return Observable.create(new Observable.OnSubscribe<String>() {
          @Override public void call(Subscriber<? super String> subscriber) {
            try {
              if (incoming.toLowerCase().startsWith("ping ")) {
                writeln("PONG " + incoming.substring(5));
              }
              subscriber.onNext(incoming);
            } catch (IOException e) {
              subscriber.onError(e);
            }
          }
        });
      }
    };
  }

  /**
   * Performs login and raises the subscriber exception {@link Subscriber#onError(Throwable)} if
   * login process is failed.
   * <p>
   * Note: This involves subscribing to incoming messages in same thread, subscribe using
   * {@link rx.Scheduler} to avoid blocking
   * <p>
   * Tip: Can reuse the same {@link rx.Scheduler} if created when
   * called from {@link #connect(String, int)} )}
   * result
   *
   * @return Observable that listens to the incoming messages.
   */
  public Observable<String> login(final String username, final String channelName) {
    return ioAction.reader()
        .doOnSubscribe(performLogin(username, channelName))
        .lift(new LineSeparator()).lift(new LoginOperator())
        // keep playing ping pong with the server to keep the session alive.
        .flatMap(playPingPong());
  }

  // Don't know if this is a good approach to take an observable as input
  public Subscription subscribeOutgoingMessages(Observable<String> inputReader) {
    return inputReader.subscribe(new Action1<String>() {
      @Override public void call(String s) {
        try {
          writeln(s);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void writeln(String message) throws IOException {
    ioAction.write(message + NEWLINE);
  }
}
