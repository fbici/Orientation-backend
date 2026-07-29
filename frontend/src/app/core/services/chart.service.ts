import { Injectable } from '@angular/core';

export interface ChartConfig {
  type: 'line' | 'bar' | 'pie' | 'doughnut' | 'radar' | 'area';
  labels: string[];
  datasets: ChartDataset[];
  options?: any;
}

export interface ChartDataset {
  label: string;
  data: number[];
  backgroundColor?: string | string[];
  borderColor?: string | string[];
  borderWidth?: number;
  fill?: boolean;
  tension?: number;
}

@Injectable({ providedIn: 'root' })
export class ChartService {

  private readonly colors = [
    '#1a1a2e', '#16213e', '#0f3460', '#e94560',
    '#533483', '#2b9348', '#0077b6', '#f77f00',
    '#d62828', '#7209b7', '#3a0ca3', '#4361ee'
  ];

  createLineChart(labels: string[], data: number[], label: string): ChartConfig {
    return {
      type: 'line',
      labels,
      datasets: [{
        label,
        data,
        borderColor: this.colors[0],
        backgroundColor: this.colors[0] + '20',
        fill: true,
        tension: 0.4
      }]
    };
  }

  createBarChart(labels: string[], data: number[], label: string): ChartConfig {
    return {
      type: 'bar',
      labels,
      datasets: [{
        label,
        data,
        backgroundColor: this.colors.slice(0, data.length),
        borderWidth: 1
      }]
    };
  }

  createPieChart(labels: string[], data: number[]): ChartConfig {
    return {
      type: 'pie',
      labels,
      datasets: [{
        label: '',
        data,
        backgroundColor: this.colors.slice(0, data.length)
      }]
    };
  }

  createRadarChart(labels: string[], datasets: ChartDataset[]): ChartConfig {
    return {
      type: 'radar',
      labels,
      datasets: datasets.map(ds => ({
        ...ds,
        backgroundColor: ds.backgroundColor || this.colors[0] + '40',
        borderColor: ds.borderColor || this.colors[0],
        borderWidth: 2
      }))
    };
  }

  createAreaChart(labels: string[], data: number[], label: string): ChartConfig {
    return {
      type: 'area',
      labels,
      datasets: [{
        label,
        data,
        backgroundColor: this.colors[0] + '30',
        borderColor: this.colors[0],
        fill: true,
        tension: 0.4
      }]
    };
  }

  getColors(): string[] {
    return [...this.colors];
  }
}
