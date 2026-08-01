package com.orientation.orientationapp.modules.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service d'envoi d'emails via SendGrid API.
 *
 * Configurez SENDGRID_API_KEY dans vos variables d'environnement.
 */
@Slf4j
@Service
public class EmailService {

    @Value("${app.sendgrid.api-key:}")
    private String apiKey;

    @Value("${app.sendgrid.from-email:noreply@orientation.com}")
    private String fromEmail;

    @Value("${app.sendgrid.enabled:false}")
    private boolean enabled;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Envoie un email de vérification.
     */
    public void sendVerificationEmail(String toEmail, String firstName, String verificationToken) {
        String subject = "Confirmez votre compte Orientation";
        String verificationUrl = "http://localhost:4200/auth/verify?token=" + verificationToken;
        String htmlBody = buildVerificationEmailHtml(firstName, verificationUrl);

        sendEmail(toEmail, subject, htmlBody);
    }

    /**
     * Envoie un email de réinitialisation de mot de passe.
     */
    public void sendPasswordResetEmail(String toEmail, String firstName, String resetToken) {
        String subject = "Réinitialisation de votre mot de passe";
        String resetUrl = "http://localhost:4200/auth/reset-password?token=" + resetToken;
        String htmlBody = buildPasswordResetHtml(firstName, resetUrl);

        sendEmail(toEmail, subject, htmlBody);
    }

    /**
     * Envoie un email de bienvenue après vérification.
     */
    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "Bienvenue sur Orientation !";
        String htmlBody = buildWelcomeHtml(firstName);

        sendEmail(toEmail, subject, htmlBody);
    }

    private void sendEmail(String toEmail, String subject, String htmlBody) {
        if (!enabled || apiKey == null || apiKey.isBlank()) {
            log.warn("Email not sent (SendGrid not configured): to={}, subject={}", toEmail, subject);
            log.info("EMAIL WOULD BE SENT: to={} subject={} url={}", toEmail, subject, extractUrl(htmlBody));
            return;
        }

        try {
            String json = String.format("""
                {
                  "personalizations": [{"to": [{"email": "%s"}]}],
                  "from": {"email": "%s"},
                  "subject": "%s",
                  "content": [{"type": "text/html", "value": "%s"}]
                }
                """, toEmail, fromEmail, subject, escapeJson(htmlBody));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Email sent successfully to {}", toEmail);
            } else {
                log.error("Failed to send email to {}: status={} body={}", toEmail, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String extractUrl(String html) {
        var matcher = java.util.regex.Pattern.compile("href=\"([^\"]+)\"").matcher(html);
        return matcher.find() ? matcher.group(1) : "N/A";
    }

    private String buildVerificationEmailHtml(String firstName, String url) {
        return """
            <div style="font-family:Inter,sans-serif;max-width:480px;margin:0 auto;padding:40px 24px;">
              <div style="text-align:center;margin-bottom:32px;">
                <div style="width:48px;height:48px;background:#2563eb;border-radius:12px;display:inline-flex;align-items:center;justify-content:center;">
                  <span style="color:white;font-size:24px;">🎓</span>
                </div>
                <h1 style="font-size:20px;color:#111827;margin:16px 0 4px;">Confirmez votre email</h1>
              </div>
              <div style="background:white;border:1px solid #e5e7eb;border-radius:12px;padding:28px;">
                <p style="color:#374151;font-size:14px;line-height:1.6;">Bonjour <strong>%s</strong>,</p>
                <p style="color:#6b7280;font-size:14px;line-height:1.6;">Merci de vous etre inscrit sur Orientation. Cliquez sur le bouton ci-dessous pour activer votre compte :</p>
                <div style="text-align:center;margin:28px 0;">
                  <a href="%s" style="display:inline-block;background:#2563eb;color:white;padding:12px 32px;border-radius:8px;font-weight:600;font-size:14px;text-decoration:none;">Confirmer mon email</a>
                </div>
                <p style="color:#9ca3af;font-size:12px;">Ce lien expire dans 24 heures. Si vous n'avez pas cree de compte, ignorez cet email.</p>
              </div>
            </div>
            """.formatted(firstName, url);
    }

    private String buildPasswordResetHtml(String firstName, String url) {
        return """
            <div style="font-family:Inter,sans-serif;max-width:480px;margin:0 auto;padding:40px 24px;">
              <div style="text-align:center;margin-bottom:32px;">
                <h1 style="font-size:20px;color:#111827;">Reinitialisation du mot de passe</h1>
              </div>
              <div style="background:white;border:1px solid #e5e7eb;border-radius:12px;padding:28px;">
                <p style="color:#374151;font-size:14px;">Bonjour <strong>%s</strong>,</p>
                <p style="color:#6b7280;font-size:14px;">Cliquez pour definir un nouveau mot de passe :</p>
                <div style="text-align:center;margin:28px 0;">
                  <a href="%s" style="display:inline-block;background:#2563eb;color:white;padding:12px 32px;border-radius:8px;font-weight:600;font-size:14px;text-decoration:none;">Reinitialiser</a>
                </div>
              </div>
            </div>
            """.formatted(firstName, url);
    }

    private String buildWelcomeHtml(String firstName) {
        return """
            <div style="font-family:Inter,sans-serif;max-width:480px;margin:0 auto;padding:40px 24px;">
              <div style="text-align:center;margin-bottom:32px;">
                <h1 style="font-size:20px;color:#111827;">Bienvenue %s !</h1>
              </div>
              <div style="background:white;border:1px solid #e5e7eb;border-radius:12px;padding:28px;">
                <p style="color:#6b7280;font-size:14px;line-height:1.6;">Votre compte est active. Vous pouvez maintenant :</p>
                <ul style="color:#374151;font-size:14px;line-height:2;">
                  <li>Uploader votre releve de notes</li>
                  <li>Explorer les universites</li>
                  <li>Obtenir des recommandations personnalisees</li>
                </ul>
              </div>
            </div>
            """.formatted(firstName);
    }
}
