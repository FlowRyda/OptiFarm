package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class PlannedTaskUpdateDto {
    private Long id;
    private String name;
    private LocalDateTime assignedStart;
    private LocalDateTime assignedEnd;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private List<Long> toolIds;
    private List<Long> categoryIds;
    private boolean fixed;
    private Long fieldId;
    private float workTimeEstimate;

    public PlannedTaskUpdateDto() {
    }

    public PlannedTaskUpdateDto(Long id, String name, LocalDateTime assignedStart, LocalDateTime assignedEnd,
                                List<Long> toolIds, boolean fixed, Long fieldId,
                                float workTimeEstimate, LocalDateTime windowStart,
                                LocalDateTime windowEnd, List<Long> categoryIds) {
        this.id = id;
        this.name = name;
        this.assignedStart = assignedStart;
        this.assignedEnd = assignedEnd;
        this.toolIds = toolIds;
        this.fixed = fixed;
        this.fieldId = fieldId;
        this.workTimeEstimate = workTimeEstimate;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.categoryIds = categoryIds;
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

    public List<Long> getToolIds() {
        return toolIds;
    }

    public void setToolIds(List<Long> toolIds) {
        this.toolIds = toolIds;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public float getWorkTimeEstimate() {
        return workTimeEstimate;
    }

    public void setWorkTimeEstimate(float workTimeEstimate) {
        this.workTimeEstimate = workTimeEstimate;
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

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlannedTaskUpdateDto that = (PlannedTaskUpdateDto) o;
        return fixed == that.fixed && Float.compare(workTimeEstimate, that.workTimeEstimate) == 0
            && Objects.equals(id, that.id) && Objects.equals(name, that.name)
            && Objects.equals(assignedStart, that.assignedStart) && Objects.equals(assignedEnd, that.assignedEnd)
            && Objects.equals(toolIds, that.toolIds) && Objects.equals(fieldId, that.fieldId)
            && Objects.equals(windowStart, that.windowStart) && Objects.equals(windowEnd, that.windowEnd)
            && Objects.equals(categoryIds, that.categoryIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, assignedStart, assignedEnd, toolIds, fixed, fieldId, workTimeEstimate, windowStart, windowEnd, categoryIds);
    }

    @Override
    public String toString() {
        return "PlannedTaskDto{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", assignedStart=" + assignedStart
            + ", assignedEnd=" + assignedEnd
            + ", fixedEquipmentIds=" + toolIds
            + ", fixed=" + fixed
            + ", fieldId=" + fieldId
            + ", workTimeEstimate=" + workTimeEstimate
            + ", windowStart=" + windowStart
            + ", windowEnd=" + windowEnd
            + ", toolCategoryIds=" + categoryIds
            + '}';
    }
}
