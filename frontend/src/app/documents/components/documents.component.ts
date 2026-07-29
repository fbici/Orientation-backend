import { Component, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [DecimalPipe],
  template: `
    <div class="documents">
      <h1>Documents</h1>

      <div class="upload-card">
        <h3>Upload Document</h3>
        <input type="file" (change)="onFileSelected($event)" accept=".pdf,.docx,.txt,.csv">
        <button (click)="upload()" [disabled]="!selectedFile() || uploading()">
          {{ uploading() ? 'Upload en cours...' : 'Uploader' }}
        </button>
        @if (uploadMessage()) {
          <div class="message" [class.success]="uploadSuccess()">{{ uploadMessage() }}</div>
        }
      </div>

      <div class="documents-list">
        <h3>Documents Récents</h3>
        @if (documents()?.content?.length) {
          @for (doc of documents().content; track doc.id) {
            <div class="document-item">
              <span class="doc-type">{{ doc.documentType }}</span>
              <span class="doc-title">{{ doc.title }}</span>
              <span class="doc-status">{{ doc.status }}</span>
              <span class="doc-score">OCR: {{ doc.ocrScore | number:'1.0-0' }}%</span>
            </div>
          }
        } @else {
          <p>Aucun document trouvé.</p>
        }
      </div>
    </div>
  `,
  styles: [`
    .documents { max-width: 1400px; }
    h1 { color: #1a1a2e; margin-bottom: 30px; }
    .upload-card, .documents-list { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); margin-bottom: 20px; }
    .upload-card input { margin-right: 10px; }
    .upload-card button { padding: 10px 20px; background: #1a1a2e; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .message { margin-top: 10px; padding: 10px; border-radius: 4px; }
    .message.success { background: #d4edda; color: #155724; }
    .document-item { display: grid; grid-template-columns: 100px 1fr 100px 100px; padding: 12px; border-bottom: 1px solid #f0f0f0; gap: 10px; }
    .doc-type { background: #e8f4fd; color: #1a1a2e; padding: 2px 8px; border-radius: 4px; font-size: 0.8rem; }
    .doc-status { color: #27ae60; }
    .doc-score { color: #1a1a2e; font-weight: bold; }
  `]
})
export class DocumentsComponent {
  selectedFile = signal<File | null>(null);
  uploading = signal(false);
  uploadMessage = signal('');
  uploadSuccess = signal(false);
  documents = signal<any>(null);

  constructor(private api: ApiService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile.set(input.files[0]);
    }
  }

  upload(): void {
    const file = this.selectedFile();
    if (!file) return;

    this.uploading.set(true);
    this.api.uploadDocument(file).subscribe({
      next: (data) => {
        this.uploadMessage.set('Document uploadé avec succès');
        this.uploadSuccess.set(true);
        this.uploading.set(false);
        this.loadDocuments();
      },
      error: () => {
        this.uploadMessage.set('Erreur lors de l\'upload');
        this.uploadSuccess.set(false);
        this.uploading.set(false);
      }
    });
  }

  private loadDocuments(): void {
    this.api.getDocuments().subscribe({
      next: (data) => this.documents.set(data)
    });
  }
}
