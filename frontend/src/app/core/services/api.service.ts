import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly b = '/api/v1';

  constructor(private http: HttpClient) {}

  // ── Dashboard / Backoffice ──
  getDashboard(): Observable<any> {
    return this.http.get(`${this.b}/backoffice/dashboard`);
  }

  getMonitoring(): Observable<any> {
    return this.http.get(`${this.b}/backoffice/monitoring`);
  }

  getAnalytics(): Observable<any> {
    return this.http.get(`${this.b}/backoffice/analytics`);
  }

  // ── Universities ──
  getUniversities(page = 0, size = 20, sort?: string): Observable<PageResponse<any>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (sort) params = params.set('sort', sort);
    return this.http.get<PageResponse<any>>(`${this.b}/universities`, { params });
  }

  getUniversity(id: string): Observable<any> {
    return this.http.get(`${this.b}/universities/${id}`);
  }

  createUniversity(data: any): Observable<any> {
    return this.http.post(`${this.b}/universities`, data);
  }

  updateUniversity(id: string, data: any): Observable<any> {
    return this.http.put(`${this.b}/universities/${id}`, data);
  }

  deleteUniversity(id: string): Observable<any> {
    return this.http.delete(`${this.b}/universities/${id}`);
  }

  // ── Programs ──
  getPrograms(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/programs`, { params: { page, size } as any });
  }

  getProgram(id: string): Observable<any> {
    return this.http.get(`${this.b}/programs/${id}`);
  }

  // ── Faculties ──
  getFaculties(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/faculties`, { params: { page, size } as any });
  }

  // ── Candidates ──
  getCandidates(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/candidates`, { params: { page, size } as any });
  }

  getCandidate(id: string): Observable<any> {
    return this.http.get(`${this.b}/candidates/${id}`);
  }

  // ── Recommendations ──
  generateRecommendations(data: any): Observable<any> {
    return this.http.post(`${this.b}/recommendations/generate`, data);
  }

  simulate(data: any): Observable<any> {
    return this.http.post(`${this.b}/recommendations/simulate`, data);
  }

  getRecommendations(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.b}/recommendations`, { params: { page, size } as any });
  }

  getRecommendationScores(id: string): Observable<any> {
    return this.http.get(`${this.b}/recommendations/${id}/scores`);
  }

  getRecommendationExplanation(id: string): Observable<any> {
    return this.http.get(`${this.b}/recommendations/${id}/explanation`);
  }

  // ── Transcripts ──
  getTranscripts(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/transcripts`, { params: { page, size } as any });
  }

  // ── Imports ──
  getImports(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.b}/imports`, { params: { page, size } as any });
  }

  uploadImport(file: File, type?: string): Observable<any> {
    const fd = new FormData();
    fd.append('file', file);
    if (type) fd.append('type', type);
    return this.http.post(`${this.b}/imports`, fd);
  }

  // ── Documents ──
  getDocuments(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.b}/documents`, { params: { page, size } as any });
  }

  uploadDocument(file: File): Observable<any> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post(`${this.b}/documents`, fd);
  }

  // ── Admin: Users ──
  getUsers(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/admin/users`, { params: { page, size } as any });
  }

  getUser(id: string): Observable<any> {
    return this.http.get(`${this.b}/admin/users/${id}`);
  }

  createUser(data: any): Observable<any> {
    return this.http.post(`${this.b}/admin/users`, data);
  }

  updateUser(id: string, data: any): Observable<any> {
    return this.http.put(`${this.b}/admin/users/${id}`, data);
  }

  deleteUser(id: string): Observable<any> {
    return this.http.delete(`${this.b}/admin/users/${id}`);
  }

  // ── Admin: Roles ──
  getRoles(): Observable<any> {
    return this.http.get(`${this.b}/admin/roles`);
  }

  // ── Admin: Organizations ──
  getOrganizations(): Observable<any> {
    return this.http.get(`${this.b}/admin/organizations`);
  }

  // ── Admin: Tenants ──
  getTenants(): Observable<any> {
    return this.http.get(`${this.b}/admin/tenants`);
  }

  // ── Reports ──
  getReports(): Observable<any> {
    return this.http.get(`${this.b}/backoffice/report`);
  }

  generateReport(type: string): Observable<any> {
    return this.http.post(`${this.b}/backoffice/report/generate`, { type });
  }

  // ── Settings ──
  getSettings(): Observable<any> {
    return this.http.get(`${this.b}/settings`);
  }

  updateSettings(data: any): Observable<any> {
    return this.http.put(`${this.b}/settings`, data);
  }

  // ── Notifications ──
  getNotifications(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.b}/notifications`, { params: { page, size } as any });
  }

  markNotificationRead(id: string): Observable<any> {
    return this.http.put(`${this.b}/notifications/${id}/read`, {});
  }

  markAllNotificationsRead(): Observable<any> {
    return this.http.put(`${this.b}/notifications/read-all`, {});
  }

  // ── Orientation Guides ──
  getGuides(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.b}/guides`, { params: { page, size } as any });
  }

  // ── Scholarships ──
  getScholarships(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.b}/scholarships`, { params: { page, size } as any });
  }

  // ── Academic Years ──
  getAcademicYears(): Observable<any> {
    return this.http.get(`${this.b}/academic-years`);
  }

  // ── Subjects ──
  getSubjects(): Observable<any> {
    return this.http.get(`${this.b}/subjects`);
  }

  // ── Grade Scales ──
  getGradeScales(): Observable<any> {
    return this.http.get(`${this.b}/grade-scales`);
  }

  // ── Countries / Cities ──
  getCountries(): Observable<any> {
    return this.http.get(`${this.b}/countries`);
  }

  getCities(): Observable<any> {
    return this.http.get(`${this.b}/cities`);
  }
}
