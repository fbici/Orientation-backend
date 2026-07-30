import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-rules',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Règles d'admission</h1><p>Configuration des critères du moteur de recommandation</p></div>
        <div class="page-header-actions"><button class="btn btn-primary"><span class="material-symbols-rounded">add</span>Nouvelle règle</button></div>
      </div>
      <div class="g3 stagger">
        @for (cat of cats; track cat.title) {
          <div class="card anim-fade-up">
            <div class="card-header">
              <div style="display:flex;align-items:center;gap:10px"><span class="material-symbols-rounded" [style.color]="cat.color" style="font-size:20px">{{ cat.icon }}</span><h3>{{ cat.title }}</h3></div>
              <span class="badge badge-primary">{{ cat.rules.length }}</span>
            </div>
            <div class="card-body" style="padding:8px 24px">
              @for (rule of cat.rules; track rule.name) {
                <div style="display:flex;align-items:center;justify-content:space-between;padding:11px 0;border-bottom:1px solid var(--n-100)">
                  <div><div style="font-size:.8125rem;font-weight:600;color:var(--n-800)">{{ rule.name }}</div><div style="font-size:.6875rem;color:var(--n-500)">{{ rule.desc }}</div></div>
                  <label class="toggle"><input type="checkbox" [checked]="rule.on"><span class="toggle-track"></span></label>
                </div>
              }
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`:host{display:block}`]
})
export class RulesComponent {
  cats = [
    { icon: 'school', title: 'Critères académiques', color: '#3b82f6', rules: [
      { name: 'Moyenne minimum Bac', desc: 'Moyenne >= 10/20', on: true },
      { name: 'Type de Bac requis', desc: 'Sciences, Maths, etc.', on: true },
      { name: 'Note minimum matière', desc: 'Note >= 8 en spécialité', on: true },
    ]},
    { icon: 'location_on', title: 'Critères géographiques', color: '#16a34a', rules: [
      { name: "Pays d'origine", desc: 'Priorité pays du candidat', on: true },
      { name: 'Distance campus', desc: 'Proximité géographique', on: false },
      { name: 'Zone prioritaire', desc: 'Régions spécifiques', on: true },
    ]},
    { icon: 'payments', title: 'Critères financiers', color: '#ea580c', rules: [
      { name: 'Frais de scolarité', desc: 'Budget max du candidat', on: true },
      { name: 'Bourse disponible', desc: 'Éligibilité aux bourses', on: true },
      { name: 'Coût de la vie', desc: 'Ville du campus', on: false },
    ]},
  ];
}
