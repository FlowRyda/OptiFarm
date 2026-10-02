import {Tool, ToolCategory} from "./tool";
import {FieldDataDto} from "./field-data/fieldDataDto";
import {Schedule} from "./schedule";
import {FieldDetailDto} from "./field";


export interface PlannedTask {
  id: number;
  name: string;
  assignedStart: string | null;
  assignedEnd: string | null;
  fixedEquipment: Tool[];
  fixed: boolean;
  schedule?: Schedule | null;
  field: FieldDetailDto;
  workTimeEstimate: number;
  taskId: number;
  conflicted: boolean;
}

export interface UpdatePlannedTaskDto {
  id: number;
  name: string;
  workTimeEstimate?: number;
  fieldId: FieldDataDto;
  categoryIds: ToolCategory[];
  toolIds: Tool[];
  assignedStart: string;
  assignedEnd: string;
  windowStart: string;
  windowEnd: string;
}
