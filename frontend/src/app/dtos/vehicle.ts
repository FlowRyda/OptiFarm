import {Tool} from "./tool";

export interface Vehicle extends Tool {
  drivenKm: number;
  fuelLevel: number;
}
