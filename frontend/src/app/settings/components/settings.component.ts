import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Paramètres</h1><p>Configuration générale de la plateforme</p></div>
        <div class="page-header-actions"><button class="btn btn-primary"><span class="material-symbols-rounded">save</span>Sauvegarder</button></div>
      </div>
      <div class="g2">
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Général</h3></div>
          <div class="card-body">
            <div class="form-group"><label class="form-label">Nom de la plateforme</label><input type="text" class="form-input" value="Orientation Platform"></div>
            <div class="form-group"><label class="form-label">URL API backend</label><input type="text" class="form-input" value="/api/v1"></div>
            <div class="form-group"><label class="form-label">Langue par défaut</label><select class="form-input"><option>Français</option><option>Anglais</option></select></div>
            <div class="form-group"><label class="form-label">Fuseau horaire</label><select class="form-input"><option>UTC</option><option>Africa/Porto-Novo (WAT)</option></select></div>
          </div>
        </div>
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Sécurité</h3></div>
          <div class="card-body">
            <div class="form-group"><label class="form-label">Durée token d'accès (sec)</label><input type="number" class="form-input" value="86400"></div>
            <div class="form-group"><label class="form-label">Durée refresh token (sec)</label><input type="number" class="form-input" value="604800"></div>
            <div class="form-group"><label class="form-label">Tentatives max avant verrouillage</label><input type="number" class="form-input" value="5"></div>
            <div class="form-group"><label class="form-label">Durée verrouillage (sec)</label><input type="number" class="form-input" value="900"></div>
          </div>
        </div>
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Notifications</h3></div>
          <div class="card-body">
            @for (s of notifSettings; track s.label) {
              <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 0;border-bottom:1px solid var(--n-100)">
                <div><div style="font-size:.8125rem;font-weight:500;color:var(--n-800)">{{ s.label }}</div><div style="font-size:.6875rem;color:var(--n-500)">{{ s.desc }}</div></div>
                <label class="toggle"><input type="checkbox" [checked]="s.on"><span class="toggle-track"></span></label>
              </div>
            }
          </div>
        </div>
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Stockage</h3></div>
          <div class="card-body">
            <div class="form-group"><label class="form-label">Répertoire d'upload</label><input type="text" class="form-input" value="./uploads"></div>
            <div class="form-group"><label class="form-label">Taille max fichier (MB)</label><input type="number" class="form-input" value="10"></div>
            <div class="form-group"><label class="form-label">Types autorisés</label><input type="text" class="form-input" value="application/pdf, image/jpeg, image/png"></div>
            <div style="margin-top:16px;padding:16px;background:var(--n-50);border-radius:var(--radius-sm)">
              <div style="display:flex;justify-content:space-between;margin-bottom:8px"><span style="font-size:.8125rem;color:var(--n-600)">Espace utilisé</span><span style="font-size:.8125rem;font-weight:700;color:var(--n-800)">2.4 Go / 10 Go</span></div>
              <div class="progress" style="height:8px"><div class="progress-bar blue" style="width:24%"></div></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`:host{display:block}`]
})
export class SettingsComponent {
  notifSettings = [
    { label: 'Notifications email', desc: "Envoyer des emails pour les événements importants", on: true },
    { label: 'Notifications push', desc: 'Notifications dans le backoffice en temps réel', on: true },
    { label: 'Alertes import', desc: "Notifier en cas d'erreur lors des imports", on: true },
    { label: 'Alertes sécurité', desc: 'Notifier les tentatives de connexion suspectes', on: true },
    { label: 'Rapports automatiques', desc: 'Générer et envoyer des rapports hebdomadaires', on: false },
  ];
}
