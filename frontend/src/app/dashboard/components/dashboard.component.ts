import { Component, OnInit, AfterViewInit, OnDestroy, ViewChild, ElementRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { Chart, registerables } from 'chart.js';
Chart.register(...registerables);
@Component({
  selector: 'app-dashboard', standalone: true, imports: [CommonModule, RouterLink],
  template: `
    <div class="anim-fade-up">
      <div class="page-header"><div><h1>Tableau de bord</h1><p>Vue d'ensemble de la plateforme d'orientation universitaire</p></div>
        <div class="page-header-actions"><button class="btn btn-secondary"><span class="material-symbols-rounded">refresh</span> Actualiser</button></div></div>
      <div class="g4 stagger" style="margin-bottom:22px">
        @for (k of kpis; track k.label) { <div class="stat-card anim-fade-up"><div class="stat-icon" [style.background]="k.g"><span class="material-symbols-rounded filled">{{ k.icon }}</span></div><div class="stat-content"><div class="stat-label">{{ k.label }}</div><div class="stat-value">{{ k.value | number }}</div><div class="stat-change" [class.up]="k.change>0" [class.down]="k.change<0"><span class="material-symbols-rounded">{{ k.change>0?'trending_up':'trending_down' }}</span> {{ k.change>0?'+':'' }}{{ k.change }}% <span style="color:var(--n-400);font-weight:400;margin-left:2px">vs mois dernier</span></div></div></div> }
      </div>
      <div class="g2" style="margin-bottom:22px">
        <div class="card anim-fade-up"><div class="card-header"><h3>Évolution des recommandations</h3><div style="display:flex;gap:4px">@for(p of ['7j','30j','90j','12m'];track p){<button class="btn btn-sm" [class.btn-primary]="period()===p" [class.btn-ghost]="period()!==p" (click)="period.set(p)">{{p}}</button>}</div></div><div class="card-body"><div class="chart-wrap"><canvas #lineCanvas></canvas></div></div></div>
        <div class="card anim-fade-up"><div class="card-header"><h3>Répartition par filière</h3></div><div class="card-body"><div class="chart-wrap"><canvas #doughnutCanvas></canvas></div></div></div>
      </div>
      <div class="g3" style="margin-bottom:22px">
        <div class="card anim-fade-up"><div class="card-header"><h3>Santé du système</h3><span class="badge badge-success">Opérationnel</span></div><div class="card-body"><div style="display:flex;flex-direction:column;gap:20px">@for(h of health;track h.label){<div><div style="display:flex;justify-content:space-between;margin-bottom:7px"><div style="display:flex;align-items:center;gap:8px"><span class="material-symbols-rounded" [style.color]="h.color" style="font-size:18px">{{h.icon}}</span><span style="font-size:.8125rem;color:var(--n-700)">{{h.label}}</span></div><span style="font-size:.8125rem;font-weight:700;color:var(--n-900)">{{h.val}}</span></div><div class="progress" style="height:7px"><div class="progress-bar" [class]="h.cls" [style.width.%]="h.pct"></div></div></div>}</div></div></div>
        <div class="card anim-fade-up"><div class="card-header"><h3>Activité récente</h3><a routerLink="/monitoring" class="btn btn-ghost btn-sm">Tout voir <span class="material-symbols-rounded" style="font-size:16px">arrow_forward</span></a></div><div class="card-body" style="padding:8px 24px">@for(a of activity;track a.text){<div style="display:flex;gap:12px;padding:11px 0;border-bottom:1px solid var(--n-100)"><div style="width:32px;height:32px;border-radius:var(--radius-sm);display:flex;align-items:center;justify-content:center;flex-shrink:0" [style.background]="a.bg"><span class="material-symbols-rounded" [style.color]="a.color" style="font-size:16px">{{a.icon}}</span></div><div style="flex:1;min-width:0"><div style="font-size:.8125rem;color:var(--n-700);line-height:1.45">{{a.text}}</div><div style="font-size:.6875rem;color:var(--n-400);margin-top:2px">{{a.time}}</div></div></div>}</div></div>
        <div class="card anim-fade-up"><div class="card-header"><h3>Programmes populaires</h3><a routerLink="/recommendations" class="btn btn-ghost btn-sm">Tout voir <span class="material-symbols-rounded" style="font-size:16px">arrow_forward</span></a></div><div class="card-body" style="padding:8px 24px">@for(t of top;track t.name){<div style="display:flex;align-items:center;gap:12px;padding:10px 0;border-bottom:1px solid var(--n-100)"><div [class]="'rank rank-'+t.r" style="width:28px;height:28px;border-radius:var(--radius-sm);display:flex;align-items:center;justify-content:center;font-size:.75rem;font-weight:800;flex-shrink:0">{{t.r}}</div><div style="flex:1;min-width:0"><div style="font-size:.8125rem;font-weight:600;color:var(--n-800);margin-bottom:5px">{{t.name}}</div><div class="progress"><div class="progress-bar" [class]="t.c" [style.width.%]="t.s"></div></div></div><span style="font-size:.8125rem;font-weight:700;color:var(--n-800);min-width:36px;text-align:right">{{t.s}}%</span></div>}</div></div>
      </div>
      <div class="card anim-fade-up"><div class="card-header"><h3>Actions rapides</h3></div><div class="card-body"><div style="display:grid;grid-template-columns:repeat(4,1fr);gap:14px">@for(act of actions;track act.label){<a [routerLink]="act.route" class="qa"><span class="material-symbols-rounded" style="font-size:28px;color:var(--brand)">{{act.icon}}</span><span>{{act.label}}</span></a>}</div></div></div>
    </div>`,
  styles: [`
    .rank{background:var(--n-100);color:var(--n-600)}.rank-1{background:var(--brand-100);color:var(--brand-dark)}.rank-2{background:var(--n-200);color:var(--n-700)}.rank-3{background:#fef3c7;color:#92400e}
    .qa{display:flex;flex-direction:column;align-items:center;gap:10px;padding:26px 14px;border:1px solid var(--n-200);border-radius:var(--radius-md);text-decoration:none;font-size:.8125rem;font-weight:600;color:var(--n-700);transition:all var(--dur-base) var(--ease-out);cursor:pointer}.qa:hover{border-color:var(--brand-200);background:var(--brand-50);transform:translateY(-2px);box-shadow:var(--shadow-md)}
  `]
})
export class DashboardComponent implements AfterViewInit, OnDestroy {
  @ViewChild('lineCanvas') lc!: ElementRef<HTMLCanvasElement>;
  @ViewChild('doughnutCanvas') dc!: ElementRef<HTMLCanvasElement>;
  period = signal('12m');
  private c1?: Chart; private c2?: Chart;
  kpis = [
    { icon:'group', label:'Candidats inscrits', value:2847, change:12, g:'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
    { icon:'recommend', label:'Recommandations', value:18432, change:8, g:'linear-gradient(135deg,#22c55e,#15803d)' },
    { icon:'description', label:'Documents traités', value:563, change:5, g:'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
    { icon:'school', label:'Universités', value:124, change:3, g:'linear-gradient(135deg,#14b8a6,#0d9488)' },
  ];
  health = [
    { icon:'memory', label:'Processeur', val:'34%', pct:34, color:'#3b82f6', cls:'blue' },
    { icon:'storage', label:'Mémoire', val:'4.2 / 8 Go', pct:52, color:'#8b5cf6', cls:'violet' },
    { icon:'hard_drive_2', label:'Disque', val:'45 / 100 Go', pct:45, color:'#14b8a6', cls:'teal' },
    { icon:'dns', label:'Connexions DB', val:'12 / 100', pct:12, color:'#22c55e', cls:'green' },
  ];
  activity = [
    { icon:'upload_file', color:'#3b82f6', bg:'#eff6ff', text:'Import universities_benin.csv — 124 entrées validées', time:'Il y a 5 min' },
    { icon:'recommend', color:'#16a34a', bg:'#f0fdf4', text:'12 nouvelles recommandations générées pour le lot 2026', time:'Il y a 12 min' },
    { icon:'description', color:'#7c3aed', bg:'#f5f3ff', text:'Guide Orientation 2025 traité par OCR — 48 pages', time:'Il y a 30 min' },
    { icon:'person_add', color:'#3b82f6', bg:'#eff6ff', text:'Nouveau candidat inscrit : Jean Dupont (Sciences)', time:'Il y a 1h' },
    { icon:'warning', color:'#d97706', bg:'#fffbeb', text:'Import bulletin_math.csv — 3 erreurs de validation', time:'Il y a 2h' },
  ];
  top = [
    { r:1, name:'Génie Informatique', s:92, c:'blue' },
    { r:2, name:'Médecine Générale', s:87, c:'green' },
    { r:3, name:'Droit Privé', s:78, c:'violet' },
    { r:4, name:'Génie Civil', s:74, c:'amber' },
    { r:5, name:'Pharmacie', s:71, c:'teal' },
  ];
  actions = [
    { icon:'upload_file', label:'Importer des données', route:'/imports' },
    { icon:'recommend', label:'Générer des reco.', route:'/recommendations' },
    { icon:'document_scanner', label:'Scanner un document', route:'/documents' },
    { icon:'assessment', label:'Créer un rapport', route:'/reports' },
  ];
  ngAfterViewInit(): void { this.buildLine(); this.buildDoughnut(); }
  ngOnDestroy(): void { this.c1?.destroy(); this.c2?.destroy(); }
  private buildLine(): void {
    this.c1 = new Chart(this.lc.nativeElement.getContext('2d')!, {
      type:'line', data:{ labels:['Jan','Fév','Mar','Avr','Mai','Jun','Jul','Aoû','Sep','Oct','Nov','Déc'],
        datasets:[{label:'Recommandations',data:[820,932,1100,1290,1400,1520,1680,1890,2050,2200,2450,2680],borderColor:'#2563eb',backgroundColor:'rgba(37,99,235,.08)',fill:true,tension:.4,pointRadius:0,pointHoverRadius:5,borderWidth:2.5},
          {label:'Candidats',data:[120,145,180,210,250,290,340,400,460,520,590,680],borderColor:'#8b5cf6',backgroundColor:'rgba(139,92,246,.06)',fill:true,tension:.4,pointRadius:0,pointHoverRadius:5,borderWidth:2}]},
      options:{responsive:true,maintainAspectRatio:false,interaction:{mode:'index',intersect:false},
        plugins:{legend:{position:'top',align:'end',labels:{boxWidth:12,boxHeight:3,padding:20,font:{size:11,family:'Inter',weight:'500'},color:'#6b7280'}},
          tooltip:{backgroundColor:'#111827',titleFont:{family:'Inter',size:12},bodyFont:{family:'Inter',size:11},padding:12,cornerRadius:8}},
        scales:{x:{grid:{display:false},ticks:{font:{size:11,family:'Inter'},color:'#9ca3af'},border:{display:false}},y:{grid:{color:'#f3f4f6'},ticks:{font:{size:11,family:'Inter'},color:'#9ca3af'},border:{display:false}}}}
    });
  }
  private buildDoughnut(): void {
    this.c2 = new Chart(this.dc.nativeElement.getContext('2d')!, {
      type:'doughnut', data:{labels:['Sciences & Tech','Santé','Droit & Éco','Lettres','Arts'],datasets:[{data:[38,22,20,12,8],backgroundColor:['#2563eb','#16a34a','#f59e0b','#8b5cf6','#f43f5e'],borderWidth:0,spacing:3,borderRadius:4}]},
      options:{responsive:true,maintainAspectRatio:false,cutout:'68%',plugins:{legend:{position:'bottom',labels:{boxWidth:10,boxHeight:10,usePointStyle:true,pointStyle:'circle',padding:16,font:{size:11,family:'Inter',weight:'500'},color:'#6b7280'}},tooltip:{backgroundColor:'#111827',titleFont:{family:'Inter'},bodyFont:{family:'Inter'},padding:12,cornerRadius:8,callbacks:{label:(c)=>` ${c.label}: ${c.parsed}%`}}}}
    });
  }
}
