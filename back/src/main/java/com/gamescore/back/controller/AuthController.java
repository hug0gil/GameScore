package com.gamescore.back.controller;

import com.gamescore.back.service.EmailService;
import com.mailjet.client.errors.MailjetException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailService emailService;

@PostMapping("/forgot-password")
public ResponseEntity<String> forgotPassword(@RequestParam String email) 
        throws MailjetException {
    
    String html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8" />
            <title>GameScore - Acceso</title>
        </head>
        <body style="background-color: #2D2D2D; padding: 20px; font-family: 'Courier New', Courier, monospace; color: #333;">
            
            <div style="max-width: 600px; margin: 0 auto; background: #fff; border: 4px solid #000; box-shadow: 10px 10px 0 #F38801; padding: 20px;">
                
                <!-- HEADER -->
                <div style="border-bottom: 4px dashed #000; padding-bottom: 15px; text-align: center;">
                    <h1 style="color: #FFB800; text-shadow: 2px 2px 0 #000; margin: 0;">GAME SCORE 🎮</h1>
                    <p style="margin: 5px 0 0 0; font-size: 12px; color: #666;">RECUPERACIÓN DE CUENTA</p>
                </div>

                <!-- ICONO DE ALERTA -->
                <div style="text-align: center; padding: 20px 0;">
                    <span style="font-size: 60px;">🔐</span>
                </div>

                <!-- MENSAJE PRINCIPAL -->
                <div style="background-color: #eee; border: 2px solid #000; padding: 20px; margin-bottom: 20px;">
                    <h3 style="margin-top: 0; font-size: 14px; text-transform: uppercase; border-bottom: 2px solid #000; padding-bottom: 5px; color: #DB4040;">
                        ⚠️ SOLICITUD RECIBIDA
                    </h3>
                    <p style="margin-bottom: 15px;">
                        Recibimos una solicitud para recuperar el acceso a tu cuenta.
                    </p>
                    <p style="margin-bottom: 0;">
                        En <strong style="color: #FFB800;">GameScore</strong> no usamos contraseñas tradicionales. 
                        Tu cuenta está vinculada a un proveedor externo.
                    </p>
                </div>

                <!-- OPCIONES DE LOGIN -->
                <div style="text-align: center; margin-bottom: 20px;">
                    <h3 style="font-size: 14px; text-transform: uppercase; margin-bottom: 15px;">
                        🎯 INICIA SESIÓN CON:
                    </h3>
                    
                    <table style="width: 100%; text-align: center; border-collapse: collapse;">
                        <tr>
                            <td style="border: 2px solid #000; padding: 15px; width: 33%;">
                                <span style="font-size: 30px;">🔴</span>
                                <br/>
                                <strong style="font-size: 12px;">GOOGLE</strong>
                            </td>
                            <td style="border: 2px solid #000; padding: 15px; width: 33%;">
                                <span style="font-size: 30px;">🟣</span>
                                <br/>
                                <strong style="font-size: 12px;">DISCORD</strong>
                            </td>
                            <td style="border: 2px solid #000; padding: 15px; width: 33%;">
                                <span style="font-size: 30px;">⚫</span>
                                <br/>
                                <strong style="font-size: 12px;">GITHUB</strong>
                            </td>
                        </tr>
                    </table>
                </div>

                <!-- INFO ADICIONAL -->
                <div style="background-color: #55B957; border: 2px solid #000; padding: 15px; text-align: center; color: #fff;">
                    <p style="margin: 0; font-size: 12px;">
                        💡 <strong>TIP:</strong> Usa el mismo proveedor con el que creaste tu cuenta originalmente.
                    </p>
                </div>
                
                <!-- BOTÓN -->
                <div style="text-align: center; margin-top: 30px;">
                    <a href="http://localhost:8080/login" style="background: #000; color: #FFB800; padding: 15px 30px; text-decoration: none; font-weight: bold; display: inline-block; border: 2px solid #FFB800;">
                        🚀 IR A INICIAR SESIÓN
                    </a>
                </div>

                <!-- NOTA DE SEGURIDAD -->
                <div style="margin-top: 20px; padding: 10px; border-top: 2px dashed #ccc;">
                    <p style="font-size: 10px; color: #888; margin: 0; text-align: center;">
                        🛡️ Si no solicitaste este correo, puedes ignorarlo. Tu cuenta está segura.
                    </p>
                </div>
            </div>
            
            <div style="text-align: center; color: #888; font-size: 10px; margin-top: 20px;">
                &copy; 2025 GameScore Systems. Todos los derechos reservados.
            </div>
        </body>
        </html>
        """;

    emailService.sendEmail(email, "Usuario", "🔐 Acceso a tu cuenta GameScore", html);

    return ResponseEntity.ok("Si el correo existe, recibirás instrucciones.");
}
}