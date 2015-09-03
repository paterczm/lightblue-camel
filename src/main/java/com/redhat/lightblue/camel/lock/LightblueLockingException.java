package com.redhat.lightblue.camel.lock;

public class LightblueLockingException extends RuntimeException {

    private static final long serialVersionUID = 4781039300076260961L;

    public LightblueLockingException(String message) {
        super(message);
    }

    public LightblueLockingException(Throwable cause) {
        super(cause);
    }

    public LightblueLockingException(String message, Throwable cause) {
        super(message, cause);
    }

}
