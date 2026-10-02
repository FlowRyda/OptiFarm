import {BaseLocatedObjectDto} from "./locatedObjectDto";


export interface MarkerDto extends BaseLocatedObjectDto {
  type: 'MARKER';
  iconUrl: string;
  iconWidth: number;
  iconHeight: number;
  isUsable: boolean;
  fuelLevel: number | undefined;
  markerId?: number;
  garage: boolean;
}
