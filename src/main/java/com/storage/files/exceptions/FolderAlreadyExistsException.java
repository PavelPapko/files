package com.storage.files.exceptions;

public class FolderAlreadyExistsException extends RuntimeException {

    public FolderAlreadyExistsException(String message) {
        super(message);
    }

    public FolderAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}