package at.ac.tuwien.ase.groupphase.backend.endpoint.dto.schedule;

import java.util.List;
import java.util.Objects;

public class ScheduleDto {
    private Long id;
    private String name;
    private List<PlannedTaskDto> plannedTasks;

    public ScheduleDto() {
    }

    public ScheduleDto(Long id, String name, List<PlannedTaskDto> plannedTasks) {
        this.id = id;
        this.name = name;
        this.plannedTasks = plannedTasks;
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

    public List<PlannedTaskDto> getPlannedTasks() {
        return plannedTasks;
    }

    public void setPlannedTasks(List<PlannedTaskDto> plannedTasks) {
        this.plannedTasks = plannedTasks;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScheduleDto that = (ScheduleDto) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(plannedTasks, that.plannedTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, plannedTasks);
    }

    @Override
    public String toString() {
        return "ScheduleDto{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", plannedTasks=" + plannedTasks
            + '}';
    }
}
