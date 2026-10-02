package at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling;

import java.time.LocalDateTime;

public class GridCursor {
    private LocalDateTime start;
    private final long duration;

    public GridCursor(LocalDateTime start, long duration) {
        this.start = start;
        this.duration = duration;
    }

    public void advance(int minutes) {
        this.start = this.start.plusMinutes(minutes);
    }

    public void advanceTo(LocalDateTime newStart) {
        if (newStart != null && newStart.isAfter(this.start)) {
            this.start = newStart;
        }
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return TimeUtil.calculateDurationEnd(start, duration);
    }

    public boolean isValidWorkHours() {
        return start.getHour() >= 6 && start.getHour() < 22;
    }
}