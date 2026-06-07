package com.github.davidmoten.odata.client.generator;

import java.util.List;

public final class Options {

    private final String outputDirectory;

    private final List<SchemaOptions> schemaOptions;

    private final boolean typeSafe;

    public Options(String outputDirectory, List<SchemaOptions> schemaOptions) {
        this(outputDirectory, schemaOptions, false);
    }

    public Options(String outputDirectory, List<SchemaOptions> schemaOptions, boolean typeSafe) {
        this.outputDirectory = outputDirectory;
        this.schemaOptions = schemaOptions;
        this.typeSafe = typeSafe;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public SchemaOptions getSchemaOptions(String namespace) {
        return schemaOptions //
                .stream() //
                .filter(x -> namespace.equals(x.namespace)) //
                .findFirst() //
                .<IllegalArgumentException>orElseThrow(() -> new IllegalArgumentException(
                        "namespace not found in schemaOptions: " + namespace));
    }

    /**
     * Returns true if generation should emit type-safe metamodel classes ({@code *_})
     * and typed overloads on the generated request builders.
     */
    public boolean isTypeSafe() {
        return typeSafe;
    }

}