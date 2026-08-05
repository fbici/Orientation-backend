import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface UploadProgress {
  fileId: string;
  fileName: string;
  progress: number;
  status: 'pending' | 'uploading' | 'completed' | 'error';
  error?: string;
}

@Injectable({ providedIn: 'root' })
export class FileUploadService {

  private readonly API_URL = environment.apiUrl;
  uploads = signal<UploadProgress[]>([]);

  constructor(private http: HttpClient) {}

  uploadFile(file: File, endpoint: string, additionalData?: Map<string, string>): Observable<HttpEvent<any>> {
    const formData = new FormData();
    formData.append('file', file);

    if (additionalData) {
      additionalData.forEach((value, key) => formData.append(key, value));
    }

    const request = new HttpRequest('POST', `${this.API_URL}/${endpoint}`, formData, {
      reportProgress: true
    });

    return this.http.request(request);
  }

  uploadMultipleFiles(files: File[], endpoint: string): Observable<HttpEvent<any>> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));

    const request = new HttpRequest('POST', `${this.API_URL}/${endpoint}`, formData, {
      reportProgress: true
    });

    return this.http.request(request);
  }

  validateFile(file: File, allowedTypes: string[], maxSize: number): { valid: boolean; error?: string } {
    if (!allowedTypes.includes(file.type)) {
      return { valid: false, error: `Type de fichier non autorisé: ${file.type}` };
    }
    if (file.size > maxSize) {
      return { valid: false, error: `Fichier trop volumineux: ${(file.size / 1024 / 1024).toFixed(2)}MB > ${maxSize / 1024 / 1024}MB` };
    }
    return { valid: true };
  }

  getSupportedTypes(): string[] {
    return [
      'application/pdf',
      'image/jpeg',
      'image/png',
      'text/csv',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'application/json'
    ];
  }
}
