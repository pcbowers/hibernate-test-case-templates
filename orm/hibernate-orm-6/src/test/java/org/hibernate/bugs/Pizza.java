package org.hibernate.bugs;

import jakarta.persistence.*;
import org.hibernate.annotations.CurrentTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pizza")
public class Pizza {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "pizza", cascade = CascadeType.ALL)
    private List<Topping> toppings = new ArrayList<>();

    // The main reason flush fails is because Pizza has a generated property
    @CurrentTimestamp
    private ZonedDateTime lastUpdated;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public List<Topping> getToppings() {
        return toppings;
    }

    public void setToppings(final List<Topping> toppings) {
        this.toppings = toppings;
    }

}
