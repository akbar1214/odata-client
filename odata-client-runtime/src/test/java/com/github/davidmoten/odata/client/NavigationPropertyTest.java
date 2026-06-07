package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link NavigationProperty}, {@link NavigationSingle} and
 * {@link NavigationCollection}, including nested select/filter/expand rendering.
 *
 * <p>
 * Each test instantiates its own {@link NavigationProperty} because the fluent setters
 * mutate state on the instance.
 */
public class NavigationPropertyTest {

    public static final class Manager {
    }

    public static final class Person {
    }

    public static final Property<String, Manager> MGR_NAME =
            Property.create("name", String.class);

    public static final Property<String, Person> NAME =
            Property.create("name", String.class);

    @Test
    public void navigationTextWithoutOptionsIsBareName() {
        NavigationSingle<Manager, Person> m = new NavigationSingle<Manager, Person>("manager", Manager.class);
        assertEquals("manager", m.text());
        NavigationCollection<Person, Person> f = new NavigationCollection<Person, Person>("friends", Person.class);
        assertEquals("friends", f.text());
    }

    @Test
    public void navigationWithSelectWrapsInParens() {
        NavigationSingle<Manager, Person> m = new NavigationSingle<Manager, Person>("manager", Manager.class);
        String text = m.select(MGR_NAME).text();
        assertEquals("manager($select=name)", text);
    }

    @Test
    public void navigationWithFilterWrapsInParens() {
        NavigationSingle<Manager, Person> m = new NavigationSingle<Manager, Person>("manager", Manager.class);
        String text = m.filter(MGR_NAME.eq("bob")).text();
        assertEquals("manager($filter=name eq 'bob')", text);
    }

    @Test
    public void navigationWithMultipleNestedOptions() {
        NavigationSingle<Manager, Person> m = new NavigationSingle<Manager, Person>("manager", Manager.class);
        String text = m.select(MGR_NAME).filter(MGR_NAME.eq("bob")).text();
        assertTrue("expected: " + text,
                text.equals("manager($select=name;$filter=name eq 'bob')"));
    }

    @Test
    public void navigationWithNestedExpand() {
        NavigationSingle<Manager, Person> m = new NavigationSingle<Manager, Person>("manager", Manager.class);
        NavigationCollection<Manager, Manager> sub = new NavigationCollection<Manager, Manager>("subordinates", Manager.class);
        String text = m.expand(sub.select(MGR_NAME)).text();
        assertTrue("expected: " + text,
                text.equals("manager($expand=subordinates($select=name))"));
    }

    @Test
    public void navigationWithTopAndSkip() {
        NavigationCollection<Person, Person> f = new NavigationCollection<Person, Person>("friends", Person.class);
        String text = f.top(5).skip(2).text();
        assertEquals("friends($top=5;$skip=2)", text);
    }

    @Test
    public void navigationWithOrderBy() {
        NavigationCollection<Person, Person> f = new NavigationCollection<Person, Person>("friends", Person.class);
        String text = f.orderBy(NAME.asc()).text();
        assertEquals("friends($orderby=name asc)", text);
    }

    @Test
    public void navigationAnyStringRendersCorrectly() {
        NavigationCollection<Person, Person> f = new NavigationCollection<Person, Person>("friends", Person.class);
        FilterExpression<Person> expr = f.any("name eq 'bob'");
        assertEquals("friends/any(name eq 'bob')", expr.text());
    }

    @Test
    public void navigationAllStringRendersCorrectly() {
        NavigationCollection<Person, Person> f = new NavigationCollection<Person, Person>("friends", Person.class);
        FilterExpression<Person> expr = f.all("name eq 'bob'");
        assertEquals("friends/all(name eq 'bob')", expr.text());
    }

    @Test
    public void navigationAnyWithLambdaRendersCorrectly() {
        NavigationCollection<Person, Person> f = new NavigationCollection<Person, Person>("friends", Person.class);
        FilterExpression<Person> expr = f.any(p -> p.string("name").eq("bob"));
        // The AnyAllBuilder prefixes property names with x/ to render the lambda variable
        assertEquals("friends/any(x:x/name eq 'bob')", expr.text());
    }

    @Test
    public void navigationCountIsRendable() {
        NavigationCollection<Person, Person> f = new NavigationCollection<Person, Person>("friends", Person.class);
        Property<Integer, Person> count = f.count();
        assertEquals("friends/$count", count.name());
    }
}
