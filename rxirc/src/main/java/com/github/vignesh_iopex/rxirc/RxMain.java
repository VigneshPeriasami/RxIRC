package com.github.vignesh_iopex.rxirc;

import rx.Subscriber;

public class RxMain {
  public static void main(String[] args) throws Exception {
    RxIrc.connect("irc.freenode.net", 6667).login("jjjb", "#hijunk")
        .subscribe(new Subscriber<String>() {
          @Override public void onCompleted() {
            System.out.println("completed");
          }

          @Override public void onError(Throwable e) {
            e.printStackTrace();
          }

          @Override public void onNext(String s) {
            System.out.println(" " + s);
          }
        });
  }
}
