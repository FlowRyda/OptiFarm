package at.ac.tuwien.ase.groupphase.backend.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

//already harvested
@Entity
public class Crop extends OwnedObject {
    @Column(nullable = false)
    private double currentPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_type_id")
    private CropType type;
    @Column(nullable = false)

    private double quantity;
    @Column(nullable = false)

    private LocalDateTime harvestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Field field;


    public Crop() {
    }

    public Crop(double currentPrice, CropType type, double quantity, LocalDateTime harvestDate, Farmer owner, Field field) {
        super(owner);
        this.currentPrice = currentPrice;
        this.type = type;
        this.quantity = quantity;
        this.harvestDate = harvestDate;
        this.field = field;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public CropType getType() {
        return type;
    }

    public void setType(CropType type) {
        this.type = type;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(LocalDateTime harvestDate) {
        this.harvestDate = harvestDate;
    }

    public Field getFieldId() {
        return field;
    }

    public void setFieldId(Field field) {
        this.field = field;
    }
}
