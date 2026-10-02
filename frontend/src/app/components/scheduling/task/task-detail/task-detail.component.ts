import {Component, EventEmitter, Inject, Output} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Task} from "../../../../dtos/task";
import {Tool, ToolCategory} from "../../../../dtos/tool";
import {TaskService} from "../../../../services/task.service";
import {EditTaskComponent} from "../edit-task/edit-task/edit-task.component";
import {PlannedTaskService} from "../../../../services/planned-task.service";
import {formatSmartDateRange} from "../../../../utils/date.utils";
import {SelectedItem} from "../tool-selector/tool-selector";

interface ExtendedTask extends Task {
  isScheduledTask: boolean;
  taskId?: number;
}

@Component({
  selector: 'app-task-detail',
  templateUrl: './task-detail.component.html',
  styleUrls: ['./task-detail.component.scss'],
  standalone: false
})
export class TaskDetailComponent {
  @Output() deleted = new EventEmitter();
  showDeleteConfirmation = false;
  taskToDelete: Task | null = null;
  deletionMessage = 'Are you sure you want to permanently delete this task?';

  constructor(
    @Inject(MAT_DIALOG_DATA) public task: ExtendedTask,
    private taskService: TaskService,
    private plannedTaskService: PlannedTaskService,
    private dialogRef: MatDialogRef<TaskDetailComponent>,
    private dialog: MatDialog
  ) {
  }

  close() {
    this.dialogRef.close();
  }

  backAction = {
    action: () => this.close()
  };

  onEditTask(task: ExtendedTask) {
    const dialogRef = this.dialog.open(EditTaskComponent, {
      minWidth: '45vw',
      data: {task, isScheduledTask: task.isScheduledTask}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (!result) return;

      this.dialogRef.close({updated: result, isScheduledTask: task.isScheduledTask});
    });
  }

  onDeleteTask(task: Task) {
    this.taskToDelete = task;
    if (this.task.isScheduledTask) {
      this.deletionMessage = 'Are you sure you want to permanently delete this task?' +
        '\n This will delete the task from all schedules.';
    } else {
      this.deletionMessage = 'Are you sure you want to permanently delete this task?';
    }
    this.showDeleteConfirmation = true;
  }

  confirmDelete() {
    if (!this.taskToDelete) return;
    if (this.task.isScheduledTask) {
      this.plannedTaskService.delete(this.taskToDelete.id).subscribe({
        next: () => {
          console.log('Scheduled Task deleted:', this.taskToDelete);
          this.dialogRef.close({deleted: true, isScheduledTask: true});
          this.deleted.emit();
        },
        error: (err) => {
          console.error('Failed to delete scheduled task', err);
          alert('Failed to delete scheduled task. Please try again.');
        }
      })
    } else {
      this.taskService.delete(this.taskToDelete.id).subscribe({
        next: () => {
          console.log('Task deleted:', this.taskToDelete);
          this.dialogRef.close({deleted: true});
          this.deleted.emit();
        },
        error: (err) => {
          console.error('Failed to delete task', err);
          alert('Failed to delete task. Please try again.');
        }
      });
    }

    this.closeDeleteModal();
  }

  closeDeleteModal() {
    this.showDeleteConfirmation = false;
    this.taskToDelete = null;
  }

  getToolNames(tools: Tool[] | undefined): string {
    if (!tools || tools.length === 0) return 'None';
    return tools.map(tool => tool.name).join(', ');
  }

  getCategoryNames(categories: ToolCategory[] | undefined): string {
    if (!categories || categories.length === 0) return 'None';
    return categories.map(category => category.name).join(', ');
  }

  getPlannedDateString(): string {
    return formatSmartDateRange(this.task.assignedStart, this.task.assignedEnd);
  }
}
