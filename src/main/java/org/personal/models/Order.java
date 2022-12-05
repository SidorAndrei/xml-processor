package org.personal.models;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XStreamAlias("order")
public class Order {
    @XStreamAsAttribute
    private LocalDateTime created;
    @XStreamAsAttribute
    private Integer ID;
    @XStreamImplicit
    private List<Product> products = new ArrayList<>();

    @Override
    public String toString() {
        return "Order{" +
                "created='" + created + '\'' +
                ", ID=" + ID +
                ", products=" + products +
                '}';
    }
}
