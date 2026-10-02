import {ActivatedRoute} from '@angular/router';
import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Component, ViewChild, inject, OnInit} from '@angular/core';
import {MatSort, Sort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {Chart} from 'chart.js/auto';
import {FarmDataDto} from "../../../dtos/farm-data/farmDataDto";
import {MatPaginator} from "@angular/material/paginator";
import {forkJoin} from "rxjs";
import {ForecastDto} from "../../../dtos/forecast/forecastDto";
import {FarmDataService} from "../../../services/farmdata.service";
import {AuthService} from "../../../services/auth.service";
import {FieldDataService} from "../../../services/fielddata.service";

@Component({
  selector: 'app-farm-data',
  templateUrl: './farm-data.component.html',
  standalone: false,
  styleUrl: './farm-data.component.scss'
})
export class FarmDataComponent implements OnInit {

  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  isLoading = true;
  errorMessage = '';
  currentYear: number;
  farmData?: FarmDataDto;
  farmForecast?: ForecastDto;
  dataSource = new MatTableDataSource<any>([]);
  totalEarnings: number = 0;
  differencePrevYear: number = 0;
  numberOfHarvests: number = 0;
  fieldData: any[] = [];
  scatterDataPoints: any[] = [];
  id: any;

  constructor(
    private route: ActivatedRoute,
    private farmDataService: FarmDataService,
    private fieldDataService: FieldDataService,
    private authService: AuthService,
  ) {
  }

  ngOnInit(): void {
    this.isLoading = true;
    this.currentYear = new Date().getFullYear();
    this.id = this.authService.getUserId();

    if (!this.id) {
      this.errorMessage = 'Invalid user ID.';
      this.isLoading = false;
      return;
    }

    forkJoin({
      farmData: this.farmDataService.getById(this.id),
      fields: this.fieldDataService.getFieldDataByUserId(this.id),
      forecast: this.farmDataService.getFarmForecastByUserId(this.id)
    }).subscribe({
      next: ({ farmData, fields, forecast}) => {
        this.farmData = farmData;
        this.totalEarnings = farmData.totalEarnings;
        this.differencePrevYear = farmData.diffPrevYear;

        let sum = 0;
        for (const value of Object.values(this.farmData.plantedCrops)) {
          sum += value.valueOf();
        }
        this.numberOfHarvests = sum;
        this.farmForecast = forecast;

        this.fieldData = fields.map((field, index) => ({
          position: index + 1,
          fieldName: field.fieldName,
          totalEarnings: field.totalEarnings,
          totalQuantity: field.totalQuantity,
          numberOfHarvests: field.numberOfHarvests
        }));

        this.dataSource.data = this.fieldData;
        this.dataSource.sort = this.sort;
        this.dataSource.paginator = this.paginator;

        this.scatterDataPoints = this.fieldData.map(item => ({
          x: item.totalQuantity,
          y: item.totalEarnings,
          fieldName: item.fieldName,
          numberOfHarvests: item.numberOfHarvests
        }));
        this.isLoading = false;
        this.initChart();
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to load farm data or fields.';
        this.isLoading = false;
      }
    });
  }

  private _liveAnnouncer = inject(LiveAnnouncer);

  displayedColumns: string[] = ['position', 'fieldName', 'totalEarnings', 'totalQuantity', 'numberOfHarvests'];

  initChart() {
    const farmTimeSeriesLabels = Object.keys(this.farmData.farmTimeSeries);
    const farmTimeSeriesData = Object.values(this.farmData.farmTimeSeries);

    const farmLabels = Object.keys(this.farmData.farmTimeSeries);

    const fieldTimeSeriesLabels = new Set(farmLabels);

    this.farmData.fieldTimeSeries.forEach(fieldMap => {
      Object.keys(fieldMap).forEach(key => fieldTimeSeriesLabels.add(key));
    });

    const allTimeSeriesLabels = Array.from(fieldTimeSeriesLabels).sort((a, b) => Number(a) - Number(b));

    const fieldDatasets = this.farmData.fieldTimeSeries.map((fieldMap, index) => {

      const fieldName = this.fieldData && this.fieldData[index]
        ? this.fieldData[index].fieldName
        : `Field ${index + 1}`;

      const paddedData = allTimeSeriesLabels.map(label => {
        const value = fieldMap[label];

        return (value !== undefined) ? value : null;
      });

      return {
        label: fieldName,
        data: paddedData,
        tension: 0.4,
        borderWidth: 1.5,
        spanGaps: false
      };
    });

    const forecastLabels = Object.keys(this.farmForecast.forecastTimeSeries);
    const forecastData = Object.values(this.farmForecast.forecastTimeSeries);

    const allLabels = [...farmTimeSeriesLabels, ...forecastLabels];

    const paddedActualData = [
      ...farmTimeSeriesData,
      ...new Array(forecastData.length).fill(null)
    ];

    const paddedForecastData = [
      ...new Array(farmTimeSeriesData.length - 1).fill(null),
      farmTimeSeriesData[farmTimeSeriesData.length - 1],
      ...forecastData
    ];

    const paddedLowerConfidenceData = [
      ...new Array(farmTimeSeriesData.length - 1).fill(null),
      farmTimeSeriesData[farmTimeSeriesData.length - 1],
      ...Object.values(this.farmForecast.lowerConfidenceIntervalTimeSeries)
    ];

    const paddedUpperConfidenceData = [
      ...new Array(farmTimeSeriesData.length - 1).fill(null),
      farmTimeSeriesData[farmTimeSeriesData.length - 1],
      ...Object.values(this.farmForecast.upperConfidenceIntervalTimeSeries)
    ];

    new Chart('farm-timeseries', {
      type: 'line',
      data: {
        labels: allLabels,
        datasets: [
          {
            label: 'Actual Farm Yield',
            data: paddedActualData,
            tension: 0.4,
            borderWidth: 3
          },
          {
            label: 'Farm Yield Forecast',
            data: paddedForecastData,
            tension: 0.4,
            borderDash: [8, 6],
            borderWidth: 2,
            pointStyle: 'circle'
          },
          {
            label: 'Lower Confidence Interval',
            data: paddedLowerConfidenceData,
            tension: 0.4,
            borderDash: [8, 6],
            borderWidth: 2,
            pointStyle: 'circle',
          },
          {
            label: 'Upper Confidence Interval',
            data: paddedUpperConfidenceData,
            tension: 0.4,
            borderDash: [8, 6],
            borderWidth: 2,
            pointStyle: 'circle',
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            align: 'start',
            text: 'Farm Yield with Forecast',
            font: {
              size: 20
            }
          }
        },
        scales: {
          x: {
            display: true,
            title: {
              display: true,
              text: 'Years'
            }
          },
          y: {
            display: true,
            title: {
              display: true,
              text: 'Farm Yield in $'
            }
          }
        }
      }
    });

    new Chart('field-timeseries', {
      type: 'line',
      data: {
        labels: allTimeSeriesLabels,
        datasets: [
          ...fieldDatasets
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            align: 'start',
            text: 'Field Yields',
            font: {
              size: 20
            }
          }
        },
        scales: {
          x: {
            display: true,
            title: {
              display: true,
              text: 'Years'
            }
          },
          y: {
            display: true,
            title: {
              display: true,
              text: 'Farm Yield in $'
            }
          }
        }
      }
    });

    new Chart('pie-chart-croptypes', {
      type: 'pie',
      data: {
        labels: Object.keys(this.farmData.plantedCrops),
        datasets: [
          {
            label: 'Crop Type Distribution',
            data: Object.values(this.farmData.plantedCrops),
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false
      }
    });

    new Chart('scatter-chart-fields', {
      type: 'scatter',
      data: {
        datasets: [{
          label: 'Field Yield',
          data: this.scatterDataPoints,
          backgroundColor: (context) => {
            const dataPoint = context.raw as any;
            return this.getFieldColor(dataPoint.fieldName);
          },
          borderColor: (context) => {
            const dataPoint = context.raw as any;
            return this.getFieldColor(dataPoint.fieldName);
          },
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            align: 'start',
            text: 'Scatter Chart Field Performance',
            font: {
              size: 20
            }
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                const dataPoint = context.dataset.data[context.dataIndex] as any;
                const label = [];

                if (context.dataset.label) {
                  label.push(context.dataset.label);
                }
                label.push(`Field: ${dataPoint.fieldName}`);
                label.push(`Number of Harvests: ${dataPoint.numberOfHarvests}`);
                label.push(`Quantity (tons): ${dataPoint.x}`);
                label.push(`Earnings ($): ${dataPoint.y}`);
                return label;
              }
            }
          }
        },
        scales: {
          x: {
            type: 'linear',
            title: {
              display: true,
              text: 'Quantity (tons)'
            }
          },
          y: {
            title: {
              display: true,
              text: 'Price ($)'
            }
          }
        }
      }
    });
  }

  getFieldColor(field: string): string {
    let hash = 0;

    for (let i = 0; i < field.length; i++) {
      hash = field.charCodeAt(i) + ((hash << 5) - hash);
    }

    const hue = Math.abs(hash) % 360;
    return `hsl(${hue}, 70%, 50%)`;
  }

  announceSortChange(sortState: Sort) {
    if (sortState.direction) {
      this._liveAnnouncer.announce(`Sorted ${sortState.direction}ending`);
    } else {
      this._liveAnnouncer.announce('Sorting cleared');
    }
  }
}
