package com.gamescore.back.service;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private MailjetClient mailjetClient;

    @Value("${mailjet.sender.email}")
    private String senderEmail;

    public void sendEmail(String toEmail, String toName, String subject, String htmlContent) throws MailjetException {
        MailjetRequest request;
        MailjetResponse response;

        request = new MailjetRequest(Emailv31.resource)
            .property(Emailv31.MESSAGES, new JSONArray()
                .put(new JSONObject()
                    .put(Emailv31.Message.FROM, new JSONObject()
                        .put("Email", senderEmail)
                        .put("Name", "Mi Aplicación Spring"))
                    .put(Emailv31.Message.TO, new JSONArray()
                        .put(new JSONObject()
                            .put("Email", toEmail)
                            .put("Name", toName)))
                    .put(Emailv31.Message.SUBJECT, subject)
                    .put(Emailv31.Message.HTMLPART, htmlContent)
                    .put(Emailv31.Message.CUSTOMID, "AppNotification")));

        response = mailjetClient.post(request);
        
        if (response.getStatus() != 200) {
            throw new RuntimeException("Error al enviar email: " + response.getStatus());
        }
    }
}