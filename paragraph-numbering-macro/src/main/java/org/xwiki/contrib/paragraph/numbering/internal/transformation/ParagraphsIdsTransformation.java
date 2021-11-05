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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.contrib.paragraph.numbering.internal.ParagraphsNumberingMacro;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

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
    @Inject
    private Execution execution;

    private class ListItemBlockTree
    {
        private final List<ListItemBlockTree> children = new ArrayList<>();

        private final ListItemBlock data;

        ListItemBlockTree()
        {
            this.data = null;
        }

        ListItemBlockTree(ListItemBlock data)
        {
            this.data = data;
        }

        public List<ListItemBlockTree> getChildren()
        {
            return this.children;
        }

        public void traverse(int[] indexes)
        {
            if (this.data == null) {
                this.children.forEach(child -> {
                    child.traverse(indexes);
                    indexes[indexes.length - 1]++;
                });
            } else {
                int[] newIndexes = new int[indexes.length + 1];
                String id = StringUtils.join(ArrayUtils.toObject(indexes), ".");
                String idParameter = "id";
                if (this.data.getParameter(idParameter) == null) {
                    this.data.setParameter(idParameter, String.format("P%s", id));
                }
                this.data.setParameter("data-numbering", id);

                System.arraycopy(indexes, 0, newIndexes, 0, indexes.length);
                newIndexes[newIndexes.length - 1] = 1;
                this.children.forEach(child -> {
                    child.traverse(newIndexes);
                    newIndexes[newIndexes.length - 1]++;
                });
            }
        }
    }

    @Override
    public void transform(Block rootBlock, TransformationContext context) throws TransformationException
    {
        List<ListItemBlock> blocks = rootBlock.getBlocks(ListItemBlock.class::isInstance, Block.Axes.DESCENDANT);
        String key = String.format("%d.%s", System.identityHashCode(context), CONTEXT_INDEXES);
        treeInitialization(blocks).traverse((int[]) this.execution.getContext().getProperty(key));
    }

    private ListItemBlockTree treeInitialization(List<ListItemBlock> blocks)
    {
        ListItemBlockTree listItemBlockTree = new ListItemBlockTree();
        Map<ListItemBlock, ListItemBlockTree> cache = new HashMap<>();
        for (ListItemBlock block : blocks) {
            Block parent = findParent(block);
            ListItemBlockTree currentTree;
            if (parent == null) {
                currentTree = listItemBlockTree;
            } else {
                currentTree = cache.get(parent);
            }
            ListItemBlockTree subTree = new ListItemBlockTree(block);
            cache.put(block, subTree);
            currentTree.getChildren().add(subTree);
        }
        return listItemBlockTree;
    }

    private Block findParent(ListItemBlock block)
    {
        Block parent = block.getParent();
        while (parent != null) {
            if (parent instanceof ListItemBlock) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
}
