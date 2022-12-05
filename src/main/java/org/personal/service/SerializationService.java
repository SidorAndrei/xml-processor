package org.personal.service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import org.personal.interfaces.Serializer;
import org.personal.models.Orders;
import org.personal.models.SupplierProducts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

public class SerializationService<T> implements Serializer<T> {
    private final XStream xStream;
    private final Logger LOGGER;

    public SerializationService() {
        LOGGER = LoggerFactory.getLogger(SerializationService.class);

        LOGGER.debug("Initializing XStream");
        xStream = new XStream(new StaxDriver());

        LOGGER.debug("Configuring XStream...");
        xStream.addPermission(NoTypePermission.NONE);
        xStream.addPermission(NullPermission.NULL);
        xStream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xStream.allowTypeHierarchy(Collection.class);
        xStream.allowTypesByWildcard(new String[]{"org.example.**"});
        xStream.autodetectAnnotations(true);

        xStream.processAnnotations(SupplierProducts.class);
        xStream.processAnnotations(Orders.class);

        LOGGER.debug("XStream configured");

        LOGGER.debug("XStream initialized");

        LOGGER.debug("Serialization Service created");
    }

    @Override
    public T deserialize(File xml) throws AbstractReflectionConverter.UnknownFieldException, CannotResolveClassException {
        LOGGER.debug("Starting deserialization");
        T t;
        try {
            LOGGER.debug(String.format("Try to extract object from file %s", xml.getAbsolutePath()));
            t = (T) xStream.fromXML(xml);
            LOGGER.info(String.format("Object %s was extracted from file %s", t.getClass().getName(), xml.getAbsolutePath()));
        } catch (AbstractReflectionConverter.UnknownFieldException e) {
            // wrong xml tag but already used (order -> order -> ....)
            LOGGER.error(String.format("Wrong xml tag found in file %s - %s", xml.getAbsolutePath(), e.getMessage()));
            throw e;
        } catch (CannotResolveClassException e) {
            // non-existent tag
            LOGGER.error(String.format("Invalid xml tag found in file %s - %s", xml.getAbsolutePath(), e.getMessage()));
            throw e;
        }
        LOGGER.debug("Deserialization finished");
        return t;
    }

    @Override
    public String serialize(T object) {
        LOGGER.debug(String.format("Starting serialization object %s", object.getClass().getName()));
        String result = xStream.toXML(object);
        LOGGER.debug("Serialization finished");
        return result;
    }


}
