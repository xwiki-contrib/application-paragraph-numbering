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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.generic.EscapeTool;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.paragraph.numbering.ParagraphsNumberingMacroParameters;
import org.xwiki.contrib.paragraph.numbering.internal.util.MacroIdGenerator;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.skinx.SkinExtension;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOf;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.xwiki.rendering.block.Block.LIST_BLOCK_TYPE;
import static org.xwiki.text.StringUtils.isEmpty;

/**
 * Automatically add numbers on the paragraphs contained in the body of the macro.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("paragraphs-numbering")
@Singleton
public class ParagraphsNumberingMacro extends AbstractMacro<ParagraphsNumberingMacroParameters>
{
    private static final EscapeTool ESCAPE_TOOL = new EscapeTool();

    private static final String CLASS_PARAMETER = "class";

    /**
     * The component used to import style-sheet file extensions.
     */
    @Inject
    @Named("ssrx")
    private SkinExtension ssrx;

    @Inject
    private MacroContentParser contentParser;

    @Inject
    private MacroIdGenerator macroIdGenerator;

    /**
     * Default constructor. Create and initialize the macro descriptor.
     */
    public ParagraphsNumberingMacro()
    {
        super("ParagraphNumbering", "Automatically add numbers on the paragraphs contained in the body of the macro",
            new DefaultContentDescriptor("body", false, LIST_BLOCK_TYPE), ParagraphsNumberingMacroParameters.class);
        setDefaultCategory(DEFAULT_CATEGORY_NAVIGATION);
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
        int[] startIndexes = parseStartParameter(parameters.getStart());
        int offset = Math.max(0, startIndexes[startIndexes.length - 1] - 1);
        String macroId = this.macroIdGenerator.generateId("numbered-lists-");
        String prefix = computePrefix(startIndexes);

        this.ssrx.use("macroedit.css");

        List<Block> contentBlock = this.contentParser.parse(content, context, false, context.isInline())
            .getChildren();

        return singletonList(new GroupBlock(asList(
            getDynamicCssBlock(offset, macroId),
            getViewBlock(contentBlock),
            getEditBlock(contentBlock)
        ), rootBlockParameters(macroId, prefix)));
    }

    private GroupBlock getEditBlock(List<Block> contentBlock)
    {
        // Wrap the content in a MetaDataBlock initialized with the non generated content meta-data to make the content 
        // of the macro editable inline in the editor.
        return new GroupBlock(singletonList(new MetaDataBlock(contentBlock, getNonGeneratedContentMetaData())),
            singletonMap(CLASS_PARAMETER, "numbered-lists-edit"));
    }

    private GroupBlock getViewBlock(List<Block> contentBlock)
    {
        return new GroupBlock(contentBlock, singletonMap(CLASS_PARAMETER, "numbered-lists-view"));
    }

    /**
     * Dynamically computes a CSS style to reset the numbering of the block to the configured start value.
     *
     * @param offset the offset to apply to the start value
     * @param macroId the id of the current macro
     * @return the computed CSS style
     */
    private RawBlock getDynamicCssBlock(int offset, String macroId)
    {
        return new RawBlock(String.format(
            "<style>\n"
                + "#%1$s > .numbered-lists-edit > .xwiki-metadata-container > ol:not(.skip-numbering):first-of-type,\n" 
                + "#%1$s > .numbered-lists-view > .xwiki-metadata-container > ol:not(.skip-numbering):first-of-type {\n"
                + "  counter-reset: numbering %2$d;\n"
                + "}\n"
                + "</style>",
            macroId, offset), Syntax.XHTML_1_0);
    }

    private Map<String, String> rootBlockParameters(String macroId, String prefix)
    {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(CLASS_PARAMETER, "numbered-lists");
        parameters.put("id", macroId);
        parameters.put("style", String.format("--numbering-prefix: '%s';", ESCAPE_TOOL.xml(prefix)));
        return parameters;
    }

    /**
     * Concatenate all indexes except the last one with '.'. If startIndexes is empty, return an empty string.
     *
     * @param startIndexes the list of indexes of the start parameter
     * @return the computed prefix (for instance, "1.3" for when startIndexes=[1, 3, 5])
     */
    private String computePrefix(int[] startIndexes)
    {
        String prefix;
        if (startIndexes.length > 1) {
            int[] ints = copyOf(startIndexes, startIndexes.length - 1);
            String dot = ".";
            prefix = StringUtils.join(ArrayUtils.toObject(ints), dot) + dot;
        } else {
            prefix = "";
        }
        return prefix;
    }

    private int[] parseStartParameter(String start) throws MacroExecutionException
    {
        if (isEmpty(start)) {
            throw invalidStartParameterFormat(start);
        }
        String[] segments = start.split("\\.");
        int[] numbers = new int[segments.length];

        try {
            for (int i = 0; i < segments.length; i++) {
                numbers[i] = Integer.parseInt(segments[i]);

                if (numbers[i] < 0) {
                    throw new NumberFormatException();
                }
            }
        } catch (NumberFormatException e) {
            throw invalidStartParameterFormat(start);
        }
        return numbers;
    }

    private MacroExecutionException invalidStartParameterFormat(String start)
    {
        // TODO: i18n
        return new MacroExecutionException(
            String.format("Invalid start format [%s]. A list of integers separated with dots is expected", start));
    }
}
