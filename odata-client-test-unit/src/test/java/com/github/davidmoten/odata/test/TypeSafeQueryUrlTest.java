package com.github.davidmoten.odata.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.odata.client.RequestHeader;

import test5.container.Test5Service;
import test5.entity.Product_;

/**
 * Round-trip tests that assert the type-safe query API and the string-based query API
 * produce byte-for-byte identical URLs. Uses the {@code expectRequest} pattern of the
 * existing test fixtures.
 */
public class TypeSafeQueryUrlTest {

    @Test
    public void typedSelectEqualsStringSelect() {
        // Reference the metamodel to force the generator to include it in compilation
        // path; ensures renames in the CSDL would cause a compile error here too.
        @SuppressWarnings("unused")
        String firstName = Product_.name.name();
        Test5Service client = Test5Service.test() //
                .expectRequest("/Products?$select=Name") //
                .withResponse("/response-empty-collection.json") //
                .withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        client.products().select(Product_.name).toList();
    }

    @Test
    public void stringSelectStillWorks() {
        Test5Service client = Test5Service.test() //
                .expectRequest("/Products?$select=Name") //
                .withResponse("/response-empty-collection.json") //
                .withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        client.products().select("Name").toList();
    }

    @Test
    public void typedFilterEqualsStringFilter() {
        Test5Service client = Test5Service.test() //
                .expectRequest("/Products?$filter=Name%20eq%20'bob'") //
                .withResponse("/response-empty-collection.json") //
                .withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        client.products().filter(Product_.name.eq("bob")).toList();
    }

    @Test
    public void stringFilterStillWorks() {
        Test5Service client = Test5Service.test() //
                .expectRequest("/Products?$filter=Name%20eq%20'bob'") //
                .withResponse("/response-empty-collection.json") //
                .withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        client.products().filter("Name eq 'bob'").toList();
    }

    @Test
    public void typedAndFilterEqualsStringAndFilter() {
        // just a sanity check that the fluent and() works
        Test5Service client = Test5Service.test() //
                .expectRequest(
                        "/Products?$filter=Name%20eq%20'bob'%20and%20ID%20gt%2010") //
                .withResponse("/response-empty-collection.json") //
                .withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        client.products().filter(Product_.name.eq("bob").and(Product_.ID.gt(10))).toList();
    }

    @Test
    public void typedOrderByEqualsStringOrderBy() {
        Test5Service client = Test5Service.test() //
                .expectRequest(
                        "/Products?$orderby=Name%20desc%2CID%20asc") //
                .withResponse("/response-empty-collection.json") //
                .withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        client.products().orderBy(Product_.name.desc(), Product_.ID.asc()).toList();
    }

    @Test
    public void typedSelectAndFilterCombined() {
        // ensure the typed select and filter compose into the same URL as the
        // string equivalents
        Test5Service stringClient = Test5Service.test() //
                .expectRequest(
                        "/Products?$filter=Name%20eq%20'bob'&$select=Name") //
                .withResponse("/response-empty-collection.json") //
                .withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        stringClient.products().select("Name").filter("Name eq 'bob'").toList();

        Test5Service typedClient = Test5Service.test() //
                .expectRequest(
                        "/Products?$filter=Name%20eq%20'bob'&$select=Name") //
                .withResponse("/response-empty-collection.json") //
                .withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_MINIMAL, RequestHeader.ODATA_VERSION) //
                .build();
        typedClient.products().select(Product_.name).filter(Product_.name.eq("bob")).toList();
    }
}
