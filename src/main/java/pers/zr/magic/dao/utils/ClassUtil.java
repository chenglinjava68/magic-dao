/**
 * beidou-core-493#com.baidu.beidou.common.utils.ClassUtil.java
 * 下午3:40:26 created by Darwin(Tianxin)
 */
package pers.zr.magic.dao.utils;


import pers.zr.magic.dao.annotation.Column;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassUtil {


    public final static Type[] getGenericTypes(Class<?> clazz) {

        Class<?> myClass = clazz;

        if (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
            myClass = clazz.getSuperclass();
        }

        Type superClass = myClass.getGenericSuperclass();
        ParameterizedType type = (ParameterizedType) superClass;
        return type.getActualTypeArguments();
    }

    public static Set<Field> getAllFiled(Class<?> entityClass) {

        // 获取本类的所有字段
        Set<Field> fs = new HashSet<Field>();
        for (Field f : entityClass.getFields()) {
            fs.add(f);
        }
        for (Field f : entityClass.getDeclaredFields()) {
            fs.add(f);
        }

        // 递归获取父类的所有字段
        Class<?> superClass = entityClass.getSuperclass();
        if (!superClass.equals(Object.class)) {
            Set<Field> superFileds = getAllFiled(superClass);
            fs.addAll(superFileds);
        }

        return fs;
    }


    public static Set<Method> getAllMethod(Class<?> entityClass) {

        // 获取本类的所有的方法
        Set<Method> ms = new HashSet<Method>();
        for (Method m : entityClass.getMethods()) {
            ms.add(m);
        }
        for (Method m : entityClass.getDeclaredMethods()) {
            ms.add(m);
        }

        // 递归获取父类的所有方法
        Class<?> superClass = entityClass.getSuperclass();
        if (!superClass.equals(Object.class)) {
            Set<Method> superFileds = getAllMethod(superClass);
            ms.addAll(superFileds);
        }

        return ms;
    }

    public static Map<String, Method> filter2Map(Set<Method> ms) {

        Map<String, Method> map = new HashMap<String, Method>();
        for (Method m : ms) {
            boolean flag = Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers());
            if (flag) {

                String name = m.getName().toLowerCase();
                if (name.startsWith("get") && m.getParameterTypes().length == 0) {
                } else if (name.startsWith("is") && m.getParameterTypes().length == 0) {
                } else if (name.startsWith("set") && m.getParameterTypes().length == 1) {
                } else {
                    continue;
                }

                // 获取同名的方法
                Method old = map.get(name);

                // 如果之前没有同名方法,则添加本方法
                if (old == null) {
                    map.put(name, m);

                    // 如果有同名方法，且本方法在子类中声明，且，父类本方法包含了annotation，则替换原来的方法
                } else if (old.getDeclaringClass().isAssignableFrom(m.getDeclaringClass()) &&
                               m.getAnnotation(Column.class) != null) {
                    map.put(name, m);
                }
            }
        }
        return map;
    }


    public final static void copyProperties(Object from, Object to) {

        Set<Field> fromSet = getAllFiled(from.getClass());
        Set<Field> toSet = getAllFiled(to.getClass());

        Map<String, Field> toMap = new HashMap<String, Field>();
        for (Field f : toSet) {
            toMap.put(f.getName(), f);
        }

        for (Field f : fromSet) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            String name = f.getName();
            Field toField = toMap.get(name);
            if (toField == null) {
                continue;
            }

            toField.setAccessible(true);
            f.setAccessible(true);
            try {
                toField.set(to, f.get(from));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    public static Field getFieldFromClass(String field, Class<? extends Object> clazz) {
        try {
            return clazz.getDeclaredField(field);
        } catch (Exception e) {
            try {
                return clazz.getField(field);
            } catch (Exception ex) {
            }
        }
        return null;
    }



}