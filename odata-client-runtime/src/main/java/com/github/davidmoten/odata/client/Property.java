package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * Typed descriptor for an OData property of an entity. A {@code Property} is the
 * Hibernate-JPA-style metamodel equivalent of an OData structural or navigation
 * property and is used in place of stringly-typed property names in query options
 * ({@code $select}, {@code $filter}, {@code $expand}, {@code $orderby}).
 *
 * <p>
 * Instances are created by the generated metamodel class for each entity (e.g.
 * {@code User_.mail}). They are stateless, safe to share, and intended to be referenced
 * as {@code public static final} constants.
 *
 * <p>
 * A {@code Property} also acts as a factory of {@link FilterExpression} leaves for use in
 * {@code $filter} (e.g. {@code User_.age.gt(18)} or {@code User_.mail.startsWith("a")}).
 *
 * @param <T> the Java type of the property value
 * @param <E> the owning entity type
 */
public class Property<T, E> {

    private final String name;
    private final Class<T> type;
    private final boolean collection;
    private final String edmType;

    /**
     * Constructor used by generator for primitive / complex / enum properties.
     */
    public Property(String name, Class<T> type) {
        this(name, type, false, null);
    }

    /**
     * Constructor used by generator for collection properties.
     */
    public Property(String name, Class<T> type, boolean collection) {
        this(name, type, collection, null);
    }

