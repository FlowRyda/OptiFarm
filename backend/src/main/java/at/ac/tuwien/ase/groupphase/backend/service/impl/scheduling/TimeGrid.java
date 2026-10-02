package at.ac.tuwien.ase.groupphase.backend.service.impl.scheduling;

import at.ac.tuwien.ase.groupphase.backend.entity.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class TimeGrid {

    private static final int SLOT_MINUTES = 15;
    private static final int WORK_START = 6;
    private static final int WORK_END = 22;
    private static final int SLOTS_PER_DAY = (WORK_END - WORK_START) * (60 / SLOT_MINUTES);

    private final LocalDateTime referenceStart;
    private final Map<Long, BitSet> fieldOccupancy = new HashMap<>();
    private final Map<Long, BitSet> toolOccupancy = new HashMap<>();
    private final Map<ToolCategory, List<Tool>> toolsByCategory;

    public TimeGrid(LocalDateTime referenceStart, int daysHorizon, List<Tool> allTools) {
        this.referenceStart = referenceStart.truncatedTo(ChronoUnit.DAYS);
        this.toolsByCategory = allTools.stream().collect(Collectors.groupingBy(Tool::getCategory));
        allTools.forEach(t -> toolOccupancy.put(t.getId(), new BitSet(daysHorizon * SLOTS_PER_DAY)));
    }

    public void occupy(PlannedTask pt) {
        setRange(pt, true);
    }

    public void release(PlannedTask pt) {
        setRange(pt, false);
    }

    private void setRange(PlannedTask pt, boolean status) {
        Range range = toRange(pt.getAssignedStart(), pt.getAssignedEnd());
        if (range == null) {
            return;
        }

        if (pt.getTask().getField() != null) {
            getBitSet(fieldOccupancy, pt.getTask().getField().getId()).set(range.startIdx, range.endIdx, status);
        }
        if (pt.getFixedEquipment() != null) {
            for (Tool t : pt.getFixedEquipment()) {
                getBitSet(toolOccupancy, t.getId()).set(range.startIdx, range.endIdx, status);
            }
        }
    }

    public boolean isSlotAvailable(Field field, List<Tool> tools, LocalDateTime start, LocalDateTime end) {
        return isFieldAvailable(field, start, end) && areToolsAvailable(tools, start, end);
    }

    public boolean isFieldAvailable(Field field, LocalDateTime start, LocalDateTime end) {
        if (field == null) {
            return true;
        }
        Range range = toRange(start, end);
        if (range == null) {
            return true;
        }
        BitSet bs = getBitSet(fieldOccupancy, field.getId());
        return bs.nextSetBit(range.startIdx) == -1 || bs.nextSetBit(range.startIdx) >= range.endIdx;
    }

    public List<Tool> findAvailableTools(
        List<ToolCategory> categories,
        List<Tool> alreadySelectedTools,
        LocalDateTime start,
        LocalDateTime end
    ) {
        if (categories == null || categories.isEmpty()) {
            return new ArrayList<>();
        }

        Range range = toRange(start, end);
        if (range == null) {
            return null;
        }

        // Hardest-first: categories with fewer usable candidates first
        List<ToolCategory> ordered = new ArrayList<>(categories);
        ordered.sort((a, b) -> Integer.compare(countUsableTools(a), countUsableTools(b)));

        List<Tool> selection = new ArrayList<>();
        Set<Long> chosenToolIds = new HashSet<>(alreadySelectedTools.stream().map(BaseObject::getId).toList());

        for (ToolCategory cat : ordered) {
            Tool chosen = pickBestAvailableTool(cat, range, chosenToolIds);
            if (chosen == null) {
                return null;
            }
            selection.add(chosen);
            chosenToolIds.add(chosen.getId());
        }

        return selection;
    }

    private int countUsableTools(ToolCategory cat) {
        List<Tool> tools = toolsByCategory.getOrDefault(cat, Collections.emptyList());
        int count = 0;
        for (Tool t : tools) {
            if (t != null && t.isUsable()) {
                count++;
            }
        }
        return count;
    }

    private Tool pickBestAvailableTool(
        ToolCategory cat,
        Range range,
        Set<Long> alreadyChosen
    ) {
        List<Tool> candidates = toolsByCategory.getOrDefault(cat, Collections.emptyList());
        if (candidates.isEmpty()) {
            return null;
        }

        Tool best = null;
        int bestLocal = Integer.MAX_VALUE;
        int bestGlobal = Integer.MAX_VALUE;
        long bestId = Long.MAX_VALUE;

        for (Tool t : candidates) {
            if (t == null) {
                continue;
            }
            if (!t.isUsable()) {
                continue;
            }
            if (alreadyChosen.contains(t.getId())) {
                continue;
            }

            BitSet bs = getBitSet(toolOccupancy, t.getId());
            if (!isFree(bs, range)) {
                continue;
            }

            int local = localBusySlots(bs, range);
            int global = bs.cardinality();
            long id = t.getId();

            if (local < bestLocal
                || (local == bestLocal && global < bestGlobal)
                || (local == bestLocal && global == bestGlobal && id < bestId)) {
                best = t;
                bestLocal = local;
                bestGlobal = global;
                bestId = id;
            }
        }

        return best;
    }

    private boolean isFree(BitSet bs, Range range) {
        int next = bs.nextSetBit(range.startIdx);
        return next == -1 || next >= range.endIdx;
    }

    /**
     * Counts how busy a tool is "around" the requested slot.
     * Uses a 1 day horizon (15min slots => 64 slots/day).
     */
    private int localBusySlots(BitSet bs, Range range) {
        final int slotsPerDay = SLOTS_PER_DAY;
        int from = range.startIdx - slotsPerDay;
        if (from < 0) {
            from = 0;
        }
        int to = range.endIdx + slotsPerDay;

        // BitSet.get(from,to) safely handles "to" beyond current size.
        return bs.get(from, to).cardinality();
    }

    public boolean areToolsAvailable(List<Tool> tools, LocalDateTime start, LocalDateTime end) {
        Range range = toRange(start, end);
        if (range == null) {
            return true;
        }
        for (Tool t : tools) {
            if (t == null) {
                return false;
            }
            if (!t.isUsable()) {
                return false;
            }
            BitSet bs = getBitSet(toolOccupancy, t.getId());
            int next = bs.nextSetBit(range.startIdx);
            if (next != -1 && next < range.endIdx) {
                return false;
            }
        }
        return true;
    }

    public List<PlannedTask> findBlockers(Field field, List<Tool> specificTools, List<ToolCategory> categories,
                                          LocalDateTime start, LocalDateTime end, List<PlannedTask> candidates) {
        // filter relevant candidates (range & !Conflicted)
        List<PlannedTask> overlapping = candidates.stream()
            .filter(pt -> !pt.isConflicted())
            .filter(pt -> TimeUtil.intervalsOverlap(start, end, pt.getAssignedStart(), pt.getAssignedEnd()))
            .collect(Collectors.toList());

        Set<PlannedTask> identifiedBlockers = new HashSet<>();

        // field conflicts (strict: 1 field can only have 1 task at a time)
        if (field != null) {
            overlapping.stream()
                .filter(pt -> pt.getTask().getField() != null
                    && pt.getTask().getField().getId().equals(field.getId()))
                .forEach(identifiedBlockers::add);
        }

        // specific tool conflicts (if the specific tool is in use => blocker)
        if (specificTools != null && !specificTools.isEmpty()) {
            Set<Long> requiredIds = specificTools.stream().map(Tool::getId).collect(Collectors.toSet());

            overlapping.stream()
                .filter(pt -> pt.getFixedEquipment().stream()
                    .anyMatch(t -> requiredIds.contains(t.getId())))
                .forEach(identifiedBlockers::add);
        }

        // category conflicts => capacity based
        if (categories != null && !categories.isEmpty()) {
            // count required per category
            Map<Long, Long> requiredCounts = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getId(), Collectors.counting()));

            for (Map.Entry<Long, Long> entry : requiredCounts.entrySet()) {
                Long catId = entry.getKey();
                long needed = entry.getValue();

                ToolCategory catObj = categories.stream()
                    .filter(c -> c.getId().equals(catId))
                    .findFirst()
                    .orElse(null);

                if (catObj != null) {
                    // Check availability
                    int available = countAvailableTools(catObj, start, end);

                    if (available < needed) {
                        // bottleneck detected
                        overlapping.stream()
                            .filter(pt -> pt.getFixedEquipment().stream()
                                .anyMatch(t -> t.getCategory().getId().equals(catId)))
                            .forEach(identifiedBlockers::add);
                    }
                    // If available >= needed: no conflict
                }
            }
        }

        return new ArrayList<>(identifiedBlockers);

    }

    public List<PlannedTask> findBlockersOld(Field field, List<Tool> specificTools, List<ToolCategory> categories,
                                          LocalDateTime start, LocalDateTime end, List<PlannedTask> candidates) {
        return candidates.stream()
            .filter(pt -> !pt.isConflicted())
            .filter(pt -> {
                if (!TimeUtil.intervalsOverlap(start, end, pt.getAssignedStart(), pt.getAssignedEnd())) {
                    return false;
                }

                if (field != null && pt.getTask().getField() != null) {
                    if (pt.getTask().getField().getId().equals(field.getId())) {
                        return true;
                    }
                }

                List<Tool> blockerTools = pt.getFixedEquipment();
                if (blockerTools == null || blockerTools.isEmpty()) {
                    return false;
                }

                if (specificTools != null && !specificTools.isEmpty()) {
                    boolean hasConflict = blockerTools.stream().anyMatch(bt ->
                        specificTools.stream().anyMatch(st -> st.getId().equals(bt.getId()))
                    );
                    if (hasConflict) {
                        return true;
                    }
                }

                if (categories != null && !categories.isEmpty()) {
                    boolean hasConflict = blockerTools.stream().anyMatch(bt ->
                        categories.contains(bt.getCategory())
                    );
                    return hasConflict;
                }

                return false;
            })
            .collect(Collectors.toList());
    }

    public int countAvailableTools(ToolCategory category, LocalDateTime start, LocalDateTime end) {
        if (category == null) {
            return 0;
        }

        // compute range
        Range range = toRange(start, end);
        if (range == null) {
            return 0;
        }

        List<Tool> tools = toolsByCategory.getOrDefault(category, Collections.emptyList());
        int count = 0;

        for (Tool t : tools) {
            if (t != null && t.isUsable()) {
                BitSet bs = getBitSet(toolOccupancy, t.getId());
                // check availability for specific tool
                if (isFree(bs, range)) {
                    count++;
                }
            }
        }
        return count;
    }

    private BitSet getBitSet(Map<Long, BitSet> map, Long id) {
        return map.computeIfAbsent(id, k -> new BitSet());
    }

    private Range toRange(LocalDateTime start, LocalDateTime end) {
        int s = toIndex(start);
        int e = toIndex(end);
        if (s == -1 || e == -1 || s >= e) {
            return null;
        }
        return new Range(s, e);
    }

    private int toIndex(LocalDateTime time) {
        long days = ChronoUnit.DAYS.between(referenceStart.toLocalDate(), time.toLocalDate());
        if (days < 0) {
            return -1;
        }
        int hour = time.getHour();
        int minute = time.getMinute();

        if (hour < WORK_START) {
            return (int) days * SLOTS_PER_DAY;
        }
        if (hour >= WORK_END) {
            return (int) (days + 1) * SLOTS_PER_DAY;
        }

        int slotsIntoDay = ((hour - WORK_START) * (60 / SLOT_MINUTES)) + (minute / SLOT_MINUTES);
        return (int) (days * SLOTS_PER_DAY) + slotsIntoDay;
    }

    private record Range(int startIdx, int endIdx) {
    }
}