package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.davidmoten.guavamini.Preconditions;

/**
 * Fluent builder for OData {@code $filter} expressions.
 *
 * <p>
 * Instances are created from {@link Property} operator methods (e.g.
 * {@code User_.mail.startsWith("a")}) and combined with {@link #and(FilterExpression)},
 * {@link #or(FilterExpression)} and {@link #not()}. The {@link #text()} method returns the
 * URL-encoded OData filter expression to be sent in the {@code $filter} query option.
 *
 * <p>
 * Implementations are immutable and safe to share.
 *
 * @param <E> owning entity type
 */
public interface FilterExpression<E> {

    /**
     * Returns the OData filter expression text (the value to use as the {@code $filter}
     * query option value).
     *
     * @return filter expression text
     */
    String text();

    /**
     * Combines this expression and the given expression with the {@code and} operator.
     *
     * @param other right-hand side of the conjunction
     * @return a new FilterExpression that evaluates to this AND other
     */
    FilterExpression<E> and(FilterExpression<E> other);

    /**
     * Combines this expression and the given expression with the {@code or} operator.
     *
     * @param other right-hand side of the disjunction
     * @return a new FilterExpression that evaluates to this OR other
     */
    FilterExpression<E> or(FilterExpression<E> other);

    /**
     * Negates this expression.
     *
     * @return a new FilterExpression that is the logical negation of this expression
     */
    FilterExpression<E> not();

    /**
     * Wraps a string in a constant filter expression. Useful for one-off filter text
     * mixed with typed expressions (rarely needed in practice but useful as an escape
     * hatch).
     */
    static <E> FilterExpression<E> of(String text) {
        Preconditions.checkNotNull(text);
        return new ConstantFilterExpression<E>(text);
    }

    /**
     * Returns a builder that allows the concatenation of multiple filter expressions
     * separated by {@code and}.
     */
    static <E> Builder<E> conjunction() {
        return new Builder<E>("and");
    }

    /**
     * Returns a builder that allows the concatenation of multiple filter expressions
     * separated by {@code or}.
     */
    static <E> Builder<E> disjunction() {
        return new Builder<E>("or");
    }

    final class Builder<E> {
        private final String operator;
        private final List<FilterExpression<E>> parts = new ArrayList<>();

        Builder(String operator) {
            this.operator = operator;
        }

        public Builder<E> add(FilterExpression<E> e) {
            Preconditions.checkNotNull(e);
            parts.add(e);
            return this;
        }

        public FilterExpression<E> build() {
            if (parts.isEmpty()) {
                return new ConstantFilterExpression<E>("true");
            }
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (FilterExpression<E> p : parts) {
                if (!first) {
                    sb.append(' ').append(operator).append(' ');
                }
                String t = p.text();
                if (needsParentheses(t)) {
                    sb.append('(').append(t).append(')');
                } else {
                    sb.append(t);
                }
                first = false;
            }
            return new ConstantFilterExpression<E>(sb.toString());
        }

        public List<FilterExpression<E>> parts() {
            return Collections.unmodifiableList(parts);
        }
    }

    static boolean needsParentheses(String t) {
        if (t == null || t.isEmpty()) {
            return false;
        }
        if (t.charAt(0) == '(' && t.charAt(t.length() - 1) == ')') {
            return false;
        }
        // wrap if the expression contains an " and " or " or " at top level (heuristic)
        return t.contains(" and ") || t.contains(" or ");
    }
}
