package com.atguigu.gmall.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;

public class Jsons {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String toStr(Object obj) {

        try {
            String s = objectMapper.writeValueAsString(obj);
            return s;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static <T> T toObj(String json,Class<T> tClass) {
        if(StringUtils.isEmpty(json)){
            return null;
        }
        T t = null;
        try {
            t = objectMapper.readValue(json, tClass);
            return t;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 带复杂泛型的封装
     * @param json
     * @param tr
     * @param <T>
     * @return
     */
    public static <T> T toObj(String json, TypeReference<T> tr) {
        if(StringUtils.isEmpty(json)){
            return null;
        }
        T t = null;
        try {
            t = objectMapper.readValue(json, tr);
            return t;
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
