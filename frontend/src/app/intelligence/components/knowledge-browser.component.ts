import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { IntelligenceService } from '../../core/services/intelligence.service';

@Component({
  selector: 'app-knowledge-browser',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div style="display:flex;align-items:center;gap:12px">
          <a routerLink="/intelligence" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a>
          <div><h1>Knowledge Graph</h1><p>Explorez la base de connaissances</p></div>
        </div>
      </div>

      <!-- Search -->
      <div class="card" style="margin-bottom:22px">
        <div class="card-body">
          <div style="display:flex;gap:12px">
            <div style="flex:1;position:relative">
              <span class="material-symbols-rounded" style="position:absolute;left:14px;top:50%;transform:translateY(-50%);font-size:20px;color:var(--n-400)">search</span>
              <input type="text" class="form-input" style="padding:12px 14px 12px 44px;font-size:.9375rem" placeholder="Rechercher dans le Knowledge Graph..." [(ngModel)]="searchQuery" (keydown.enter)="search()">
            </div>
            <button class="btn btn-primary" (click)="search()" [disabled]="searching()">
              @if (searching()) { <span class="spinner-sm"></span> } @else { <span class="material-symbols-rounded">search</span> }
            </button>
          </div>
        </div>
      </div>

      <!-- Nodes -->
      <div class="card">
        <div class="card-header">
          <span style="font-size:.8125rem;color:var(--n-500)">{{ nodes().length }} nœuds</span>
          <button class="btn btn-secondary btn-sm" (click)="load()"><span class="material-symbols-rounded" style="font-size:16px">refresh</span></button>
        </div>
        <div class="card-body" style="padding:0">
          @if (loading()) { <div style="padding:48px;text-align:center"><div class="spinner"></div></div> } @else {
            <table class="data-table">
              <thead><tr><th>Type</th><th>Nom</th><th>Description</th><th>Source</th></tr></thead>
              <tbody>
                @for (n of nodes(); track n.id) {
                  <tr>
                    <td><span class="badge" [class]="nodeTypeClass(n.type)">{{ n.type }}</span></td>
                    <td style="font-weight:600;color:var(--n-900)">{{ n.name }}</td>
                    <td style="font-size:.8125rem;color:var(--n-600);max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ n.description || n.metadata || '-' }}</td>
                    <td style="font-size:.75rem;color:var(--n-500)">{{ n.source || '-' }}</td>
                  </tr>
                } @empty {
                  <tr><td colspan="4" style="text-align:center;padding:48px;color:var(--n-400)">
                    <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px;color:var(--n-300)">hub</span>
                    <p style="font-weight:600;color:var(--n-600)">Base de connaissances vide</p>
                    <p style="font-size:.8125rem">Importez des documents via le pipeline pour enrichir la base.</p>
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
export class KnowledgeBrowserComponent implements OnInit {
  nodes = signal<any[]>([]);
  loading = signal(false);
  searching = signal(false);
  searchQuery = '';

  constructor(private intelSvc: IntelligenceService) {}
  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.intelSvc.searchKnowledge('').subscribe({
      next: (r) => { this.nodes.set(r || []); this.loading.set(false); },
      error: () => { this.loading.set(false); }
    });
  }

  search(): void {
    if (!this.searchQuery.trim()) { this.load(); return; }
    this.searching.set(true);
    this.intelSvc.searchKnowledge(this.searchQuery).subscribe({
      next: (r) => { this.nodes.set(r || []); this.searching.set(false); },
      error: () => { this.searching.set(false); }
    });
  }

  nodeTypeClass(type: string): string {
    if (!type) return 'badge-gray';
    const t = type.toLowerCase();
    if (t.includes('university')) return 'badge-primary';
    if (t.includes('program')) return 'badge-success';
    if (t.includes('scholarship')) return 'badge-warning';
    if (t.includes('subject')) return 'badge-violet';
    if (t.includes('language')) return 'badge-info';
    return 'badge-gray';
  }
}
