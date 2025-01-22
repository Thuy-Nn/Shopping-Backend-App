package de.berlin.htw.entity.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "ORD")
public class OrdersEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USERID", nullable = false)
    private UserEntity user;

    @Column(name = "TOTAL", nullable = false)
    private Float total;

    @Column(name = "ITEMS",nullable = false)
    private String items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {

        this.items = items;
    }

}
