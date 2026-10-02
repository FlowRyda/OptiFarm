import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {ToolService} from "../../../../../services/tool.service";
import {FieldService} from "../../../../../services/field.service";
import {ToolCategoryService} from "../../../../../services/tool-category.service";
import {DropdownCategory, mapToolsToDropdownCategories, Tool, ToolCategory} from "../../../../../dtos/tool";
import {SimpleFieldDto} from "../../../../../dtos/field";
import {TaskService} from "../../../../../services/task.service";
import {
  toHtmlDate,
  toHtmlDateTime,
  toBackendDateTime,
  createFixedDateTime,
  calculateTaskEndTime, roundUpHoursToQuarter, roundUpToMinuteStepForDate
} from '../../../../../utils/date.utils';
import {TaskType} from '../../../../../dtos/task';
import {SelectedCategory, SelectedItem, SelectedTool} from "../../tool-selector/tool-selector";
import {atLeastOneSelected} from "../../add-task/add-task/add-task.component";

@Component({
  selector: 'app-edit-task',
  templateUrl: './edit-task.component.html',
  styleUrls: ['./edit-task.component.scss'],
  standalone: false
})
export class EditTaskComponent implements OnInit {
  taskForm!: FormGroup;

  availableTools: Tool[] = [];
  availableCategories: ToolCategory[] = [];
  availableFields: SimpleFieldDto[] = [];

  isLoadingCategories = true;
  isLoadingTools = true;
  isLoadingFields = true;

  isSubmitting = false;
  submitError: string | null = null;
  showNightShiftHint = false;

  dropdownCategories: DropdownCategory[] = [];
  initialSelection: SelectedItem[] = [];

  selectedTools: number[] = [];
  selectedCategories: number[] = [];

  startHour = 6;
  endHour = 22;

