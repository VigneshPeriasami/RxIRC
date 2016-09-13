package com.github.vignesh_iopex.rxirc;

import static com.github.vignesh_iopex.rxirc.RxIrc.NEWLINE;

public class IrcCommands {
  public static String login(String nickname, String username) {
    String login = "NICK %s " + NEWLINE;
    login += "USER %s 8 * : RxIrc login" + NEWLINE;
    return String.format(login, nickname, username);
  }

  public static String join(String channelName) {
    return String.format("JOIN %s" + NEWLINE, channelName);
  }

  public static String privmsg(String recipient, String message) {
    return String.format("PRIVMSG %s :%s" + NEWLINE, recipient, message);
  }

  public static String commandify(String commandMessage) {
    return commandMessage.substring(1).toUpperCase();
  }
}
