package ua.maksym.hlushchenko.exception;

public class ParamsValidationException extends RuntimeException {
    public ParamsValidationException() {
    }

    public ParamsValidationException(String message) {
        super(message);
    }

    public ParamsValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParamsValidationException(Throwable cause) {
        super(cause);
    }

    public ParamsValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
