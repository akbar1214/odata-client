package com.github.davidmoten.odata.client.generator;

import java.util.Collections;
import java.util.Set;

public class SchemaOptions {
    public final String namespace;
    public final String pkg;
    public final String packageSuffixEnum;
    public final String packageSuffixEntity;
    public final String packageSuffixComplexType;
    public final String packageSuffixEntityRequest;
    public final String packageSuffixCollectionRequest;
    public final String packageSuffixContainer;
    public final String packageSuffixSchema;
    public final String simpleClassNameSchema;
    public final String collectionRequestClassSuffix;
    public final String entityRequestClassSuffix;
    public final boolean pageComplexTypes;
    public final boolean failOnMissingEntitySet;
    public final Set<String> enumDefaultValues;
    public final boolean typeSafe;
    public final String metamodelSuffix;

    // TODO make configurable
    private static final String PACKAGE_SUFFIX_COMPLEX_TYPE_COLLECTION_REQUEST = ".complex.collection.request";
    private static final String DEFAULT_METAMODEL_SUFFIX = "_";

    public SchemaOptions(String namespace, String pkg, String packageSuffixEnum,
            String packageSuffixEntity, String packageSuffixComplexType,
            String packageSuffixEntityRequest, String packageSuffixCollectionRequest,
            String packageSuffixContainer,
            String packageSuffixSchema, String simpleClassNameSchema,
            String collectionRequestClassSuffix, String entityRequestClassSuffix,
            boolean pageComplexTypes, boolean failOnMissingEntitySet, //
            Set<String> enumDefaultValues) {
        this(namespace, pkg, packageSuffixEnum, packageSuffixEntity, packageSuffixComplexType,
                packageSuffixEntityRequest, packageSuffixCollectionRequest, packageSuffixContainer,
                packageSuffixSchema, simpleClassNameSchema, collectionRequestClassSuffix,
                entityRequestClassSuffix, pageComplexTypes, failOnMissingEntitySet, enumDefaultValues,
                false, DEFAULT_METAMODEL_SUFFIX);
    }

    public SchemaOptions(String namespace, String pkg, String packageSuffixEnum,
            String packageSuffixEntity, String packageSuffixComplexType,
            String packageSuffixEntityRequest, String packageSuffixCollectionRequest,
            String packageSuffixContainer,
            String packageSuffixSchema, String simpleClassNameSchema,
            String collectionRequestClassSuffix, String entityRequestClassSuffix,
            boolean pageComplexTypes, boolean failOnMissingEntitySet, //
            Set<String> enumDefaultValues, boolean typeSafe, String metamodelSuffix) {
        this.namespace = namespace;
        this.pkg = pkg;
        this.packageSuffixEnum = packageSuffixEnum;
        this.packageSuffixEntity = packageSuffixEntity;
        this.packageSuffixComplexType = packageSuffixComplexType;
        this.packageSuffixEntityRequest = packageSuffixEntityRequest;
        this.packageSuffixCollectionRequest = packageSuffixCollectionRequest;
        this.packageSuffixContainer = packageSuffixContainer;
        this.packageSuffixSchema = packageSuffixSchema;
        this.simpleClassNameSchema = simpleClassNameSchema;
        this.collectionRequestClassSuffix = collectionRequestClassSuffix;
        this.entityRequestClassSuffix = entityRequestClassSuffix;
        this.pageComplexTypes = pageComplexTypes;
        this.failOnMissingEntitySet = failOnMissingEntitySet;
        this.enumDefaultValues = enumDefaultValues;
        this.typeSafe = typeSafe;
        this.metamodelSuffix = metamodelSuffix;
    }

    public SchemaOptions(String namespace, String pkg) {
        this(namespace, pkg, ".enums", ".entity", ".complex", ".entity.request",
                ".entity.collection.request",
                ".container", ".schema", "SchemaInfo", "CollectionRequest", "Request", true, true, Collections.emptySet(),
                false, DEFAULT_METAMODEL_SUFFIX);
    }

    public String pkg() {
        return pkg;
    }

    public String packageSuffixEnum() {
        return packageSuffixEnum;
    }

    public String packageSuffixEntity() {
        return packageSuffixEntity;
    }

    public String packageSuffixComplexType() {
        return packageSuffixComplexType;
    }

    public String packageSuffixEntityRequest() {
        return packageSuffixEntityRequest;
    }

    public String packageSuffixEntityCollectionRequest() {
        return packageSuffixCollectionRequest;
    }

    public String packageSuffixContainer() {
        return packageSuffixContainer;
    }

    public String packageSuffixSchema() {
        return packageSuffixSchema;
    }

    public String simpleClassNameSchema() {
        return simpleClassNameSchema;
    }

    public String collectionRequestClassSuffix() {
        return collectionRequestClassSuffix;
    }

    public String entityRequestClassSuffix() {
        return entityRequestClassSuffix;
    }

    public boolean pageComplexTypes() {
        return pageComplexTypes;
    }

    public String packageSuffixComplexTypeCollectionRequest() {
        return PACKAGE_SUFFIX_COMPLEX_TYPE_COLLECTION_REQUEST;
    }

    public String packageSuffixEntitySet() {
        // TODO make configurable
        return packageSuffixEntity() + ".set";
    }
    
    public Set<String> enumDefaultValues() {
        return enumDefaultValues;
    }

    public boolean typeSafe() {
        return typeSafe;
    }

    public String metamodelSuffix() {
        return metamodelSuffix == null ? DEFAULT_METAMODEL_SUFFIX : metamodelSuffix;
    }

    public String metamodelClassName(String baseName) {
        return baseName + metamodelSuffix();
    }

}
