package org.entur.lamassu.delta;

/**
 * Exception thrown when there is an error calculating deltas between GBFS files.
 */
public class GBFSDeltaException extends RuntimeException {

  public GBFSDeltaException(String message) {
    super(message);
  }

  public GBFSDeltaException(String message, Throwable cause) {
    super(message, cause);
  }
}
