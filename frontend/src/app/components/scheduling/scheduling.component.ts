import {Component, OnInit} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {TaskDetailComponent} from "./task/task-detail/task-detail.component";
import AddTaskComponent from "./task/add-task/add-task/add-task.component";
import {TaskService} from "../../services/task.service";
import {Task as TaskDTO} from "../../dtos/task";
import {ScheduleService} from "../../services/schedule.service";
import {Schedule} from "../../dtos/schedule";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {PlannedTaskService} from "../../services/planned-task.service";
import {formatSmartDateRange} from "../../utils/date.utils";

export interface Machine {
  id: string;
  name: string;
}

interface DisplayTask extends TaskDTO {
  color?: string;
  plannedStart?: Date;
  plannedEnd?: Date;
  isScheduledTask?: boolean;
  taskId?: number;
}

interface DragState {
  task: DisplayTask;
  machineId: string;
  startX: number;
  initialLeft: number;
  isDragging: boolean;
}

@Component({
  selector: 'app-scheduling',
  templateUrl: './scheduling.component.html',
  styleUrl: './scheduling.component.scss',
  standalone: false
})
export class SchedulingComponent implements OnInit {
  startHour = 6;
  endHour = 22;
  hourWidth = 40;
  numDays = 10;

  hoursPerDay: number[] = [];
  dayDates: Date[] = [];

  searchTerm: string | null = null;
  filteredTasks = [];
  windowFrom: string | null = null;
  windowTo: string | null = null;

  onWindowChange() {
    if (this.windowFrom && this.windowTo && this.windowFrom > this.windowTo) {
      [this.windowFrom, this.windowTo] = [this.windowTo, this.windowFrom];
    }

    this.applyFilter();
  }

  clearWindow() {
    this.windowFrom = null;
    this.windowTo = null;
    this.searchTerm = null;
    this.applyFilter();
  }

  tasks: DisplayTask[] = [];
  schedulingTasks: DisplayTask[] = [];
  conflictedSchedulingTasks: DisplayTask[] = [];
  machines: Machine[] = [];
  timelineStartDate: Date = new Date();

  currentSchedule: Schedule = null;
  schedules: Schedule[] = [];
  selectedScheduleId: number | null = null;

  scheduleDropdownOpen = false;
  showDeleteScheduleConfirmation = false;
  scheduleToDelete: Schedule | null = null;

  startScheduleForm!: FormGroup;
  showSaveScheduleModal = false;
  showConflictModal = false;

  isLoading = true;

  dragState: DragState | null = null;
  dragValidPosition: boolean = true;

  private readonly palette = ['#2e8b57', '#2f9e44', '#1f7a5a', '#4CAF50', '#388E3C', '#2E7D32'];
  private readonly conflictPalette = ['#bc2601'];

  constructor(
    private fb: FormBuilder,
    private dialog: MatDialog,
    private taskService: TaskService,
    private plannedTaskService: PlannedTaskService,
    private schedulingService: ScheduleService,
  ) {
  }

  ngOnInit(): void {
    this.startScheduleForm = this.fb.group({name: ['', Validators.required],});

    this.initializeHours();
    this.loadTasks();
    this.loadSchedules();

    document.addEventListener('mousemove', this.onMouseMove.bind(this));
    document.addEventListener('mouseup', this.onMouseUp.bind(this));
  }

  applyFilter() {
    let result = this.tasks;

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase().trim();
      result = this.tasks.filter(t =>
        t.name?.toLowerCase().includes(term) ||
        t.field?.name?.toLowerCase().includes(term) ||
        t.tools?.some(tool => tool.name?.toLowerCase().includes(term))
      );
    }

    const from = this.windowFrom ? new Date(this.windowFrom).getTime() : null;
    const to = this.windowTo ? new Date(this.windowTo).getTime() : null;

