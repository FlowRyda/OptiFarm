package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
public class Seed extends OwnedObject {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_type_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CropType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double buyingPrice;

    @Column(nullable = false)
    private LocalDateTime buyingTime;

    public Seed() {
    }

    public Seed(CropType type, String name, double buyingPrice, LocalDateTime buyingTime, Farmer owner) {
        super(owner);
        this.type = type;
        this.name = name;
        this.buyingPrice = buyingPrice;
        this.buyingTime = buyingTime;
    }

    public CropType getType() {
        return type;
    }

    public Seed setType(CropType type) {
        this.type = type;
        return this;
    }

    public double getBuyingPrice() {
        return buyingPrice;
    }

    public Seed setBuyingPrice(double buyingPrice) {
        this.buyingPrice = buyingPrice;
        return this;
    }

    public String getName() {
        return name;
    }

    public Seed setName(String name) {
        this.name = name;
        return this;
    }

    public LocalDateTime buyingTime() {
        return buyingTime;
    }

    public Seed setBuyingTime(LocalDateTime buyingTime) {
        this.buyingTime = buyingTime;
        return this;
    }


}
