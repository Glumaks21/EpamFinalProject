package ua.maksym.hlushchenko.orm.exception;

public class EntityParserException extends RuntimeException {
    public EntityParserException() {
    }

    public EntityParserException(String message) {
        super(message);
    }

    public EntityParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityParserException(Throwable cause) {
        super(cause);
    }

    public EntityParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
