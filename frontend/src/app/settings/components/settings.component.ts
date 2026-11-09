import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Paramètres</h1><p>Configuration générale de la plateforme</p></div>
        <div class="page-header-actions"><button class="btn btn-primary" disabled title="Aucune API de configuration disponible"><span class="material-symbols-rounded">save</span>Sauvegarder</button></div>
      </div>
      <div class="api-notice" role="status">La configuration serveur est geree par les variables de deploiement. Aucune API de lecture ou sauvegarde des parametres n existe actuellement. Les champs ci-dessous ne sont pas des valeurs mesurees.</div><fieldset disabled style="border:0;padding:0;min-width:0"><div class="g2">
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Général</h3></div>
          <div class="card-body">
            <div class="form-group"><label class="form-label">Nom de la plateforme</label><input type="text" class="form-input" placeholder="Non expose par le serveur"></div>
            <div class="form-group"><label class="form-label">URL API backend</label><input type="text" class="form-input" [value]="apiBase"></div>
            <div class="form-group"><label class="form-label">Langue par défaut</label><select class="form-input"><option>Français</option><option>Anglais</option></select></div>
            <div class="form-group"><label class="form-label">Fuseau horaire</label><select class="form-input"><option>UTC</option><option>Africa/Porto-Novo (WAT)</option></select></div>
          </div>
        </div>
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Sécurité</h3></div>
          <div class="card-body">
            <div class="form-group"><label class="form-label">Durée token d'accès (sec)</label><input type="number" class="form-input" placeholder="Non expose par le serveur"></div>
            <div class="form-group"><label class="form-label">Durée refresh token (sec)</label><input type="number" class="form-input" placeholder="Non expose par le serveur"></div>
            <div class="form-group"><label class="form-label">Tentatives max avant verrouillage</label><input type="number" class="form-input" placeholder="Non expose par le serveur"></div>
            <div class="form-group"><label class="form-label">Durée verrouillage (sec)</label><input type="number" class="form-input" placeholder="Non expose par le serveur"></div>
          </div>
        </div>
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Notifications</h3></div>
          <div class="card-body">
            @for (s of notifSettings; track s.label) {
              <div style="display:flex;align-items:center;justify-content:space-between;padding:12px 0;border-bottom:1px solid var(--n-100)">
                <div><div style="font-size:.8125rem;font-weight:500;color:var(--n-800)">{{ s.label }}</div><div style="font-size:.6875rem;color:var(--n-500)">{{ s.desc }}</div></div>
                <label class="toggle"><input type="checkbox" [checked]="false"><span class="toggle-track"></span></label>
              </div>
            }
          </div>
        </div>
        <div class="card anim-fade-up">
          <div class="card-header"><h3>Stockage</h3></div>
          <div class="card-body">
            <div class="form-group"><label class="form-label">Répertoire d'upload</label><input type="text" class="form-input" placeholder="Non expose par le serveur"></div>
            <div class="form-group"><label class="form-label">Taille max fichier (MB)</label><input type="number" class="form-input" placeholder="Non expose par le serveur"></div>
            <div class="form-group"><label class="form-label">Types autorisés</label><input type="text" class="form-input" placeholder="Non expose par le serveur"></div>
            <div style="margin-top:16px;padding:16px;background:var(--n-50);border-radius:var(--radius-sm)">
              <div style="display:flex;justify-content:space-between;margin-bottom:8px"><span style="font-size:.8125rem;color:var(--n-600)">Espace utilisé</span><span style="font-size:.8125rem;font-weight:700;color:var(--n-800)">Non mesure</span></div>
              <div class="progress" style="height:8px"><div class="progress-bar blue" style="width:0%"></div></div>
            </div>
          </div>
        </div>
      </div>
    </fieldset></div>
  `,
  styles: [`:host{display:block}`]
})
export class SettingsComponent {
  apiBase = environment.apiUrl || 'Meme origine /';
  notifSettings = [
    { label: 'Notifications email', desc: "Envoyer des emails pour les événements importants", on: true },
    { label: 'Notifications push', desc: 'Notifications dans le backoffice en temps réel', on: true },
    { label: 'Alertes import', desc: "Notifier en cas d'erreur lors des imports", on: true },
    { label: 'Alertes sécurité', desc: 'Notifier les tentatives de connexion suspectes', on: true },
    { label: 'Rapports automatiques', desc: 'Générer et envoyer des rapports hebdomadaires', on: false },
  ];
}
