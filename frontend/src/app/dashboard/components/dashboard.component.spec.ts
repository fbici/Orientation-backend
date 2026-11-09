import {of,throwError,EMPTY} from 'rxjs';
import {DashboardComponent} from './dashboard.component';
describe('Dashboard response contract',()=>{
 const make=(api:any)=>new DashboardComponent(api,{subscribe:()=>EMPTY} as any,{set:()=>{}} as any);
 it('maps dated chart data and applies the selected period',()=>{const chart:any={};for(let d=1;d<=12;d++)chart['2026-11-'+String(d).padStart(2,'0')]=d;const page=make({getDashboard:()=>of({kpis:{totalCandidates:3},charts:{recommendationsByDay:chart,importsByDay:{},documentsByType:{PDF:2}}})});page.loadData();page.updatePeriod('7j');expect(page.chartLabels.length).toBe(7);expect(page.lineDataReco).toEqual([6,7,8,9,10,11,12]);expect(page.doughnutValues).toEqual([2]);expect(page.kpis[0].value).toBe(3);});
 it('preserves missing measurements instead of generating fake counts',()=>{const page=make({getDashboard:()=>of({kpis:{totalCandidates:0},systemHealth:{memoryUsage:0},charts:{}})});page.loadData();expect(page.kpis[0].value).toBe(0);expect(page.kpis[1].value).toBeNull();expect(page.health.some(x=>x.val==='Non mesure')).toBeTrue();expect(page.lineDataReco).toEqual([]);});
 it('exposes a dashboard API failure',()=>{const page=make({getDashboard:()=>throwError(()=>({status:503}))});page.loadData();expect(page.error()).toBeTruthy();expect(page.loading()).toBeFalse();});
});
