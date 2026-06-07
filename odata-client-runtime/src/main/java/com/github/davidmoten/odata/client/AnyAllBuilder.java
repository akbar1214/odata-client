package com.github.davidmoten.odata.client;

/**
 * Helper for lambda-style any/all filters on {@link NavigationCollection}. The
 * {@link #property(String)} method returns a {@link Property} that, when rendered as part
 * of a filter expression, is prefixed with the OData lambda variable ({@code x:}). This
 * makes it possible to write {@code nav.any(p -> p.property("firstName").eq("bob"))}
 * without needing the metamodel of the target entity to be loaded by the IDE.
 *
 * <p>
 * Most users will not need this class directly - they will use the
 * {@code Person_.firstName} metamodel field through a custom lambda-aware overload in
 * the generated metamodel. This class is the building block for that.
 *
 * @param <T> target entity type
 */
public final class AnyAllBuilder<T> {

    private final Class<T> targetType;

    public AnyAllBuilder(Class<T> targetType) {
        this.targetType = targetType;
    }

    public Class<T> targetType() {
        return targetType;
    }

    /**
     * Returns a property reference bound to the lambda variable.
     */
    public Property<Object, T> property(String name) {
        return new Property<Object, T>("x/" + name, Object.class, false, null);
    }

    public Property<String, T> string(String name) {
        return new Property<String, T>("x/" + name, String.class, false, "Edm.String");
    }

    public Property<Integer, T> int32(String name) {
        return new Property<Integer, T>("x/" + name, Integer.class, false, "Edm.Int32");
    }

    public Property<Boolean, T> bool(String name) {
        return new Property<Boolean, T>("x/" + name, Boolean.class, false, "Edm.Boolean");
    }
}
