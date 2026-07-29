import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly BASE_URL = '/api/v1';

  constructor(private http: HttpClient) {}

  // Dashboard
  getDashboard(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/backoffice/dashboard`);
  }

  getAnalytics(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/backoffice/analytics`);
  }

  getMonitoring(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/backoffice/monitoring`);
  }

  // Administration
  getOrganizations(page: number = 0, size: number = 20): Observable<any> {
    return this.http.get(`${this.BASE_URL}/admin/organizations`, { params: { page, size } });
  }

  createOrganization(data: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/admin/organizations`, data);
  }

  updateOrganization(id: string, data: any): Observable<any> {
    return this.http.put(`${this.BASE_URL}/admin/organizations/${id}`, data);
  }

  deleteOrganization(id: string): Observable<any> {
    return this.http.delete(`${this.BASE_URL}/admin/organizations/${id}`);
  }

  getTenants(page: number = 0, size: number = 20): Observable<any> {
    return this.http.get(`${this.BASE_URL}/admin/tenants`, { params: { page, size } });
  }

  createTenant(data: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/admin/tenants`, data);
  }

  getUsers(tenantId?: string, page: number = 0, size: number = 20): Observable<any> {
    let params: any = { page, size };
    if (tenantId) params.tenantId = tenantId;
    return this.http.get(`${this.BASE_URL}/admin/users`, { params });
  }

  createUser(data: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/admin/users`, data);
  }

  // Recommendations
  generateRecommendations(data: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/recommendations/generate`, data);
  }

  simulateRecommendations(data: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/recommendations/simulate`, data);
  }

  getRecommendations(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/recommendations`);
  }

  // Documents
  uploadDocument(file: File, title?: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    if (title) formData.append('title', title);
    return this.http.post(`${this.BASE_URL}/documents/upload`, formData);
  }

  getDocuments(page: number = 0, size: number = 20): Observable<any> {
    return this.http.get(`${this.BASE_URL}/documents`, { params: { page, size } });
  }

  searchDocuments(query: string): Observable<any> {
    return this.http.get(`${this.BASE_URL}/documents/search`, { params: { q: query } });
  }

  // Imports
  importFile(file: File, dataType: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('dataType', dataType);
    return this.http.post(`${this.BASE_URL}/imports`, formData);
  }

  getImportStatus(id: string): Observable<any> {
    return this.http.get(`${this.BASE_URL}/imports/${id}`);
  }

  // Notifications
  getNotifications(userId: string, page: number = 0): Observable<any> {
    return this.http.get(`${this.BASE_URL}/backoffice/notifications`, { params: { userId, page } });
  }

  getUnreadCount(userId: string): Observable<any> {
    return this.http.get(`${this.BASE_URL}/backoffice/notifications/unread-count`, { params: { userId } });
  }

  markNotificationRead(id: string): Observable<any> {
    return this.http.post(`${this.BASE_URL}/backoffice/notifications/${id}/read`, {});
  }

  // Audit
  getAuditHistory(page: number = 0, size: number = 20): Observable<any> {
    return this.http.get(`${this.BASE_URL}/backoffice/audit`, { params: { page, size } });
  }
}
