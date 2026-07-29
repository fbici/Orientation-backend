import { Component } from '@angular/core';

@Component({
  selector: 'app-rules',
  standalone: true,
  template: `
    <div class="rules">
      <h1>Gestion des Règles</h1>
      <div class="placeholder-card">
        <h3>Éditeur de Règles DSL</h3>
        <p>Module en cours de développement. Permettra de créer, modifier et simuler des règles d'admission.</p>
        <div class="features">
          <div class="feature">✅ Création de règles</div>
          <div class="feature">✅ Simulation</div>
          <div class="feature">✅ Versionning</div>
          <div class="feature">✅ Historique</div>
          <div class="feature">⬜ Éditeur visuel</div>
          <div class="feature">⬜ Drag & Drop</div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .rules { max-width: 1400px; }
    h1 { color: #1a1a2e; margin-bottom: 30px; }
    .placeholder-card { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); text-align: center; }
    .features { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; margin-top: 20px; }
    .feature { padding: 10px; background: #f8f9fa; border-radius: 4px; }
  `]
})
export class RulesComponent {}
