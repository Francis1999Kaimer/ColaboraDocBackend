package com.example.project.services;

import com.example.project.entities.ProjectUser;
import com.example.project.entities.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine thymeleafTemplateEngine;

    @Value("${spring.mail.from:no-reply@colaboradoc.com}") 
    private String mailFrom;
    
    @Value("${app.frontend.url:https://localhost:3000}")
    private String frontendBaseUrl;

    @Async
    public void sendProjectInvitationEmail(User invitedUser, User invitingUser, ProjectUser invitation) {
        if (invitedUser.getEmail() == null) {
            logger.warn("No se puede enviar email de invitación: el usuario invitado no tiene email. User ID: {}", invitedUser.getIduser());
            return;
        }

        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariable("invitedUserName", invitedUser.getNames() + " " + invitedUser.getLastnames());
            thymeleafContext.setVariable("invitingUserName", invitingUser.getNames() + " " + invitingUser.getLastnames());
            thymeleafContext.setVariable("projectName", invitation.getProject().getName());
            thymeleafContext.setVariable("roleName", formatRoleName(invitation.getRoleCode()));
            
      
            String invitationLink = frontendBaseUrl + "/dashboard";

            thymeleafContext.setVariable("invitationLink", invitationLink);

            String htmlBody = thymeleafTemplateEngine.process("email/invitation-email", thymeleafContext);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); 

            helper.setFrom(mailFrom);
            helper.setTo(invitedUser.getEmail());
            helper.setSubject("¡Has sido invitado a un proyecto en ColaboraDoc!");
            helper.setText(htmlBody, true);

           // mailSender.send(message);
          //  logger.info("Email de invitación enviado a {} para el proyecto '{}'", invitedUser.getEmail(), invitation.getProject().getName());
            logger.info("Por ahora desactivado");
        } catch (MessagingException e) {
            logger.error("Error al enviar email de invitación a {}: {}", invitedUser.getEmail(), e.getMessage(), e);
          
        } catch (Exception e) {
            logger.error("Error inesperado al procesar plantilla de email para {}: {}", invitedUser.getEmail(), e.getMessage(), e);
        }
    }


    private String formatRoleName(String roleCode) {
        if (roleCode == null) return "No especificado";
        return switch (roleCode.toUpperCase()) {
            case "ADMIN", "ADMIN_PROJECT" -> "Administrador del Proyecto";
            case "EDITOR" -> "Editor";
            case "VIEWER" -> "Visualizador";
            default -> roleCode;
        };
    }
}