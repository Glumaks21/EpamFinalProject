package ua.maksym.hlushchenko.exception;

public class MyReflectionException extends Exception {
    public MyReflectionException() {
    }

    public MyReflectionException(String message) {
        super(message);
    }

    public MyReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyReflectionException(Throwable cause) {
        super(cause);
    }

    public MyReflectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
