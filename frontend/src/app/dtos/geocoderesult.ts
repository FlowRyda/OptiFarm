export interface GeocodeResult {
  place_id: number;
  osm_type: string;
  osm_id: string;
  lat: string;
  lon: string;
  display_name: string;
  type: string;
  importance: number;
  address: {
    house_number?: string;
    road?: string;
    city?: string;
    town?: string;
    village?: string;
    state?: string;
    postcode?: string;
    country: string;
    country_code: string;
  }
}
