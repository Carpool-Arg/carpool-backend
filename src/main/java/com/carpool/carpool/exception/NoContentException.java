package com.carpool.carpool.exception;


public class NoContentException extends RuntimeException {
    
    public NoContentException(String message) {
        super(message);
    }
    
    /*
     * El parametro Throwable cause es opcional y se utiliza para
     * encapsular otra excepción que haya causado esta excepción.
     * 
     */
    public NoContentException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
