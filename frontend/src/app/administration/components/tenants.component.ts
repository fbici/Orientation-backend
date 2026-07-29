import { Component } from '@angular/core';

@Component({
  selector: 'app-tenants',
  standalone: true,
  template: `
    <div class="tenants">
      <h1>Tenants</h1>
      <div class="content-card">
        <h3>Gestion des Tenants</h3>
        <p>Module de gestion des tenants en cours de développement.</p>
      </div>
    </div>
  `,
  styles: [`.tenants { max-width: 1400px; } h1 { color: #1a1a2e; margin-bottom: 30px; } .content-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }`]
})
export class TenantsComponent {}
