package com.orientation.orientationapp.modules.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service d'envoi d'emails via SendGrid.
 *
 * Configuration (variables d'environnement) :
 *   SENDGRID_API_KEY=SG.xxxxxx.xxxxxx
 *   SENDGRID_FROM_EMAIL=noreply@votredomaine.com
 *   SENDGRID_ENABLED=true
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
     * Envoie un email de vérification d'adresse.
     */
    public void sendVerificationEmail(String toEmail, String firstName, String verificationToken) {
        String subject = "Confirmez votre adresse email — Orientia";
        String verificationUrl = "http://localhost:4200/auth/verify?token=" + verificationToken;
        String html = verificationEmailHtml(firstName, verificationUrl);
        send(toEmail, subject, html);
    }

    /**
     * Envoie un email de réinitialisation de mot de passe.
     */
    public void sendPasswordResetEmail(String toEmail, String firstName, String resetToken) {
        String subject = "Réinitialisation de votre mot de passe — Orientia";
        String resetUrl = "http://localhost:4200/auth/reset-password?token=" + resetToken;
        String html = resetPasswordHtml(firstName, resetUrl);
        send(toEmail, subject, html);
    }

    /**
     * Envoie un email de bienvenue après vérification.
     */
    public void sendWelcomeEmail(String toEmail, String firstName) {
        String subject = "Bienvenue sur Orientia !";
        String html = welcomeHtml(firstName);
        send(toEmail, subject, html);
    }

    // ── Envoi via SendGrid API ──

    private void send(String toEmail, String subject, String htmlBody) {
        log.info("📧 Email → {} | Objet: {}", toEmail, subject);

        if (!enabled || apiKey == null || apiKey.isBlank()) {
            log.warn("⚠️  SendGrid non configuré. Email non envoyé. Contenu loggé ci-dessus.");
            log.info("Pour activer : export SENDGRID_API_KEY=votre_clé && export SENDGRID_ENABLED=true");
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
                log.info("✅ Email envoyé à {}", toEmail);
            } else {
                log.error("❌ Échec envoi email à {}: status={}", toEmail, response.statusCode());
            }
        } catch (Exception e) {
            log.error("❌ Erreur envoi email à {}: {}", toEmail, e.getMessage());
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    // ── Templates HTML ──

    private String verificationEmailHtml(String firstName, String url) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body style="margin:0;padding:0;background:#f3f4f6;">
              <div style="max-width:480px;margin:40px auto;padding:0;">
                <div style="background:#2563eb;padding:32px;text-align:center;border-radius:16px 16px 0 0;">
                  <div style="width:56px;height:56px;background:rgba(255,255,255,0.15);border-radius:14px;display:inline-flex;align-items:center;justify-content:center;margin-bottom:16px;">
                    <span style="font-size:28px;">🎓</span>
                  </div>
                  <h1 style="color:white;font-size:22px;margin:0;font-family:Inter,sans-serif;">Confirmez votre email</h1>
                  <p style="color:rgba(255,255,255,0.7);font-size:14px;margin:8px 0 0;font-family:Inter,sans-serif;">Un seul clic pour activer votre compte</p>
                </div>
                <div style="background:white;padding:36px 32px;border-radius:0 0 16px 16px;font-family:Inter,sans-serif;">
                  <p style="color:#374151;font-size:15px;line-height:1.7;margin:0 0 24px;">Bonjour <strong>%s</strong>,</p>
                  <p style="color:#6b7280;font-size:14px;line-height:1.7;margin:0 0 32px;">Merci de vous être inscrit sur <strong>Orientation</strong>. Pour activer votre compte, cliquez sur le bouton ci-dessous :</p>
                  <div style="text-align:center;margin:32px 0;">
                    <a href="%s" style="display:inline-block;background:#2563eb;color:white;padding:14px 40px;border-radius:10px;font-weight:700;font-size:15px;text-decoration:none;font-family:Inter,sans-serif;">Confirmer mon adresse email</a>
                  </div>
                  <div style="background:#f9fafb;border-radius:8px;padding:16px;margin:24px 0;">
                    <p style="color:#9ca3af;font-size:12px;margin:0;line-height:1.6;">Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :<br><span style="color:#2563eb;word-break:break-all;">%s</span></p>
                  </div>
                  <p style="color:#9ca3af;font-size:12px;margin:24px 0 0;">Ce lien expire dans 24 heures. Si vous n'avez pas créé de compte, ignorez cet email.</p>
                </div>
                <p style="text-align:center;color:#9ca3af;font-size:11px;margin:24px 0 0;font-family:Inter,sans-serif;">© Orientia</p>
              </div>
            </body>
            </html>
            """.formatted(firstName, url, url);
    }

    private String resetPasswordHtml(String firstName, String url) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body style="margin:0;padding:0;background:#f3f4f6;">
              <div style="max-width:480px;margin:40px auto;padding:0;">
                <div style="background:#1e293b;padding:32px;text-align:center;border-radius:16px 16px 0 0;">
                  <h1 style="color:white;font-size:20px;margin:0;font-family:Inter,sans-serif;">Réinitialisation du mot de passe</h1>
                </div>
                <div style="background:white;padding:36px 32px;border-radius:0 0 16px 16px;font-family:Inter,sans-serif;">
                  <p style="color:#374151;font-size:15px;margin:0 0 24px;">Bonjour <strong>%s</strong>,</p>
                  <p style="color:#6b7280;font-size:14px;margin:0 0 32px;">Vous avez demandé la réinitialisation de votre mot de passe. Cliquez ci-dessous :</p>
                  <div style="text-align:center;margin:32px 0;">
                    <a href="%s" style="display:inline-block;background:#2563eb;color:white;padding:14px 40px;border-radius:10px;font-weight:700;font-size:15px;text-decoration-none;">Réinitialiser mon mot de passe</a>
                  </div>
                  <p style="color:#9ca3af;font-size:12px;margin:0;">Ce lien expire dans 1 heure.</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(firstName, url);
    }

    private String welcomeHtml(String firstName) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body style="margin:0;padding:0;background:#f3f4f6;">
              <div style="max-width:480px;margin:40px auto;">
                <div style="background:#2563eb;padding:32px;text-align:center;border-radius:16px 16px 0 0;">
                  <h1 style="color:white;font-size:22px;margin:0;font-family:Inter,sans-serif;">Bienvenue %s ! 🎉</h1>
                </div>
                <div style="background:white;padding:36px 32px;border-radius:0 0 16px 16px;font-family:Inter,sans-serif;">
                  <p style="color:#6b7280;font-size:14px;line-height:1.7;">Votre compte est activé. Vous pouvez maintenant :</p>
                  <ul style="color:#374151;font-size:14px;line-height:2.2;padding-left:20px;">
                    <li>Uploader votre relevé de notes</li>
                    <li>Explorer les universités</li>
                    <li>Obtenir des recommandations personnalisées</li>
                    <li>Simuler différents scénarios d'admission</li>
                  </ul>
                  <div style="text-align:center;margin:32px 0;">
                    <a href="http://localhost:4200" style="display:inline-block;background:#2563eb;color:white;padding:14px 40px;border-radius:10px;font-weight:700;font-size:15px;text-decoration:none;">Accéder à la plateforme</a>
                  </div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(firstName);
    }
}
