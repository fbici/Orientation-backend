import { Component } from '@angular/core';

@Component({
  selector: 'app-roles',
  standalone: true,
  template: `
    <div class="roles">
      <h1>Rôles & Permissions</h1>
      <div class="content-card">
        <h3>Gestion des Rôles</h3>
        <p>Module de gestion des rôles et permissions en cours de développement.</p>
      </div>
    </div>
  `,
  styles: [`.roles { max-width: 1400px; } h1 { color: #1a1a2e; margin-bottom: 30px; } .content-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }`]
})
export class RolesComponent {}
