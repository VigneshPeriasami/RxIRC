package com.github.vignesh_iopex.rxirc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class RxIrc {
  private BufferedReader reader;
  private BufferedWriter writer;
  private static final String NEWLINE = "\r\n";

  private RxIrc(BufferedReader reader, BufferedWriter writer) throws IOException {
    this.reader = reader;
    this.writer = writer;
  }

  public static RxIrc connect(String host, int port) throws IOException {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(host, port));
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    return new RxIrc(reader, writer);
  }

  public Observable<String> login(final String username, final String channelName) {
    return Observable.create(new Observable.OnSubscribe<String>() {
      @Override public void call(final Subscriber<? super String> subscriber) {
        String login = "NICK %s " + NEWLINE;
        login += "USER %s 8 * : RxIrc login" + NEWLINE;
        login += "JOIN %s" + NEWLINE;
        login = String.format(login, username, username, channelName);
        try {
          writeln(login);
        } catch (Exception e) {
          subscriber.onError(e);
        }
      }
    }).lift(new ChannelOperator(reader)).flatMap(new Func1<String, Observable<String>>() {
      @Override public Observable<String> call(final String incoming) {
        return Observable.create(new Observable.OnSubscribe<String>() {

          @Override public void call(Subscriber<? super String> subscriber) {
            try {
              if (incoming.toLowerCase().startsWith("ping ")) {
                writeln("PONG " + incoming.substring(5));
              } else if (incoming.toLowerCase().contains("privmsg")) {
                //writeln("PRIVMSG " + channelName + " : Acknowledge message received");
              }
              System.out.println("==> " + incoming);
              subscriber.onNext(incoming);
            } catch (IOException e) {
              subscriber.onError(e);
            }
          }
        });
      }
    });
  }

  public Subscription readOutgoingMessageFrom(Observable<String> inputReader) {
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
    writer.write(message + NEWLINE);
    writer.flush();
  }
}
