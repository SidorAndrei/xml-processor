package org.personal.models;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XStreamAlias("product")
public class Product {
    @XStreamAlias("description")
    private String description;
    @XStreamAlias("gtin")
    private String gtin;
    @XStreamAlias("price")
    private Price price;
    @XStreamAlias("supplier")
    private String supplier;

    @XStreamAlias("orderid")
    private Integer orderID;

    @Override
    public String toString() {
        return "Product{" +
                "description='" + description + '\'' +
                ", gtin='" + gtin + '\'' +
                ", price=" + price +
                ", supplier='" + supplier + '\'' +
                '}';
    }
}