  taskTypes = [TaskType.EMERGENCY, TaskType.HARVESTING, TaskType.PLANTING, TaskType.MAINTENANCE, TaskType.NONE];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<EditTaskComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { task: any }, // <-- base task only
    private toolService: ToolService,
    private fieldService: FieldService,
    private categoryService: ToolCategoryService,
    private taskService: TaskService,
  ) {
  }

  ngOnInit(): void {
    this.createForm();
    this.loadTools();
    this.loadFields();
    this.loadCategories();

    const selectedCategories: SelectedCategory[] = this.data.task.categories.map(c => ({
      kind: "category",
      categoryId: c.id,
      label: c.name,
      instanceId: crypto.randomUUID()
    }));

    const selectedTools: SelectedTool[] = this.data.task.tools.map(c => ({
      kind: "tool",
      categoryId: c.categoryDto.id,
      label: `${c.categoryDto.name} • ${c.name}`,
      toolId: c.id,
      instanceId: crypto.randomUUID()
    }));

    this.initialSelection = [
      ...selectedCategories,
      ...selectedTools,
    ];

    this.onSelectionChange(this.initialSelection);

    this.taskForm.markAsPristine();
    this.taskForm.markAsUntouched();

    this.taskForm.get('assignedEnd')?.disable();
    this.setupAutoSchedulingListener();

    this.setupModeValidation();
  }

  private createForm() {
    this.taskForm = this.fb.group({
        name: [this.data.task.name, Validators.required],
        field: [this.data.task.field?.id, Validators.required],
        workTimeEstimate: [this.data.task.workTimeEstimate, [Validators.required, Validators.min(0)]],
        tools: [[] as string[]],
        categories: [[] as string[]],
        windowStart: [toHtmlDate(this.data.task.windowStart)],
        windowEnd: [toHtmlDate(this.data.task.windowEnd)],

        fixed: [this.data.task.fixed],

        assignedStart: [toHtmlDateTime(this.data.task.assignedStart)],
        assignedEnd: [toHtmlDateTime(this.data.task.assignedEnd)],

        taskType: [this.data.task.taskType || TaskType.NONE, Validators.required]
      },
      {
        validators: [atLeastOneSelected('tools', 'categories')]
      });
  }

  private setupModeValidation(): void {
    const fixedCtrl = this.taskForm.get('fixed');
    if (!fixedCtrl) return;

    this.applyModeValidation(!!fixedCtrl.value);

    fixedCtrl.valueChanges.subscribe((fixed: boolean) => {
      this.applyModeValidation(!!fixed);
    });
  }

  private applyModeValidation(fixed: boolean): void {
    const windowStartCtrl = this.taskForm.get('windowStart');
    const windowEndCtrl = this.taskForm.get('windowEnd');
    const assignedStartCtrl = this.taskForm.get('assignedStart');

    if (!windowStartCtrl || !windowEndCtrl || !assignedStartCtrl) return;

    if (fixed) {
      assignedStartCtrl.setValidators([Validators.required]);
      assignedStartCtrl.enable({emitEvent: false});

      windowStartCtrl.clearValidators();
      windowEndCtrl.clearValidators();
      windowStartCtrl.setValue('', {emitEvent: false});
      windowEndCtrl.setValue('', {emitEvent: false});
      windowStartCtrl.disable({emitEvent: false});
      windowEndCtrl.disable({emitEvent: false});

    } else {
      windowStartCtrl.setValidators([Validators.required]);
      windowEndCtrl.setValidators([Validators.required]);
      windowStartCtrl.enable({emitEvent: false});
      windowEndCtrl.enable({emitEvent: false});

      assignedStartCtrl.clearValidators();
      assignedStartCtrl.disable({emitEvent: false});
    }

    assignedStartCtrl.updateValueAndValidity({emitEvent: false});
    windowStartCtrl.updateValueAndValidity({emitEvent: false});
    windowEndCtrl.updateValueAndValidity({emitEvent: false});
    this.taskForm.updateValueAndValidity({emitEvent: false});
  }

  onAssignedStartBlur() {
    setTimeout(() => {
      const rawValue = this.taskForm.get('assignedStart')?.value;
      this.correctStartTimeIfNeeded(rawValue);
    }, 1000);
  }

  private correctStartTimeIfNeeded(isoString: string | null) {
    if (!isoString) return;

    let d = new Date(isoString);

    d = roundUpToMinuteStepForDate(d, 15);

    const hour = d.getHours();
    if (hour < this.startHour) {
      d.setHours(this.startHour, 0, 0, 0);
      d = roundUpToMinuteStepForDate(d, 15);
    } else if (hour >= this.endHour) {
      d.setDate(d.getDate() + 1);
      d.setHours(this.startHour, 0, 0, 0);
      d = roundUpToMinuteStepForDate(d, 15);
    }

    const correctedIso = toHtmlDateTime(d);

    if (correctedIso !== isoString) {
      this.taskForm.get('assignedStart')?.setValue(correctedIso, {emitEvent: true});
    }

    this.calculateEndDate();

    if (new Date(isoString).getHours() < this.startHour || new Date(isoString).getHours() >= this.endHour) {
      this.showNightShiftHint = true;
      setTimeout(() => (this.showNightShiftHint = false), 3500);
    }
  }

  private setupAutoSchedulingListener() {
    const startControl = this.taskForm.get('assignedStart');
    const estimateControl = this.taskForm.get('workTimeEstimate');

    startControl?.valueChanges.subscribe(() => this.calculateEndDate());
    estimateControl?.valueChanges.subscribe(() => this.calculateEndDate());
  }

  private calculateEndDate() {
    const startVal = this.taskForm.get('assignedStart')?.value;
    const estimateVal = this.taskForm.get('workTimeEstimate')?.value;

    const endString = calculateTaskEndTime(startVal, estimateVal, this.startHour, this.endHour);
    this.taskForm.get('assignedEnd')?.setValue(endString);
  }

  private loadTools(): void {
    this.toolService.getAll().subscribe({
      next: tools => {
        this.availableTools = tools;
        this.isLoadingTools = false;
        this.dropdownCategories = mapToolsToDropdownCategories(this.availableTools, {
          includeUncategorized: true,
          uncategorizedName: 'No category',
        });
      },
      error: err => {
        console.error('Error loading tools:', err);
        this.isLoadingTools = false;
      }
    });
  }

  private loadFields(): void {
    this.fieldService.getAllFields().subscribe({
      next: fields => {
        this.availableFields = fields;
        this.isLoadingFields = false;
      },
      error: err => {
        console.error('Error loading fields:', err);
        this.isLoadingFields = false;
      }
    });
  }

  private loadCategories(): void {
    this.categoryService.getAll().subscribe({
      next: categories => {
        this.availableCategories = categories;
        this.isLoadingCategories = false;
      },
      error: err => {
        console.error('Error loading categories:', err);
        this.isLoadingCategories = false;
      }
    });
  }

  private isOutsideWorkingHours(isoString: string): boolean {
    const date = new Date(isoString);
    const minutes = date.getHours() * 60 + date.getMinutes();
    return minutes < this.startHour * 60 || minutes >= this.endHour * 60;
  }

  onSave(): void {
    if (this.taskForm.get('fixed')?.value) {
      const assignedTime = this.taskForm.get('assignedStart')?.value;
      if (assignedTime && this.isOutsideWorkingHours(assignedTime)) {
        return;
      }
    }

    if (this.taskForm.invalid) {
      Object.values(this.taskForm.controls).forEach(c => c.markAsTouched());
      return;
    }

    this.isSubmitting = true;
    this.submitError = null;

    const task = this.data.task;
    const formValue = this.taskForm.value;
    const raw = this.taskForm.getRawValue();

    const fixed = !!formValue.fixed;

    const assignedStart = toBackendDateTime(raw.assignedStart);
    const assignedEnd = toBackendDateTime(raw.assignedEnd);

    let windowStartDate: string | null = null;
    let windowEndDate: string | null = null;

    if (fixed) {
      windowStartDate = this.extractDatePart(raw.assignedStart);
      windowEndDate = this.extractDatePart(raw.assignedEnd) || windowStartDate;
    } else {
      windowStartDate = this.extractDatePart(formValue.windowStart);
      windowEndDate = this.extractDatePart(formValue.windowEnd) || windowStartDate;
    }

    const windowStart = createFixedDateTime(windowStartDate!, 6, 0);
    const windowEnd = createFixedDateTime(windowEndDate!, 22, 0);

    const tools = this.availableTools
      .filter(tool => this.selectedTools.includes(tool.id))
      .map(tool => ({id: tool.id, name: tool.name}));

    const categoryById = new Map(
      this.availableCategories
        .filter(c => c.id != null)
        .map(c => [c.id!, c] as const)
    );

    const categories = this.selectedCategories
      .map(id => categoryById.get(id))
      .filter((c): c is ToolCategory => !!c)
      .map(c => ({id: c.id, name: c.name}));

    const selectedField = this.availableFields.find(f => f.id === Number(formValue.field));
    const field = selectedField ? {id: selectedField.id, name: selectedField.name} : null;

    const payload = {
      id: task.id,
      name: formValue.name,
      workTimeEstimate: formValue.workTimeEstimate,
      field,
      tools,
      categories,
      windowStart,
      windowEnd,
      assignedStart,
      assignedEnd,
      fixed,
      taskType: formValue.taskType
    };

    console.log(payload);

    this.taskService.update(task.id, payload as any).subscribe({
      next: (result) => {
        this.isSubmitting = false;
        this.dialogRef.close({updated: true, result: result ?? payload});
      },
      error: (err) => {
        console.log(err);
        this.isSubmitting = false;
        this.submitError = err.error;
      }
    });
  }

  private extractDatePart(value: string | null | undefined): string | null {
    if (!value) return null;
    return value.substring(0, 10);
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  protected onSelectionChange($event: SelectedItem[]) {
    this.selectedTools = $event
      .filter(i => i.kind === 'tool')
      .map(i => i.toolId);

    this.selectedCategories = $event
      .filter(i => i.kind === 'category')
      .map(i => i.categoryId);

    this.taskForm.get('tools')?.setValue(this.selectedTools, {emitEvent: false});
    this.taskForm.get('categories')?.setValue(this.selectedCategories, {emitEvent: false});

    this.taskForm.updateValueAndValidity({emitEvent: false});
  }

  onWorkTimeEstimateBlur(): void {
    const ctrl = this.taskForm.get('workTimeEstimate');
    if (!ctrl) return;

    const raw = ctrl.value;

    if (raw === null || raw === undefined || raw === '') return;

    const n = Number(raw);
    if (!Number.isFinite(n)) return;

    const rounded = roundUpHoursToQuarter(n);

    if (rounded !== n) {
      ctrl.setValue(rounded, {emitEvent: true});
    }
  }
}
