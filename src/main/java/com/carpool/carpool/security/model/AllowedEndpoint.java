package com.carpool.carpool.security.model;

import org.springframework.http.HttpMethod;

import java.util.Set;

/**
 * Record que representa un endpoint permitido con sus métodos HTTP asociados.
 * Si methods es null o vacío, se permiten todos los métodos HTTP para ese endpoint.
 */
public record AllowedEndpoint(
        String path,
        Set<HttpMethod> methods,
        boolean allowSubpaths
) {
    /**
     * Constructor para endpoint que permite todos los métodos HTTP
     */
    public AllowedEndpoint(String path, boolean allowSubpaths) {
        this(path, null, allowSubpaths);
    }

    /**
     * Constructor para endpoint con métodos específicos
     */
    public AllowedEndpoint(String path, Set<HttpMethod> methods) {
        this(path, methods, false);
    }

    /**
     * Constructor para endpoint simple sin subrutas ni restricciones de método
     */
    public AllowedEndpoint(String path) {
        this(path, null, false);
    }

    /**
     * Verifica si el request path y método están permitidos por este endpoint
     */
    public boolean matches(String requestPath, HttpMethod requestMethod) {
        // Verificar si el path coincide
        boolean pathMatches = allowSubpaths
                ? requestPath.startsWith(path)
                : requestPath.equals(path);

        if (!pathMatches) {
            return false;
        }

        // Si no hay restricciones de método, permitir cualquier método
        if (methods == null || methods.isEmpty()) {
            return true;
        }

        // Verificar si el método está en la lista de métodos permitidos
        return methods.contains(requestMethod);
    }
}