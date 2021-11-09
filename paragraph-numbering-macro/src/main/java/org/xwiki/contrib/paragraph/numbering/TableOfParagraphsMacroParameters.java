/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.paragraph.numbering;

import org.xwiki.contrib.paragraph.numbering.internal.TableOfParagraphsMacro;

/**
 * Parameters for the {@link TableOfParagraphsMacro} Macro.
 *
 * @version $Id$
 * @since 1.0
 */
public class TableOfParagraphsMacroParameters
{
    /**
     * Name of the scope parameter.
     */
    public static final String SCOPE_PARAMETER = "scope";
    
    /**
     * The available scope options.
     */
    public enum Scope
    {
        /**
         * List section starting where macro block is located in the XDOM.
         */
        LOCAL,

        /**
         * List the sections of the whole page.
         */
        PAGE
    }

    private int depth = 2;

    private Scope scope = Scope.PAGE;

    /**
     * @return the depth of the paragraphs to include in the table (the default value is 2)
     */
    public int getDepth()
    {
        return this.depth;
    }

    /**
     * @param depth the depth of the paragraphs to include in the table (the default value is 2)
     */
    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    /**
     * @return the scope to use to generate the table (the default value is {@link Scope#PAGE})
     */
    public Scope getScope()
    {
        return this.scope;
    }

    /**
     * @param scope the scope to use to generate the table (the default value is {@link Scope#PAGE})
     */
    public void setScope(Scope scope)
    {
        this.scope = scope;
    }
}
