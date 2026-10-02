package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
public class ActiveField extends OwnedObject {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seed_id")
    private Seed seed;

    @Column(nullable = false)
    private double sellingPrice;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Column(nullable = false)
    private LocalDateTime expectedHarvestDate;

    public ActiveField() {
    }

    public ActiveField(Farmer owner, Field field, Seed seed, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime expectedHarvestDate, double sellingPrice) {
        super(owner);
        this.field = field;
        this.seed = seed;
        this.startDate = startDate;
        this.endDate = endDate;
        this.expectedHarvestDate = expectedHarvestDate;
        this.sellingPrice = sellingPrice;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Seed getSeed() {
        return seed;
    }

    public void setSeed(Seed seed) {
        this.seed = seed;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    /**
     * Returns the expected harvest date.
     * If user has set a custom date, returns that.
     * Otherwise calculates from startDate + seed growth duration.
     */
    public LocalDateTime getExpectedHarvestDate() {
        if (expectedHarvestDate != null) {
            return expectedHarvestDate;
        }
        if (startDate != null && seed != null && seed.getType() != null) {
            return startDate.plusDays((long) seed.getType().getGrowthDuration());
        }
        return null;
    }

    public void setExpectedHarvestDate(LocalDateTime expectedHarvestDate) {
        this.expectedHarvestDate = expectedHarvestDate;
    }

    /**
     * Calculates growth status as percentage (0-100) based on time elapsed
     * from startDate to expectedHarvestDate.
     */
    public int getGrowthStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = this.startDate;
        LocalDateTime expected = this.getExpectedHarvestDate();

        if (expected == null || start == null) {
            return 0;
        }

        if (now.isBefore(start)) {
            return 0;
        }

        if (now.isAfter(expected)) {
            return 100;
        }

        long totalDays = ChronoUnit.DAYS.between(start, expected);
        long daysPassed = ChronoUnit.DAYS.between(start, now);

        if (totalDays <= 0) {
            return 0;
        }

        return (int) Math.min(100, (daysPassed * 100.0) / totalDays);
    }
}