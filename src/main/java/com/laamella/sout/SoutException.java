package com.laamella.sout;

public class SoutException extends RuntimeException {
    public SoutException(Throwable wrapped) {
        super(wrapped);
    }

    public SoutException(String message, Object... args) {
        super(String.format(message, args));
    }
}
