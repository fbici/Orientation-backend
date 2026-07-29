import { Component } from '@angular/core';

@Component({
  selector: 'app-settings',
  standalone: true,
  template: `
    <div class="settings">
      <h1>Paramètres</h1>
      <div class="content-card">
        <h3>Configuration Système</h3>
        <p>Module de paramètres en cours de développement.</p>
      </div>
    </div>
  `,
  styles: [`.settings { max-width: 1400px; } h1 { color: #1a1a2e; margin-bottom: 30px; } .content-card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }`]
})
export class SettingsComponent {}
