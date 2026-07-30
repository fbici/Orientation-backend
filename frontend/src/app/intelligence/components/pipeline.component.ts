import { Component, OnInit, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IntelligenceService } from '../../core/services/intelligence.service';
import { ToastService } from '../../shared/components/toast.service';

@Component({
  selector: 'app-pipeline',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="anim-fade-up">
      <div class="page-header">
        <div><h1>Orientation Intelligence Platform</h1><p>Pipeline complet : Document → OCR → Extraction → Knowledge Graph → Recommandation</p></div>
        <div class="page-header-actions">
          <a routerLink="/intelligence/smart-query" class="btn btn-secondary"><span class="material-symbols-rounded">psychology</span>Smart Query</a>
          <a routerLink="/intelligence/knowledge" class="btn btn-secondary"><span class="material-symbols-rounded">hub</span>Knowledge Graph</a>
        </div>
      </div>

      <!-- Pipeline visualization -->
      <div class="card" style="margin-bottom:22px">
        <div class="card-header"><h3>Pipeline de traitement documentaire</h3></div>
        <div class="card-body">
          <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;justify-content:center;padding:20px 0">
            @for (step of pipelineSteps; track step.id) {
              <div style="display:flex;align-items:center;gap:8px">
                <div class="pipeline-step" [class.active]="isStepActive(step.id)" [class.done]="isStepDone(step.id)" [class.error]="isStepError(step.id)">
                  <span class="material-symbols-rounded" style="font-size:20px">{{ step.icon }}</span>
                  <span style="font-size:.75rem;font-weight:600">{{ step.label }}</span>
                </div>
                @if (step.id !== 'feedback') {
                  <span class="material-symbols-rounded" style="color:var(--n-300);font-size:18px">arrow_forward</span>
                }
              </div>
            }
          </div>
        </div>
      </div>

      <!-- Upload zone -->
      <div class="card" style="margin-bottom:22px">
        <div class="card-header"><h3>Importer un document</h3></div>
        <div class="card-body">
          <div class="upload-zone" (click)="fileInput.click()" (dragover)="$event.preventDefault()" (drop)="onDrop($event)">
            @if (uploading()) {
              <div style="text-align:center">
                <div class="spinner-lg"></div>
                <p style="margin-top:16px;font-size:.875rem;color:var(--n-600)">Traitement en cours...</p>
                <p style="font-size:.75rem;color:var(--n-400)">Le pipeline analyse votre document automatiquement</p>
              </div>
            } @else {
              <span class="material-symbols-rounded" style="font-size:48px;color:var(--n-300);display:block;margin-bottom:12px">upload_file</span>
              <p style="font-size:.875rem;font-weight:600;color:var(--n-700);margin-bottom:4px">Glissez un fichier ici ou cliquez pour parcourir</p>
              <p style="font-size:.75rem;color:var(--n-400)">PDF, Word, Excel, Images — Le pipeline traitera automatiquement le document</p>
            }
          </div>
          <input #fileInput type="file" accept=".pdf,.docx,.xlsx,.xls,.csv,.jpg,.jpeg,.png" style="display:none" (change)="onFileSelected($event)">
        </div>
      </div>

      <!-- Progress -->
      @if (progress().length > 0) {
        <div class="card" style="margin-bottom:22px">
          <div class="card-header">
            <h3>Progression du pipeline</h3>
            <span class="badge" [class]="status() === 'COMPLETED' ? 'badge-success' : status() === 'ERROR' ? 'badge-danger' : 'badge-info'">{{ status() }}</span>
          </div>
          <div class="card-body" style="padding:0">
            @for (p of progress(); track p.step) {
              <div style="display:flex;align-items:center;gap:12px;padding:12px 20px;border-bottom:1px solid var(--n-100)">
                <span class="material-symbols-rounded" [style.color]="getStepColor(p.step)" style="font-size:18px">{{ getStepIcon(p.step) }}</span>
                <div style="flex:1">
                  <div style="font-size:.8125rem;font-weight:600;color:var(--n-800)">{{ p.step }}</div>
                  <div style="font-size:.75rem;color:var(--n-500)">{{ p.message }}</div>
                </div>
              </div>
            }
          </div>
        </div>
      }

      <!-- Result -->
      @if (result()) {
        <div class="card" style="margin-bottom:22px">
          <div class="card-header">
            <h3>Resultat du traitement</h3>
            <span class="badge" [class]="result()!.status === 'COMPLETED' ? 'badge-success' : 'badge-danger'">{{ result()!.status }}</span>
          </div>
          <div class="card-body">
            <div class="g2" style="margin-bottom:16px">
              <div><span style="font-size:.75rem;color:var(--n-500)">Fichier</span><div style="font-weight:600">{{ result()!.fileName }}</div></div>
              <div><span style="font-size:.75rem;color:var(--n-500)">Type detecte</span><div style="font-weight:600">{{ result()!.documentType || '-' }}</div></div>
              <div><span style="font-size:.75rem;color:var(--n-500)">Duree</span><div style="font-weight:600">{{ getDuration() }}</div></div>
              <div><span style="font-size:.75rem;color:var(--n-500)">Caracteres extraits</span><div style="font-weight:600">{{ result()!.extractedText?.length || 0 | number }}</div></div>
            </div>

            @if (result()!.entities) {
              <div style="border-top:1px solid var(--n-100);padding-top:16px">
                <h4 style="font-size:.875rem;font-weight:600;color:var(--n-800);margin-bottom:12px">Entites extraites</h4>
                <div class="g4">
                  @if (result()!.entities!.universities?.length) {
                    <div class="entity-card">
                      <span class="material-symbols-rounded" style="color:var(--brand)">school</span>
                      <span style="font-weight:700;font-size:1.25rem">{{ result()!.entities!.universities.length }}</span>
                      <span style="font-size:.75rem;color:var(--n-500)">Universites</span>
                    </div>
                  }
                  @if (result()!.entities!.programs?.length) {
                    <div class="entity-card">
                      <span class="material-symbols-rounded" style="color:var(--green-600)">workspace_premium</span>
                      <span style="font-weight:700;font-size:1.25rem">{{ result()!.entities!.programs.length }}</span>
                      <span style="font-size:.75rem;color:var(--n-500)">Programmes</span>
                    </div>
                  }
                  @if (result()!.entities!.scholarships?.length) {
                    <div class="entity-card">
                      <span class="material-symbols-rounded" style="color:var(--amber-500)">payments</span>
                      <span style="font-weight:700;font-size:1.25rem">{{ result()!.entities!.scholarships.length }}</span>
                      <span style="font-size:.75rem;color:var(--n-500)">Bourses</span>
                    </div>
                  }
                  @if (result()!.entities!.subjects?.length) {
                    <div class="entity-card">
                      <span class="material-symbols-rounded" style="color:var(--violet-500)">science</span>
                      <span style="font-weight:700;font-size:1.25rem">{{ result()!.entities!.subjects.length }}</span>
                      <span style="font-size:.75rem;color:var(--n-500)">Matieres</span>
                    </div>
                  }
                </div>
              </div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .upload-zone{border:2px dashed var(--n-300);border-radius:var(--radius-lg);padding:48px 24px;text-align:center;cursor:pointer;transition:all .2s}
    .upload-zone:hover{border-color:var(--brand);background:var(--brand-50)}
    .spinner-lg{width:40px;height:40px;border:4px solid var(--n-200);border-top-color:var(--brand);border-radius:50%;animation:spin .6s linear infinite;margin:0 auto}
    @keyframes spin{to{transform:rotate(360deg)}}
    .pipeline-step{display:flex;flex-direction:column;align-items:center;gap:4px;padding:12px 16px;border-radius:var(--radius-md);border:2px solid var(--n-200);transition:all .2s;min-width:80px}
    .pipeline-step.active{border-color:var(--brand);background:var(--brand-50);animation:pulse 1.5s infinite}
    .pipeline-step.done{border-color:var(--green-500);background:var(--green-50)}
    .pipeline-step.error{border-color:var(--red-500);background:var(--red-50)}
    @keyframes pulse{0%,100%{opacity:1}50%{opacity:.7}}
    .entity-card{display:flex;flex-direction:column;align-items:center;gap:4px;padding:16px;background:var(--n-50);border-radius:var(--radius-md)}
  `]
})
export class PipelineComponent implements OnInit {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  uploading = signal(false);
  status = signal<string>('idle');
  progress = signal<any[]>([]);
  result = signal<any>(null);

  pipelineSteps = [
    { id: 'upload', icon: 'upload_file', label: 'Upload' },
    { id: 'ocr', icon: 'document_scanner', label: 'OCR' },
    { id: 'extraction', icon: 'auto_fix_high', label: 'Extraction' },
    { id: 'classification', icon: 'category', label: 'Classification' },
    { id: 'knowledge', icon: 'hub', label: 'Knowledge Graph' },
    { id: 'indexing', icon: 'manage_search', label: 'Indexation' },
    { id: 'recommendation', icon: 'recommend', label: 'Recommandation' },
    { id: 'feedback', icon: 'feedback', label: 'Feedback' },
  ];

  constructor(private intelSvc: IntelligenceService, private toast: ToastService) {}
  ngOnInit(): void {}

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) this.processFile(file);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0];
    if (file) this.processFile(file);
  }

  private processFile(file: File): void {
    this.uploading.set(true);
    this.status.set('processing');
    this.progress.set([]);
    this.result.set(null);

    this.intelSvc.processDocument(file).subscribe({
      next: (result) => {
        this.uploading.set(false);
        this.result.set(result);
        this.status.set(result.status);
        if (result.steps) {
          this.progress.set(result.steps.map(s => ({ step: s[0], message: s[1] })));
        }
        if (result.status === 'COMPLETED') {
          this.toast.success('Document traite avec succes !');
        } else {
          this.toast.error(result.error || 'Erreur lors du traitement.');
        }
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      },
      error: (e) => {
        this.uploading.set(false);
        this.status.set('ERROR');
        this.toast.error(e.error?.message || 'Erreur lors du traitement.');
        if (this.fileInput) this.fileInput.nativeElement.value = '';
      }
    });
  }

  isStepActive(stepId: string): boolean {
    if (this.status() !== 'processing') return false;
    const currentStep = this.progress().length > 0 ? this.progress()[this.progress().length - 1].step : '';
    return currentStep.toLowerCase().includes(stepId);
  }

  isStepDone(stepId: string): boolean {
    return this.progress().some(p => p.step.toLowerCase().includes(stepId));
  }

  isStepError(stepId: string): boolean {
    return this.status() === 'ERROR' && this.progress().some(p => p.step.toLowerCase().includes(stepId));
  }

  getStepColor(step: string): string {
    if (step.includes('ERROR')) return '#ef4444';
    if (step.includes('OCR')) return '#3b82f6';
    if (step.includes('CLASSIFICATION')) return '#8b5cf6';
    if (step.includes('EXTRACTION')) return '#f97316';
    if (step.includes('KNOWLEDGE')) return '#14b8a6';
    if (step.includes('INDEXING')) return '#22c55e';
    return '#6b7280';
  }

  getStepIcon(step: string): string {
    if (step.includes('OCR')) return 'document_scanner';
    if (step.includes('CLASSIFICATION')) return 'category';
    if (step.includes('EXTRACTION')) return 'auto_fix_high';
    if (step.includes('KNOWLEDGE')) return 'hub';
    if (step.includes('INDEXING')) return 'manage_search';
    if (step.includes('PERSISTENCE')) return 'database';
    if (step.includes('ERROR')) return 'error';
    return 'check_circle';
  }

  getDuration(): string {
    const r = this.result();
    if (!r?.startTime || !r?.endTime) return '-';
    return ((r.endTime - r.startTime) / 1000).toFixed(1) + 's';
  }
}
