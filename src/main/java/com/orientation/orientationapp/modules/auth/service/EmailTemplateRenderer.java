package com.orientation.orientationapp.modules.auth.service;

import java.net.URI;

/** Shared, image-independent email layout; all dynamic content is escaped. */
final class EmailTemplateRenderer {
    private EmailTemplateRenderer() {}
    static String render(String baseUrl, String title, String name, String message,
                         String action, String url, String notice) {
        safeUrl(baseUrl);
        safeUrl(url);
        String greeting = name == null || name.isBlank() ? "Bonjour," : "Bonjour " + name.strip() + ",";
        String logo = baseUrl.replaceAll("/+$", "") + "/assets/email/orientia-logo.png";
        return """
            <!doctype html>
            <html lang="fr"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
            <title>%s</title></head>
            <body style="margin:0;padding:0;background-color:#f3f0ff;color:#171249;font-family:Arial,Helvetica,sans-serif;">
            <div style="display:none;max-height:0;overflow:hidden;mso-hide:all;">%s</div>
            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" bgcolor="#f3f0ff" style="table-layout:fixed;"><tr><td align="center" style="padding:32px 16px;">
            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:600px;table-layout:fixed;">
            <tr><td align="center" style="padding:0 0 24px;"><img src="%s" width="190" alt="Orientia" style="display:block;width:190px;max-width:100%%;height:auto;border:0;"></td></tr>
            <tr><td bgcolor="#ffffff" style="border:1px solid #e2dafa;border-radius:24px;padding:36px 24px;">
            <p style="margin:0 0 12px;color:#7132d4;font-size:12px;font-weight:bold;letter-spacing:2px;">VOTRE ESPACE ORIENTIA</p>
            <h1 style="margin:0 0 24px;font-size:28px;line-height:1.2;color:#171249;">%s</h1>
            <p style="margin:0 0 12px;font-size:16px;line-height:1.6;">%s</p>
            <p style="margin:0 0 28px;font-size:16px;line-height:1.6;color:#49416c;white-space:pre-line;">%s</p>
            <table role="presentation" cellpadding="0" cellspacing="0"><tr><td bgcolor="#7725d8" style="border-radius:28px;text-align:center;">
            <a href="%s" style="display:inline-block;padding:16px 28px;border:1px solid #7725d8;border-radius:28px;color:#ffffff;text-decoration:none;font-size:16px;line-height:1.4;font-weight:bold;">%s</a>
            </td></tr></table>
            <p style="margin:24px 0 0;padding:16px;background-color:#f6f3ff;border-radius:12px;color:#49416c;font-size:14px;line-height:1.6;">%s</p>
            <p style="margin:24px 0 8px;font-size:13px;line-height:1.6;color:#635a80;">Si le bouton ne fonctionne pas, copiez ce lien dans votre navigateur :</p>
            <p style="margin:0;font-size:13px;line-height:1.6;overflow-wrap:anywhere;word-break:break-all;"><a href="%s" style="color:#7132d4;">%s</a></p>
            </td></tr><tr><td align="center" style="padding:24px 12px;font-size:12px;line-height:1.7;color:#635a80;">
            <strong style="color:#171249;">Orientia · Votre avenir commence ici</strong><br>
            Un accompagnement pour vos choix d’orientation, au Bénin et dans le monde.<br>
            Besoin d’aide ? <a href="mailto:lestudioviral@gmail.com" style="color:#7132d4;">lestudioviral@gmail.com</a><br>
            Ne partagez jamais vos liens personnels ni votre mot de passe.
            </td></tr></table></td></tr></table></body></html>
            """.formatted(escape(title), escape(title), escape(logo), escape(title), escape(greeting),
                escape(message), escape(url), escape(action), escape(notice), escape(url), escape(url));
    }
    private static void safeUrl(String value) {
        URI uri = URI.create(value);
        if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null)
            throw new IllegalArgumentException("Invalid email link configuration");
    }
    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
