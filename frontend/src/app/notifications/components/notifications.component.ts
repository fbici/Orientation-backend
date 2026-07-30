import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Notifications</h1><p>Centre de notifications et alertes système</p></div>
        <div class="page-header-actions"><button class="btn btn-secondary"><span class="material-symbols-rounded">done_all</span>Tout marquer comme lu</button></div>
      </div>
      <div class="card anim-fade-up">
        <div class="card-header">
          <div style="display:flex;gap:4px">
            @for (tab of tabs; track tab.id) {
              <button class="btn btn-sm" [class.btn-primary]="active===tab.id" [class.btn-ghost]="active!==tab.id" (click)="active=tab.id">
                {{ tab.label }} @if (tab.count>0) { <span class="badge" [class]="active===tab.id?'badge-gray':'badge-primary'" style="margin-left:4px">{{ tab.count }}</span> }
              </button>
            }
          </div>
        </div>
        <div class="card-body" style="padding:0">
          @for (n of notifs; track n.id) {
            <div [style.background]="n.read?'transparent':'var(--brand-50)'" style="display:flex;align-items:flex-start;gap:14px;padding:16px 24px;border-bottom:1px solid var(--n-100);cursor:pointer;transition:background var(--dur-fast)" (click)="n.read=true">
              <div style="width:36px;height:36px;border-radius:var(--radius-sm);display:flex;align-items:center;justify-content:center;flex-shrink:0" [style.background]="n.bg">
                <span class="material-symbols-rounded" [style.color]="n.color" style="font-size:18px">{{ n.icon }}</span>
              </div>
              <div style="flex:1;min-width:0"><div style="font-size:.8125rem;color:var(--n-800);line-height:1.5">{{ n.text }}</div><div style="font-size:.6875rem;color:var(--n-400);margin-top:4px">{{ n.time }}</div></div>
              @if (!n.read) { <div style="width:8px;height:8px;border-radius:50%;background:var(--brand);flex-shrink:0;margin-top:6px"></div> }
            </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`:host{display:block}`]
})
export class NotificationsComponent {
  active = 'all';
  tabs = [{ id: 'all', label: 'Toutes', count: 0 }, { id: 'unread', label: 'Non lues', count: 3 }, { id: 'system', label: 'Système', count: 1 }];
  notifs = [
    { id: 1, icon: 'upload_file', bg: '#eff6ff', color: '#3b82f6', text: 'Import universities_benin.csv terminé — 124 entrées importées avec succès.', time: 'Il y a 5 min', read: false },
    { id: 2, icon: 'recommend', bg: '#f0fdf4', color: '#16a34a', text: '12 nouvelles recommandations générées pour les candidats du lot 2026.', time: 'Il y a 12 min', read: false },
    { id: 3, icon: 'warning', bg: '#fffbeb', color: '#d97706', text: 'Import bulletin_math.csv — 3 lignes avec erreurs de validation.', time: 'Il y a 2h', read: false },
    { id: 4, icon: 'person_add', bg: '#f5f3ff', color: '#7c3aed', text: 'Nouveau compte créé : prof.diallo@univ.edu (rôle ADMIN)', time: 'Il y a 3h', read: true },
    { id: 5, icon: 'security', bg: '#fef2f2', color: '#dc2626', text: 'Tentative de connexion échouée depuis 41.82.103.5 — compte admin', time: 'Il y a 5h', read: true },
    { id: 6, icon: 'update', bg: '#f0f9ff', color: '#0284c7', text: 'Mise à jour v1.0.1 disponible — corrections de sécurité.', time: 'Hier', read: true },
  ];
}
