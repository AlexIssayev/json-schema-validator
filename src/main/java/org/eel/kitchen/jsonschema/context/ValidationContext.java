/*
 * Copyright (c) 2011, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.eel.kitchen.jsonschema.context;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.MissingNode;
import org.eel.kitchen.jsonschema.JsonValidator;
import org.eel.kitchen.jsonschema.ValidationReport;
import org.eel.kitchen.jsonschema.base.AlwaysFalseValidator;
import org.eel.kitchen.jsonschema.base.Validator;
import org.eel.kitchen.jsonschema.factories.KeywordFactory;
import org.eel.kitchen.jsonschema.factories.SyntaxFactory;
import org.eel.kitchen.jsonschema.keyword.KeywordValidator;
import org.eel.kitchen.jsonschema.keyword.RefKeywordValidator;
import org.eel.kitchen.jsonschema.syntax.SyntaxValidator;
import org.eel.kitchen.util.NodeType;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Class passed to all {@link Validator} implementations. This class is
 * responsible for several things:</p>
 * <ul>
 *     <li>checking the schema correctness (using {@link SyntaxValidator}
 *     instances);</li>
 *     <li>create validator instances;</li>
 *     <li>resolve {@code $ref} (see {@link RefKeywordValidator}) <b>and</b>
 *     detect ref looping;</li>
 *     <li>provide {@link ValidationReport} instances;</li>
 *     <li>provide other instances of itself.</li>
 * </ul>
 */
public final class ValidationContext
{
    /**
     * Pattern matching a JSON path (could probably be refined). Not anchored
     * since it uses {@link Matcher#matches()}.
     */
    private static final Pattern JSONPATH_REGEX
        = Pattern.compile("(?:/[^/]++)*+");

    /**
     * Pattern to split JSON Path components
     */
    private static final Pattern SPLIT_PATTERN = Pattern.compile("/");

    /**
     * The root schema of this validation context
     */
    private JsonNode rootSchema;

    /**
     * The schema used by the current context
     */
    private JsonNode schemaNode;

    /**
     * The JSON path within the instance for the current context
     */
    private String path;

    /**
     * The keyword validator factory
     */
    private KeywordFactory keywordFactory;

    /**
     * The syntax validator factory
     */
    private SyntaxFactory syntaxFactory;

    private final Map<URI, JsonNode> locators = new HashMap<URI, JsonNode>();

    /**
     * The ref result lookups for this {@link #path},
     * used for ref looping detection
     */
    private Set<JsonNode> refLookups;

    /**
     * The default constructor, which is private by design
     */
    private ValidationContext()
    {
    }

    /**
     * The public constructor. Only used from {@link JsonValidator}. On
     * initial setup, the argument is the root schema, see #rootSchema.
     *
     * @param schema the root schema used by this context
     */
    public ValidationContext(final JsonNode schema)
    {
        path = "#";
        rootSchema = schema;
        schemaNode = schema;

        keywordFactory = new KeywordFactory();
        syntaxFactory = new SyntaxFactory();
        refLookups = new HashSet<JsonNode>();
    }

    /**
     * Unregister all validators ({@link SyntaxValidator} and
     * {@link KeywordValidator} for a given keyword. Note that the null case
     * is handled in the factories themselves.
     *
     * @param keyword the victim
     */
    public void unregisterValidator(final String keyword)
    {
        syntaxFactory.unregisterValidator(keyword);
        keywordFactory.unregisterValidator(keyword);
    }

    /**
     * Register a validator for a new keyword, or replace the existing
     * validators by new ones for ths keyword.
     *
     * @param keyword the new/modified keyword
     * @param sv the {@link SyntaxValidator} implementation
     * @param kv the {@link KeywordValidator} implementation
     * @param types the list of JSON types the keyword validator is able to
     * validate
     * @throws IllegalArgumentException one validator is null,
     * or the type list is empty
     */
    public void registerValidator(final String keyword,
        final Class<? extends SyntaxValidator> sv,
        final Class<? extends KeywordValidator> kv, final NodeType... types)
    {
        if (sv == null)
            throw new IllegalArgumentException("syntax validator is null");
        syntaxFactory.registerValidator(keyword, sv);

        if (kv == null)
            throw new IllegalArgumentException("keyword validator is null");

        if (types.length == 0)
            throw new IllegalArgumentException("validator wouldn't match any "
                + "JSON instance (empty type set)");

        keywordFactory.registerValidator(keyword, kv, types);
    }

    /**
>>>>>>> ValidationContext: implement .registerValidator
     * Return the schema node of this context -- <b>not</b> the root schema!
     *
     * @return the matching {@link JsonNode}
     */
    public JsonNode getSchemaNode()
    {
        return schemaNode;
    }

