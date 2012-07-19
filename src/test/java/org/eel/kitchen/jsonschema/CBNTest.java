package org.eel.kitchen.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import org.eel.kitchen.jsonschema.bundle.KeywordBundles;
import org.eel.kitchen.jsonschema.main.ValidationContext;
import org.eel.kitchen.jsonschema.validator.JsonValidator;
import org.eel.kitchen.jsonschema.validator.JsonValidatorFactory;
import org.eel.kitchen.util.JsonLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class CBNTest
{
    private JsonValidatorFactory factory;
    private JsonNode schemaNode;
    private JsonNode data;

    @BeforeClass
    public void setUp()
        throws IOException
    {
        factory = new JsonValidatorFactory(KeywordBundles.defaultBundle());
        schemaNode = JsonLoader.fromResource("/cbn-schema.json");
        data = JsonLoader.fromResource("/cbn-data.json");
    }

    @Test
    public void CBNTestCaseYieldsExpectedResult()
    {
        /*
         * Validation should fail because of enum
         */
        final JsonValidator validator = factory.fromNode(schemaNode);
        final ValidationContext ctx = new ValidationContext();

        validator.validate(ctx, data);
        assertFalse(ctx.isSuccess(), "validation should have failed");
    }
}
