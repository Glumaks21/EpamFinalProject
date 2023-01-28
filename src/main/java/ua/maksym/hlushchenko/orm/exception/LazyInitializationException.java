package ua.maksym.hlushchenko.orm.exception;

public class LazyInitializationException extends RuntimeException {
    public LazyInitializationException() {
    }

    public LazyInitializationException(String message) {
        super(message);
    }

    public LazyInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LazyInitializationException(Throwable cause) {
        super(cause);
    }

    public LazyInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
