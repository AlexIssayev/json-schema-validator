/*
 * Copyright (c) 2011, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eel.kitchen.jsonschema.v2.validation.container;

import eel.kitchen.jsonschema.v2.schema.PathProvider;
import eel.kitchen.jsonschema.v2.schema.PathProviderFactory;
import eel.kitchen.jsonschema.v2.validation.ValidatorFactory;
import eel.kitchen.jsonschema.v2.validation.base.Validator;
import eel.kitchen.util.NodeType;
import org.codehaus.jackson.JsonNode;

public final class ArrayValidator
    extends ContainerValidator
{
    public ArrayValidator(final Validator validator,
        final ValidatorFactory factory, final JsonNode schema,
        final JsonNode instance)
    {
        super(validator, factory, schema, instance);
    }

    @Override
    protected void buildQueue()
    {
        final PathProvider provider = PathProviderFactory.getPathProvider
            (schema, NodeType.ARRAY);

        int i = 0;
        JsonNode schemaNode;
        Validator v;

        for (final JsonNode element: instance) {
            schemaNode = provider.getSchema(Integer.toString(i++));
            v = factory.getValidator(schemaNode, element);
            queue.add(v);
        }
    }
}