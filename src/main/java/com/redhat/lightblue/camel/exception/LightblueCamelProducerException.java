package com.redhat.lightblue.camel.exception;

public class LightblueCamelProducerException extends LightblueCamelException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public LightblueCamelProducerException(String message) {
        super(message);
    }

    public LightblueCamelProducerException(String message, Throwable cause) {
        super(message, cause);
    }

    public LightblueCamelProducerException(Throwable cause) {
        super(cause);
    }

}
