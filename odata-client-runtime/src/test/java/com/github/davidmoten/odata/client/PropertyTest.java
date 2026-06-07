package com.github.davidmoten.odata.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Unit tests for the type-safe metamodel classes ({@link Property},
 * {@link NavigationSingle}, {@link NavigationCollection}, {@link FilterExpression},
 * {@link OrderBy}).
 */
public class PropertyTest {

    public static final class TestEntity {
        public String name;
        public Integer age;
    }

    public static final Property<String, TestEntity> NAME = Property.create("Name", String.class);
    public static final Property<Integer, TestEntity> AGE = Property.create("Age", Integer.class);

    @Test
    public void propertyToStringReturnsWireName() {
        assertEquals("Name", NAME.toString());
        assertEquals("Age", AGE.toString());
    }

    @Test
    public void propertyFactoryString() {
        Property<String, TestEntity> p = Property.string("firstName");
        assertEquals("firstName", p.name());
        assertEquals(String.class, p.type());
        assertEquals("Edm.String", p.edmType());
    }

    @Test
    public void propertyFactoryBool() {
        Property<Boolean, TestEntity> p = Property.bool("active");
        assertEquals(Boolean.class, p.type());
        assertEquals("Edm.Boolean", p.edmType());
    }

    @Test
    public void propertyFactoryInt32() {
        Property<Integer, TestEntity> p = Property.int32("age");
        assertEquals(Integer.class, p.type());
        assertEquals("Edm.Int32", p.edmType());
    }

    @Test
    public void propertyFactoryInt64() {
        Property<Long, TestEntity> p = Property.int64("ts");
        assertEquals(Long.class, p.type());
        assertEquals("Edm.Int64", p.edmType());
    }

    @Test
    public void propertyFactoryGuid() {
        Property<UUID, TestEntity> p = Property.guid("id");
        assertEquals(UUID.class, p.type());
        assertEquals("Edm.Guid", p.edmType());
    }

    @Test
    public void propertyFactoryDateTime() {
        Property<Instant, TestEntity> p = Property.datetime("created");
        assertEquals(Instant.class, p.type());
        assertEquals("Edm.DateTimeOffset", p.edmType());
    }

    @Test
    public void propertyFactoryDate() {
        Property<LocalDate, TestEntity> p = Property.date("dob");
        assertEquals(LocalDate.class, p.type());
        assertEquals("Edm.Date", p.edmType());
    }

    @Test
    public void propertyFactoryTime() {
        Property<LocalTime, TestEntity> p = Property.time("open");
        assertEquals(LocalTime.class, p.type());
        assertEquals("Edm.TimeOfDay", p.edmType());
    }

    @Test
    public void eqProducesCorrectFilterText() {
        assertEquals("Name eq 'bob'", NAME.eq("bob").text());
    }

    @Test
    public void neProducesCorrectFilterText() {
        assertEquals("Age ne 0", AGE.ne(0).text());
    }

    @Test
    public void gtGeLtLeProduceCorrectFilterText() {
        assertEquals("Age gt 18", AGE.gt(18).text());
        assertEquals("Age ge 18", AGE.ge(18).text());
        assertEquals("Age lt 65", AGE.lt(65).text());
        assertEquals("Age le 65", AGE.le(65).text());
    }

    @Test
    public void startsWithEndsWithContainsProduceCorrectFilterText() {
        assertEquals("startswith(Name,'bob')", NAME.startsWith("bob").text());
        assertEquals("endswith(Name,'bob')", NAME.endsWith("bob").text());
        assertEquals("contains(Name,'bob')", NAME.contains("bob").text());
    }

    @Test
    public void inProducesCorrectFilterText() {
        assertEquals("Age in (1,2,3)", AGE.in(1, 2, 3).text());
    }

    @Test
    public void andCombinesWithAnd() {
        FilterExpression<TestEntity> f = NAME.eq("bob").and(AGE.gt(18));
        assertEquals("Name eq 'bob' and Age gt 18", f.text());
    }

    @Test
    public void orCombinesWithOr() {
        FilterExpression<TestEntity> f = NAME.eq("bob").or(AGE.gt(18));
        assertEquals("Name eq 'bob' or Age gt 18", f.text());
    }

    @Test
    public void notPrependsNot() {
        FilterExpression<TestEntity> f = NAME.eq("bob").not();
        assertEquals("not Name eq 'bob'", f.text());
    }

    @Test
    public void complexExpressionIsParenthesized() {
        FilterExpression<TestEntity> f = NAME.eq("a").or(NAME.eq("b")).and(AGE.gt(0));
        // The (a or b) side needs parentheses so AND binds tighter than expected
        assertTrue("expected parens around or: " + f.text(),
                f.text().contains("(Name eq 'a' or Name eq 'b')"));
    }

    @Test
    public void ascDescProduceCorrectOrderByText() {
        assertEquals("Name asc", NAME.asc().text());
        assertEquals("Name desc", NAME.desc().text());
    }

    @Test
    public void orderByJoinJoinsWithComma() {
        String joined = OrderBy.join(NAME.asc(), AGE.desc());
        assertEquals("Name asc,Age desc", joined);
    }

    @Test
    public void stringLiteralIsEscaped() {
        assertEquals("Name eq 'o''brien'", NAME.eq("o'brien").text());
    }

    @Test
    public void guidLiteralIsUnquotedUuid() {
        UUID u = UUID.fromString("12345678-1234-1234-1234-123456789012");
        Property<UUID, TestEntity> id = Property.guid("Id");
        assertEquals("Id eq 12345678-1234-1234-1234-123456789012", id.eq(u).text());
    }
}
