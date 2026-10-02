import {Component, EventEmitter, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {HttpClient, HttpParams} from '@angular/common/http';
import {
  debounceTime,
  distinctUntilChanged,
  switchMap
} from 'rxjs/operators';
import {Observable, of} from 'rxjs';

interface NominatimResult {
  display_name: string;
  lat: string;
  lon: string;
  address: {
    road?: string;
    house_number?: string;
    city?: string;
    town?: string;
    village?: string;
    postcode?: string;
    country?: string;
  };
}

@Component({
  selector: 'app-address-autocomplete',
  templateUrl: './address-autocomplete.component.html',
  styleUrls: ['./address-autocomplete.component.scss'],
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule]
})
export class AddressAutocompleteComponent {
  @Output() addressSelected = new EventEmitter<{
    lat: string;
    lon: string;
  }>();

  searchControl = new FormControl('');
  results: NominatimResult[] = [];
  showResults = false;

  constructor(private http: HttpClient) {
    this.initSearch();
  }

  private initSearch() {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(value => this.search(value || ''))
      )
      .subscribe(results => {
        this.results = results;
        this.showResults = results.length > 0;
      });
  }

  private search(query: string): Observable<NominatimResult[]> {
    if (!query || query.length < 3) {
      this.showResults = false;
      return of([]);
    }

    const params = new HttpParams()
      .set('q', query)
      .set('format', 'json')
      .set('addressdetails', '1')
      .set('limit', '5');

    return this.http.get<NominatimResult[]>(
      'https://nominatim.openstreetmap.org/search',
      {
        params,
        headers: {'Accept-Language': 'en'}
      }
    );
  }

  onSelect(result: NominatimResult) {
    this.showResults = false;
    this.searchControl.setValue(result.display_name, {emitEvent: false});

    this.addressSelected.emit({
      lat: result.lat,
      lon: result.lon
    });
  }

  onBlur() {
    setTimeout(() => (this.showResults = false), 200);
  }
}
