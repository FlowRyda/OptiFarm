package at.ac.tuwien.ase.groupphase.backend.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class ToolCategory extends OwnedObject {

    @Column(nullable = false)
    private String name;


    public ToolCategory() {
    }

    public ToolCategory(String name, Farmer owner) {
        super(owner);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ToolCategory category = (ToolCategory) o;
        return Objects.equals(name, category.name) && Objects.equals(owner, category.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, owner);
    }

    @Override
    public String toString() {
        return "ToolCategory{" + "name='" + name + '\'' + ", owner=" + owner + '}';
    }

    public static class ToolCategoryBuilder {
        private String name;
        private Farmer owner;

        private ToolCategoryBuilder() {
        }

        public static ToolCategoryBuilder builder() {
            return new ToolCategoryBuilder();
        }

        public ToolCategoryBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ToolCategoryBuilder withOwner(Farmer owner) {
            this.owner = owner;
            return this;
        }

        public ToolCategory build() {
            return new ToolCategory(name, owner);
        }
    }
}