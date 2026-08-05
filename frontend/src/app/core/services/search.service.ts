import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SearchResult {
  id: string;
  type: string;
  title: string;
  description: string;
  relevance: number;
}

export interface SearchFilters {
  type?: string;
  dateFrom?: string;
  dateTo?: string;
}

@Injectable({ providedIn: 'root' })
export class SearchService {

  private readonly API_URL = environment.apiUrl;
  private searchSubject = new Subject<string>();
  searchResults = signal<SearchResult[]>([]);
  isSearching = signal(false);

  constructor(private http: HttpClient) {}

  search(query: string, filters?: SearchFilters): Observable<SearchResult[]> {
    if (!query || query.length < 2) {
      return of([]);
    }

    let params: any = { q: query };
    if (filters?.type) params.type = filters.type;
    if (filters?.dateFrom) params.dateFrom = filters.dateFrom;
    if (filters?.dateTo) params.dateTo = filters.dateTo;

    return this.http.get<SearchResult[]>(`${this.API_URL}/search`, { params });
  }

  searchInstant(query: string): Observable<SearchResult[]> {
    return this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => this.search(q))
    );
  }

  triggerSearch(query: string): void {
    this.searchSubject.next(query);
  }
}
