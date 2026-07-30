import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface PipelineProgress {
  documentId: string;
  step: string;
  message: string;
}

export interface PipelineResult {
  documentId: string;
  fileName: string;
  status: string;
  error?: string;
  extractedText?: string;
  documentType?: string;
  entities?: any;
  steps: string[][];
  startTime: number;
  endTime?: number;
}

export interface SmartQueryResult {
  question: string;
  keywords: string[];
  knowledgeNodes: any[];
  groupedNodes: Record<string, any[]>;
  answer: string;
  recommendedPrograms: string[];
  sources: string[];
}

@Injectable({ providedIn: 'root' })
export class IntelligenceService {
  private readonly API = '/api/v1/intelligence';

  pipelineProgress = signal<PipelineProgress[]>([]);
  pipelineStatus = signal<string>('idle');

  constructor(private http: HttpClient) {}

  /**
   * Traite un document via le pipeline complet.
   * Upload → OCR → Extraction → Classification → Knowledge Graph → Indexation
   */
  processDocument(file: File): Observable<PipelineResult> {
    const fd = new FormData();
    fd.append('file', file);

    this.pipelineStatus.set('processing');
    this.pipelineProgress.set([]);

    return this.http.post<PipelineResult>(`${this.API}/process`, fd).pipe(
      tap(result => {
        this.pipelineStatus.set(result.status);
        if (result.steps) {
          this.pipelineProgress.set(result.steps.map(s => ({
            documentId: result.documentId,
            step: s[0],
            message: s[1]
          })));
        }
      })
    );
  }

  /**
   * Requête en langage naturel.
   */
  smartQuery(query: string): Observable<SmartQueryResult> {
    return this.http.post<SmartQueryResult>(`${this.API}/smart-query`, { query });
  }

  /**
   * Recherche dans le Knowledge Graph.
   */
  searchKnowledge(query: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API}/knowledge`, { params: { q: query } });
  }

  /**
   * Récupère les nœuds liés.
   */
  getRelatedNodes(nodeId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API}/knowledge/${nodeId}/related`);
  }

  /**
   * Enregistre un feedback (acceptation/refus).
   */
  recordFeedback(data: {
    recommendationId: string;
    candidateId: string;
    programId: string;
    action: 'ACCEPTED' | 'REJECTED' | 'VIEWED';
    reason?: string;
  }): Observable<any> {
    return this.http.post(`${this.API}/feedback`, data);
  }

  /**
   * Récupère l'historique d'apprentissage.
   */
  getLearningHistory(candidateId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.API}/history/${candidateId}`);
  }
}
