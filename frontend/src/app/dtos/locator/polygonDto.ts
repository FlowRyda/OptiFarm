import {BaseLocatedObjectDto} from "./locatedObjectDto";
import {CoordinateDto} from "./coordinateDto";

export interface PolygonDto extends BaseLocatedObjectDto {
  type: 'POLYGON';
  color: string;
  fillColor: string;
  fillOpacity: number;
  weight: number;
  currentSeed: string;
  fieldId?: number;
  exclusions?: CoordinateDto[][];
  center: [number, number];
}
