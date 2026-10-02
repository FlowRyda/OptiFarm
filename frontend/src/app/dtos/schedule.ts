import {PlannedTask} from "./plannedtask";

export interface Schedule {
  id: number;
  name: string;
  plannedTasks: PlannedTask[];
}
