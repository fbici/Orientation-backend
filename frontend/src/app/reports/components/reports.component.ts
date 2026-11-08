import {Component} from '@angular/core';
import {DecisionsComponent} from '../../intelligence/decisions.component';
@Component({selector:'app-reports',standalone:true,imports:[DecisionsComponent],template:'<app-decisions />'})
export class ReportsComponent {}
