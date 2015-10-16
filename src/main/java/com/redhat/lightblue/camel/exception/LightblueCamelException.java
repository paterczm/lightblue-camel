package com.redhat.lightblue.camel.exception;

public class LightblueCamelException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public LightblueCamelException(String message) {
        super(message);
    }

    public LightblueCamelException(String message, Throwable cause) {
        super(message, cause);
    }

    public LightblueCamelException(Throwable cause) {
        super(cause);
    }

}
