package com.github.vignesh_iopex.rxirc;

import java.io.IOException;

import rx.Observable;

public interface IOAction {
  /**
   * Establishes socket connection
   * @throws IOException
   */
  void connect(String host, int port) throws IOException;

  /**
   * @return creates plain new Observable from the socket input stream reader
   */
  Observable<String> reader();

  /**
   * write message to the socket output stream
   */
  void write(String message) throws IOException;

  /**
   * @return {@code true} if socket is alive
   */
  boolean isConnected();
}
