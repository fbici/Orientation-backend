import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly b = environment.apiUrl;

  importAcademic(file: File, dataType: string, dryRun: boolean): Observable<any> {
    const data = new FormData(); data.append('file', file); data.append('dataType', dataType); data.append('dryRun', String(dryRun));
    return this.http.post(`${this.b}/imports`, data);
  }
  academicSources(resource: string, id: string): Observable<any> {return this.http.get(`${this.b}/${resource}/${id}/sources`);}
  catalog(resource: string, page = 0, filters: Record<string, any> = {}): Observable<PageResponse<any>> {
    const params: Record<string, string> = {page: String(page), size: '20'};
    Object.entries(filters).forEach(([k,v]) => {if(v !== undefined && v !== null && v !== '') params[k] = String(v);});
    return this.http.get<PageResponse<any>>(`${this.b}/${resource}`, {params});
  }
  saveCatalog(resource: string, id: string | undefined, values: any): Observable<any> {
    return id ? this.http.put(`${this.b}/${resource}/${id}`, values) : this.http.post(`${this.b}/${resource}`, values);
  }
  disableCatalog(resource: string, id: string): Observable<any> {return this.http.delete(`${this.b}/${resource}/${id}`);}
  reference(resource: string, search = ''): Observable<any> {return this.http.get(`${this.b}/${resource}`, {params:{size:'100',search}});}
  reviewDocument(id: string): Observable<any> {return this.http.get(`${this.b}/documents/${id}/review`);}
  processDocument(id: string): Observable<any> {return this.http.post(`${this.b}/documents/${id}/process`, {});}
  documentPage(id: string, page: number): Observable<Blob> {return this.http.get(`${this.b}/documents/${id}/pages/${page}/preview`, {responseType:'blob'});}
  reviewFact(id: string, fact: string, values: any): Observable<any> {return this.http.put(`${this.b}/documents/${id}/extractions/${fact}`, values);}
  addSourceFact(id: string, values: any): Observable<any> {return this.http.post(`${this.b}/documents/${id}/extractions`, values);}
  publishDocument(id: string, values: any): Observable<any> {return this.http.post(`${this.b}/documents/${id}/publish`, values);}
  guideVersions(id: string): Observable<any> {return this.http.get(`${this.b}/guides/${id}/versions`);}
  publishedRules(page = 0, programId = ''): Observable<PageResponse<any>> {return this.catalog('admission-criteria', page, {programId});}
  knowledgeSearch(query: string, filters: Record<string, string> = {}): Observable<any> {return this.http.get(`${this.b}/knowledge/search`, {params:{query,limit:'20',...filters}});}
  knowledgeGraph(): Observable<any> {return this.http.get(`${this.b}/knowledge/graph`);}


  simulateDecision(data: unknown): Observable<any> {return this.http.post(this.b+'/recommendations/simulate',data);}
  compareDecision(data: unknown): Observable<any> {return this.http.post(this.b+'/ai/compare/programs',data);}
  scholarshipDecision(data: unknown): Observable<any> {return this.http.post(this.b+'/scholarships/evaluate',data);}
  decisionHistory(candidateId:string,kind:string,page:number):Observable<any>{return this.http.get(this.b+'/ai/decisions',{params:{candidateId,kind,page:String(page),size:'20'}});}
  createDecisionChat(candidateId:string):Observable<any>{return this.http.post(this.b+'/ai/chat/sessions',{}, {params:{candidateId}});}
  askDecisionChat(session:string,data:unknown):Observable<any>{return this.http.post(this.b+'/ai/chat/sessions/'+session+'/messages',data);}
  exportDecision(id:string,format:'pdf'|'csv'|'excel'):Observable<Blob>{return this.http.get(this.b+'/ai/export/'+format+'/'+id,{responseType:'blob'});}
  constructor(private http: HttpClient) {}

  // ── Auth ──
  login(data: any): Observable<any> { return this.http.post(`${this.b}/auth/login`, data); }
  refreshToken(data: any): Observable<any> { return this.http.post(`${this.b}/auth/refresh`, data); }
  logout(data: any): Observable<any> { return this.http.post(`${this.b}/auth/logout`, data); }
  forgotPassword(data: any): Observable<any> { return this.http.post(`${this.b}/auth/forgot-password`, data); }
  resetPassword(data: any): Observable<any> { return this.http.post(`${this.b}/auth/reset-password`, data); }
  getMe(): Observable<any> { return this.http.get(`${this.b}/auth/me`); }
  updateMe(data: any): Observable<any> { return this.http.put(`${this.b}/auth/me`, data); }
  changePassword(data: any): Observable<any> { return this.http.put(`${this.b}/auth/me/password`, data); }

  // ── Dashboard ──
  getDashboard(): Observable<any> { return this.http.get(`${this.b}/backoffice/dashboard`); }
  getDashboardByTenant(tenantId: string): Observable<any> { return this.http.get(`${this.b}/backoffice/dashboard/tenant/${tenantId}`); }

  // ── Analytics ──
  getAnalytics(): Observable<any> { return this.http.get(`${this.b}/backoffice/analytics`); }

  // ── Monitoring ──
  getMonitoring(): Observable<any> { return this.http.get(`${this.b}/backoffice/monitoring`); }

  // ── Reports ──
  getReports(): Observable<any> { return throwError(()=>new Error('La liste des rapports n est pas exposee ; utiliser l historique des decisions.')); }
  generateReport(data: {type:string;format?:string}): Observable<any> { return this.http.get(`${this.b}/backoffice/reports/generate`, {params:{type:data.type,format:data.format || 'json'}}); }

  // ── Notifications ──
  getNotifications(page = 0, size = 50): Observable<any> {
    return this.http.get(`${this.b}/backoffice/notifications`, { params: this.p(page, size) });
  }
  markNotificationRead(id: string): Observable<any> { return this.http.post(`${this.b}/backoffice/notifications/${id}/read`, {}); }
  markAllNotificationsRead(): Observable<any> { return this.http.post(`${this.b}/backoffice/notifications/read-all`, {}); }
  deleteNotification(id: string): Observable<any> { return this.http.delete(`${this.b}/backoffice/notifications/${id}`); }

  // ── Universities ──
  getUniversities(page = 0, size = 20, sort?: string): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/universities`, { params: this.p(page, size, sort) });
  }
  getUniversity(id: string): Observable<any> { return this.http.get(`${this.b}/universities/${id}`); }
  createUniversity(data: any): Observable<any> { return this.http.post(`${this.b}/universities`, data); }
  updateUniversity(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/universities/${id}`, data); }
  deleteUniversity(id: string): Observable<any> { return this.http.delete(`${this.b}/universities/${id}`); }

  // ── Campuses ──
  getCampuses(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/campuses`, { params: this.p(page, size) });
  }
  getCampus(id: string): Observable<any> { return this.http.get(`${this.b}/campuses/${id}`); }
  createCampus(data: any): Observable<any> { return this.http.post(`${this.b}/campuses`, data); }
  updateCampus(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/campuses/${id}`, data); }
  deleteCampus(id: string): Observable<any> { return this.http.delete(`${this.b}/campuses/${id}`); }

  // ── Faculties ──
  getFaculties(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/faculties`, { params: this.p(page, size) });
  }
  getFaculty(id: string): Observable<any> { return this.http.get(`${this.b}/faculties/${id}`); }
  createFaculty(data: any): Observable<any> { return this.http.post(`${this.b}/faculties`, data); }
  updateFaculty(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/faculties/${id}`, data); }
  deleteFaculty(id: string): Observable<any> { return this.http.delete(`${this.b}/faculties/${id}`); }

  // ── Programs ──
  getPrograms(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/programs`, { params: this.p(page, size) });
  }
  getProgram(id: string): Observable<any> { return this.http.get(`${this.b}/programs/${id}`); }
  createProgram(data: any): Observable<any> { return this.http.post(`${this.b}/programs`, data); }
  updateProgram(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/programs/${id}`, data); }
  deleteProgram(id: string): Observable<any> { return this.http.delete(`${this.b}/programs/${id}`); }

  // ── Candidates ──
  getCandidates(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/candidates`, { params: this.p(page, size) });
  }
  getCandidate(id: string): Observable<any> { return this.http.get(`${this.b}/candidates/${id}`); }

  // ── Recommendations ──
  getRecommendations(page = 0, size = 20, candidateId?: string): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/recommendations`, { params: candidateId ? { ...this.p(page, size), candidateId } : this.p(page, size) });
  }
  generateRecommendations(data: any): Observable<any> { return this.http.post(`${this.b}/recommendations/generate`, data); }
  simulate(data: any): Observable<any> { return this.http.post(`${this.b}/recommendations/simulate`, data); }
  getRecommendationScores(id: string): Observable<any> { return this.http.get(`${this.b}/recommendations/${id}/scores`); }
  getRecommendationExplanation(id: string): Observable<any> { return this.http.get(`${this.b}/recommendations/${id}/explanation`); }

  // ── Transcripts ──
  getTranscripts(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/transcripts`, { params: this.p(page, size) });
  }

  // ── Scholarships ──
  getScholarships(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/scholarships`, { params: this.p(page, size) });
  }
  getScholarship(id: string): Observable<any> { return this.http.get(`${this.b}/scholarships/${id}`); }

  // ── Orientation Guides ──
  getGuides(page = 0, size = 20): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/guides`, { params: this.p(page, size) });
  }
  getGuide(id: string): Observable<any> { return this.http.get(`${this.b}/guides/${id}`); }
  createGuide(data: any): Observable<any> { return this.http.post(`${this.b}/guides`, data); }
  updateGuide(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/guides/${id}`, data); }
  deleteGuide(id: string): Observable<any> { return this.http.delete(`${this.b}/guides/${id}`); }

  // ── Academic Years ──
  getAcademicYears(): Observable<any> { return this.http.get(`${this.b}/academic-years`); }

  // ── Subjects ──
  getSubjects(): Observable<any> { return this.http.get(`${this.b}/subjects`); }

  // ── Grade Scales ──
  getGradeScales(): Observable<any> { return this.http.get(`${this.b}/grade-scales`); }

  // ── Countries / Cities ──
  getCountries(): Observable<any> { return this.http.get(`${this.b}/locations/countries`); }
  getCities(): Observable<any> { return this.http.get(`${this.b}/locations/cities`); }
  getCitiesByCountry(countryId: string): Observable<any> { return this.http.get(`${this.b}/locations/countries/${countryId}/cities`); }

  // ── Imports ──
  getImports(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.b}/imports`, { params: this.p(page, size) });
  }
  uploadImport(file: File, type?: string): Observable<any> {
    const fd = new FormData();
    fd.append('file', file);
    if (type) fd.append('dataType', type);
    return this.http.post(`${this.b}/imports`, fd);
  }
  rollbackImport(id: string): Observable<any> { return this.http.post(`${this.b}/imports/${id}/rollback`, {}); }
  getImportDetails(id: string): Observable<any> { return this.http.get(`${this.b}/imports/${id}`); }

  // ── Documents ──
  getDocuments(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.b}/documents`, { params: this.p(page, size) });
  }
  getDocument(id: string): Observable<any> { return this.http.get(`${this.b}/documents/${id}`); }
  uploadDocument(file: File): Observable<any> {
    const fd = new FormData();
    fd.append('file', file);
    return this.http.post(`${this.b}/documents/upload`, fd);
  }
  deleteDocument(id: string): Observable<any> { return this.http.delete(`${this.b}/documents/${id}`); }
  getDocumentExtractions(id: string): Observable<any> { return this.http.get(`${this.b}/documents/${id}/extractions`); }
  getDocumentClassifications(id: string): Observable<any> { return this.http.get(`${this.b}/documents/${id}/classifications`); }

  // ── Admin: Users ──
  getUsers(page = 0, size = 20, search = ''): Observable<PageResponse<any>> {
    return this.http.get<PageResponse<any>>(`${this.b}/admin/users`, { params: { ...this.p(page, size), search } });
  }
  getUser(id: string): Observable<any> { return this.http.get(`${this.b}/admin/users/${id}`); }
  createUser(data: any): Observable<any> { return this.http.post(`${this.b}/admin/users`, data); }
  updateUser(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/admin/users/${id}`, data); }
  deleteUser(id: string): Observable<any> { return this.http.delete(`${this.b}/admin/users/${id}`); }

  // ── Admin: Roles ──
  getRoles(): Observable<any> { return this.http.get(`${this.b}/admin/roles`); }
  getRole(id: string): Observable<any> { return this.http.get(`${this.b}/admin/roles/${id}`); }
  createRole(data: any): Observable<any> { return this.http.post(`${this.b}/admin/roles`, data); }
  updateRole(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/admin/roles/${id}`, data); }
  deleteRole(id: string): Observable<any> { return this.http.delete(`${this.b}/admin/roles/${id}`); }

  // ── Admin: Organizations ──
  getOrganizations(page = 0, size = 20): Observable<any> { return this.http.get(`${this.b}/admin/organizations`, {params: this.p(page, size)}); }
  getOrganization(id: string): Observable<any> { return this.http.get(`${this.b}/admin/organizations/${id}`); }
  createOrganization(data: any): Observable<any> { return this.http.post(`${this.b}/admin/organizations`, data); }
  updateOrganization(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/admin/organizations/${id}`, data); }
  deleteOrganization(id: string): Observable<any> { return this.http.delete(`${this.b}/admin/organizations/${id}`); }

  // ── Admin: Tenants ──
  getTenants(page = 0, size = 20): Observable<any> { return this.http.get(`${this.b}/admin/tenants`, {params: this.p(page, size)}); }
  getTenant(id: string): Observable<any> { return this.http.get(`${this.b}/admin/tenants/${id}`); }
  createTenant(data: any): Observable<any> { return this.http.post(`${this.b}/admin/tenants`, data); }
  updateTenant(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/admin/tenants/${id}`, data); }
  deleteTenant(id: string): Observable<any> { return this.http.delete(`${this.b}/admin/tenants/${id}`); }

  // ── Admin: Departments ──
  getDepartments(): Observable<any> { return this.http.get(`${this.b}/admin/departments`); }
  createDepartment(data: any): Observable<any> { return this.http.post(`${this.b}/admin/departments`, data); }
  updateDepartment(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/admin/departments/${id}`, data); }
  deleteDepartment(id: string): Observable<any> { return this.http.delete(`${this.b}/admin/departments/${id}`); }

  // ── Admin: Teams ──
  getTeams(): Observable<any> { return this.http.get(`${this.b}/admin/teams`); }
  createTeam(data: any): Observable<any> { return this.http.post(`${this.b}/admin/teams`, data); }
  updateTeam(id: string, data: any): Observable<any> { return this.http.put(`${this.b}/admin/teams/${id}`, data); }
  deleteTeam(id: string): Observable<any> { return this.http.delete(`${this.b}/admin/teams/${id}`); }

  // ── Admin: Invitations ──
  getInvitations(): Observable<any> { return this.http.get(`${this.b}/admin/invitations`); }
  sendInvitation(data: any): Observable<any> { return this.http.post(`${this.b}/admin/invitations`, data); }
  cancelInvitation(id: string): Observable<any> { return this.http.delete(`${this.b}/admin/invitations/${id}`); }

  // ── Knowledge Engine ──
  getKnowledgeNodes(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.b}/knowledge/nodes`, { params: this.p(page, size) });
  }
  getKnowledgeNode(id: string): Observable<any> { return this.http.get(`${this.b}/knowledge/nodes/${id}`); }
  searchKnowledge(query: string): Observable<any> { return this.http.get(`${this.b}/knowledge/search`, { params: { query } }); }
  smartQuery(query: string): Observable<any> { return this.http.post(`${this.b}/knowledge/query`, { query }); }

  // ── Audit ──
  getAuditLogs(page = 0, size = 50): Observable<any> {
    return this.http.get(`${this.b}/backoffice/audit`, { params: this.p(page, size) });
  }

  // ── Settings ──
  getSettings(): Observable<any> { return this.http.get(`${this.b}/settings`); }
  updateSettings(data: any): Observable<any> { return this.http.put(`${this.b}/settings`, data); }

  // ── Backoffice: Activity Logs ──
  getActivityLogs(page = 0, size = 50): Observable<any> {
    return this.http.get(`${this.b}/backoffice/audit`, { params: this.p(page, size) });
  }

  // Helper: build params
  private p(page: number, size: number, sort?: string): any {
    const params: any = { page: String(page), size: String(size) };
    if (sort) params.sort = sort;
    return params;
  }
}
