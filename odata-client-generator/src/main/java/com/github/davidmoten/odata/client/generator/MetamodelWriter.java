package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.odata.client.generator.model.ComplexType;
import com.github.davidmoten.odata.client.generator.model.EntityType;
import com.github.davidmoten.odata.client.generator.model.Structure;

/**
 * Writes the JPA-style static metamodel class for an entity or complex type.
 *
 * <p>
 * For each generated entity (e.g. {@code User}), a sibling class {@code User_} is
 * generated in the same package containing one {@code public static final} property or
 * navigation descriptor per CSDL property. The descriptors can be used in place of
 * stringly-typed property names in {@code $select}, {@code $filter}, {@code $expand}
 * and {@code $orderby}.
 */
public final class MetamodelWriter {

    private static final String IMPORTSHERE = "IMPORTSHERE";

    private final Names names;

    public MetamodelWriter(Names names) {
        this.names = names;
    }

    public void write(Schema schema, TEntityType entityType) {
        EntityType t = new EntityType(schema, entityType, names);
        writeStructure(schema, t);
    }

    public void writeComplex(Schema schema, TComplexType complexType) {
        ComplexType t = new ComplexType(schema, complexType, names);
        writeStructure(schema, t);
    }

    private void writeStructure(Schema schema, Structure<?> t) {
        SchemaOptions opts = names.getOptions(schema);
        if (!opts.typeSafe()) {
            return;
        }
        t.getClassFile().getParentFile().mkdirs();
        File out = names.getClassFileMetamodel(schema, t.getName());
        out.getParentFile().mkdirs();
        Imports imports = new Imports(names.getFullClassNameMetamodel(schema, t.getName()));
        Indent indent = new Indent();
        StringWriter w = new StringWriter();
        try (PrintWriter p = new PrintWriter(w)) {
            p.format("package %s;\n\n", t.getPackage());
            p.format(IMPORTSHERE);
            p.format("public final class %s {\n", names.getSimpleClassNameMetamodel(schema, t.getName()));
            indent.right();

            // Properties
            for (TProperty prop : t.getProperties()) {
                String propName = prop.getName();
                String typeName = names.getType(prop);
                String edmType = primaryType(typeName);
                PropertyType pt = classify(typeName, imports);
                String javaTypeClass = pt.javaClass;
                String initializer;
                switch (pt.factory) {
                case STRING:
                    initializer = String.format("Property.string(\"%s\")", propName);
                    break;
                case BOOL:
                    initializer = String.format("Property.bool(\"%s\")", propName);
                    break;
                case INT32:
                    initializer = String.format("Property.int32(\"%s\")", propName);
                    break;
                case INT64:
                    initializer = String.format("Property.int64(\"%s\")", propName);
                    break;
                case FLOAT:
                    initializer = String.format("Property.float_(\"%s\")", propName);
                    break;
                case DOUBLE:
                    initializer = String.format("Property.double_(\"%s\")", propName);
                    break;
                case DECIMAL:
                    initializer = String.format("Property.decimal(\"%s\")", propName);
                    break;
                case DATETIME:
                    initializer = String.format("Property.datetime(\"%s\")", propName);
                    break;
                case DATE:
                    initializer = String.format("Property.date(\"%s\")", propName);
                    break;
                case TIME:
                    initializer = String.format("Property.time(\"%s\")", propName);
                    break;
                case BINARY:
                    initializer = String.format("Property.binary(\"%s\")", propName);
                    break;
                case GUID:
                    initializer = String.format("Property.guid(\"%s\")", propName);
                    break;
                case ENUM:
                case COMPLEX:
                    initializer = String.format("Property.create(\"%s\", %s.class)", propName,
                            javaTypeClass);
                    break;
                case COLLECTION_PRIMITIVE:
                case COLLECTION_COMPLEX:
                case COLLECTION_ENUM:
                    initializer = String.format("Property.createCollection(\"%s\", %s.class, \"%s\")",
                            propName, javaTypeClass, edmType);
                    break;
                default:
                    initializer = String.format("Property.create(\"%s\", %s.class)", propName,
                            javaTypeClass);
                    break;
                }
                p.format("\n%spublic static final %s<%s, %s> %s = %s;\n", indent,
                        imports.add("com.github.davidmoten.odata.client.Property"), javaTypeClass,
                        t.getSimpleClassName(), Names.getIdentifier(propName), initializer);
            }

            // Navigation properties
            for (TNavigationProperty nav : t.getNavigationProperties()) {
                String navName = nav.getName();
                String typeName = names.getType(nav);
                String targetType = targetClassName(schema, typeName, imports);
                boolean isColl = names.isCollection(nav);
                String collectionOrSingle = isColl
                        ? imports.add("com.github.davidmoten.odata.client.NavigationCollection")
                        : imports.add("com.github.davidmoten.odata.client.NavigationSingle");
                p.format("\n%spublic static final %s<%s, %s> %s = new %s<%s, %s>(\"%s\", %s.class);\n",
                        indent, collectionOrSingle, targetType, t.getSimpleClassName(),
                        Names.getIdentifier(navName), collectionOrSingle, targetType, t.getSimpleClassName(),
                        navName, targetType);
            }

            p.format("\n%sprivate %s() {}\n", indent,
                    names.getSimpleClassNameMetamodel(schema, t.getName()));
            p.format("%s}\n", indent.left());
            writeToFile(imports, w, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String primaryType(String type) {
        if (type.startsWith("Collection(") && type.endsWith(")")) {
            return type.substring("Collection(".length(), type.length() - 1);
        }
        return type;
    }

    private String targetClassName(Schema schema, String type, Imports imports) {
        if (names.isCollection(type)) {
            type = names.getInnerType(type);
        }
        return imports.add(names.getFullClassNameFromTypeWithNamespace(type));
    }

    private PropertyType classify(String type, Imports imports) {
        if (type.startsWith("Collection(") && type.endsWith(")")) {
            String inner = type.substring("Collection(".length(), type.length() - 1);
            switch (inner) {
            case "Edm.String":
            case "Edm.Binary":
            case "Edm.Boolean":
            case "Edm.Byte":
            case "Edm.Date":
            case "Edm.DateTimeOffset":
            case "Edm.Decimal":
            case "Edm.Double":
            case "Edm.Duration":
            case "Edm.Guid":
            case "Edm.Int16":
            case "Edm.Int32":
            case "Edm.Int64":
            case "Edm.SByte":
            case "Edm.Single":
            case "Edm.Stream":
            case "Edm.Time":
            case "Edm.TimeOfDay":
                return new PropertyType(javaClassForEdm(inner), Factory.COLLECTION_PRIMITIVE);
            default:
                if (inner.contains(".")) {
                    String simpleName = imports
                            .add(names.getFullClassNameFromTypeWithNamespace(inner));
                    return new PropertyType(simpleName, Factory.COLLECTION_COMPLEX);
                }
                return new PropertyType("Object", Factory.COLLECTION_PRIMITIVE);
            }
        }
        switch (type) {
        case "Edm.String":
            return new PropertyType("String", Factory.STRING);
        case "Edm.Boolean":
            return new PropertyType("Boolean", Factory.BOOL);
        case "Edm.Byte":
        case "Edm.SByte":
            return new PropertyType("Byte", Factory.INT32);
        case "Edm.Int16":
            return new PropertyType("Short", Factory.INT32);
        case "Edm.Int32":
            return new PropertyType("Integer", Factory.INT32);
        case "Edm.Int64":
            return new PropertyType("Long", Factory.INT64);
        case "Edm.Single":
            return new PropertyType("Float", Factory.FLOAT);
        case "Edm.Double":
            return new PropertyType("Double", Factory.DOUBLE);
        case "Edm.Decimal":
            return new PropertyType("java.math.BigDecimal", Factory.DECIMAL);
        case "Edm.DateTimeOffset":
            return new PropertyType("java.time.Instant", Factory.DATETIME);
        case "Edm.Date":
            return new PropertyType("java.time.LocalDate", Factory.DATE);
        case "Edm.Time":
        case "Edm.TimeOfDay":
            return new PropertyType("java.time.LocalTime", Factory.TIME);
        case "Edm.Duration":
            return new PropertyType("java.time.Duration", Factory.OTHER);
        case "Edm.Binary":
        case "Edm.Stream":
            return new PropertyType("byte[]", Factory.BINARY);
        case "Edm.Guid":
            return new PropertyType("java.util.UUID", Factory.GUID);
        default:
            if (!type.startsWith("Edm.") && type.contains(".")) {
                String simpleName = imports
                        .add(names.getFullClassNameFromTypeWithNamespace(type));
                return new PropertyType(simpleName, Factory.COMPLEX);
            }
            return new PropertyType("Object", Factory.OTHER);
        }
    }

    private static String javaClassForEdm(String edm) {
        switch (edm) {
        case "Edm.String":
            return "String";
        case "Edm.Boolean":
            return "Boolean";
        case "Edm.Byte":
        case "Edm.SByte":
            return "Byte";
        case "Edm.Int16":
            return "Short";
        case "Edm.Int32":
            return "Integer";
        case "Edm.Int64":
            return "Long";
        case "Edm.Single":
            return "Float";
        case "Edm.Double":
            return "Double";
        case "Edm.Decimal":
            return "java.math.BigDecimal";
        case "Edm.DateTimeOffset":
            return "java.time.Instant";
        case "Edm.Date":
            return "java.time.LocalDate";
        case "Edm.Time":
        case "Edm.TimeOfDay":
            return "java.time.LocalTime";
        case "Edm.Duration":
            return "java.time.Duration";
        case "Edm.Binary":
        case "Edm.Stream":
            return "byte[]";
        case "Edm.Guid":
            return "java.util.UUID";
        default:
            return "Object";
        }
    }

    private enum Factory {
        STRING, BOOL, INT32, INT64, FLOAT, DOUBLE, DECIMAL, DATETIME, DATE, TIME, BINARY, GUID, ENUM, COMPLEX,
        COLLECTION_PRIMITIVE, COLLECTION_COMPLEX, COLLECTION_ENUM, OTHER
    }

    private static final class PropertyType {
        final String javaClass;
        final Factory factory;

        PropertyType(String javaClass, Factory factory) {
            this.javaClass = javaClass;
            this.factory = factory;
        }
    }

    private void writeToFile(Imports imports, StringWriter w, File classFile) throws IOException {
        byte[] bytes = w //
                .toString() //
                .replace(IMPORTSHERE, imports.toString()) //
                .getBytes(StandardCharsets.UTF_8);
        Files.write(classFile.toPath(), bytes);
    }
}