    /**
     * Spawn a new context from this context.
     *
     * @param subPath the relative path to use from the current #path
     * @param subSchema the schema node to use for this context
     * @return the new context
     */
    public ValidationContext createContext(final String subPath,
        final JsonNode subSchema)
    {
        final String newPath = subPath == null || subPath.isEmpty()
            ? path
            : String.format("%s/%s", path, subPath);

        final ValidationContext other = new ValidationContext();
        other.path = newPath;
        other.rootSchema = rootSchema;
        other.schemaNode = subSchema;
        other.keywordFactory = keywordFactory;
        other.syntaxFactory = syntaxFactory;
        other.refLookups = new HashSet<JsonNode>();
        if (newPath.equals(path))
            other.refLookups.addAll(refLookups);
        other.locators.putAll(locators);
        return other;
    }

    /**
     * Shortcut to call {@link #createContext(String, JsonNode)}
     * with an empty path
     *
     * @param subSchema the schema node to use
     * @return the new context
     */
    public ValidationContext createContext(final JsonNode subSchema)
    {
        return createContext("", subSchema);
    }

    /**
     * Spawn a new context with a different root schema located at a given URI
     *
     * <p>This is only called from a {@link RefKeywordValidator} instance
     * when it has to grab a schema from a non local URI. It may also be
     * that the context already had the schema in the cache.</p>
     *
     * @param uri the URI for this schema (without the JSON path)
     * @param newRoot the matching schema
     * @return a new context
     *
     * @see #fromCache(URI)
     * @see RefKeywordValidator
     */
    public ValidationContext newContext(final URI uri, final JsonNode newRoot)
    {
        if (locators.containsKey(uri)) {
            if (!newRoot.equals(locators.get(uri)))
                throw new RuntimeException("This should not have happened: I "
                    + "was provided with a different schema than what I had "
                    + "in the cache");
        } else
            locators.put(uri, newRoot);

        final ValidationContext ret = new ValidationContext();

        ret.path = path;
        ret.rootSchema = newRoot;
        ret.schemaNode = newRoot;
        ret.keywordFactory = keywordFactory;
        ret.syntaxFactory = syntaxFactory;
        ret.refLookups = new HashSet<JsonNode>(refLookups);
        ret.locators.putAll(locators);
        return ret;
    }

    /**
     * Create a {@link Validator} for a given JSON instance. This is what
     * MUST be called by validators when they need to spawn a new validator,
     * because this method handles syntax checking. If the syntax of the
     * schema itself is wrong, returns an {@link AlwaysFalseValidator}.
     *
     * @param instance the JSON instance
     * @return the validator
     */
    public Validator getValidator(final JsonNode instance)
    {
        final ValidationReport report = new ValidationReport(path);

        final Validator v = syntaxFactory.getValidator(this);

        report.mergeWith(v.validate());

        if (!report.isSuccess())
            return new AlwaysFalseValidator(report);

        return keywordFactory.getValidator(this, instance);
    }

    /**
     * Get a validator for the subschema at a given path for a given instance
     *
     * <p>This validator will spawn an {@link AlwaysFalseValidator} if the
     * path doesn't match anything, <b>or</b> if a ref loop is detected.</p>
     *
     * @param path the JSON path (<b>without</b> the initial {@code #})
     * @param instance the instance to validate
     * @return the matching validator
     */
    public Validator getValidator(final String path, final JsonNode instance)
    {
        final ValidationReport report = createReport();

        if (path == null) {
            report.error("path is null");
            return new AlwaysFalseValidator(report);
        }

        if (!JSONPATH_REGEX.matcher(path).matches()) {
            report.error("invalid JSON path " + path);
            return new AlwaysFalseValidator(report);
        }

        final JsonNode schema = getSubSchema(path);

        if (schema.isMissingNode()) {
            report.error("no match in schema for path #" + path);
            return new AlwaysFalseValidator(report);
        }

        if (refLookups.contains(schema)) {
            report.error("schema " + schema + " loops on itself");
            return new AlwaysFalseValidator(report);
        }

        refLookups.add(schema);
        schemaNode = schema;

        return getValidator(instance);
    }

    /**
     * Create a new report with, optionally, a prefix (this DOES NOT affect
     * the path)
     *
     * @param prefix the prefix to use
     * @return the newly created report
     */
    public ValidationReport createReport(final String prefix)
    {
        return new ValidationReport(path + prefix);
    }

    /**
     * Shortcut to {@link #createReport(String)} with an empty prefix
     *
     * @return the newly created report
     */
    public ValidationReport createReport()
    {
        return createReport("");
    }


    /**
     * Get a subschema from #rootSchema, given a JSON path as an argument
     *
     * @param path the JSON Path (<b>without</b> the initial {@code #})
     * @return the subschema, which is a {@link MissingNode} if the path is
     * not found
     */
    private JsonNode getSubSchema(final String path)
    {
        JsonNode ret = rootSchema;

        for (final String pathElement: SPLIT_PATTERN.split(path)) {
            if (pathElement.isEmpty())
                continue;
            ret = ret.path(pathElement);
        }

        return ret;
    }

    /**
     * Returns the schema corresponding to the given URI in #locators.
     *
     * @param uri the URI to lookup
     * @return the matching entry, null if none is found
     */
    public JsonNode fromCache(final URI uri)
    {
        return locators.get(uri);
    }
}