package com.example.betsettlement.exception;

public class BetSettlementPublishingException extends RuntimeException {
  public BetSettlementPublishingException(String message) {
    super(message);
  }

  public BetSettlementPublishingException(String message, Throwable cause) {
    super(message, cause);
  }
}
