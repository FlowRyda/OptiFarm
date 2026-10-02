package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
public class Task extends OwnedObject {
    @Column(nullable = false)
    private String name;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private LocalDateTime assignedStart;
    private LocalDateTime assignedEnd;
    @ManyToOne
    private Field field;
    @ManyToMany
    @JoinTable(
        name = "task_tool",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tool_id")
    )
    private List<Tool> tools = new java.util.ArrayList<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "task_tool_category",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<ToolCategory> categories = new java.util.ArrayList<>();
    @Column(nullable = false)
    private float workTimeEstimate;
    @Column(nullable = false)
    private boolean fixed;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType taskType = TaskType.NONE;

    public Task() {
    }

    public Task(String name,
                LocalDateTime windowStart,
                LocalDateTime windowEnd,
                LocalDateTime assignedStart,
                LocalDateTime assignedEnd,
                Field field, List<Tool> tools,
                List<ToolCategory> categories,
                float workTimeEstimate,
                boolean fixed,
                TaskType taskType
    ) {
        this.name = name;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.assignedStart = assignedStart;
        this.assignedEnd = assignedEnd;
        this.field = field;
        this.tools = (tools != null) ? new java.util.ArrayList<>(tools) : new java.util.ArrayList<>();
        this.categories = (categories != null) ? new java.util.ArrayList<>(categories) : new java.util.ArrayList<>();
        this.workTimeEstimate = workTimeEstimate;
        this.fixed = fixed;
        this.taskType = taskType != null ? taskType : TaskType.NONE;
    }

    public Task(Farmer owner,
                String name,
                LocalDateTime windowStart,
                LocalDateTime windowEnd,
                LocalDateTime assignedStart,
                LocalDateTime assignedEnd,
                Field field, List<Tool> tools,
                List<ToolCategory> categories,
                float workTimeEstimate,
                boolean fixed,
                TaskType taskType
    ) {
        super(owner);
        this.name = name;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.assignedStart = assignedStart;
        this.assignedEnd = assignedEnd;
        this.field = field;
        this.tools = (tools != null) ? new java.util.ArrayList<>(tools) : new java.util.ArrayList<>();
        this.categories = (categories != null) ? new java.util.ArrayList<>(categories) : new java.util.ArrayList<>();
        this.workTimeEstimate = workTimeEstimate;
        this.fixed = fixed;
        this.taskType = taskType != null ? taskType : TaskType.NONE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public void setTools(List<Tool> tools) {
        this.tools.clear();
        if (tools != null) {
            this.tools.addAll(tools);
        }
    }

    public List<ToolCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<ToolCategory> categories) {
        this.categories.clear();
        if (categories != null) {
            this.categories.addAll(categories);
        }
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

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType != null ? taskType : TaskType.NONE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return Float.compare(workTimeEstimate, task.workTimeEstimate) == 0 && Objects.equals(name, task.name) && Objects.equals(windowStart, task.windowStart)
            && Objects.equals(windowEnd, task.windowEnd) && Objects.equals(assignedStart, task.assignedStart) && Objects.equals(assignedEnd, task.assignedEnd)
            && Objects.equals(field, task.field) && Objects.equals(tools, task.tools) && Objects.equals(categories, task.categories) && Objects.equals(fixed, task.fixed)
            && taskType == task.taskType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, windowStart, windowEnd, assignedStart, assignedEnd, field, tools, categories, workTimeEstimate, fixed, taskType);
    }

    @Override
    public String toString() {
        return "Task{" + "name='" + name + '\''
            + ", windowStart=" + windowStart
            + ", windowEnd=" + windowEnd
            + ", assignedStart=" + assignedStart
            + ", assignedEnd=" + assignedEnd
            + ", field=" + field
            + ", tools=" + tools
            + ", categories=" + categories
            + ", workTimeEstimate=" + workTimeEstimate
            + ", fixed=" + fixed
            + ", taskType=" + taskType
            + '}';
    }
}