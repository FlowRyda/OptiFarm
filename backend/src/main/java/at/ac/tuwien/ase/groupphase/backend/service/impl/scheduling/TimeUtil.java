package at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtil {
    private static final int WORK_START = 6;
    private static final int WORK_END = 22;

    public static LocalDateTime calculateDurationEnd(LocalDateTime start, long durationMinutes) {
        LocalDateTime ptr = start;
        long remaining = durationMinutes;

        while (remaining > 0) {
            LocalDateTime endOfDay = ptr.toLocalDate().atTime(WORK_END, 0);
            if (ptr.isAfter(endOfDay) || ptr.isEqual(endOfDay)) {
                ptr = ptr.plusDays(1).withHour(WORK_START).withMinute(0);
                endOfDay = ptr.toLocalDate().atTime(WORK_END, 0);
            }
            long avail = ChronoUnit.MINUTES.between(ptr, endOfDay);
            if (remaining <= avail) {
                return ptr.plusMinutes(remaining);
            }
            remaining -= avail;
            ptr = ptr.plusDays(1).withHour(WORK_START).withMinute(0);
        }
        return ptr;
    }

    public static boolean intervalsOverlap(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        return s1.isBefore(e2) && s2.isBefore(e1);
    }
}