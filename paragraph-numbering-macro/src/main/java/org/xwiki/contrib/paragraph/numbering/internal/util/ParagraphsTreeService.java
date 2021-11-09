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
package org.xwiki.contrib.paragraph.numbering.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.paragraph.numbering.internal.transformation.ParagraphsIdsTransformation;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.BulletedListBlock;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.ListItemBlock;
import org.xwiki.rendering.block.NumberedListBlock;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.syntax.Syntax;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.xwiki.contrib.paragraph.numbering.internal.transformation.ParagraphsIdsTransformation.DATA_NUMBERING_PARAMETER;
import static org.xwiki.rendering.block.Block.Axes.DESCENDANT;

/**
 * This service helps manipulate paragraphs by first transforming them to a tree, and then visiting this tree. Two
 * operations are currently provided: visiting the tree to add numbers on each paragraphs, and transforming the
 * paragraphs into a bullet list for the table of paragraphs.
 * <p>
 * TODO: add a unit test class for this class, it is currently only covered by the integration tests.
 *
 * @version $Id$
 * @since 1.0
 */
@Component(roles = ParagraphsTreeService.class)
@Singleton
public class ParagraphsTreeService
{
    private static final String ID_PARAMETER = "id";

    private static class ListItemBlockTree
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
    }

    /**
     * Transform the list of {@link ListItemBlock} to a tree and annotate each item with its indexes. The indexes are
     * used to define the paragraph's ID as well as the {@link ParagraphsIdsTransformation#DATA_NUMBERING_PARAMETER} use
     * by the CSS to display the paragraph numbers in view mode
     *
     * @param blocks the list of blocks to annotate
     * @param indexes the starting indices for the numbering
     */
    public void annotateWithIndexes(List<ListItemBlock> blocks, int[] indexes)
    {
        annotateWithIndexes(this.treeInitialization(blocks), indexes);
    }

    /**
     * Transform the list of {@link ListItemBlock} to a tree and transform it to a list of items, used to display the
     * table of paragraphs.
     *
     * @param blocks the list of block to use for the table of paragraphs
     * @return the bullet list used for the table of paragraphs
     */
    public Block buildTableOfParagraphs(List<ListItemBlock> blocks)
    {
        return buildTableOfParagraphs(this.treeInitialization(blocks));
    }

    private Block buildTableOfParagraphs(ListItemBlockTree listItemBlockTree)
    {
        List<Block> children = new ArrayList<>();
        listItemBlockTree.children.forEach(child -> {
            if (child.data != null) {
                children.add(new ListItemBlock(singletonList(buildItem(child))));
            }
            if (!child.children.isEmpty()) {
                children.add(buildTableOfParagraphs(child));
            }
        });
        return new BulletedListBlock(children);
    }

    private Block buildItem(ListItemBlockTree tree)
    {
        String parameter = tree.data.getParameter(DATA_NUMBERING_PARAMETER);
        DocumentResourceReference reference = new DocumentResourceReference(null);
        reference.setAnchor(tree.data.getParameter(ID_PARAMETER));

        // TODO: use the syntax from the rendering context.
        List<Block> blocks = asList(
            new LinkBlock(singletonList(new RawBlock(parameter, Syntax.XHTML_1_0)), reference, false),
            new SpaceBlock(),
            new CompositeBlock(findHeaderBlocks(tree))
        );

        return new GroupBlock(blocks);
    }

    /**
     * Take the children blocks until the first block containing a NumberedListBlock is found.
     *
     * @param tree the paragraphs tree
     * @return the list of children not containing a {@code NumberedListBlock}
     */
    private List<Block> findHeaderBlocks(ListItemBlockTree tree)
    {
        // TODO: Simplify and instead find only the first line of the paragraph, which is considered as the "header" 
        //  of the paragraph.
        // TODO: If the header finishes with ':', remove it.
        if (tree.data == null) {
            return emptyList();
        }

        List<Block> headerBlocks;
        List<Block> children = tree.data.getChildren();
        do {
            headerBlocks = new ArrayList<>();
            for (Block child : children) {
                if (child instanceof NumberedListBlock || child.getChildren().stream()
                    .anyMatch(block -> block.getBlocks(NumberedListBlock.class::isInstance, DESCENDANT).isEmpty()))
                {
                    break;
                }
                headerBlocks.add(child);
            }
            children = children.get(0).getChildren();
        } while (headerBlocks.isEmpty() && !children.isEmpty());
        return headerBlocks;
    }

    private void annotateWithIndexes(ListItemBlockTree tree, int[] indexes)
    {
        if (tree.data == null) {
            tree.children.forEach(child -> {
                annotateWithIndexes(child, indexes);
                indexes[indexes.length - 1]++;
            });
        } else {
            int[] newIndexes = new int[indexes.length + 1];
            String id = StringUtils.join(ArrayUtils.toObject(indexes), ".");
            if (tree.data.getParameter(ID_PARAMETER) == null) {
                tree.data.setParameter(ID_PARAMETER, String.format("P%s", id));
            }
            if (tree.data.getParameter(DATA_NUMBERING_PARAMETER) == null) {
                tree.data.setParameter(DATA_NUMBERING_PARAMETER, id);
            }

            System.arraycopy(indexes, 0, newIndexes, 0, indexes.length);
            newIndexes[newIndexes.length - 1] = 1;
            tree.children.forEach(child -> {
                annotateWithIndexes(child, newIndexes);
                newIndexes[newIndexes.length - 1]++;
            });
        }
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
