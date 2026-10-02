package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class Schedule extends OwnedObject {
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlannedTask> plannedTasks;
    @Column(nullable = false)
    private String name;

    public List<PlannedTask> getPlannedTasks() {
        return plannedTasks;
    }

    public void setPlannedTasks(List<PlannedTask> plannedTasks) {
        this.plannedTasks = plannedTasks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
