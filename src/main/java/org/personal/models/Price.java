package org.personal.models;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XStreamAlias("price")
@XStreamConverter(value = ToAttributedValueConverter.class,strings = "value")
public class Price {
    private String currency;
    private Float value;

    @Override
    public String toString() {
        return "Price{" +
                "currency='" + currency + '\'' +
                ", value=" + value +
                '}';
    }
}
