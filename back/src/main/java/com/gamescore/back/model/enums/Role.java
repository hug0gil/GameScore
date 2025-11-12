package com.gamescore.back.model.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {
    ADMIN,
    USER,
    GUEST;

    /**
     * Convierte el rol en un objeto SimpleGrantedAuthority que Spring Security entiende.
     * Es crucial añadir el prefijo "ROLE_" para que funcione con las anotaciones
     * de seguridad como @PreAuthorize("hasRole('ADMIN')").
     *
     * @return Un objeto SimpleGrantedAuthority, por ejemplo, "ROLE_ADMIN".
     */
    public SimpleGrantedAuthority asGrantedAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }
}