import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-university-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="anim-fade-up">
      @if (loading()) {
        <div style="padding:48px;text-align:center"><div class="spinner"></div></div>
      } @else if (uni()) {
        <div class="page-header">
          <div style="display:flex;align-items:center;gap:12px">
            <a routerLink="/universities" class="btn btn-ghost btn-icon btn-sm"><span class="material-symbols-rounded">arrow_back</span></a>
            <div>
              <h1>{{ uni().name }}</h1>
              <p>{{ uni().shortName }} — {{ uni().country?.name }}, {{ uni().city?.name }}</p>
            </div>
          </div>
        </div>

        <div class="g3" style="margin-bottom:22px">
          <div class="stat-card"><div class="stat-icon" style="background:linear-gradient(135deg,#3b82f6,#1d4ed8)"><span class="material-symbols-rounded filled">school</span></div><div class="stat-content"><div class="stat-label">Campus</div><div class="stat-value">{{ uni().campuses?.length || 0 }}</div></div></div>
          <div class="stat-card"><div class="stat-icon" style="background:linear-gradient(135deg,#22c55e,#15803d)"><span class="material-symbols-rounded filled">workspace_premium</span></div><div class="stat-content"><div class="stat-label">Classement national</div><div class="stat-value">#{{ uni().ranking || '—' }}</div></div></div>
          <div class="stat-card"><div class="stat-icon" style="background:linear-gradient(135deg,#8b5cf6,#6d28d9)"><span class="material-symbols-rounded filled">groups</span></div><div class="stat-content"><div class="stat-label">Étudiants</div><div class="stat-value">{{ uni().studentCount || '—' | number }}</div></div></div>
        </div>

        <div class="g2">
          <div class="card">
            <div class="card-header"><h3>Informations</h3></div>
            <div class="card-body">
              <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
                <div><span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:2px">Adresse</span><span style="font-size:.875rem;color:var(--n-800)">{{ uni().address || '—' }}</span></div>
                <div><span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:2px">Email</span><span style="font-size:.875rem;color:var(--n-800)">{{ uni().email || '—' }}</span></div>
                <div><span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:2px">Téléphone</span><span style="font-size:.875rem;color:var(--n-800)">{{ uni().phone || '—' }}</span></div>
                <div><span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:2px">Site web</span><span style="font-size:.875rem;color:var(--n-800)">{{ uni().website || '—' }}</span></div>
                <div><span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:2px">Fondée en</span><span style="font-size:.875rem;color:var(--n-800)">{{ uni().foundedYear || '—' }}</span></div>
                <div><span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:2px">Taux d'acceptation</span><span style="font-size:.875rem;color:var(--n-800)">{{ uni().acceptationRate ? uni().acceptationRate + '%' : '—' }}</span></div>
              </div>
              @if (uni().description) {
                <div style="margin-top:16px;padding-top:16px;border-top:1px solid var(--n-100)">
                  <span style="font-size:.75rem;color:var(--n-500);display:block;margin-bottom:6px">Description</span>
                  <p style="font-size:.875rem;color:var(--n-700);line-height:1.6">{{ uni().description }}</p>
                </div>
              }
            </div>
          </div>

          <div class="card">
            <div class="card-header"><h3>Campus</h3></div>
            <div class="card-body" style="padding:0">
              @for (c of uni().campuses || []; track c.id) {
                <div style="display:flex;align-items:center;gap:12px;padding:14px 20px;border-bottom:1px solid var(--n-100)">
                  <span class="material-symbols-rounded" style="font-size:20px;color:var(--n-400)">location_on</span>
                  <div style="flex:1"><div style="font-size:.875rem;font-weight:600;color:var(--n-800)">{{ c.name }}</div><div style="font-size:.75rem;color:var(--n-500)">{{ c.address || '—' }}</div></div>
                </div>
              } @empty {
                <div style="padding:32px;text-align:center;color:var(--n-400);font-size:.8125rem">Aucun campus enregistré</div>
              }
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`.spinner{width:32px;height:32px;border:3px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}@keyframes spin{to{transform:rotate(360deg)}}`]
})
export class UniversityDetailComponent implements OnInit {
  uni = signal<any>(null);
  loading = signal(true);

  constructor(private api: ApiService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.api.getUniversity(id).subscribe({
        next: (u) => { this.uni.set(u); this.loading.set(false); },
        error: () => { this.loading.set(false); }
      });
    }
  }
}
