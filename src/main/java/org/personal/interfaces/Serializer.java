package org.personal.interfaces;

import java.io.File;

public interface Serializer<T> {
    T deserialize(File file);
    String serialize(T object);
}
