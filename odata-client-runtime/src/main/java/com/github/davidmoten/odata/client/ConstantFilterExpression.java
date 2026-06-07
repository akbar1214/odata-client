package com.github.davidmoten.odata.client;

/**
 * A constant filter expression that wraps an arbitrary (already-formatted) OData filter
 * string. Used as the leaf type for the various fluent expression methods and as the
 * implementation of {@link FilterExpression#and(FilterExpression)},
 * {@link FilterExpression#or(FilterExpression)} and {@link FilterExpression#not()}.
 *
 * @param <E> owning entity type
 */
final class ConstantFilterExpression<E> implements FilterExpression<E> {

    private final String text;

    ConstantFilterExpression(String text) {
        this.text = text;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public FilterExpression<E> and(FilterExpression<E> other) {
        String otherText = other.text();
        String combined;
        if (FilterExpression.needsParentheses(text)) {
            combined = "(" + text + ") and " + wrap(otherText);
        } else {
            combined = text + " and " + wrap(otherText);
        }
        return new ConstantFilterExpression<E>(combined);
    }

    @Override
    public FilterExpression<E> or(FilterExpression<E> other) {
        String otherText = other.text();
        String combined;
        if (FilterExpression.needsParentheses(text)) {
            combined = "(" + text + ") or " + wrap(otherText);
        } else {
            combined = text + " or " + wrap(otherText);
        }
        return new ConstantFilterExpression<E>(combined);
    }

    @Override
    public FilterExpression<E> not() {
        if (FilterExpression.needsParentheses(text)) {
            return new ConstantFilterExpression<E>("not (" + text + ")");
        } else {
            return new ConstantFilterExpression<E>("not " + text);
        }
    }

    private static String wrap(String t) {
        return FilterExpression.needsParentheses(t) ? "(" + t + ")" : t;
    }
}
