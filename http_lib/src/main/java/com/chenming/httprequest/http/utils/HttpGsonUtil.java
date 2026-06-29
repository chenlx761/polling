package com.chenming.httprequest.http.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpGsonUtil {

    public static <T> T gsonToBean(String json, Class<T> t) {
        return GsonUtil.fromJson(json, t);
    }

    public static <T> List<T> gson2List(String json, Class<T> t) {

        return GsonUtil.fromJson(json, new ParameterizedTypeImpl(t));
    }


    //对象转换为json
    public static String toJson(Object object) {
        return GsonUtil.toJson(object);
    }


    private static class ParameterizedTypeImpl implements ParameterizedType {
        Class claszz;

        ParameterizedTypeImpl(Class clz) {
            claszz = clz;
        }

        @NonNull
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{claszz};
        }

        @NonNull
        @Override
        public Type getRawType() {
            return List.class;
        }

        @Nullable
        @Override
        public Type getOwnerType() {
            return null;
        }
    }

    /**
     * @param json map的序列化结果
     * @param <K>  k类型
     * @param <V>  v类型
     * @return Map<K, V>
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> kClazz, Class<V> vClazz) {
        return GsonUtil.fromJson(json, new ParameterizedTypeMapImpl(kClazz, vClazz));
    }


    private static class ParameterizedTypeMapImpl implements ParameterizedType {
        Class keyZz;
        Class valueZz;

        ParameterizedTypeMapImpl(Class keyZz, Class valueZz) {
            this.keyZz = keyZz;
            this.valueZz = valueZz;
        }

        @NonNull
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{keyZz, valueZz};
        }

        @NonNull
        @Override
        public Type getRawType() {
            return Map.class;
        }

        @Nullable
        @Override
        public Type getOwnerType() {
            return null;
        }
    }


    /**
     * @param json map的序列化结果
     * @param <K>  k类型
     * @param <V>  v类型
     * @return Map<K, V>
     */
    public static <K, V> List<Map<K, V>> toListMap(String json, Class<K> kClazz, Class<V> vClazz) {
        List<Object> ps = GsonUtil.fromJson(json, new TypeToken<List<Object>>() {
        }.getType());
        //        List<Map<K, V>> collect = ps.stream().map(o -> toMap(o.toString(), kClazz, vClazz)).collect(Collectors.toList());
        List<Map<K, V>> collect = new ArrayList<>();
        for (Object object : ps) {
            collect.add(toMap(object.toString(), kClazz, vClazz));
        }
        return collect;
    }


    /**
     * json转有泛型的list
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> json2ListObj(String json, Class<T> cls) {
        List<T> reList = new ArrayList<>();
        JsonElement jsonElement = new JsonParser().parse(json);
        JsonArray array = jsonElement.getAsJsonArray();
        Iterator<JsonElement> iterator = array.iterator();
        Gson gson = new Gson();

        while (iterator.hasNext()) {
            JsonElement json2 = iterator.next();
            T contact = gson.fromJson(json2, cls);
            //can set some values in contact, if required
            reList.add(contact);
        }

        return reList;
    }
}
