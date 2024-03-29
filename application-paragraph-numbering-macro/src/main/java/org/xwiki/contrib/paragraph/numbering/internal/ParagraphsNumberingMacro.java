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

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.paragraph.numbering.ParagraphsNumberingMacroParameters;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.skinx.SkinExtension;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService.NUMBERED_CONTENT_ROOT_CLASS;
import static org.xwiki.rendering.block.Block.LIST_BLOCK_TYPE;
import static org.xwiki.rendering.macro.toc.TocMacroParameters.Scope.LOCAL;

/**
 * Automatically add numbers on the paragraphs contained in the body of the macro.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named(ParagraphsNumberingMacro.PARAGRAPHS_NUMBERING_MACRO)
@Singleton
public class ParagraphsNumberingMacro extends AbstractMacro<ParagraphsNumberingMacroParameters>
{
    /**
     * Hint of the paragraphs numbering macro.
     */
    public static final String PARAGRAPHS_NUMBERING_MACRO = "paragraphs-numbering";

    private static final String CLASS_PARAMETER = "class";

    /**
     * The component used to import style-sheet file extensions.
     */
    @Inject
    @Named("ssrx")
    private SkinExtension ssrx;

    /**
     * The component used to import javascript file extensions.
     */
    @Inject
    @Named("jsrx")
    private SkinExtension jsrx;

    @Inject
    private MacroContentParser contentParser;

    /**
     * Default constructor. Create and initialize the macro descriptor.
     */
    public ParagraphsNumberingMacro()
    {
        super("Paragraphs Numbering", "Automatically add numbers on the paragraphs contained in the body of the macro",
            new DefaultContentDescriptor("body", false, LIST_BLOCK_TYPE), ParagraphsNumberingMacroParameters.class);
        setDefaultCategories(Set.of(DEFAULT_CATEGORY_NAVIGATION));
    }

    @Override
    public boolean supportsInlineMode()
    {
        return false;
    }

    @Override
    public List<Block> execute(ParagraphsNumberingMacroParameters parameters, String content,
        MacroTransformationContext context) throws MacroExecutionException
    {
        this.ssrx.use("paragraphsnumbering.css");
        this.jsrx.use("paragraphsnumbering.js");

        List<Block> blocks = new ArrayList<>();
        if (parameters.isTableOfParagraphs()) {
            blocks.add(new MacroBlock("top", singletonMap("scope", LOCAL.name()), false));
        }

        XDOM parse;
        if (StringUtils.isBlank(content)) {
            parse = new XDOM(
                singletonList(new SectionBlock(singletonList(new HeaderBlock(emptyList(), HeaderLevel.LEVEL1)))));
        } else {
            parse = this.contentParser.parse(content, context, false, context.isInline());
        }

        // Exclude the edit block when exporting since it cannot be hidden using css.
        blocks.add(new MetaDataBlock(parse.getChildren(), getNonGeneratedContentMetaData()));

        return singletonList(
            new GroupBlock(blocks, singletonMap(CLASS_PARAMETER,
                String.format("paragraphs-numbering-root %s", NUMBERED_CONTENT_ROOT_CLASS))));
    }
}
