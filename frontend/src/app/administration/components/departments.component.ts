import { Component } from '@angular/core';

@Component({
  selector: 'app-departments',
  standalone: true,
  template: `
    <div class="departments">
      <h1>Départements</h1>
      <div class="content-card">
        <h3>Gestion des Départements</h3>
        <p>Module de gestion des départements en cours de développement.</p>
      </div>
    </div>
  `,
  styles: [`.departments { max-width: 1400px; } h1 { color: #1a1a2e; margin-bottom: 30px; } .content-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }`]
})
export class DepartmentsComponent {}
