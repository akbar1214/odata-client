package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * A typed list of {@link Property} values used in {@code $select} and as a child
 * expression inside a navigation ({@code $select=nav/sub1,nav/sub2}). A
 * {@code PropertyCollection} is built by passing a varargs of properties to
 * {@link #of(Property[])} or {@link #create(Property[])}.
 *
 * <p>
 * {@link #toString()} renders the {@code $select} clause value (e.g. {@code a,b,c}).
 * The companion {@link #segmentText()} renders the form used inside a nested expand or
 * filter (e.g. {@code a,b,c}).
 *
 * @param <E> owning entity type
 */
public final class PropertyCollection<E> {

    private final List<Property<?, ?>> properties;

    private PropertyCollection(List<Property<?, ?>> properties) {
        this.properties = properties;
    }

    /**
     * Returns the list of properties in this collection. Used by the builder to render
     * the collection.
     */
    public List<Property<?, ?>> properties() {
        return Collections.unmodifiableList(properties);
    }

    /**
     * Returns the {@code $select} text for these properties (a comma-separated list of
     * property names).
     */
    public String text() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Property<?, ?> p : properties) {
            if (!first) {
                sb.append(',');
            }
            sb.append(p.name());
            first = false;
        }
        return sb.toString();
    }

    /**
     * Same as {@link #text()} - exists so navigation sub-builders can treat select /
     * nested select uniformly.
     */
    public String segmentText() {
        return text();
    }

    @Override
    public String toString() {
        return text();
    }

    /**
     * Builds a {@code PropertyCollection} from a varargs of properties. An empty
     * collection is permitted (produces an empty string in {@link #text()}).
     */
    @SafeVarargs
    public static <E> PropertyCollection<E> of(Property<?, ? extends E>... properties) {
        Preconditions.checkNotNull(properties);
        List<Property<?, ?>> list = new ArrayList<>(properties.length);
        for (Property<?, ? extends E> p : properties) {
            Preconditions.checkNotNull(p);
            list.add(p);
        }
        return new PropertyCollection<E>(list);
    }

    /**
     * Builds a {@code PropertyCollection} from a list of properties.
     */
    public static <E> PropertyCollection<E> create(List<Property<?, ? extends E>> properties) {
        Preconditions.checkNotNull(properties);
        return new PropertyCollection<E>(new ArrayList<Property<?, ?>>(properties));
    }

    /**
     * Returns an empty {@code PropertyCollection} (e.g. for use as a default value in
     * tests).
     */
    public static <E> PropertyCollection<E> empty() {
        return new PropertyCollection<E>(Collections.<Property<?, ?>>emptyList());
    }
}
