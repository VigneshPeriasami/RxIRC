package com.github.vignesh_iopex.rxirc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import rx.Observable;
import rx.observables.StringObservable;

final class IrcConnector implements IOAction {
  private Socket socket;
  private BufferedWriter writer;
  private BufferedReader reader;
  private String host;
  private int port;

  public IrcConnector(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override public void safeConnect() throws IOException {
    if (isConnected()) {
      return;
    }
    socket = new Socket(host, port);
    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
  }

  @Override public Observable<String> reader() {
    return StringObservable.from(reader);
  }

  @Override public void write(String message) throws IOException {
    writer.write(message);
    writer.flush();
  }

  @Override public boolean isConnected() {
    return socket != null && socket.isConnected();
  }
}
