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

import org.xwiki.contrib.paragraph.numbering.internal.ParagraphsNumberingMacro;
import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Parameters for the {@link ParagraphsNumberingMacro} Macro.
 *
 * @version $Id$
 * @since 1.0
 */
public class ParagraphsNumberingMacroParameters
{
    /**
     * The starting number of the paragraphs sequence. The default value is 1.
     */
    private String start = "1";

    /**
     * @return the number of the paragraphs sequence. The default value is 1.
     */
    public String getStart()
    {
        return start;
    }

    /**
     * The number of the paragraphs sequence. The default value is 1.
     *
     * @param start the starting number of the paragraphs sequence (for instance, {@code 2.1})
     */
    @PropertyDescription("The starting number of the paragraphs sequence.")
    public void setStart(String start)
    {
        this.start = start;
    }
}
