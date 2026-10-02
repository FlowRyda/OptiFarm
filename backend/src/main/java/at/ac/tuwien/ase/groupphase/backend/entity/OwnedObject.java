package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class OwnedObject extends BaseObject {
    @Column(nullable = false, name = "owner_id")
    protected Long ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", insertable = false, updatable = false)
    protected Farmer owner;

    public OwnedObject() {
    }

    public OwnedObject(Farmer owner) {
        this.owner = owner;
        this.ownerId = owner.getId();
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Farmer getOwner() {
        return owner;
    }

    public void setOwner(Farmer owner) {
        this.owner = owner;
    }
}
