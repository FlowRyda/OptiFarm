import {ActivatedRoute} from '@angular/router';
import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Component, ViewChild, inject, OnInit} from '@angular/core';
import {MatSort, Sort} from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import {Chart} from 'chart.js/auto';
import {FieldDataService} from '../../../services/fielddata.service';
import {FieldDataDto} from "../../../dtos/field-data/fieldDataDto";
import {CropService} from '../../../services/crop.service';
import {MatPaginator} from "@angular/material/paginator";
import {forkJoin} from "rxjs";
import {ForecastDto} from "../../../dtos/forecast/forecastDto";

type ScatterPoint = {
  x: number;
  y: number;
  crop: string;
  date: string;
};

@Component({
  selector: 'app-field-data',
  templateUrl: './field-data.component.html',
  standalone: false,
  styleUrl: './field-data.component.scss'
})
export class FieldDataComponent implements OnInit {

  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  isLoading = true;
  errorMessage = '';
  currentYear: number;
  fieldData?: FieldDataDto;
  fieldForecast?: ForecastDto;
  dataSource = new MatTableDataSource<any>([]);
  id: any;
  name: any;
  totalEarnings: number = 0;
  differencePrevYear: number = 0;
  harvestedCrops: any[] = [];
  scatterDataPoints: any[] = [];
  scatterDatasets: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private fieldDataService: FieldDataService,
    private cropService: CropService,
  ) {
  }

  ngOnInit(): void {
    this.isLoading = true;
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    if (!this.id) {
      this.errorMessage = 'Invalid field ID.';
      this.isLoading = false;
      return;
    }

    this.isLoading = true;
    this.currentYear = new Date().getFullYear();

    forkJoin({
      fieldData: this.fieldDataService.getById(this.id),
      crops: this.cropService.getCropsByFieldId(this.id),
      forecast: this.fieldDataService.getFieldForecastById(this.id)
    }).subscribe({
      next: ({ fieldData, crops, forecast }) => {
        console.log(fieldData);
        this.fieldData = fieldData;
        this.totalEarnings = fieldData.totalEarnings;
        this.differencePrevYear = fieldData.diffPrevYear;
        this.fieldForecast = forecast;
        this.name = fieldData.fieldName;

        this.harvestedCrops = crops.map((crop, index) => ({
          position: index + 1,
          cropType: crop.cropType,
          price: crop.currentPrice,
          quantity: crop.quantity,
          harvestDate: new Date(crop.harvestDate).toLocaleDateString()
        }));

        this.dataSource.data = this.harvestedCrops;
        this.dataSource.sort = this.sort;
        this.dataSource.paginator = this.paginator;

        this.scatterDataPoints = this.harvestedCrops.map(item => ({
          x: item.quantity,
          y: item.price,
          crop: item.cropType,
          date: item.harvestDate
        }));

        const grouped = this.scatterDataPoints.reduce((acc, p) => {
          (acc[p.crop] ??= []).push(p);
          return acc;
        }, {} as Record<string, ScatterPoint[]>);

        this.scatterDatasets = Object.entries(grouped).map(([cropType, data]) => ({
          label: cropType,
          data,
        }));
        this.isLoading = false;
        this.initChart();
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to load field data or crops.';
        this.isLoading = false;
      }
    });
  }

  private _liveAnnouncer = inject(LiveAnnouncer);

  displayedColumns: string[] = ['position', 'cropType', 'price', 'quantity', 'harvestDate'];

  initChart() {
    const timeSeriesLabels = Object.keys(this.fieldData.harvestedCropsTimeSeries ?? {});
    const timeSeriesData = Object.values(this.fieldData.harvestedCropsTimeSeries ?? {});
    const forecastLabels = Object.keys(this.fieldForecast.forecastTimeSeries ?? {});
    const forecastData = Object.values(this.fieldForecast.forecastTimeSeries ?? {});

    const allLabels = [...timeSeriesLabels, ...forecastLabels];

    const paddedActualData = [
      ...timeSeriesData,
      ...new Array(forecastData.length).fill(null)
    ];

    let paddedForecastData = [];
    let paddedLowerConfidenceData = [];
    let paddedUpperConfidenceData = [];

    if(forecastData.length > 0) {
      paddedForecastData = [
        ...new Array(timeSeriesData.length - 1).fill(null),
        timeSeriesData[timeSeriesData.length - 1],
        ...forecastData
      ];

      if (this.fieldForecast.lowerConfidenceIntervalTimeSeries != null) {
        paddedLowerConfidenceData = [
          ...new Array(timeSeriesData.length - 1).fill(null),
          timeSeriesData[timeSeriesData.length - 1],
          ...Object.values(this.fieldForecast.lowerConfidenceIntervalTimeSeries)
        ];
      }

      if (this.fieldForecast.upperConfidenceIntervalTimeSeries != null) {
        paddedUpperConfidenceData = [
          ...new Array(timeSeriesData.length - 1).fill(null),
          timeSeriesData[timeSeriesData.length - 1],
          ...Object.values(this.fieldForecast.upperConfidenceIntervalTimeSeries)
        ];
      }
    }

    new Chart('field-timeseries', {
      type: 'line',
      data: {
        labels: allLabels,
        datasets: [
          {
            label: 'Actual Field Yield',
            data: paddedActualData,
            tension: 0.4,
            borderWidth: 3
          },
          {
            label: 'Field Yield Forecast',
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
            text: 'Field Yield with Forecast',
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
              text: 'Field Yield in $'
            }
          }
        }
      }
    });

    new Chart('pie-chart-croptypes', {
      type: 'pie',
      data: {
        labels: Object.keys(this.fieldData.plantedCrops),
        datasets: [
          {
            label: 'Crop Type Distribution',
            data: Object.values(this.fieldData.plantedCrops),
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false
      }
    });

    new Chart('scatter-chart-crops', {
      type: 'scatter',
      data: {
        datasets: this.scatterDatasets
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            align: 'start',
            text: 'Scatter Chart Crop Harvests',
            font: {
              size: 20
            }
          },
          tooltip: {
            callbacks: {
              label: function (context) {
                const dataPoint = context.raw as any;
                return [
                  context.dataset.label ?? '',
                  `Crop: ${dataPoint.crop}`,
                  `Date: ${dataPoint.date}`,
                  `Quantity (tons): ${dataPoint.x}`,
                  `Price ($): ${dataPoint.y}`,
                ].filter(Boolean);
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

  getCropColor(crop: string): string {
    if (crop == null) return;
    let hash = 0;

    for (let i = 0; i < crop.length; i++) {
      hash = crop.charCodeAt(i) + ((hash << 5) - hash);
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
