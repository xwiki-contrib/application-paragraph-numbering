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
package org.xwiki.contrib.paragraph.numbering.internal.transformation;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.contrib.paragraph.numbering.internal.ParagraphsNumberingMacro;
import org.xwiki.contrib.paragraph.numbering.internal.util.ParagraphsTreeService;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;

import static org.xwiki.contrib.paragraph.numbering.internal.ParagraphsNumberingMacro.CONTEXT_INDEXES;

/**
 * Define IDs to the paragraphs inside the {@link ParagraphsNumberingMacro}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named("paragraphs-ids")
public class ParagraphsIdsTransformation extends AbstractTransformation
{
    /**
     * Name of the data numbering parameter. This parameter is used to define the numbering of the numbered paragraphs
     * (for instance, 1.7.2).
     */
    public static final String DATA_NUMBERING_PARAMETER = "data-numbering";

    @Inject
    private Execution execution;

    @Inject
    private ParagraphsTreeService paragraphsTreeService;

    @Override
    public void transform(Block rootBlock, TransformationContext context)
    {
        List<ListItemBlock> blocks = rootBlock.getBlocks(ListItemBlock.class::isInstance, Block.Axes.DESCENDANT);
        String key = String.format("%d.%s", System.identityHashCode(context), CONTEXT_INDEXES);
        this.paragraphsTreeService.annotateWithIndexes(blocks, (int[]) this.execution.getContext().getProperty(key));
    }
}
