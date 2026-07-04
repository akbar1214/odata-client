package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TDataServices;
import org.oasisopen.odata.csdl.v4.TEdmx;
import org.oasisopen.odata.csdl.v4.TInclude;
import org.oasisopen.odata.csdl.v4.TReference;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

/**
 * Resolves {@code edmx:Reference} and {@code edmx:Include} directives in OData EDMX
 * metadata files, merging schemas from referenced external metadata files into a
 * single flat list.
 */
public final class SchemaResolver {

    private static final Logger LOG = Logger.getLogger(SchemaResolver.class.getName());

    private SchemaResolver() {
    }

    /**
     * Resolves all {@code edmx:Reference} elements in the given EDMX, fetching and
     * merging schemas from the referenced external metadata files.
     *
     * @param primaryEdmx the primary EDMX document already unmarshalled
     * @param baseDir     the directory of the primary metadata file, used to resolve
     *                   relative URIs; may be {@code null} if the metadata was loaded
     *                   from the classpath
     * @return a new list containing schemas from the primary EDMX plus all referenced
     *         external EDMX files
     */
    public static List<Schema> resolve(TEdmx primaryEdmx, File baseDir) {
        List<Schema> allSchemas = new ArrayList<>(primaryEdmx.getDataServices().getSchema());
        List<TReference> references = primaryEdmx.getReference();

        if (references == null || references.isEmpty()) {
            return allSchemas;
        }

        for (TReference ref : references) {
            String uri = ref.getUri();
            if (uri == null || uri.isEmpty()) {
                LOG.warning("Skipping edmx:Reference with empty Uri");
                continue;
            }

            try {
                File refFile = resolveUriToFile(uri, baseDir);
                LOG.info("Resolving edmx:Reference URI=" + uri + " -> " + refFile.getAbsolutePath());

                TEdmx refEdmx = unmarshalEdmx(refFile);
                List<Schema> refSchemas = refEdmx.getDataServices().getSchema();

                applyIncludes(ref, refSchemas);

                allSchemas.addAll(refSchemas);
            } catch (Exception e) {
                LOG.warning("Failed to resolve edmx:Reference URI=" + uri + ": " + e.getMessage());
            }
        }

        return allSchemas;
    }

    /**
     * Applies {@code edmx:Include} directives from a reference, setting the alias on
     * matching schemas.
     */
    private static void applyIncludes(TReference ref, List<Schema> refSchemas) {
        for (Object item : ref.getIncludeOrIncludeAnnotationsOrAnnotation()) {
            if (item instanceof TInclude) {
                TInclude include = (TInclude) item;
                String namespace = include.getNamespace();
                String alias = include.getAlias();

                if (namespace != null) {
                    for (Schema schema : refSchemas) {
                        if (namespace.equals(schema.getNamespace()) && alias != null) {
                            schema.setAlias(alias);
                            LOG.info("Applied alias '" + alias + "' to schema namespace=" + namespace);
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolves a URI string to a local {@link File}. Supports absolute and relative
     * file paths. If {@code baseDir} is provided, relative paths are resolved against
     * it; otherwise the path is used as-is.
     */
    private static File resolveUriToFile(String uri, File baseDir) {
        // Strip any fragment identifiers
        int fragmentIndex = uri.indexOf('#');
        if (fragmentIndex >= 0) {
            uri = uri.substring(0, fragmentIndex);
        }

        File file = new File(uri);
        if (file.isAbsolute()) {
            return file;
        }

        if (baseDir != null) {
            return new File(baseDir, uri).getAbsoluteFile();
        }

        return file.getAbsoluteFile();
    }

    /**
     * Unmarshals an EDMX file into a {@link TEdmx} object.
     */
    private static TEdmx unmarshalEdmx(File file) throws Exception {
        JAXBContext context = JAXBContext.newInstance(TDataServices.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        try (InputStream is = new FileInputStream(file)) {
            TEdmx edmx = unmarshaller.unmarshal(new StreamSource(is), TEdmx.class).getValue();
            if (edmx == null || edmx.getDataServices() == null) {
                throw new IllegalStateException("Invalid EDMX file: " + file.getAbsolutePath());
            }
            return edmx;
        }
    }
}
