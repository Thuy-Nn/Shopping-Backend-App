package de.berlin.htw.boundary.dto;

import jakarta.validation.bootstrap.*;
import jakarta.validation.constraints.*;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class Item {

	@NotNull (message = "Der Artikelname darf nicht null sein.")
	@Size(max = 255, message = "Der Artikelname darf nicht länger als 255 Zeichen sein.")
    private String productName;

	@NotNull
	@Pattern(regexp = "\\d-\\d-\\d-\\d-\\d-\\d", message = "Die Artikelnummer besteht aus 6 Zahlen, die durch ein Bindestrich getrennt sind (Beispiel: '1-2-3-4-5-6')")
    private String productId;

	@NotNull
	@Min(value = 1, message = "Die Anzahl muss mindestens 1 sein")
    private Integer count;

	@NotNull
	@DecimalMin(value = "10.0", message = "Der Preis muss mindestens 10 Euro betragen.")
	@DecimalMax(value = "100.0", message = "Der Preis darf hoechstens 100 Euro betragen.")
    private Float price;

    public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public Integer getCount() {
        return count;
    }

    public void setCount(final Integer count) {
        this.count = count;
    }
    
	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

}
