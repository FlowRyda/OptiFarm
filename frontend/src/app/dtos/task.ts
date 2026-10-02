import {FieldDetailDto} from "./field";
import {Tool, ToolCategory} from "./tool";

export enum TaskType {
  HARVESTING = 'HARVESTING',
  PLANTING = 'PLANTING',
  MAINTENANCE = 'MAINTENANCE',
  EMERGENCY = 'EMERGENCY',
  NONE = 'NONE'
}

export interface Task {
  id: number;
  name: string;
  windowStart: Date;
  windowEnd: Date;
  assignedStart: Date;
  assignedEnd: Date;
  fixed: boolean;
  field?: FieldDetailDto;
  tools?: Tool[];
  categories?: ToolCategory[];
  workTimeEstimate: number;
  taskType: TaskType;
}
