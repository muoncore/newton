package io.muoncore.newton.command;

public class CommandCreateException extends RuntimeException {

  public CommandCreateException() {
  }

  public CommandCreateException(String message) {
    super(message);
  }

  public CommandCreateException(String message, Throwable cause) {
    super(message, cause);
  }

  public CommandCreateException(Throwable cause) {
    super(cause);
  }

  public CommandCreateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
