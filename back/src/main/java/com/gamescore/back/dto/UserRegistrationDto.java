package com.gamescore.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // Anotación de Lombok para generar getters, setters, toString, etc.
public class UserRegistrationDto {

    /**
     * Nombre de usuario para el registro.
     * No debe estar vacío y debe tener entre 3 y 20 caracteres.
     */
    @NotEmpty(message = "El nombre de usuario no puede estar vacío.")
    @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres.")
    private String username;

    /**
     * Dirección de correo electrónico.
     * No debe estar vacía y debe tener un formato de email válido.
     */
    @NotEmpty(message = "El email no puede estar vacío.")
    @Email(message = "Por favor, introduce una dirección de email válida.")
    private String email;

    /**
     * Contraseña del usuario.
     * No debe estar vacía y debe tener al menos 8 caracteres.
     */
    @NotEmpty(message = "La contraseña no puede estar vacía.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    private String password;

}