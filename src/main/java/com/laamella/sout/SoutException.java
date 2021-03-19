package com.laamella.sout;

/**
 * Something went wrong!
 */
public class SoutException extends RuntimeException {
    public SoutException(Position position, Throwable wrapped) {
        super(String.format(position + " Exception:", position), wrapped);
    }

    public SoutException(Position position, String message, Object... args) {
        super(String.format(position + " " + message, args));
    }

    public SoutException(String message, Object... args) {
        super(String.format(message, args));
    }
}
