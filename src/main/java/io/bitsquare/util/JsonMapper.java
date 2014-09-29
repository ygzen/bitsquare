package io.bitsquare.util;

import java.io.IOException;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class JsonMapper<T extends JsonEntity> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Class<T> clazz;

    public JsonMapper(Class<T> clazz) {
        this.clazz = clazz;
        mapper.registerModule(new JodaModule());
    }

    public T fromJsonBytes(byte[] jsonBytes) {
        return fromJson(new String(jsonBytes));
    }

    public T fromJson(String json) {
        T result = null;
        try {
            result = mapper.readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<T> fromJsonBytesList(byte[] jsonBytes) {
        return fromJsonList(new String(jsonBytes));
    }

    public List<T> fromJsonList(String json) {
        List<T> result = null;
        try {
            result = mapper.readValue(json, new TypeReference<List<T>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public final String toJson(T obj) {

        String json = null;

        try {
            json = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
