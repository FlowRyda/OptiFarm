import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {MatDialogRef} from '@angular/material/dialog';
import {ToolService} from "../../../../../services/tool.service";
import {FieldService} from "../../../../../services/field.service";
import {DropdownCategory, mapToolsToDropdownCategories, Tool, ToolCategory} from "../../../../../dtos/tool";
import {SimpleFieldDto} from "../../../../../dtos/field";
import {ToolCategoryService} from "../../../../../services/tool-category.service";
import {TaskService} from "../../../../../services/task.service";
import {
  toHtmlDateTime,
  toBackendDateTime,
  createFixedDateTime,
  calculateTaskEndTime, roundUpHoursToQuarter, roundUpToMinuteStepForDate
} from '../../../../../utils/date.utils';
import {TaskType} from '../../../../../dtos/task';
import {SelectedItem} from "../../tool-selector/tool-selector";

export function atLeastOneSelected(toolsKey: string, categoriesKey: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const tools = group.get(toolsKey)?.value as string[] | null;
    const categories = group.get(categoriesKey)?.value as string[] | null;

    const hasTools = Array.isArray(tools) && tools.length > 0;
    const hasCategories = Array.isArray(categories) && categories.length > 0;

    return (hasTools || hasCategories) ? null : {atLeastOne: true};
  };
}

@Component({
  selector: 'app-add-task',
  templateUrl: './add-task.component.html',
  styleUrls: ['./add-task.component.scss'],
  standalone: false
})
class AddTaskComponent implements OnInit {
  taskForm: FormGroup;
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

  selectedTools: number[] = [];
  selectedCategories: number[] = [];

  startHour = 6;
  endHour = 22;

  taskTypes = [TaskType.EMERGENCY, TaskType.HARVESTING, TaskType.PLANTING, TaskType.MAINTENANCE, TaskType.NONE];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddTaskComponent>,
    private toolService: ToolService,
    private fieldService: FieldService,
    private categoryService: ToolCategoryService,
    private taskService: TaskService,
  ) {
    this.taskForm = this.fb.group({
        name: ['', Validators.required],
        field: ['', Validators.required],
        workTimeEstimate: ['', [Validators.required, Validators.min(0)]],
        tools: [[] as string[]],
        categories: [[] as string[]],
        windowStart: [''],
        windowEnd: [''],
        fixed: [false],
        assignedStart: [''],
        assignedEnd: [''],
        taskType: [TaskType.NONE, Validators.required],
      },
      {
        validators: [atLeastOneSelected('tools', 'categories')]
      });
  }

  ngOnInit(): void {
    this.loadTools();
    this.loadFields();
    this.loadCategories();

    this.taskForm.get('assignedEnd')?.disable();
    this.setupAutoSchedulingListener();

    this.taskForm.get('tools')?.disable();

    this.setupModeValidation();
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
      assignedStartCtrl.setValue('', {emitEvent: false});
      this.taskForm.get('assignedEnd')?.setValue('', {emitEvent: false});
      assignedStartCtrl.disable({emitEvent: false});
    }

    assignedStartCtrl.updateValueAndValidity({emitEvent: false});
    windowStartCtrl.updateValueAndValidity({emitEvent: false});
    windowEndCtrl.updateValueAndValidity({emitEvent: false});
    this.taskForm.updateValueAndValidity({emitEvent: false});
  }

  onAssignedStartBlur() {
    console.log("blur event triggered");
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
      next: (tools) => {
        this.availableTools = tools.filter(t => t.usable === true);
        this.isLoadingTools = false;
        this.dropdownCategories = mapToolsToDropdownCategories(this.availableTools, {
          includeUncategorized: true,
          uncategorizedName: 'No category',
        });
      },
      error: (error) => {
        console.error('Error loading tools:', error);
        this.isLoadingTools = false;
      }
    });
  }

  private loadFields(): void {
    this.fieldService.getAllFields().subscribe({
      next: (fields) => {
        this.availableFields = fields;
        this.isLoadingFields = false;
      },
      error: (error) => {
        console.error('Error loading fields:', error);
        this.isLoadingFields = false;
      }
    });
  }

  private loadCategories(): void {
    this.categoryService.getAll().subscribe({
      next: (categories) => {
        this.availableCategories = categories;
        this.isLoadingCategories = false;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
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
    // prevent submit if assigned time is outside working hours, to make time for the shownNightShiftHint -> user is notified
    const assignedTime = this.taskForm.get('assignedStart')?.value;
    if (assignedTime && this.isOutsideWorkingHours(assignedTime)) {
      return;
    }
    if (this.taskForm.invalid) {
      Object.keys(this.taskForm.controls).forEach(key =>
        this.taskForm.get(key)?.markAsTouched()
      );
      return;
    }

    const formValue = this.taskForm.value;
    const fixed = formValue.fixed;

    let assignedStart = fixed ? toBackendDateTime(formValue.assignedStart) : null;

    if (fixed && formValue.assignedStart) {
      let dateObj = new Date(formValue.assignedStart);
      if (dateObj.getHours() < this.startHour || dateObj.getHours() >= this.endHour) {
        if (dateObj.getHours() < this.startHour) {
          dateObj.setHours(this.startHour, 0, 0, 0);
        } else {
          dateObj.setDate(dateObj.getDate() + 1);
          dateObj.setHours(this.startHour, 0, 0, 0);
        }
        assignedStart = toBackendDateTime(toHtmlDateTime(dateObj));
      }
    }

    const rawAssignedEndValue = this.taskForm.getRawValue().assignedEnd;
    const assignedEnd = fixed ? toBackendDateTime(rawAssignedEndValue) : null;

    let windowStartDate: string | null = null;
    let windowEndDate: string | null = null;

    if (fixed) {
      windowStartDate = this.extractDatePart(formValue.assignedStart);
      windowEndDate = this.extractDatePart(rawAssignedEndValue) || windowStartDate;
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
      name: formValue.name,
      workTimeEstimate: formValue.workTimeEstimate,
      field,
      tools,
      categories,
      windowStart,
      windowEnd,
      fixed,
      assignedStart,
      assignedEnd,
      taskType: formValue.taskType
    };

    this.isSubmitting = true;
    this.submitError = null;

    this.taskService.create(payload as any).subscribe({
      next: (created) => {
        this.isSubmitting = false;
        this.dialogRef.close({created});
      },
      error: (err) => {
        this.isSubmitting = false;
        console.error('Error creating task:', err.error);
        this.submitError = err.error?.message ?? err.error ?? 'Validation failed.';
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

export default AddTaskComponent
