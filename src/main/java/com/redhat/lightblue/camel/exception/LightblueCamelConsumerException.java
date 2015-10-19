package com.redhat.lightblue.camel.exception;

public class LightblueCamelConsumerException extends LightblueCamelException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public LightblueCamelConsumerException(String message) {
        super(message);
    }

    public LightblueCamelConsumerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LightblueCamelConsumerException(Throwable cause) {
        super(cause);
    }

}
