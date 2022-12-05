package org.example.models;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("products")
@Getter
public class SupplierProducts {

    @XStreamOmitField
    private final String supplier;

    @XStreamImplicit
    private final List<Product> products;


    public SupplierProducts(String supplier) {
        this.supplier = supplier;
        this.products = new ArrayList<>();
    }

    public void addProduct(Product product) {
        products.add(product);
    }
}
