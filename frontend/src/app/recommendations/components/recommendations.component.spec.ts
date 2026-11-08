import { of, throwError } from 'rxjs';
import { RecommendationsComponent } from './recommendations.component';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../shared/components/toast.service';

describe('Recommendation diagnostics', () => {
  const id = '11111111-1111-4111-8111-111111111111';
  let api: jasmine.SpyObj<ApiService>;
  let component: RecommendationsComponent;
  beforeEach(() => {
    api = jasmine.createSpyObj('ApiService', ['getRecommendations', 'generateRecommendations']);
    const toast = jasmine.createSpyObj('ToastService', ['success']);
    component = new RecommendationsComponent(api, toast as ToastService);
  });
  it('distinguishes refusal and insufficient data from eligible', () => {
    expect(component.eligClass('ELIGIBLE')).toBe('badge-success');
    expect(component.eligClass('NOT_ELIGIBLE')).toBe('badge-danger');
    expect(component.eligClass('INSUFFICIENT_DATA')).toBe('badge-warning');
  });
  it('generates using only the persistent candidate identity', () => {
    component.candidateId = id;
    api.generateRecommendations.and.returnValue(of({}));
    api.getRecommendations.and.returnValue(of({content: [], totalElements: 0} as any));
    component.generate();
    expect(api.generateRecommendations).toHaveBeenCalledWith({candidateId: id});
    expect(api.getRecommendations).toHaveBeenCalledWith(0, 20, id);
  });
  it('refuses an invalid candidate identity before calling the API', () => {
    component.candidateId = 'not-a-uuid';
    component.generate();
    expect(api.generateRecommendations).not.toHaveBeenCalled();
    expect(component.error()).toBeTruthy();
  });
  it('clears results after access refusal', () => {
    component.candidateId = id;
    api.getRecommendations.and.returnValue(throwError(() => ({status: 403})));
    component.load();
    expect(component.recommendations()).toEqual([]);
    expect(component.error()).toBeTruthy();
    expect(component.loading()).toBeFalse();
  });
});
