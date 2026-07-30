import { Component, Input, Output, EventEmitter, TemplateRef, ContentChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface TableColumn {
  key: string;
  label: string;
  width?: string;
  align?: 'left' | 'center' | 'right';
  sortable?: boolean;
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="table-wrap">
      @if (searchable || title) {
        <div class="table-toolbar">
          @if (title) { <h3 style="font-size:.9375rem;font-weight:600;color:var(--n-900)">{{ title }}</h3> }
          <div style="display:flex;gap:8px;align-items:center">
            @if (searchable) {
              <div style="position:relative">
                <span class="material-symbols-rounded" style="position:absolute;left:10px;top:50%;transform:translateY(-50%);font-size:18px;color:var(--n-400)">search</span>
                <input type="text" class="form-input" style="padding-left:36px;width:220px" placeholder="Rechercher…" [(ngModel)]="searchQuery" (ngModelChange)="onSearch()">
              </div>
            }
            <ng-content select="[toolbar]"></ng-content>
          </div>
        </div>
      }
      <div class="table-scroll">
        <table class="data-table">
          <thead>
            <tr>
              @for (col of columns; track col.key) {
                <th [style.width]="col.width" [style.text-align]="col.align || 'left'" (click)="col.sortable && onSort(col.key)" [class.sortable]="col.sortable">
                  <div style="display:flex;align-items:center;gap:4px">
                    {{ col.label }}
                    @if (col.sortable) {
                      <span class="material-symbols-rounded sort-icon" [class.active]="sortKey === col.key" [class.desc]="sortDir === 'desc'" style="font-size:14px">
                        {{ sortKey === col.key ? (sortDir === 'asc' ? 'arrow_upward' : 'arrow_downward') : 'unfold_more' }}
                      </span>
                    }
                  </div>
                </th>
              }
              @if (actions) { <th style="width:100px;text-align:right">Actions</th> }
            </tr>
          </thead>
          <tbody>
            @if (loading) {
              @for (i of skeletonRows; track i) {
                <tr>@for (col of columns; track col.key) { <td><div class="skeleton" style="height:14px;width:80%"></div></td> } @if (actions) { <td><div class="skeleton" style="height:14px;width:60px;float:right"></div></td> }</tr>
              }
            } @else if (data.length === 0) {
              <tr><td [attr.colspan]="columns.length + (actions ? 1 : 0)" style="text-align:center;padding:48px;color:var(--n-400)">
                <span class="material-symbols-rounded" style="font-size:48px;display:block;margin-bottom:12px;color:var(--n-300)">{{ emptyIcon }}</span>
                <p style="font-weight:600;color:var(--n-600);margin-bottom:4px">{{ emptyTitle }}</p>
                <p style="font-size:.8125rem">{{ emptyMessage }}</p>
              </td></tr>
            } @else {
              @for (row of filteredData; track trackBy(row)) {
                <ng-template [ngTemplateOutlet]="rowTemplate" [ngTemplateOutletContext]="{ $implicit: row }"></ng-template>
              }
            }
          </tbody>
        </table>
      </div>
      @if (paginated && totalElements > pageSize) {
        <div class="table-pagination">
          <span style="font-size:.8125rem;color:var(--n-500)">{{ totalElements }} résultats</span>
          <div style="display:flex;gap:4px">
            <button class="btn btn-ghost btn-sm btn-icon" [disabled]="currentPage === 0" (click)="goToPage(currentPage - 1)"><span class="material-symbols-rounded" style="font-size:18px">chevron_left</span></button>
            @for (p of pages; track p) {
              <button class="btn btn-sm" [class.btn-primary]="p === currentPage" [class.btn-ghost]="p !== currentPage" (click)="goToPage(p)">{{ p + 1 }}</button>
            }
            <button class="btn btn-ghost btn-sm btn-icon" [disabled]="currentPage >= totalPages - 1" (click)="goToPage(currentPage + 1)"><span class="material-symbols-rounded" style="font-size:18px">chevron_right</span></button>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .table-wrap{background:#fff;border-radius:var(--radius-lg);border:1px solid var(--n-200);overflow:hidden}
    .table-toolbar{display:flex;align-items:center;justify-content:space-between;padding:16px 20px;border-bottom:1px solid var(--n-100)}
    .table-scroll{overflow-x:auto}
    .sortable{cursor:pointer;user-select:none}.sortable:hover{color:var(--n-700)}
    .sort-icon{opacity:.3;transition:all .15s}.sort-icon.active{opacity:1;color:var(--brand)}.sort-icon.desc{transform:rotate(180deg)}
    .table-pagination{display:flex;align-items:center;justify-content:space-between;padding:12px 20px;border-top:1px solid var(--n-100)}
    .skeleton{background:linear-gradient(90deg,var(--n-200) 25%,var(--n-100) 50%,var(--n-200) 75%);background-size:200% 100%;animation:shimmer 1.5s infinite;border-radius:var(--radius-sm)}
    @keyframes shimmer{0%{background-position:-200% 0}100%{background-position:200% 0}}
  `]
})
export class DataTableComponent {
  @Input() columns: TableColumn[] = [];
  @Input() data: any[] = [];
  @Input() loading = false;
  @Input() searchable = true;
  @Input() paginated = false;
  @Input() totalElements = 0;
  @Input() totalPages = 0;
  @Input() currentPage = 0;
  @Input() pageSize = 20;
  @Input() actions = false;
  @Input() title = '';
  @Input() emptyIcon = 'inbox';
  @Input() emptyTitle = 'Aucune donnée';
  @Input() emptyMessage = 'Les données apparaîtront ici une fois chargées.';
  @Input() trackByKey = 'id';
  @Input() rowTemplate!: TemplateRef<any>;
  @Output() pageChange = new EventEmitter<number>();
  @Output() searchChange = new EventEmitter<string>();
  @Output() sortChange = new EventEmitter<{ key: string; dir: string }>();

  searchQuery = '';
  sortKey = '';
  sortDir: 'asc' | 'desc' = 'asc';
  skeletonRows = [1, 2, 3, 4, 5];

  get filteredData(): any[] {
    let result = [...this.data];
    if (this.searchQuery) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(row =>
        this.columns.some(col => String(row[col.key] || '').toLowerCase().includes(q))
      );
    }
    if (this.sortKey) {
      result.sort((a, b) => {
        const va = a[this.sortKey] || '';
        const vb = b[this.sortKey] || '';
        const cmp = String(va).localeCompare(String(vb));
        return this.sortDir === 'asc' ? cmp : -cmp;
      });
    }
    return result;
  }

  get pages(): number[] {
    const p: number[] = [];
    for (let i = Math.max(0, this.currentPage - 2); i <= Math.min(this.totalPages - 1, this.currentPage + 2); i++) {
      p.push(i);
    }
    return p;
  }

  trackBy(row: any): any {
    return row[this.trackByKey] || JSON.stringify(row);
  }

  onSearch(): void {
    this.searchChange.emit(this.searchQuery);
  }

  onSort(key: string): void {
    if (this.sortKey === key) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortKey = key;
      this.sortDir = 'asc';
    }
    this.sortChange.emit({ key: this.sortKey, dir: this.sortDir });
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.pageChange.emit(page);
  }
}
