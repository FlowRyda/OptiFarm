package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolCategoryDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.ToolDto;
import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.field.FieldDetailDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class PlannedTaskDto {
    private Long id;
    private String name;
    private LocalDateTime assignedStart;
    private LocalDateTime assignedEnd;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private List<ToolDto> fixedEquipment;
    private boolean fixed;
    private ScheduleDto schedule;
    private FieldDetailDto field;
    private List<ToolCategoryDto> categories;

    private float workTimeEstimate;

    private Long taskId;
    private boolean conflicted;

    public PlannedTaskDto() {
    }

    public PlannedTaskDto(Long id, String name, LocalDateTime assignedStart, LocalDateTime assignedEnd,
                          List<ToolDto> fixedEquipment, boolean fixed, ScheduleDto schedule, FieldDetailDto field,
                          float workTimeEstimate, Long taskId, boolean conflicted, LocalDateTime windowStart,
                          LocalDateTime windowEnd, List<ToolCategoryDto> categories) {
        this.id = id;
        this.name = name;
        this.assignedStart = assignedStart;
        this.assignedEnd = assignedEnd;
        this.fixedEquipment = fixedEquipment;
        this.fixed = fixed;
        this.schedule = schedule;
        this.field = field;
        this.workTimeEstimate = workTimeEstimate;
        this.taskId = taskId;
        this.conflicted = conflicted;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.categories = categories;
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

    public List<ToolDto> getFixedEquipment() {
        return fixedEquipment;
    }

    public void setFixedEquipment(List<ToolDto> fixedEquipment) {
        this.fixedEquipment = fixedEquipment;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public ScheduleDto getSchedule() {
        return schedule;
    }

    public void setSchedule(ScheduleDto schedule) {
        this.schedule = schedule;
    }

    public FieldDetailDto getField() {
        return field;
    }

    public void setField(FieldDetailDto field) {
        this.field = field;
    }

    public float getWorkTimeEstimate() {
        return workTimeEstimate;
    }

    public void setWorkTimeEstimate(float workTimeEstimate) {
        this.workTimeEstimate = workTimeEstimate;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public boolean isConflicted() {
        return conflicted;
    }

    public void setConflicted(boolean conflicted) {
        this.conflicted = conflicted;
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

    public List<ToolCategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<ToolCategoryDto> categories) {
        this.categories = categories;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlannedTaskDto that = (PlannedTaskDto) o;
        return fixed == that.fixed && Float.compare(workTimeEstimate, that.workTimeEstimate) == 0 && Objects.equals(id, that.id)
            && Objects.equals(name, that.name) && Objects.equals(assignedStart, that.assignedStart) && Objects.equals(assignedEnd, that.assignedEnd)
            && Objects.equals(fixedEquipment, that.fixedEquipment) && Objects.equals(schedule, that.schedule) && Objects.equals(field, that.field)
            && Objects.equals(taskId, that.taskId) && conflicted == that.conflicted && Objects.equals(windowStart, that.windowStart)
            && Objects.equals(windowEnd, that.windowEnd) && Objects.equals(categories, that.categories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, assignedStart, assignedEnd, fixedEquipment, fixed, schedule, field, workTimeEstimate, taskId, conflicted, windowStart, windowEnd, categories);
    }

    @Override
    public String toString() {
        return "PlannedTaskDto{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", assignedStart=" + assignedStart
            + ", assignedEnd=" + assignedEnd
            + ", fixedEquipment=" + fixedEquipment
            + ", fixed=" + fixed
            + ", schedule=" + schedule
            + ", field=" + field
            + ", workTimeEstimate=" + workTimeEstimate
            + ", taskId=" + taskId
            + ", conflicted=" + conflicted
            + ", windowStart=" + windowStart
            + ", windowEnd=" + windowEnd
            + ", toolCategory=" + categories
            + '}';
    }
}
