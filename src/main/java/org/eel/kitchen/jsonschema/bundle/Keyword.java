/*
 * Copyright (c) 2012, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eel.kitchen.jsonschema.bundle;

import org.eel.kitchen.jsonschema.keyword.KeywordValidator;
import org.eel.kitchen.jsonschema.syntax.SyntaxChecker;

/**
 * Class used to build a new keyword
 *
 * <p>You will never use this class directly, but a {@link KeywordBuilder}
 * instead. As a matter of fact, this class has no public constructor.</p>
 *
 * @see KeywordBuilder
 */
public final class Keyword
{
    private final String name;
    private final SyntaxChecker syntaxChecker;
    private final Class<? extends KeywordValidator> validatorClass;

    Keyword(final String name, final SyntaxChecker syntaxChecker,
        final Class<? extends KeywordValidator> validatorClass)
    {
        this.name = name;
        this.syntaxChecker = syntaxChecker;
        this.validatorClass = validatorClass;
    }

    public String getName()
    {
        return name;
    }

    public SyntaxChecker getSyntaxChecker()
    {
        return syntaxChecker;
    }

    public Class<? extends KeywordValidator> getValidatorClass()
    {
        return validatorClass;
    }
}
