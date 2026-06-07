package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * Typed descriptor for an OData {@code $orderby} clause. Instances are created from
 * {@link Property#asc()} and {@link Property#desc()}.
 *
 * @param <E> owning entity type
 */
public final class OrderBy<E> {

    private final String text;

    OrderBy(String text) {
        Preconditions.checkNotNull(text);
        this.text = text;
    }

    /**
     * Returns the OData {@code $orderby} clause text (e.g. {@code displayName desc}).
     */
    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    /**
     * Builds an OData {@code $orderby} value from a list of orderings, joining with
     * {@code ,}.
     */
    public static String join(List<? extends OrderBy<?>> orderings) {
        if (orderings == null || orderings.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (OrderBy<?> o : orderings) {
            if (!first) {
                sb.append(',');
            }
            sb.append(o.text);
            first = false;
        }
        return sb.toString();
    }

    /**
     * Builds an OData {@code $orderby} value from a varargs of orderings.
     */
    public static String join(OrderBy<?>... orderings) {
        if (orderings == null) {
            return "";
        }
        List<OrderBy<?>> list = new ArrayList<>(orderings.length);
        for (OrderBy<?> o : orderings) {
            list.add(o);
        }
        return join(list);
    }
}
