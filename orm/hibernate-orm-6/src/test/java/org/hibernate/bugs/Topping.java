package org.hibernate.bugs;

import jakarta.persistence.*;

@Entity
@Table(name = "topping")
public class Topping {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Pizza pizza;

    private String name;

    // Getters and Setters
    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPizza(final Pizza pizza) {
        this.pizza = pizza;
    }

}
