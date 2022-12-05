package org.example.service;

import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import org.junit.jupiter.api.Test;
import org.personal.models.*;
import org.personal.service.SerializationService;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

class SerializationServiceTest {
    @Test
    public void serialize_ordersObject_returnsCompactXmlString() {
        SerializationService<Orders> service = new SerializationService<>();
        Order order = new Order(
                LocalDateTime.of(LocalDate.of(2022,10,25), LocalTime.of(20,20)),
                2,
                List.of(
                        new Product("product1", "asdas", new Price("USD", 25.5F), "supplier1", 1),
                        new Product("product2", "asdas", new Price("USD", 25.5F), "supplier2", 2),
                        new Product("product3", "asdas", new Price("USD", 25.5F), "supplier3", 1)
                        ));
        Order order2 = new Order(
                LocalDateTime.of(LocalDate.of(2022,10,25), LocalTime.of(23,30)),
                5,
                List.of(
                        new Product("product1", "asdas", new Price("USD", 25.5F), "supplier1", 1),
                        new Product("product2", "asdas", new Price("USD", 25.5F), "supplier2", 2),
                        new Product("product3", "asdas", new Price("USD", 25.5F), "supplier3", 1)
                        ));
        Orders orders = new Orders(List.of(order,order2));
        String expected = "<?xml version=\"1.0\" ?><orders><order created=\"2022-10-25T20:20:00\" ID=\"2\">" +
                "<product><description>product1</description><gtin>asdas</gtin><price currency=\"USD\">25.5</price>" +
                "<supplier>supplier1</supplier><orderid>1</orderid></product><product><description>product2</description>" +
                "<gtin>asdas</gtin><price currency=\"USD\">25.5</price><supplier>supplier2</supplier><orderid>2</orderid>" +
                "</product><product><description>product3</description><gtin>asdas</gtin><price currency=\"USD\">25.5</price>" +
                "<supplier>supplier3</supplier><orderid>1</orderid></product></order><order created=\"2022-10-25T23:30:00\" ID=\"5\">" +
                "<product><description>product1</description><gtin>asdas</gtin><price currency=\"USD\">25.5</price><supplier>supplier1</supplier>" +
                "<orderid>1</orderid></product><product><description>product2</description><gtin>asdas</gtin><price currency=\"USD\">25.5</price>" +
                "<supplier>supplier2</supplier><orderid>2</orderid></product><product><description>product3</description><gtin>asdas</gtin>" +
                "<price currency=\"USD\">25.5</price><supplier>supplier3</supplier><orderid>1</orderid></product>" +
                "</order></orders>";
        assertEquals(expected,service.serialize(orders));
    }

    @Test
    public void serialize_supplierProductsObject_returnsCompactXmlString() {
        SerializationService<SupplierProducts> service = new SerializationService<>();
        SupplierProducts supplierProducts = new SupplierProducts("supplier");
        supplierProducts.addProduct(new Product("product1", "asdas", new Price("USD", 25.5F), "supplier1", 1));
        supplierProducts.addProduct(new Product("product2", "asdas", new Price("USD", 25.5F), "supplier2", 2));
        supplierProducts.addProduct(new Product("product3", "asdas", new Price("USD", 25.5F), "supplier3", 1));

        String expected = "<?xml version=\"1.0\" ?><products><product><description>product1</description><gtin>asdas</gtin>" +
                "<price currency=\"USD\">25.5</price><supplier>supplier1</supplier><orderid>1</orderid></product><product>" +
                "<description>product2</description><gtin>asdas</gtin><price currency=\"USD\">25.5</price><supplier>supplier2</supplier>" +
                "<orderid>2</orderid></product><product><description>product3</description><gtin>asdas</gtin><price currency=\"USD\">25.5</price>" +
                "<supplier>supplier3</supplier><orderid>1</orderid></product></products>";
        assertEquals(expected,service.serialize(supplierProducts));
    }

    @Test
    public void deserialize_ordersXmlFile_ordersObject() {
        SerializationService<Orders> service = new SerializationService<>();
        File file = new File("test/orders_test.xml");

        Order order1 = new Order(
                LocalDateTime.of(LocalDate.of(2022,7,12),LocalTime.of(15,29)),
                1,
                List.of(
                        new Product("Sony 1","00027242816657",new Price("USD",2999.99F),"Sony",null),
                        new Product("Apple 1","00885909464517",new Price("USD",399.0F),"Apple",null),
                        new Product("Sony 2","00027242831438",new Price("USD",91.99F),"Sony",null)

                )
        );
        Order order2 = new Order(
                LocalDateTime.of(LocalDate.of(2022,7,13),LocalTime.of(16,2)),
                2,
                List.of(
                        new Product("Apple 2","00885909464043",new Price("USD",1149.0F),"Apple",null),
                        new Product("Panasonic 1","00885170076471",new Price("USD",999.99F),"Panasonic",null)
                )
        );
        Orders expected = new Orders(List.of(order1,order2));
        Orders actual = service.deserialize(file);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void deserialize_wrongTagOrder_throwsUnknownFieldException(){
        SerializationService<Orders> service = new SerializationService<>();
        File file = new File("test/orders_test_UnknownFieldException.xml");

        assertThrows(AbstractReflectionConverter.UnknownFieldException.class, () -> service.deserialize(file));
    }

    @Test
    public void deserialize_wrongTagOrder_throwsCannotResolveClassException(){
        SerializationService<Orders> service = new SerializationService<>();
        File file = new File("test/orders_test_CannotResolveClassException.xml");

        assertThrows(CannotResolveClassException.class, () -> service.deserialize(file));
    }


}
