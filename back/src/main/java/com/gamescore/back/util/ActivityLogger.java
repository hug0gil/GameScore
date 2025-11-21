package com.gamescore.back.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class ActivityLogger {
    
    // Usamos un logger específico para filtrarlo fácil si quisiéramos
    private static final Logger logger = LoggerFactory.getLogger("ActivityLogger");
    
    // SEPARADOR CLAVE: Usamos "|||" para separar campos
    public void log(String email, String action) {
        // FORMATO: [ACTIVITY]|||email|||acción|||fecha
        logger.info("[ACTIVITY]|||" + email + "|||" + action + "|||" + LocalDateTime.now());
    }
}