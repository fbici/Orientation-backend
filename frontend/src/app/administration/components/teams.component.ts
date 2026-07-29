import { Component } from '@angular/core';

@Component({
  selector: 'app-teams',
  standalone: true,
  template: `
    <div class="teams">
      <h1>Équipes</h1>
      <div class="content-card">
        <h3>Gestion des Équipes</h3>
        <p>Module de gestion des équipes en cours de développement.</p>
      </div>
    </div>
  `,
  styles: [`.teams { max-width: 1400px; } h1 { color: #1a1a2e; margin-bottom: 30px; } .content-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }`]
})
export class TeamsComponent {}
