package org.project.group5.gamelend.exception;

// excepción para archivo no encontrado
public class MyFileNotFoundException extends RuntimeException {
    public MyFileNotFoundException(String message) {
        super(message);
    }
    public MyFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
