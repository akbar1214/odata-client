package com.github.davidmoten.odata.client;

/**
 * Typed descriptor for a single-valued (to-one) navigation property. Supports the same
 * nested options as {@link NavigationProperty}.
 *
 * @param <T> target entity Java type
 * @param <E> owning entity type
 */
public final class NavigationSingle<T, E> extends NavigationProperty<T, E, NavigationSingle<T, E>> {

    public NavigationSingle(String name, Class<T> targetType) {
        super(name, targetType);
    }
}
