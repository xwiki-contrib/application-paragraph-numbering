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
package org.xwiki.contrib.paragraph.numbering.internal;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.paragraph.numbering.TableOfParagraphsMacroParameters;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Display a Table of Paragraphs.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("paragraphs-toc")
@Singleton
public class TableOfParagraphsMacro extends AbstractMacro<TableOfParagraphsMacroParameters>
{
    /**
     * Default constructor. Create and initialize the macro descriptor.
     */
    public TableOfParagraphsMacro()
    {
        super("Table of Paragraphs", "Generates a Table of Paragraphs.", TableOfParagraphsMacroParameters.class);

        // Make sure this macro is executed as one of the last macros to be executed since other macros can generate
        // paragraphs which need to be taken into account by the TOP macro.
        setPriority(2000);
        setDefaultCategory(DEFAULT_CATEGORY_NAVIGATION);
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(TableOfParagraphsMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        int depth = parameters.getDepth();
        if (depth <= 0) {
            throw new MacroExecutionException(
                String.format("The depth parameter must be a positive integer but is [%d].", depth));
        }

        return singletonList(new GroupBlock(emptyList()));
    }
}
