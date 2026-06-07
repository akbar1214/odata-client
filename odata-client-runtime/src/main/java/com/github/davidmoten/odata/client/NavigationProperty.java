package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * Typed descriptor for an OData navigation property. Subclasses are
 * {@link NavigationSingle} and {@link NavigationCollection}.
 *
 * <p>
 * A {@code NavigationProperty} extends {@link Property} (so it can appear in
 * {@code $select} and {@code $expand}) and additionally provides builders for nested
 * query options that apply to the navigation's target entity set: {@code $select},
 * {@code $filter}, {@code $orderby}, {@code $top}, {@code $search} and (recursively)
 * {@code $expand}.
 *
 * <p>
 * Once a nested option is set, calling {@link #text()} on the navigation produces the
 * OData sub-clause in parentheses (e.g. {@code manager($select=name;$filter=active eq
 * true)}), ready to be embedded into a parent's {@code $expand=...} value.
 *
 * @param <T> target entity Java type
 * @param <E> owning entity type
 * @param <SELF> concrete subtype (F-bound self type for fluent chaining)
 */
public abstract class NavigationProperty<T, E, SELF extends NavigationProperty<T, E, SELF>>
        extends Property<Object, E> {

    private final Class<T> targetType;
    private Optional<PropertyCollection<T>> select = Optional.empty();
    private Optional<FilterExpression<T>> filter = Optional.empty();
    private List<OrderBy<T>> orderBy = Collections.emptyList();
    private Optional<Long> top = Optional.empty();
    private Optional<Long> skip = Optional.empty();
    private List<NavigationProperty<?, T, ?>> expand = Collections.emptyList();
    private Optional<String> search = Optional.empty();
    private Optional<Boolean> count = Optional.empty();
    private Optional<Integer> level = Optional.empty();

    protected NavigationProperty(String name, Class<T> targetType) {
        super(name, Object.class, false, null);
        Preconditions.checkNotNull(targetType);
        this.targetType = targetType;
    }

    public final Class<T> targetType() {
        return targetType;
    }

    /**
     * Returns the nested query options formatted as an OData sub-clause enclosed in
     * parentheses (e.g. {@code ($select=foo,bar;$filter=active eq true)}). Returns an
     * empty string if no nested options are set.
     */
    public final String segmentText() {
        List<String> parts = new ArrayList<>();
        select.ifPresent(s -> parts.add("$select=" + s.text()));
        filter.ifPresent(f -> parts.add("$filter=" + f.text()));
        if (!orderBy.isEmpty()) {
            StringBuilder sb = new StringBuilder("$orderby=");
            boolean first = true;
            for (OrderBy<T> o : orderBy) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(o.text());
                first = false;
            }
            parts.add(sb.toString());
        }
        top.ifPresent(t -> parts.add("$top=" + t));
        skip.ifPresent(s -> parts.add("$skip=" + s));
        search.ifPresent(s -> parts.add("$search=" + s));
        count.ifPresent(c -> parts.add("$count=" + (c ? "true" : "false")));
        level.ifPresent(l -> parts.add("$level=" + l));
        if (!expand.isEmpty()) {
            StringBuilder sb = new StringBuilder("$expand=");
            boolean first = true;
            for (NavigationProperty<?, T, ?> n : expand) {
                if (!first) {
                    sb.append(',');
                }
                String seg = n.segmentText();
                sb.append(n.name());
                if (!seg.isEmpty()) {
                    sb.append('(').append(stripParens(seg)).append(')');
                }
                first = false;
            }
            parts.add(sb.toString());
        }
        if (parts.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        boolean first = true;
        for (String p : parts) {
            if (!first) {
                sb.append(';');
            }
            sb.append(p);
            first = false;
        }
        sb.append(')');
        return sb.toString();
    }

    private static String stripParens(String s) {
        if (s.startsWith("(") && s.endsWith(")")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * Returns the full text for this navigation when used in a parent's {@code $expand}
     * value: {@code name(...)} or just {@code name} if no nested options are set.
     */
    public final String text() {
        String seg = segmentText();
        if (seg.isEmpty()) {
            return name();
        }
        return name() + seg;
    }

    @Override
    public String toString() {
        return text();
    }

    // ---------------------------------------------------------------------
    // Nested option setters - all return SELF for fluent chaining.
    // ---------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    protected final SELF self() {
        return (SELF) this;
    }

    public SELF select(PropertyCollection<T> properties) {
        Preconditions.checkNotNull(properties);
        this.select = Optional.of(properties);
        return self();
    }

    @SafeVarargs
    public final SELF select(Property<?, ? extends T>... properties) {
        Preconditions.checkNotNull(properties);
        this.select = Optional.of(PropertyCollection.of(properties));
        return self();
    }

    public SELF filter(FilterExpression<T> filter) {
        Preconditions.checkNotNull(filter);
        this.filter = Optional.of(filter);
        return self();
    }

    public SELF filter(String filterText) {
        Preconditions.checkNotNull(filterText);
        this.filter = Optional.of(FilterExpression.<T>of(filterText));
        return self();
    }

    @SafeVarargs
    public final SELF orderBy(OrderBy<T>... orderings) {
        Preconditions.checkNotNull(orderings);
        this.orderBy = new ArrayList<>(orderings.length);
        for (OrderBy<T> o : orderings) {
            Preconditions.checkNotNull(o);
            this.orderBy.add(o);
        }
        return self();
    }

    public SELF top(long n) {
        Preconditions.checkArgument(n > 0);
        this.top = Optional.of(n);
        return self();
    }

    public SELF skip(long n) {
        Preconditions.checkArgument(n > 0);
        this.skip = Optional.of(n);
        return self();
    }

    public SELF search(String freeText) {
        Preconditions.checkNotNull(freeText);
        this.search = Optional.of(freeText);
        return self();
    }

    public SELF count(boolean value) {
        this.count = Optional.of(value);
        return self();
    }

    public SELF level(int n) {
        Preconditions.checkArgument(n >= 0);
        this.level = Optional.of(n);
        return self();
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final SELF expand(NavigationProperty<?, T, ?>... navigations) {
        Preconditions.checkNotNull(navigations);
        this.expand = new ArrayList<>(navigations.length);
        for (NavigationProperty<?, T, ?> n : navigations) {
            Preconditions.checkNotNull(n);
            this.expand.add(n);
        }
        return self();
    }
}
