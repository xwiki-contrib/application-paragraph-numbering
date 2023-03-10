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
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.numbered.content.toc.TocTreeBuilder;
import org.xwiki.contrib.numbered.content.toc.TocTreeBuilderFactory;
import org.xwiki.contrib.paragraph.numbering.TableOfParagraphsMacroParameters;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.block.match.MacroMarkerBlockMatcher;
import org.xwiki.rendering.internal.macro.toc.TreeParameters;
import org.xwiki.rendering.internal.macro.toc.TreeParametersBuilder;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.toc.TocMacroParameters;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.skinx.SkinExtension;

import static java.util.Collections.singletonList;
import static org.xwiki.contrib.paragraph.numbering.TableOfParagraphsMacroParameters.Scope.PAGE;

/**
 * Display a Table of Paragraphs.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named(TableOfParagraphsMacro.PARAGRAPHS_TOC_MACRO)
@Singleton
public class TableOfParagraphsMacro extends AbstractMacro<TableOfParagraphsMacroParameters> implements Initializable
{
    /**
     * Hint of the table of paragraphs macro.
     */
    public static final String PARAGRAPHS_TOC_MACRO = "top";

    private TocTreeBuilder tocTreeBuilder;

    @Inject
    private TocTreeBuilderFactory tocTreeBuilderFactory;

    @Inject
    @Named("ssrx")
    private SkinExtension ssfx;

    /**
     * Default constructor. Create and initialize the macro descriptor.
     */
    public TableOfParagraphsMacro()
    {
        super("Table of Paragraphs", "Generates a Table of Paragraphs.", TableOfParagraphsMacroParameters.class);

        // Make sure this macro is executed as one of the last macros to be executed since other macros can generate
        // paragraphs which need to be taken into account by the TOP macro.
        setPriority(2000);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_NAVIGATION));
    }

    @Override
    public void initialize() throws InitializationException
    {
        super.initialize();

        try {
            this.tocTreeBuilder = this.tocTreeBuilderFactory.build("paragraphs");
        } catch (ComponentLookupException e) {
            throw new InitializationException(String.format("Failed to initialize [%s]", TocTreeBuilder.class), e);
        }
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
        this.ssfx.use("top.css");
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

        List<Block> treeBlocks;
        if (parameters.getScope() == PAGE) {
            treeBlocks =
                xdom.getBlocks(new MacroMarkerBlockMatcher("paragraphs-numbering"), Block.Axes.DESCENDANT_OR_SELF);
        } else {
            treeBlocks = singletonList(xdom.getParent());
        }

        // Generate the ToP for each paragraph blocks and merge them in a single ToP.
        List<Block> items = new ArrayList<>();
        for (Block macro : treeBlocks) {
            Block root =
                macro.getFirstBlock(new ClassBlockMatcher(MetaDataBlock.class), Block.Axes.DESCENDANT_OR_SELF);
            TreeParametersBuilder builder = new TreeParametersBuilder();
            TocMacroParameters macroParameters = new TocMacroParameters();
            macroParameters.setNumbered(false);
            macroParameters.setDepth(depth);
            TreeParameters treeParameters = builder.build(root, macroParameters, context);
            List<Block> build = this.tocTreeBuilder.build(treeParameters);
            if (!build.isEmpty()) {
                items.addAll(build.get(0).getChildren());
            }
        }

        BulletedListBlock tableOfParagraphs = new BulletedListBlock(items);
        tableOfParagraphs.setParameter("class", "wikitop");
        return singletonList(tableOfParagraphs);
    }
}
