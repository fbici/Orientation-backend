import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly b = '/api/v1';
  constructor(private h: HttpClient) {}
  getDashboard(): Observable<any> { return this.h.get(`${this.b}/backoffice/dashboard`); }
  getUniversities(p=0,s=20): Observable<any> { return this.h.get(`${this.b}/universities`,{params:{page:p,size:s}}); }
  getPrograms(p=0,s=20): Observable<any> { return this.h.get(`${this.b}/programs`,{params:{page:p,size:s}}); }
  getCandidates(p=0,s=20): Observable<any> { return this.h.get(`${this.b}/candidates`,{params:{page:p,size:s}}); }
  generateRecommendations(d:any): Observable<any> { return this.h.post(`${this.b}/recommendations/generate`,d); }
  simulate(d:any): Observable<any> { return this.h.post(`${this.b}/recommendations/simulate`,d); }
  getRecommendations(): Observable<any> { return this.h.get(`${this.b}/recommendations`); }
  getImports(): Observable<any> { return this.h.get(`${this.b}/imports`); }
  uploadImport(f:File): Observable<any> { const fd=new FormData();fd.append('file',f);return this.h.post(`${this.b}/imports`,fd); }
  getUsers(p=0,s=20): Observable<any> { return this.h.get(`${this.b}/admin/users`,{params:{page:p,size:s}}); }
  getRoles(): Observable<any> { return this.h.get(`${this.b}/admin/roles`); }
  getMonitoring(): Observable<any> { return this.h.get(`${this.b}/backoffice/monitoring`); }
  getAnalytics(): Observable<any> { return this.h.get(`${this.b}/backoffice/analytics`); }
  getReports(): Observable<any> { return this.h.get(`${this.b}/backoffice/report`); }
}
