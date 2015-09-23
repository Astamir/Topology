package ru.etu.astamir.common.reflect;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.xml.internal.ws.util.StringUtils;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Artem Mon'ko
 */
public class ReflectUtils {
    public static Class<?> getCollectionType(Field field) {
        if (!Collection.class.isAssignableFrom(field.getType())) {
            throw new UnexpectedException("field [" + field.getName() + "] is not collection");
        }

        ParameterizedType type = (ParameterizedType) field.getGenericType();
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    public static Optional<Class> forSimpleName(String name) {
        for (Package p : Package.getPackages()) {
            try {

                Class cl = Class.forName(p.getName() + "." + StringUtils.capitalize(name));
                return Optional.of(cl);
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }

        return Optional.absent();
    }

    public static List<Field> getFieldsUpTo(Class<?> base, Class<?> parent) {
        if (!parent.isAssignableFrom(base)) {
            throw new UnexpectedException(base.getCanonicalName() + " is not derivable from " + parent.getCanonicalName());
        }
        List<Field> fields = Lists.newArrayList(base.getDeclaredFields());

        if (base.equals(parent)) {
            return fields;
        } else {
            fields.addAll(getFieldsUpTo(base.getSuperclass(), parent));
            return fields;
        }
    }

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Collections.addAll(fields, type.getDeclaredFields());

        if (type.getSuperclass() != null) {
            fields.addAll(getAllFields(type.getSuperclass()));
        }

        return fields;
    }

    public static Optional<Field> findField(Class<?> type, final String name) {
        return Iterables.tryFind(getAllFields(type), new Predicate<Field>() {
            @Override
            public boolean apply(Field input) {
                return input.getName().equals(name);
            }
        });
    }

    public static Number parse(String value, Class<?> type) {
        if (Boolean.class.isAssignableFrom(type)) {
            throw new UnexpectedException("Boolean type when number expected");
        }

        if (Long.class.isAssignableFrom(type)) {
            return Long.parseLong(value);
        }

        if (Integer.class.isAssignableFrom(type)) {
            return Integer.parseInt(value);
        }

        if (Double.class.isAssignableFrom(type)) {
            return Double.parseDouble(value);
        }

        if (Float.class.isAssignableFrom(type)) {
            return Long.parseLong(value);
        }

        try {
            return DecimalFormat.getInstance().parse(value);
        } catch (ParseException e) {
            throw new NumberFormatException(value);
        }
    }
}
