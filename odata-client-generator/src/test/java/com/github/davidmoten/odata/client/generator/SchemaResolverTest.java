package com.github.davidmoten.odata.client.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TDataServices;
import org.oasisopen.odata.csdl.v4.TEdmx;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

public class SchemaResolverTest {

    @Test
    public void testResolveReferences() throws Exception {
        File testDir = new File("src/test/odata/references");
        File mainModel = new File(testDir, "main-model.xml");

        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller
                .unmarshal(new StreamSource(new FileInputStream(mainModel)), TEdmx.class)
                .getValue();

        // Verify the primary EDMX has a Reference
        assertNotNull(t.getReference());
        assertEquals(1, t.getReference().size());
        assertEquals("shared-model.xml", t.getReference().get(0).getUri());

        // Resolve references
        List<Schema> schemas = SchemaResolver.resolve(t, testDir);

        // Should have schemas from both files
        Set<String> namespaces = schemas.stream()
                .map(Schema::getNamespace)
                .collect(Collectors.toSet());

        assertTrue("Should contain Main.App schema", namespaces.contains("Main.App"));
        assertTrue("Should contain Shared.Models schema", namespaces.contains("Shared.Models"));
        assertEquals("Should have 2 schemas total", 2, schemas.size());
    }

    @Test
    public void testResolveAliases() throws Exception {
        File testDir = new File("src/test/odata/references");
        File mainModel = new File(testDir, "main-model.xml");

        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller
                .unmarshal(new StreamSource(new FileInputStream(mainModel)), TEdmx.class)
                .getValue();

        List<Schema> schemas = SchemaResolver.resolve(t, testDir);

        // Find the Shared.Models schema and verify alias was applied
        Schema sharedSchema = schemas.stream()
                .filter(s -> "Shared.Models".equals(s.getNamespace()))
                .findFirst()
                .orElse(null);

        assertNotNull("Shared.Models schema should exist", sharedSchema);
        assertEquals("Alias should be 'shared'", "shared", sharedSchema.getAlias());
    }

    @Test
    public void testNoReferencesReturnsOriginalSchemas() throws Exception {
        File testDir = new File("src/test/odata/references");
        File sharedModel = new File(testDir, "shared-model.xml");

        JAXBContext c = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = c.createUnmarshaller();
        TEdmx t = unmarshaller
                .unmarshal(new StreamSource(new FileInputStream(sharedModel)), TEdmx.class)
                .getValue();

        // shared-model.xml has no references
        assertTrue(t.getReference().isEmpty());

        List<Schema> schemas = SchemaResolver.resolve(t, testDir);
        assertEquals("Should have 1 schema (no references)", 1, schemas.size());
        assertEquals("Shared.Models", schemas.get(0).getNamespace());
    }
}
