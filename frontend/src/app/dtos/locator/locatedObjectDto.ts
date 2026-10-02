import {CoordinateDto} from "./coordinateDto";
import { MarkerDto } from "./markerDto";
import { PolygonDto } from "./polygonDto";

export type LocatedObjectDto = MarkerDto | PolygonDto;

export interface BaseLocatedObjectDto {
  type: 'MARKER' | 'POLYGON';
  title: string;
  points?: CoordinateDto[];
}

// Type guards
export function isMarkerDto(o: LocatedObjectDto): o is MarkerDto {
  return (o as MarkerDto).iconUrl !== undefined;
}

export function isPolygonDto(o: LocatedObjectDto): o is PolygonDto {
  return (o as PolygonDto).color !== undefined;
}
