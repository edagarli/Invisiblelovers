package me.edagarli.invisiblelovers.utils;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.TypeReference;

import com.alibaba.fastjson.asm.Type;

import com.alibaba.fastjson.util.TypeUtils;


/**
 * Created by jhp-android on 14-12-12.
 */

public class FastJsonTools {

    public FastJsonTools() {

// TODO Auto-generated constructor stub

    }

    public static String createJsonString(Object object) {

        String jsonString = JSON.toJSONString(object);

        return jsonString;

    }

    public static <T> T createJsonBean(String jsonString, Class<T> cls) {

        T t = JSON.parseObject(jsonString, cls);

        return t;

    }

    public static <T> List<T> createJsonToListBean(String jsonString,

                                                   Class<T> cls) {

        List<T> list = null;

        list = JSON.parseArray(jsonString, cls);

        return list;

    }

    /**
     * @param jsonString
     * @return
     */

    public static List<Map<String, Object>> createJsonToListMap(

            String jsonString) {

        List<Map<String, Object>> list2 = JSON.parseObject(jsonString,

                new TypeReference<List<Map<String, Object>>>() {

                });

        return list2;

    }

    /**
     * @param args
     */

    public static void main(String[] args) {

// TODO Auto-generated method stub

// Person person1 = new Person("张三1", 24, "北京1");

// Person person2 = new Person("张三2", 23, "北京2");

// List<Person> list = new ArrayList<Person>();

// list.add(person1);

// list.add(person2);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("name", "jack");

        map.put("age", 23);

        Map<String, Object> map2 = new HashMap<String, Object>();

        map2.put("name", "rose");

        map2.put("age", 24);

        list.add(map);

        list.add(map2);

        String jsonString = JSON.toJSONString(list);

        System.out.println(jsonString);

// JSON.parseArray(arg0, arg1)

        List<Map<String, Object>> list2 = JSON.parseObject(jsonString,

                new TypeReference<List<Map<String, Object>>>() {

                });

// List<Person> lists = JSON.parseArray(arg0, arg1);

        System.out.println(list2.toString());

    }

}