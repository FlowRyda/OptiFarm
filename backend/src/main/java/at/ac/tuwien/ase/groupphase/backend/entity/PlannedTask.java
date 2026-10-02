package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class PlannedTask extends OwnedObject {
    private LocalDateTime assignedStart;
    private LocalDateTime assignedEnd;
    @ManyToOne
    private Task task;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "plannedTask_tool",
        joinColumns = @JoinColumn(name = "plannedTask_id"),
        inverseJoinColumns = @JoinColumn(name = "tool_id")
    )
    private List<Tool> fixedEquipment;
    private boolean fixed;
    private boolean conflicted;
    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    public PlannedTask() {
    }

    public PlannedTask(LocalDateTime assignedStart, LocalDateTime assignedEnd, Task task, List<Tool> fixedEquipment, boolean fixed, boolean conflicted, Schedule schedule) {
        this.assignedStart = assignedStart;
        this.assignedEnd = assignedEnd;
        this.task = task;
        this.fixedEquipment = fixedEquipment;
        this.fixed = fixed;
        this.conflicted = conflicted;
        this.schedule = schedule;
    }

    public boolean isConflicted() {
        return conflicted;
    }

    public void setConflicted(boolean conflicted) {
        this.conflicted = conflicted;
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

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<Tool> getFixedEquipment() {
        return fixedEquipment;
    }

    public void setFixedEquipment(List<Tool> fixedEquipment) {
        this.fixedEquipment = fixedEquipment;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}
