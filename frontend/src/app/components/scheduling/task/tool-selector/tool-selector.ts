import {
  Component,
  ElementRef, EventEmitter,
  HostListener,
  Input, Output
} from '@angular/core';

import {DropdownCategory} from "../../../../dtos/tool";
import {CommonModule} from "@angular/common";

export interface Tool {
  id: number;
  name: string;
}

export interface Category {
  id: number;
  name: string;
  tools: Tool[];
}

export interface SelectedCategory {
  kind: 'category';
  categoryId: number;
  label: string;
  instanceId: string
}

export interface SelectedTool {
  kind: 'tool';
  categoryId: number;
  toolId: number;
  label: string;
  instanceId: string
}

export type SelectedItem =
  | SelectedCategory
  | SelectedTool


@Component({
  selector: 'app-tool-selector',
  imports: [
    CommonModule
  ],
  templateUrl: './tool-selector.html',
  styleUrl: './tool-selector.scss',
})
export class ToolSelector {
  @Input({required: true}) categories: DropdownCategory[] = [];
  private _selection: SelectedItem[] = [];
  @Input()
  set selection(value: SelectedItem[]) {
    this._selection = value ?? [];
    this.selected = [...this._selection];
  }

  get selection() {
    return this._selection;
  }

  @Output() selectionChange = new EventEmitter<SelectedItem[]>();

  isOpen = false;
  query = '';

  private emitSelection() {
    this.selectionChange.emit([...this.selected]);
  }


  expanded = new Map<number, boolean>();

  selected: SelectedItem[] = [];

  constructor(private host: ElementRef<HTMLElement>) {
  }

  toggleOpen() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
    }
  }

  close() {
    this.isOpen = false;
    this.query = '';
  }

  @HostListener('document:mousedown', ['$event'])
  onDocMouseDown(ev: MouseEvent) {
    if (!this.isOpen) return;
    const target = ev.target as Node;
    if (!this.host.nativeElement.contains(target)) this.close();
  }

  isExpanded(categoryId: number): boolean {
    return this.expanded.get(categoryId) ?? false;
  }

  toggleCategory(categoryId: number) {
    this.expanded.set(categoryId, !this.isExpanded(categoryId));
  }

  addCategory(category: Category) {

    const cntAddedCategory = this.selected.filter(x => x.categoryId == category.id);
    const cntToolsInCategory = category.tools.length;

    if (cntAddedCategory.length < cntToolsInCategory) {
      let c: SelectedCategory = {
        categoryId: category.id,
        label: category.name,
        kind: 'category',
        instanceId: crypto.randomUUID()
      };

      this.selected.push(c);
      console.log(this.selected)
      this.emitSelection();
    }
  }

  addTool(category: Category, tool: Tool) {

    if (this.selected.filter((x: SelectedTool) => x.toolId == tool.id).length >= 1)
      return;

    const cntAddedCategory = this.selected.filter(x => x.categoryId == category.id);
    const cntToolsInCategory = category.tools.length;

    if (cntAddedCategory.length >= cntToolsInCategory) {
      const toRemove = cntAddedCategory.filter((x: SelectedCategory) => x.kind == "category")[0];
      this.selected = this.selected.filter((s) => s.instanceId !== toRemove.instanceId);
    }

    this.selected.push({
      kind: 'tool',
      categoryId: category.id,
      toolId: tool.id,
      label: `${category.name} • ${tool.name}`,
      instanceId: crypto.randomUUID(),
    });
    console.log(this.selected);
    this.emitSelection();
  }

  isDisabledTool(tool: Tool): boolean {
    return this.selected.filter(x => x.kind == "tool" && x.toolId == tool.id).length != 0;
  }

  isVisibleCategory(category: Category): boolean {
    const cntAddedCategory = this.selected.filter(x => x.kind == "tool" && x.categoryId == category.id).length;
    const cntToolsInCategory = category.tools.length;

    return cntAddedCategory < cntToolsInCategory;
  }

  isDisabledCategory(category: Category): boolean {
    return this.selected.filter(x => x.categoryId == category.id).length == category.tools.length;
  }

  removeSelected(instanceId: string) {
    this.selected = this.selected.filter((s) => s.instanceId !== instanceId);
    this.emitSelection();
  }

  setQuery(value: string) {
    this.query = value;

    const q = value.trim().toLowerCase();

    for (const c of this.categories) {

      if (!q) {
        this.expanded.set(c.id, false);
        continue;
      }

      const catMatch = c.name.toLowerCase().includes(q);
      const toolMatch = c.tools.some((t) => t.name.toLowerCase().includes(q));
      if (catMatch || toolMatch) this.expanded.set(c.id, true);
    }
  }

  get filteredCategories(): Category[] {
    const q = this.query.trim().toLowerCase();

    if (!q) return this.categories;

    return this.categories
      .map((c) => {
        const catMatch = c.name.toLowerCase().includes(q);
        const matchingTools = c.tools.filter((t) => t.name.toLowerCase().includes(q));

        const toolsToShow = catMatch ? c.tools : matchingTools;

        return {...c, tools: toolsToShow};
      })
      .filter((c) => c.name.toLowerCase().includes(q) || c.tools.length > 0);
  }

  trackByCatId(_: number, c: Category) {
    return c.id;
  }

  trackByToolId(_: number, t: Tool) {
    return t.id;
  }
}