    /**
     * Full constructor allowing the EDM type name to be specified (e.g. {@code Edm.Int32}
     * or a namespaced enum type).
     */
    public Property(String name, Class<T> type, boolean collection, String edmType) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);
        this.name = name;
        this.type = type;
        this.collection = collection;
        this.edmType = edmType;
    }

    /**
     * Returns the OData wire name of the property (i.e. what to put in {@code $select},
     * {@code $filter}, etc.).
     */
    public final String name() {
        return name;
    }

    /**
     * Returns the Java type of the property's value (e.g. {@code String.class},
     * {@code Integer.class}, {@code Instant.class}).
     */
    public final Class<T> type() {
        return type;
    }

    /**
     * Returns true if this property holds a collection of values.
     */
    public final boolean isCollection() {
        return collection;
    }

    /**
     * Returns the EDM type name (e.g. {@code Edm.String}, {@code Edm.Int32}) for this
     * property, or null if unknown.
     */
    public final String edmType() {
        return edmType;
    }

    @Override
    public String toString() {
        return name;
    }

    // ---------------------------------------------------------------------
    // Filter operators (leaves of the filter expression DSL)
    // ---------------------------------------------------------------------

    /** {@code property eq value} */
    public final FilterExpression<E> eq(T value) {
        return new ConstantFilterExpression<E>(
                name + " eq " + LiteralFormatter.format(value, type, edmType));
    }

    /** {@code property ne value} */
    public final FilterExpression<E> ne(T value) {
        return new ConstantFilterExpression<E>(
                name + " ne " + LiteralFormatter.format(value, type, edmType));
    }

    /** {@code property gt value} */
    public final FilterExpression<E> gt(T value) {
        return new ConstantFilterExpression<E>(
                name + " gt " + LiteralFormatter.format(value, type, edmType));
    }

    /** {@code property ge value} */
    public final FilterExpression<E> ge(T value) {
        return new ConstantFilterExpression<E>(
                name + " ge " + LiteralFormatter.format(value, type, edmType));
    }

    /** {@code property lt value} */
    public final FilterExpression<E> lt(T value) {
        return new ConstantFilterExpression<E>(
                name + " lt " + LiteralFormatter.format(value, type, edmType));
    }

    /** {@code property le value} */
    public final FilterExpression<E> le(T value) {
        return new ConstantFilterExpression<E>(
                name + " le " + LiteralFormatter.format(value, type, edmType));
    }

    /** {@code startswith(property, value)} (string only). */
    public final FilterExpression<E> startsWith(T value) {
        return new ConstantFilterExpression<E>(
                "startswith(" + name + "," + LiteralFormatter.format(value, type, edmType) + ")");
    }

    /** {@code endswith(property, value)} (string only). */
    public final FilterExpression<E> endsWith(T value) {
        return new ConstantFilterExpression<E>(
                "endswith(" + name + "," + LiteralFormatter.format(value, type, edmType) + ")");
    }

    /** {@code contains(property, value)} (string only). */
    public final FilterExpression<E> contains(T value) {
        return new ConstantFilterExpression<E>(
                "contains(" + name + "," + LiteralFormatter.format(value, type, edmType) + ")");
    }

    /** {@code indexof(property, value) eq -1} (substring not present). */
    public final FilterExpression<E> doesNotContain(T value) {
        return new ConstantFilterExpression<E>(
                "indexof(" + name + "," + LiteralFormatter.format(value, type, edmType) + ") eq -1");
    }

    /** {@code property} ascending. */
    public final OrderBy<E> asc() {
        return new OrderBy<E>(name + " asc");
    }

    /** {@code property} descending. */
    public final OrderBy<E> desc() {
        return new OrderBy<E>(name + " desc");
    }

    /** {@code property in (a, b, c)}. */
    @SafeVarargs
    public final FilterExpression<E> in(T... values) {
        Preconditions.checkNotNull(values);
        Preconditions.checkArgument(values.length > 0, "in() requires at least one value");
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" in (");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(LiteralFormatter.format(values[i], type, edmType));
        }
        sb.append(')');
        return new ConstantFilterExpression<E>(sb.toString());
    }

    // ---------------------------------------------------------------------
    // Factory methods used by generated *_ metamodel classes
    // ---------------------------------------------------------------------

    public static <E> Property<String, E> string(String name) {
        return new Property<String, E>(name, String.class, false, "Edm.String");
    }

    public static <E> Property<Boolean, E> bool(String name) {
        return new Property<Boolean, E>(name, Boolean.class, false, "Edm.Boolean");
    }

    public static <E> Property<Integer, E> int32(String name) {
        return new Property<Integer, E>(name, Integer.class, false, "Edm.Int32");
    }

    public static <E> Property<Long, E> int64(String name) {
        return new Property<Long, E>(name, Long.class, false, "Edm.Int64");
    }

    public static <E> Property<Short, E> int16(String name) {
        return new Property<Short, E>(name, Short.class, false, "Edm.Int16");
    }

    public static <E> Property<Byte, E> sbyte(String name) {
        return new Property<Byte, E>(name, Byte.class, false, "Edm.SByte");
    }

    public static <E> Property<Float, E> single(String name) {
        return new Property<Float, E>(name, Float.class, false, "Edm.Single");
    }

    public static <E> Property<Float, E> float_(String name) {
        return new Property<Float, E>(name, Float.class, false, "Edm.Single");
    }

    public static <E> Property<Double, E> double_(String name) {
        return new Property<Double, E>(name, Double.class, false, "Edm.Double");
    }

    public static <E> Property<java.math.BigDecimal, E> decimal(String name) {
        return new Property<java.math.BigDecimal, E>(name, java.math.BigDecimal.class, false,
                "Edm.Decimal");
    }

    public static <E> Property<java.time.Instant, E> datetime(String name) {
        return new Property<java.time.Instant, E>(name, java.time.Instant.class, false,
                "Edm.DateTimeOffset");
    }

    public static <E> Property<java.time.LocalDate, E> date(String name) {
        return new Property<java.time.LocalDate, E>(name, java.time.LocalDate.class, false,
                "Edm.Date");
    }

    public static <E> Property<java.time.LocalTime, E> time(String name) {
        return new Property<java.time.LocalTime, E>(name, java.time.LocalTime.class, false,
                "Edm.TimeOfDay");
    }

    public static <E> Property<byte[], E> binary(String name) {
        return new Property<byte[], E>(name, byte[].class, false, "Edm.Binary");
    }

    public static <E> Property<java.util.UUID, E> guid(String name) {
        return new Property<java.util.UUID, E>(name, java.util.UUID.class, false, "Edm.Guid");
    }

    /**
     * Generic property factory when the generator does not know a more specific type.
     * The EDM type name defaults to the full Java class name.
     */
    public static <T, E> Property<T, E> create(String name, Class<T> type) {
        return new Property<T, E>(name, type);
    }

    /**
     * Generic property factory for collections where the inner Java type is known.
     * Useful in the generated metamodel where the {@code E} type is the owning entity
     * (not the inner collection type).
     */
    public static <T, E> Property<T, E> createCollection(String name, Class<T> innerType) {
        return new Property<T, E>(name, innerType, true, null);
    }

    /**
     * Generic property factory for collections where the inner Java type and EDM type
     * are both known.
     */
    public static <T, E> Property<T, E> createCollection(String name, Class<T> innerType,
            String edmType) {
        return new Property<T, E>(name, innerType, true, edmType);
    }

    /**
     * Returns a list of property wire names (used when building a {@code $select} clause
     * from a varargs of properties).
     */
    public static List<String> names(List<? extends Property<?, ?>> properties) {
        Preconditions.checkNotNull(properties);
        if (properties.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(properties.size());
        for (Property<?, ?> p : properties) {
            result.add(p.name());
        }
        return result;
    }

    /**
     * Joins the given property names with a comma (the OData {@code $select} separator).
     */
    public static String joinNames(List<? extends Property<?, ?>> properties) {
        return String.join(",", names(properties));
    }

    /**
     * Convenience method to join a varargs of properties into a comma-separated string.
     */
    public static String joinNames(Property<?, ?>... properties) {
        Preconditions.checkNotNull(properties);
        return joinNames(Arrays.asList(properties));
    }
}
