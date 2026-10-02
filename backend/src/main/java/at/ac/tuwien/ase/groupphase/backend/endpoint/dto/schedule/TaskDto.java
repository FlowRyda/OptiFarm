package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class TaskDto {
    private Long id;
    private String name;
    private FieldDetailDto field;
    private List<ToolDto> tools;
    private List<ToolCategoryDto> categories;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private LocalDateTime assignedStart;
    private LocalDateTime assignedEnd;
    private float workTimeEstimate;
    private boolean fixed;
    private String taskType;

    public TaskDto() {
    }

    public TaskDto(Long id,
                   String name,
                   FieldDetailDto field,
                   List<ToolDto> tools,
                   List<ToolCategoryDto> categories,
                   LocalDateTime windowStart,
                   LocalDateTime windowEnd,
                   LocalDateTime assignedStart,
                   LocalDateTime assignedEnd,
                   float workTimeEstimate,
                   boolean fixed,
                   String taskType
    ) {
        this.id = id;
        this.name = name;
        this.field = field;
        this.tools = tools;
        this.categories = categories;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.assignedStart = assignedStart;
        this.assignedEnd = assignedEnd;
        this.workTimeEstimate = workTimeEstimate;
        this.fixed = fixed;
        this.taskType = taskType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FieldDetailDto getField() {
        return field;
    }

    public void setField(FieldDetailDto field) {
        this.field = field;
    }

    public List<ToolDto> getTools() {
        return tools;
    }

    public void setTools(List<ToolDto> tools) {
        this.tools = tools;
    }

    public List<ToolCategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<ToolCategoryDto> categories) {
        this.categories = categories;
    }

    public LocalDateTime getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(LocalDateTime windowStart) {
        this.windowStart = windowStart;
    }

    public LocalDateTime getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(LocalDateTime windowEnd) {
        this.windowEnd = windowEnd;
    }

    public LocalDateTime getAssignedStart() {
        return assignedStart;
    }

    public void setAssignedStart(LocalDateTime assignedStart) {
        this.assignedStart = assignedStart;
    }

    public LocalDateTime getAssignedEnd() {
        return assignedEnd;
    }

    public void setAssignedEnd(LocalDateTime assignedEnd) {
        this.assignedEnd = assignedEnd;
    }

    public float getWorkTimeEstimate() {
        return workTimeEstimate;
    }

    public void setWorkTimeEstimate(float workTimeEstimate) {
        this.workTimeEstimate = workTimeEstimate;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskDto taskDto = (TaskDto) o;
        return Float.compare(workTimeEstimate, taskDto.workTimeEstimate) == 0 && Objects.equals(id, taskDto.id) && Objects.equals(name, taskDto.name)
            && Objects.equals(field, taskDto.field) && Objects.equals(tools, taskDto.tools) && Objects.equals(categories, taskDto.categories)
            && Objects.equals(windowStart, taskDto.windowStart) && Objects.equals(windowEnd, taskDto.windowEnd) && Objects.equals(assignedStart, taskDto.assignedStart)
            && Objects.equals(assignedEnd, taskDto.assignedEnd) && Objects.equals(fixed, taskDto.fixed)
            && Objects.equals(taskType, taskDto.taskType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, field, tools, categories, windowStart, windowEnd, assignedStart, assignedEnd, workTimeEstimate, fixed, taskType);
    }

    @Override
    public String toString() {
        return "TaskDto{" + "id=" + id
            + ", name='" + name + '\''
            + ", field=" + field
            + ", tools=" + tools
            + ", categories=" + categories
            + ", windowStart=" + windowStart
            + ", windowEnd=" + windowEnd
            + ", assignedStart=" + assignedStart
            + ", assignedEnd=" + assignedEnd
            + ", workTimeEstimate=" + workTimeEstimate
            + ", fixed=" + fixed
            + ", taskType=" + taskType
            + '}';
    }
}
