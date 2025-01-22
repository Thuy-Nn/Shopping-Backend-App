package de.berlin.htw.boundary.dto;

import jakarta.validation.constraints.*;

import java.util.List;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class Order {

    @NotNull
    @Size(min = 1, max = 10, message = "Der Inhalt des Warenkorbs darf nicht mehr als 10 Artikel überschreiten")
    private List<Item> items;

    @NotNull(message = "Der Gesamtbetrag darf nicht null sein.")
    @DecimalMin(value = "10.0", message = "Der Gesamtbetrag muss mindestens 10 Euro betragen.")
    @DecimalMax(value = "10000.0", message = "Der Gesamtbetrag darf nicht mehr als 10.000 Euro betragen.")
    private Float total;
    
    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Float getTotal() {
        this.total = 0.0f;
        // calculate total
        for (Item item : items) {if (item.getCount() != null) {
            this.total += item.getCount() * item.getPrice();
        }
        }
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

}
