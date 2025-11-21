package com.gamescore.back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mailjet.client.errors.MailjetException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
public class ReportService {

    @Autowired
    private EmailService emailService;

    @Value("${logging.file.name}") // Lee "app.log" de tu properties
    private String logFilePath;

    // Ejecutar cada Viernes a las 18:00
    @Scheduled(cron = "0 0 18 * * FRI")
    public void generateWeeklyReport() throws MailjetException {
        System.out.println("--- Iniciando reporte semanal desde logs ---");
        
        Map<String, List<String>> userActivities = new HashMap<>();

        // 1. LEER EL ARCHIVO
        try (Stream<String> stream = Files.lines(Paths.get(logFilePath))) {
            stream.filter(line -> line.contains("[ACTIVITY]|||")) // Filtrar solo nuestras líneas
                  .forEach(line -> {
                      try {
                          // La línea vendrá con basura del logger al principio (fecha, thread, etc)
                          // Buscamos donde empieza nuestro marcador
                          int startIndex = line.indexOf("[ACTIVITY]|||");
                          String cleanData = line.substring(startIndex);
                          
                          // Split por "|||" (escapado para regex)
                          String[] parts = cleanData.split("\\|\\|\\|");
                          
                          // parts[0] = [ACTIVITY]
                          // parts[1] = email
                          // parts[2] = action
                          
                          if (parts.length >= 3) {
                              String email = parts[1].trim();
                              String action = parts[2].trim();
                              
                              userActivities.putIfAbsent(email, new ArrayList<>());
                              userActivities.get(email).add(action);
                          }
                      } catch (Exception e) {
                          // Ignorar línea corrupta
                      }
                  });
        } catch (IOException e) {
            System.err.println("No se pudo leer el archivo de log: " + logFilePath);
            return;
        }

        // 2. ENVIAR CORREOS
        for (Map.Entry<String, List<String>> entry : userActivities.entrySet()) {
            String email = entry.getKey();
            List<String> actions = entry.getValue();
            
            // Construir HTML
            StringBuilder html = new StringBuilder();
            html.append("<h1>Resumen Semanal GameScore</h1>");
            html.append("<p>Esta semana realizaste ").append(actions.size()).append(" acciones:</p><ul>");
            
            // Mostrar máximo 10 acciones
            actions.stream().limit(10).forEach(a -> html.append("<li>").append(a).append("</li>"));
            
            html.append("</ul>");
            
            // Enviar
            emailService.sendEmail(email, "Gamer", "Tu actividad semanal", html.toString());
            
            // Pausa de 100ms para no saturar Mailjet si hay muchos usuarios
            try { Thread.sleep(100); } catch (InterruptedException i) {}
        }
    }
}