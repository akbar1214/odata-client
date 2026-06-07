package com.github.davidmoten.odata.client;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public abstract class NonEntityRequest<T> {

    private final Class<T> cls;
    protected final ContextPath contextPath;

    public NonEntityRequest(Class<T> cls, ContextPath contextPath) {
        this.cls = cls;
        this.contextPath = contextPath;
    }

    T get(NonEntityRequestOptions<T> options) {
        return RequestHelper.get(contextPath, cls, options);
    }

    public T get() {
        return new NonEntityRequestOptionsBuilder<T>(this).get();
    }

    public NonEntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        return new NonEntityRequestOptionsBuilder<T>(this).requestHeader(key, value);
    }

    public NonEntityRequestOptionsBuilder<T> select(String clause) {
        return new NonEntityRequestOptionsBuilder<T>(this).select(clause);
    }

    public NonEntityRequestOptionsBuilder<T> select(PropertyCollection<? super T> properties) {
        return new NonEntityRequestOptionsBuilder<T>(this).select(properties);
    }

    @SafeVarargs
    public final NonEntityRequestOptionsBuilder<T> select(Property<?, ? super T>... properties) {
        return new NonEntityRequestOptionsBuilder<T>(this).select(properties);
    }

    public NonEntityRequestOptionsBuilder<T> expand(String clause) {
        return new NonEntityRequestOptionsBuilder<T>(this).expand(clause);
    }

    @SafeVarargs
    public final NonEntityRequestOptionsBuilder<T> expand(
            NavigationProperty<? super T, ?, ?>... navigations) {
        return new NonEntityRequestOptionsBuilder<T>(this).expand(navigations);
    }

    public NonEntityRequestOptionsBuilder<T> metadataFull() {
        return new NonEntityRequestOptionsBuilder<T>(this).metadataFull();
    }

    public NonEntityRequestOptionsBuilder<T> metadataMinimal() {
        return new NonEntityRequestOptionsBuilder<T>(this).metadataMinimal();
    }

    public NonEntityRequestOptionsBuilder<T> metadataNone() {
        return new NonEntityRequestOptionsBuilder<T>(this).metadataNone();
    }

}