    if (from !== null || to !== null) {
      result = result.filter(t => {
        const start = t.windowStart ? new Date(t.windowStart).getTime() : null;
        const end = t.windowEnd ? new Date(t.windowEnd).getTime() : null;

        if (start === null || end === null) return false;

        const selectedFrom = from ?? Number.NEGATIVE_INFINITY;
        const selectedTo = to ?? Number.POSITIVE_INFINITY;

        return start <= selectedTo && end >= selectedFrom;
      });
    }

    this.filteredTasks = result;
  }

  private loadSchedules(): void {
    this.schedulingService.getAll().subscribe({
      next: (schedules) => {
        this.schedules = schedules;
        if (schedules.length > 0) {
          const last = schedules[schedules.length - 1];
          this.onScheduleSelected(last.id);
        }
      },
      error: (error) => {
        console.error('Error loading schedules:', error);
      }
    });
  }

  private initializeHours(): void {
    for (let h = this.startHour; h < this.endHour; h += 2) {
      this.hoursPerDay.push(h);
    }
  }

  private loadTasks(): void {
    this.taskService.getAll().subscribe({
      next: (tasks) => {
        this.tasks = this.transformTasks(tasks)
          .sort((a, b) => Number(a.id) - Number(b.id));

        // default timeline when there is no schedule loaded/selected yet
        if (!this.currentSchedule) {
          this.extractMachines();
          this.calculateTimelineStartDate();
          this.numDays = 10;
          this.initializeDayDates();
        }

        // in case schedule got loaded before tasks OR schedule already selected
        if (this.currentSchedule) {
          this.extractScheduledTasksFromSchedule(this.currentSchedule);
          this.applyScheduleColorsToTaskList();

          this.extractMachinesFromSchedulingTasks();
          this.calculateTimelineStartDateFromSchedulingTasks();
          this.updateNumDaysFromScheduledTasks();
          this.initializeDayDates();
        }

        this.isLoading = false;

        this.filteredTasks = this.tasks;
        this.applyFilter();
      },
      error: (error) => {
        console.error('Error loading tasks:', error);
        this.isLoading = false;
      }
    });
  }

  private colorForTaskId(id: number | string | null | undefined): string {
    const n = Number(id);
    if (Number.isNaN(n)) return this.palette[0];
    return this.palette[Math.abs(n) % this.palette.length];
  }

  private conflictColorFor(): string {
    return this.conflictPalette[0];
  }

  private transformTasks(tasks: TaskDTO[]): DisplayTask[] {
    return tasks.map(task => ({
      ...task,
      color: this.colorForTaskId(task.id),
      ...(task.assignedStart && {assignedStart: this.parseLocalDateTime(task.assignedStart)}),
      ...(task.assignedEnd && {assignedEnd: this.parseLocalDateTime(task.assignedEnd)}),
      isScheduledTask: false
    }));
  }

  private extractMachines(): void {
    const machineMap = new Map<number, Machine>();

    this.schedulingTasks.forEach(task => {
      task.tools?.forEach(tool => {
        if (!machineMap.has(tool.id)) {
          machineMap.set(tool.id, {
            id: tool.id.toString(),
            name: tool.name
          });
        }
      });
    });

    let maschinen = Array.from(machineMap.values())
    this.machines = maschinen.sort((a, b) => a.name.localeCompare(b.name));
  }

  private updateNumDaysFromScheduledTasks(): void {
    const plannedEnds = this.schedulingTasks
      .map(t => t.plannedEnd)
      .filter((d): d is Date => d instanceof Date);

    if (plannedEnds.length === 0) {
      this.numDays = 10;
      return;
    }

    const maxEnd = new Date(Math.max(...plannedEnds.map(d => d.getTime())));

    const start = this.startOfLocalDay(this.timelineStartDate).getTime();
    const end = this.startOfLocalDay(maxEnd).getTime();

    const MS_PER_DAY = 24 * 60 * 60 * 1000;
    const days = Math.floor((end - start) / MS_PER_DAY) + 1;

    this.numDays = Math.min(Math.max(days, 1), 356);
  }

  private calculateTimelineStartDate(): void {
    if (this.tasks.length === 0) {
      this.timelineStartDate = this.startOfLocalDay(new Date());
      return;
    }

    const earliest = this.tasks.reduce((min, t) => {
      const ws = this.parseLocalDateTime(t.windowStart);
      return ws && ws < min ? ws : min;
    }, this.parseLocalDateTime(this.tasks[0].windowStart)!);

    this.timelineStartDate = this.startOfLocalDay(earliest);
  }

  private initializeDayDates(): void {
    this.dayDates = [];
    for (let i = 0; i < this.numDays; i++) {
      const d = new Date(this.timelineStartDate.getTime() + i * 24 * 60 * 60 * 1000);
      this.dayDates.push(d);
    }
  }

  getTasksForMachine(machineId: string): DisplayTask[] {
    return this.schedulingTasks.filter(t =>
      t.tools?.some(tool => tool.id.toString() === machineId) &&
      t.plannedStart != null &&
      t.plannedEnd != null
    );
  }

  private getDayIndex(d: Date): number {
    const MS_PER_DAY = 24 * 60 * 60 * 1000;
    const start = this.startOfLocalDay(this.timelineStartDate).getTime();
    const cur = this.startOfLocalDay(d).getTime();
    return Math.floor((cur - start) / MS_PER_DAY);
  }

  private getAxisPosition(d: Date): number {
    const dayIndex = this.getDayIndex(d);

    const hour = d.getHours();
    const minute = d.getMinutes();
    const timeInHours = hour + minute / 60;

    let activeHoursInDay = timeInHours - this.startHour;
    if (activeHoursInDay < 0) activeHoursInDay = 0;
    if (activeHoursInDay > (this.endHour - this.startHour)) {
      activeHoursInDay = (this.endHour - this.startHour);
    }

    const hoursPerDayVisible = (this.endHour - this.startHour);
    const pxPerHour = this.hourWidth / 2;

    return (dayIndex * hoursPerDayVisible * pxPerHour) + (activeHoursInDay * pxPerHour);
  }

  getOffsetPx(start?: Date): number {
    if (!start) return 0;
    return this.getAxisPosition(start);
  }

  getWidthPx(start?: Date, end?: Date): number {
    if (!start || !end) return 0;
    return this.getAxisPosition(end) - this.getAxisPosition(start);
  }

  showTaskLabel(t: any): boolean {
    if (!t.name) return false;

    const start = new Date(t.plannedStart);
    const end = new Date(t.plannedEnd);
    const durationHours = (end.getTime() - start.getTime()) / (1000 * 60 * 60);

    return durationHours >= 3;
  }

  formatDate(d?: Date | string): string {
    if (!d) return '–';
    const date = typeof d === 'string' ? new Date(d) : d;
    return date.toLocaleDateString([], {day: '2-digit', month: '2-digit'});
  }

  format(d?: Date | string): string {
    if (!d) return '–';
    const date = typeof d === 'string' ? new Date(d) : d;
    return date.toLocaleString([], {day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit'});
  }

  formatPlanned(start?: Date, end?: Date): string {
    return formatSmartDateRange(start, end);
  }

  trackById(_: number, item: any): any {
    return item.id;
  }

  onTaskMouseDown(event: MouseEvent, task: DisplayTask, machineId: string): void {
    if ((event.target as HTMLElement).classList.contains('task-block')) {
      event.preventDefault();
      event.stopPropagation();

      this.dragState = {
        task: task,
        machineId: machineId,
        startX: event.clientX,
        initialLeft: this.getOffsetPx(task.plannedStart),
        isDragging: false
      };
    }
  }

  private onMouseMove(event: MouseEvent): void {
    if (!this.dragState) return;

    const deltaX = event.clientX - this.dragState.startX;
    if (!this.dragState.isDragging && Math.abs(deltaX) > 5) {
      this.dragState.isDragging = true;
    }

    if (this.dragState.isDragging) {
      const newLeft = this.dragState.initialLeft + deltaX;
      let targetStartDate = this.getDateFromPixelPosition(newLeft);

      if (targetStartDate && this.dragState.task.plannedStart && this.dragState.task.plannedEnd) {
        targetStartDate = this.snapToFullHour(targetStartDate);

        const duration = this.dragState.task.plannedEnd.getTime() - this.dragState.task.plannedStart.getTime();

        const boundaryPos = this.adjustForDayBoundaries(targetStartDate, duration);

        const finalPosition = this.findNextAvailableSlot(
          this.dragState.task,
          boundaryPos.start,
          duration,
          this.dragState.machineId
        );

        this.dragState.task.plannedStart = finalPosition.start;
        this.dragState.task.plannedEnd = finalPosition.end;
      }
    }
  }

  private snapToFullHour(date: Date): Date {
    const snapped = new Date(date);
    snapped.setMinutes(0);
    snapped.setSeconds(0);
    snapped.setMilliseconds(0);
    return snapped;
  }

  private onMouseUp(event: MouseEvent): void {
    if (!this.dragState) return;

    const task = this.dragState.task;

    if (this.dragState.isDragging) {
      if (this.currentSchedule && task.plannedStart && task.plannedEnd) {
        const plannedTaskIndex =
          this.currentSchedule.plannedTasks.findIndex(
            pt => pt.id === task.id
          );

        if (plannedTaskIndex !== -1) {
          this.currentSchedule.plannedTasks[plannedTaskIndex] = {
            ...this.currentSchedule.plannedTasks[plannedTaskIndex],
            assignedStart: this.dateToLocalDateTimeString(task.plannedStart),
            assignedEnd: this.dateToLocalDateTimeString(task.plannedEnd),
            fixed: true
          };

          this.schedulingService
            .updateSchedule(this.currentSchedule.id, this.currentSchedule)
            .subscribe({
              next: updatedSchedule => {
                console.log('Schedule updated after drag:', updatedSchedule);
                this.currentSchedule = updatedSchedule;
                this.extractScheduledTasksFromSchedule(updatedSchedule);
                this.loadTasks();
              },
              error: err =>
                console.error('Error updating schedule after drag:', err)
            });
        }
      }
    } else {
      this.openTaskDialog(task);
    }

    this.dragState = null;
    this.dragValidPosition = true;
  }

  private getDateFromPixelPosition(pixels: number): Date | null {
    const hoursPerDayVisible = (this.endHour - this.startHour);
    const pxPerHour = this.hourWidth / 2;
    const pxPerDay = hoursPerDayVisible * pxPerHour;

    const dayIndex = Math.floor(pixels / pxPerDay);
    const remainderPx = pixels % pxPerDay;
    const hoursIntoDay = remainderPx / pxPerHour;

    if (dayIndex < 0 || dayIndex >= this.numDays) return null;

    const targetHour = this.startHour + Math.floor(hoursIntoDay);
    const targetMinutes = Math.round((hoursIntoDay % 1) * 60);

    const base = new Date(this.timelineStartDate);
    base.setHours(0, 0, 0, 0);
    base.setDate(base.getDate() + dayIndex);
    base.setHours(targetHour, targetMinutes, 0, 0);

    return base;
  }


  isDragging(task: DisplayTask): boolean {
    return this.dragState?.task === task && this.dragState?.isDragging;
  }

  openTaskDialog(task: DisplayTask): void {
    // If this entry originates from a plannedTask, it has taskId = base task id
    if (task?.taskId != null) {
      this.taskService.getById(task.taskId).subscribe({
        next: (baseTask) => {
          const baseDisplayTask: DisplayTask = {
            ...baseTask,
            color: this.colorForTaskId(baseTask.id),
            isScheduledTask: false
          };

          // optional: carry over schedule-specific info you still want to show

          this.openTaskDialog(baseDisplayTask);
        },
        error: (err) => {
          console.error('Failed to load base task, falling back to planned task:', err);
          // fallback: open whatever we have (planned task)
          this.openTaskDialog({...task, isScheduledTask: false, taskId: undefined});
        }
      });
      return;
    }

    const dialogRef = this.dialog.open(TaskDetailComponent, {
      minWidth: '30vw',
      data: task
    });

    dialogRef.componentInstance.deleted.subscribe(() => {
      console.log('Task was deleted');
      this.loadTasks();
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.deleted) {
        this.tasks = this.tasks.filter(t => t.id !== task.id);
        this.extractMachines();

        if (this.currentSchedule) {
          this.isLoading = true;
          this.schedulingService.reschedule(this.currentSchedule.id).subscribe({
            next: (schedule) => {
              this.currentSchedule = schedule;
              this.extractScheduledTasksFromSchedule(schedule);
              this.applyScheduleColorsToTaskList();
              this.extractMachinesFromSchedulingTasks();
              this.calculateTimelineStartDateFromSchedulingTasks();
              this.updateNumDaysFromScheduledTasks();
              this.initializeDayDates();
              this.isLoading = false;
            },
            error: (error) => {
              console.error('Error reloading schedule:', error);
              this.isLoading = false;
            }
          });
        }
        return;
      }

      if (result?.updated) {
        this.loadTasks();
        this.rescheduleExistingSchedule();
      }
    });
  }

  openAddTaskDialog(): void {
    const dialogRef = this.dialog.open(AddTaskComponent, {
      minWidth: '40vw'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result?.created) {
        this.loadTasks();
      }
    });
  }

  openConflictModal(): void {
    this.showConflictModal = true;
  }

  openStartScheduleModal(): void {
    this.startScheduleForm.patchValue({
      name: this.defaultScheduleName()
    });

    this.showSaveScheduleModal = true;
  }

  confirmStartSchedule(): void {
    if (this.startScheduleForm.invalid) {
      this.startScheduleForm.markAllAsTouched();
      return;
    }

    const scheduleName = (this.startScheduleForm.value.name as string).trim();

    this.isLoading = true;
    this.closeSaveScheduleModal();

    this.schedulingService.computeSchedule().subscribe({
      next: (schedule) => {
        schedule.name = scheduleName;

        this.schedulingService.save(schedule).subscribe({
          next: (savedSchedule) => {
            this.currentSchedule = savedSchedule;
            this.selectedScheduleId = savedSchedule.id;
            this.extractScheduledTasksFromSchedule(savedSchedule);
            this.applyScheduleColorsToTaskList();
            this.upsertSchedule(savedSchedule);

            this.extractMachinesFromSchedulingTasks();
            this.calculateTimelineStartDateFromSchedulingTasks();
            this.updateNumDaysFromScheduledTasks();
            this.initializeDayDates();

            this.syncTasksAndPersist();

            this.isLoading = false;

            if (this.conflictedSchedulingTasks.length > 0) {
              this.showConflictModal = true;
            }
          },
          error: (err) => {
            console.error('Error saving schedule:', err);
            this.isLoading = false;
          }
        });
      },
      error: (err) => {
        console.error('Error computing schedule:', err);
        this.isLoading = false;
      }
    });
  }

  rescheduleExistingSchedule(): void {
    if (!this.currentSchedule) return;

    this.closeConflictModal();
    this.conflictedSchedulingTasks = [];
    this.isLoading = true;
    const scheduleId = this.currentSchedule.id;
    this.schedulingService.reschedule(scheduleId).subscribe({
      next: (schedule) => {
        console.log('Rescheduling completed:', schedule);
        this.currentSchedule = schedule;
        this.extractScheduledTasksFromSchedule(schedule);
        this.applyScheduleColorsToTaskList();
        this.upsertSchedule(schedule);
        this.selectedScheduleId = schedule.id;

        this.extractMachinesFromSchedulingTasks();
        this.calculateTimelineStartDateFromSchedulingTasks();
        this.updateNumDaysFromScheduledTasks();
        this.initializeDayDates();
        this.isLoading = false;

        if (this.conflictedSchedulingTasks.length > 0) {
          this.showConflictModal = true;
        }
      },
      error: (error) => {
        console.error('Error during rescheduling:', error);
        this.isLoading = false;
      }
    });
  }

  closeSaveScheduleModal(): void {
    this.showSaveScheduleModal = false;
    this.startScheduleForm.markAsPristine();
    this.startScheduleForm.markAsUntouched();
  }

  private extractMachinesFromSchedulingTasks(): void {
    const machineMap = new Map<number, Machine>();

    this.schedulingTasks.forEach(task => {
      task.tools?.forEach(tool => {
        if (!machineMap.has(tool.id)) {
          machineMap.set(tool.id, {
            id: tool.id.toString(),
            name: tool.name
          });
        }
      });
    });

    let maschinen = Array.from(machineMap.values())
    this.machines = maschinen.sort((a, b) => a.name.localeCompare(b.name));
  }

  private calculateTimelineStartDateFromSchedulingTasks(): void {
    if (this.schedulingTasks.length === 0) {
      this.timelineStartDate = this.startOfLocalDay(new Date());
      return;
    }

    const earliest = this.schedulingTasks.reduce((min, t) => {
      if (!t.plannedStart) return min;
      return t.plannedStart < min ? t.plannedStart : min;
    }, this.schedulingTasks[0].plannedStart || new Date());

    this.timelineStartDate = this.timelineStartDate = this.startOfLocalDay(earliest);
  }

  extractScheduledTasksFromSchedule(schedule: any): void {
    const scheduled: DisplayTask[] = [];
    const conflicted: DisplayTask[] = [];

    // Map base tasks by base id
    const baseById = new Map<number, DisplayTask>(
      this.tasks.map(t => [Number(t.id), t])
    );

    for (const plannedTask of (schedule.plannedTasks ?? [])) {
      const baseId = Number(plannedTask.taskId);
      const base = baseById.get(baseId);

      const dt: DisplayTask = {
        id: plannedTask.id,
        taskId: plannedTask.taskId,
        name: plannedTask.name,
        windowStart: this.parseLocalDateTime(plannedTask.windowStart)!,
        windowEnd: this.parseLocalDateTime(plannedTask.windowEnd)!,
        assignedStart: plannedTask.assignedStart ? this.parseLocalDateTime(plannedTask.assignedStart)! : undefined,
        assignedEnd: plannedTask.assignedEnd ? this.parseLocalDateTime(plannedTask.assignedEnd)! : undefined,
        plannedStart: plannedTask.assignedStart ? this.parseLocalDateTime(plannedTask.assignedStart)! : undefined,
        plannedEnd: plannedTask.assignedEnd ? this.parseLocalDateTime(plannedTask.assignedEnd)! : undefined,
        field: plannedTask.field,
        tools: plannedTask.fixedEquipment,
        workTimeEstimate: plannedTask.workTimeEstimate,
        color: plannedTask.conflicted ? '#bc2601' : this.colorForTaskId(plannedTask.taskId),
        categories: plannedTask.categories,
        isScheduledTask: !plannedTask.conflicted,
        fixed: plannedTask.fixed,
        taskType: base?.taskType
      };

      if (plannedTask.conflicted) {
        if (base) {
          if (!dt.categories || dt.categories.length === 0) dt.categories = base.categories;
          if (!dt.tools || dt.tools.length === 0) dt.tools = base.tools;
        }
        conflicted.push(dt);
      } else {
        scheduled.push(dt);
      }
    }

    this.schedulingTasks = scheduled;
    this.conflictedSchedulingTasks = conflicted;
  }

  private upsertSchedule(schedule: Schedule): void {
    const idx = this.schedules.findIndex(s => s.id === schedule.id);
    if (idx >= 0) {
      this.schedules[idx] = schedule;
    } else {
      this.schedules = [schedule, ...this.schedules];
    }
  }

  onScheduleSelected(scheduleId: number | null): void {
    if (scheduleId === null || scheduleId === undefined) {
      this.selectedScheduleId = null;
      this.currentSchedule = null;

      this.schedulingTasks = [];
      this.conflictedSchedulingTasks = [];
      this.numDays = 10;

      this.extractMachines();
      this.calculateTimelineStartDate();
      this.initializeDayDates();

      this.applyScheduleColorsToTaskList();
      this.applyFilter();
      return;
    }

    this.selectedScheduleId = scheduleId;

    const schedule = this.schedules.find(s => s.id === scheduleId);
    if (!schedule) {
      console.warn('Schedule not found for id', scheduleId);
      return;
    }

    this.currentSchedule = schedule;
    this.extractScheduledTasksFromSchedule(schedule);
    this.applyScheduleColorsToTaskList();

    this.syncTasksWithSchedule();

    this.extractMachinesFromSchedulingTasks();
    this.calculateTimelineStartDateFromSchedulingTasks();
    this.updateNumDaysFromScheduledTasks();
    this.initializeDayDates();
    this.scheduleDropdownOpen = false;
  }

  private syncTasksWithSchedule(): void {
    if (!this.currentSchedule) return;

    this.tasks = this.tasks.map(task => {
      const plannedTask = this.schedulingTasks.find(pt => pt.taskId === task.id);
      if (!plannedTask) return task;

      const start = plannedTask.plannedStart;
      const end = plannedTask.plannedEnd;
      if (!start || !end) return task;

      return {
        ...task,
        plannedStart: start,
        plannedEnd: end,
        assignedStart: start,
        assignedEnd: end
      };
    });

    this.applyFilter();
  }

  onDeleteScheduleClick(event: MouseEvent, schedule: Schedule): void {
    event.stopPropagation();
    event.preventDefault();

    this.scheduleToDelete = schedule;
    this.showDeleteScheduleConfirmation = true;
  }

  confirmDeleteSchedule(): void {
    if (!this.scheduleToDelete) return;

    const scheduleId = this.scheduleToDelete.id;

    this.schedulingService.delete(scheduleId).subscribe({
      next: () => {
        this.schedules = this.schedules.filter(s => s.id !== scheduleId);

        if (this.currentSchedule?.id === scheduleId) {
          this.selectedScheduleId = null;
          this.currentSchedule = null;
          this.schedulingTasks = [];
          this.conflictedSchedulingTasks = [];

          this.extractMachines();
          this.calculateTimelineStartDate();
          this.updateNumDaysFromScheduledTasks();
          this.initializeDayDates();
        }

        this.closeDeleteScheduleModal();
      },
      error: (err) => {
        console.error('Error deleting schedule:', err);
        this.closeDeleteScheduleModal();
      }
    });
  }

  closeConflictModal(): void {
    this.showConflictModal = false;
  }

  closeDeleteScheduleModal(): void {
    this.showDeleteScheduleConfirmation = false;
    this.scheduleToDelete = null;
  }

  toggleScheduleDropdown(): void {
    this.scheduleDropdownOpen = !this.scheduleDropdownOpen;
  }

  private defaultScheduleName(): string {
    const d = new Date();
    const pad = (n: number) => String(n).padStart(2, '0');
    return `Schedule ${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  private syncTasksAndPersist(): void {
    if (!this.currentSchedule) return;

    this.tasks = this.tasks.map(task => {
      const plannedTask = this.schedulingTasks.find(pt => pt.taskId === task.id);
      if (!plannedTask) return task;

      return {
        ...task,
        plannedStart: plannedTask.plannedStart,
        plannedEnd: plannedTask.plannedEnd,
        assignedStart: plannedTask.plannedStart,
        assignedEnd: plannedTask.plannedEnd,
        fixed: task.fixed ?? false,
      };
    });

    this.schedulingService
      .updateSchedule(this.currentSchedule.id, this.currentSchedule)
      .subscribe({
        next: updatedSchedule => {
          this.currentSchedule = updatedSchedule;
          this.upsertSchedule(updatedSchedule);
          this.extractScheduledTasksFromSchedule(updatedSchedule);
          this.loadTasks();
        },
        error: err => console.error('Error persisting schedule:', err)
      });
  }

  private adjustForDayBoundaries(start: Date, durationMs: number): { start: Date, end: Date } {
    let currentStart = new Date(start);

    if (currentStart.getHours() < this.startHour) {
      currentStart.setHours(this.startHour, 0, 0, 0);
    } else if (currentStart.getHours() >= this.endHour) {
      currentStart.setDate(currentStart.getDate() + 1);
      currentStart.setHours(this.startHour, 0, 0, 0);
    }

    let currentEnd = new Date(currentStart.getTime() + durationMs);


    if (currentEnd.getHours() > this.endHour || (currentEnd.getHours() === this.endHour && currentEnd.getMinutes() > 0) || currentEnd.getDate() !== currentStart.getDate()) {
      currentStart.setDate(currentStart.getDate() + 1);
      currentStart.setHours(this.startHour, 0, 0, 0);
      currentEnd = new Date(currentStart.getTime() + durationMs);
    }

    return {start: currentStart, end: currentEnd};
  }

  private findNextAvailableSlot(
    task: DisplayTask,
    requestedStart: Date,
    duration: number,
    machineId: string
  ): { start: Date; end: Date } {
    let currentStart = new Date(requestedStart);
    let currentEnd = new Date(currentStart.getTime() + duration);
    let attempts = 0;
    const maxAttempts = 20;

    while (attempts < maxAttempts) {
      attempts++;

      const overlappingTask = this.getTasksForMachine(machineId).find(other => {
        if (other.id === task.id) return false;
        return currentStart < other.plannedEnd! && other.plannedStart! < currentEnd;
      });

      if (!overlappingTask) {
        return {start: currentStart, end: currentEnd};
      }

      currentStart = new Date(overlappingTask.plannedEnd!.getTime());

      const adjusted = this.adjustForDayBoundaries(currentStart, duration);
      currentStart = adjusted.start;
      currentEnd = adjusted.end;
    }

    return {start: currentStart, end: currentEnd};
  }

  private dateToLocalDateTimeString(d: Date): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
  }

  private parseLocalDateTime(value: string | Date | null | undefined): Date | null {
    if (!value) return null;
    if (value instanceof Date) return value;

    const [datePart, timePartRaw = '00:00:00'] = value.split('T');
    const [y, m, d] = datePart.split('-').map(Number);

    const timePart = timePartRaw.length === 5 ? `${timePartRaw}:00` : timePartRaw;
    const [hh, mm, ss] = timePart.split(':').map(Number);

    return new Date(y, m - 1, d, hh || 0, mm || 0, ss || 0, 0);
  }

  private startOfLocalDay(d: Date): Date {
    const x = new Date(d);
    x.setHours(0, 0, 0, 0);
    return x;
  }

  private applyScheduleColorsToTaskList(): void {
    if (!this.currentSchedule?.plannedTasks) {
      this.tasks = this.tasks.map(t => ({
        ...t,
        color: this.colorForTaskId(t.id),
      }));
      this.applyFilter();
      return;
    }

    const conflictByTaskId = new Map<number, boolean>();
    for (const pt of this.currentSchedule.plannedTasks) {
      const taskId = Number(pt.taskId);
      if (!Number.isNaN(taskId)) {
        conflictByTaskId.set(taskId, !!pt.conflicted);
      }
    }

    this.tasks = this.tasks.map(t => {
      const taskId = Number(t.id);
      const conflicted = conflictByTaskId.get(taskId) ?? false;

      return {
        ...t,
        color: conflicted ? this.conflictColorFor() : this.colorForTaskId(taskId),
      };
    });
  }
}
