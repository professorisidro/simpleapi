package com.professorisidro.simpleapi.common;

/** Thrown when an entity cannot be found by its identifier. Maps to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entity, Object id) {
        super(entity + " not found with id " + id);
    }
}
