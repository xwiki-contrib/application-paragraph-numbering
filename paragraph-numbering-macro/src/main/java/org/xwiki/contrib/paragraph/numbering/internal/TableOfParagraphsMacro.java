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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.paragraph.numbering.TableOfParagraphsMacroParameters;
import org.xwiki.contrib.paragraph.numbering.internal.util.ParagraphsTreeService;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.match.BlockMatcher;
import org.xwiki.rendering.block.match.MacroMarkerBlockMatcher;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import static org.xwiki.contrib.paragraph.numbering.TableOfParagraphsMacroParameters.Scope.PAGE;
import static org.xwiki.contrib.paragraph.numbering.internal.ParagraphsNumberingMacro.PARAGRAPHS_NUMBERING_MACRO;
import static org.xwiki.contrib.paragraph.numbering.internal.transformation.ParagraphsIdsTransformation.DATA_NUMBERING_PARAMETER;
import static org.xwiki.rendering.block.Block.Axes.DESCENDANT;

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
    @Inject
    private ParagraphsTreeService paragraphsTreeService;

    private static class NumberedParagraphsMatcher implements BlockMatcher
    {
        private final MacroMarkerBlockMatcher macroMarkerBlockMatcher =
            new MacroMarkerBlockMatcher(PARAGRAPHS_NUMBERING_MACRO);

        private final Block xdom;

        private final int depth;

        NumberedParagraphsMatcher(Block xdom, int depth)
        {
            this.xdom = xdom;
            this.depth = depth;
        }

        @Override
        public boolean match(Block block)
        {
            return block instanceof ListItemBlock
                && block.getParameters().containsKey(DATA_NUMBERING_PARAMETER)
                && depth(block) <= this.depth
                && isInANumberedParagraphsMacro(block);
        }

        private int depth(Block block)
        {
            return block.getParameter(DATA_NUMBERING_PARAMETER).split("\\.").length;
        }

        private boolean isInANumberedParagraphsMacro(Block block)
        {
            // Check if the bock is contained in a paragraphs numbering macro.
            // Check if the paragraphs numbering macro is not outside of the scope of the table of paragraphs macro.
            for (Block parent : collectParents(block)) {
                if (this.macroMarkerBlockMatcher.match(parent)) {
                    return true;
                }
                if (parent == this.xdom) {
                    return false;
                }
            }
            return false;
        }

        private List<Block> collectParents(Block block)
        {
            List<Block> parents = new ArrayList<>();
            Block parent = block.getParent();
            do {
                if (parent != null) {
                    parents.add(parent);
                    parent = parent.getParent();
                }
            } while (parent != null);
            return parents;
        }
    }

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

        Block xdom;
        if (parameters.getScope() == PAGE) {
            xdom = context.getXDOM();
        } else {
            xdom = context.getCurrentMacroBlock().getParent();
        }

        List<ListItemBlock> blocks =
            xdom.getBlocks(new NumberedParagraphsMatcher(xdom, parameters.getDepth()), DESCENDANT)
                .stream().map(ListItemBlock.class::cast)
                .collect(Collectors.toList());

        return Collections.singletonList(this.paragraphsTreeService.buildTableOfParagraphs(blocks));
    }
}
