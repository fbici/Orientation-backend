import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-knowledge',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Knowledge Engine</h1><p>Base de connaissances et moteur de recherche intelligent</p></div>
        <div class="page-header-actions">
          <a routerLink="/knowledge/smart-query" class="btn btn-primary"><span class="material-symbols-rounded">psychology</span>Smart Query</a>
        </div>
      </div>

      <!-- Search -->
      <div class="card" style="margin-bottom:22px">
        <div class="card-body">
          <div style="display:flex;gap:12px">
            <div style="flex:1;position:relative">
              <span class="material-symbols-rounded" style="position:absolute;left:14px;top:50%;transform:translateY(-50%);font-size:20px;color:var(--n-400)">search</span>
              <input type="text" class="form-input" style="padding:12px 14px 12px 44px;font-size:.9375rem" placeholder="Rechercher dans la base de connaissances…" [(ngModel)]="searchQuery" (keydown.enter)="search()">
            </div>
            <button class="btn btn-primary" (click)="search()" [disabled]="searching()">
              @if (searching()) { <span class="spinner-sm"></span> } @else { <span class="material-symbols-rounded">search</span> }
              Rechercher
            </button>
          </div>
        </div>
      </div>

      <!-- Stats -->
      <div class="g4 stagger" style="margin-bottom:22px">
        @for (k of kpis; track k.label) {
          <div class="stat-card anim-fade-up">
            <div class="stat-icon" [style.background]="k.g"><span class="material-symbols-rounded filled">{{ k.icon }}</span></div>
            <div class="stat-content"><div class="stat-label">{{ k.label }}</div><div class="stat-value">{{ k.val }}</div></div>
          </div>
        }
      </div>

      <!-- Nodes -->
      <div class="card">
        <div class="card-header">
          <span style="font-size:.8125rem;color:var(--n-500)">{{ total() }} nœuds de connaissances</span>
          <button class="btn btn-secondary btn-sm" (click)="load()"><span class="material-symbols-rounded" style="font-size:16px">refresh</span></button>
        </div>
        <div class="card-body" style="padding:0">
          @if (loading()) {
            <div style="padding:48px;text-align:center"><div class="spinner"></div></div>
          } @else {
            <table class="data-table">
              <thead><tr><th>Type</th><th>Nom</th><th>Description</th><th>Relations</th><th>Source</th></tr></thead>
              <tbody>
                @for (n of nodes(); track n.id) {
                  <tr>
                    <td><span class="badge" [class]="typeClass(n.type)">{{ n.type || '—' }}</span></td>
                    <td style="font-weight:600;color:var(--n-900)">{{ n.name || n.label }}</td>
                    <td style="font-size:.8125rem;color:var(--n-600);max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ n.description || '—' }}</td>
                    <td style="font-size:.8125rem">{{ n.relationCount || n.relations?.length || 0 }}</td>
                    <td style="font-size:.75rem;color:var(--n-500)">{{ n.source || '—' }}</td>
                  </tr>
                } @empty {
                  <tr><td colspan="5" style="text-align:center;padding:48px;color:var(--n-400)">
                    <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px;color:var(--n-300)">hub</span>
                    <p style="font-weight:600;color:var(--n-600)">Base de connaissances vide</p>
                    <p style="font-size:.8125rem">Importez des documents pour enrichir automatiquement la base.</p>
                  </td></tr>
                }
              </tbody>
            </table>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    .spinner-sm{width:16px;height:16px;border:2px solid rgba(255,255,255,.3);border-top-color:#fff;border-radius:50%;animation:spin .6s linear infinite}
    @keyframes spin{to{transform:rotate(360deg)}}
  `]
})
export class KnowledgeComponent implements OnInit {
  nodes = signal<any[]>([]);
  total = signal(0);
  loading = signal(false);
  searching = signal(false);
  searchQuery = '';
  kpis = [
    { icon: 'hub', label: 'Nœuds', val: '0', g: 'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
    { icon: 'account_tree', label: 'Relations', val: '0', g: 'linear-gradient(135deg,#22c55e,#15803d)' },
    { icon: 'description', label: 'Documents indexés', val: '0', g: 'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
    { icon: 'search', label: 'Recherches', val: '0', g: 'linear-gradient(135deg,#f97316,#ea580c)' },
  ];

  constructor(private api: ApiService) {}
  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.api.getKnowledgeNodes().subscribe({
      next: (r) => {
        const items = r?.content || r || [];
        this.nodes.set(Array.isArray(items) ? items : []);
        this.total.set(r?.totalElements ?? this.nodes().length);
        this.kpis[0].val = String(this.total());
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); }
    });
  }

  search(): void {
    if (!this.searchQuery.trim()) return;
    this.searching.set(true);
    this.api.searchKnowledge(this.searchQuery).subscribe({
      next: (r) => {
        const items = r?.content || r || [];
        this.nodes.set(Array.isArray(items) ? items : []);
        this.total.set(this.nodes().length);
        this.searching.set(false);
      },
      error: () => { this.searching.set(false); }
    });
  }

  typeClass(type: string): string {
    if (!type) return 'badge-gray';
    const t = type.toLowerCase();
    if (t.includes('university') || t.includes('université')) return 'badge-primary';
    if (t.includes('program') || t.includes('filière')) return 'badge-success';
    if (t.includes('country') || t.includes('pays')) return 'badge-info';
    if (t.includes('scholarship') || t.includes('bourse')) return 'badge-warning';
    return 'badge-gray';
  }
}
