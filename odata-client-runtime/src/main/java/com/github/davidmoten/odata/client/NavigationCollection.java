package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * Typed descriptor for a collection-valued (to-many) navigation property. Supports the
 * same nested options as {@link NavigationProperty} plus string-based any/all helpers
 * (e.g. {@code Person_.friends.any("firstName eq 'bob'")}).
 *
 * @param <T> target entity Java type
 * @param <E> owning entity type
 */
public final class NavigationCollection<T, E> extends
        NavigationProperty<T, E, NavigationCollection<T, E>> {

    public NavigationCollection(String name, Class<T> targetType) {
        super(name, targetType);
    }

    /**
     * Convenience: {@code nav/any(filterText)} where the filter text is a raw OData
     * filter expression scoped to the navigation's target entity (e.g.
     * {@code "firstName eq 'bob'"}). The lambda variable is implicit.
     */
    public FilterExpression<E> any(String filterText) {
        Preconditions.checkNotNull(filterText);
        return new ConstantFilterExpression<E>(name() + "/any(" + filterText + ")");
    }

    /**
     * Convenience: {@code nav/all(filterText)} where the filter text is a raw OData
     * filter expression scoped to the navigation's target entity.
     */
    public FilterExpression<E> all(String filterText) {
        Preconditions.checkNotNull(filterText);
        return new ConstantFilterExpression<E>(name() + "/all(" + filterText + ")");
    }

    /**
     * Lambda-style {@code any}. The lambda receives a synthetic {@link AnyAllBuilder} for
     * the navigation's target entity. The lambda is invoked once and the result is
     * rendered as an OData any/all expression.
     *
     * <p>
     * Example: {@code Person_.friends.any(p -> p.firstName.eq("bob"))} -- the variable
     * {@code p} binds to a synthetic "current item" property descriptor whose name is
     * rewritten to the OData lambda variable automatically.
     *
     * <p>
     * Note: the lambda may only reference properties of {@code T} via the
     * {@link AnyAllBuilder} parameter; calling methods on the original entity classes
     * from the lambda will not be recognized.
     */
    public FilterExpression<E> any(java.util.function.Function<AnyAllBuilder<T>, FilterExpression<T>> lambda) {
        Preconditions.checkNotNull(lambda);
        return renderLambda("any", lambda);
    }

    public FilterExpression<E> all(java.util.function.Function<AnyAllBuilder<T>, FilterExpression<T>> lambda) {
        Preconditions.checkNotNull(lambda);
        return renderLambda("all", lambda);
    }

    private FilterExpression<E> renderLambda(String op,
            java.util.function.Function<AnyAllBuilder<T>, FilterExpression<T>> lambda) {
        AnyAllBuilder<T> b = new AnyAllBuilder<T>(targetType());
        FilterExpression<T> result = lambda.apply(b);
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append('/').append(op).append('(');
        sb.append("x:").append(result.text());
        sb.append(')');
        return new ConstantFilterExpression<E>(sb.toString());
    }

    /**
     * Returns the count of related entities as a numeric property suitable for
     * filtering: {@code nav/$count}.
     */
    public Property<Integer, E> count() {
        // We use E (the owning entity) as the parent type so the result can be used in
        // filters like `Person_.friends.count().gt(5)`.
        return new Property<Integer, E>(name() + "/$count", Integer.class, false, "Edm.Int32");
    }
}
