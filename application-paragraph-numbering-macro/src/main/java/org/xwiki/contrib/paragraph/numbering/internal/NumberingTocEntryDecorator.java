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

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.headings.NumberedHeadingsConfiguration;
import org.xwiki.contrib.numbered.content.toc.AbstractNumberingTocEntryDecorator;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.xwiki.contrib.paragraph.numbering.internal.ParagraphsNumberingMacro.PARAGRAPHS_NUMBERING_MACRO;

/**
 * Prefix each table of content entry with the corresponding paragraph number.
 *
 * @version $Id$
 * @since 1.2
 */
@Component
@Singleton
@Named("paragraphs")
public class NumberingTocEntryDecorator extends AbstractNumberingTocEntryDecorator
{
    @Inject
    private NumberedHeadingsConfiguration numberedHeadingsConfiguration;

    @Inject
    private Logger logger;

    @Override
    protected boolean isNumbered(HeaderBlock headerBlock)
    {
        try {
            if (this.numberedHeadingsConfiguration.isNumberedHeadingsEnabled()) {
                return false;
            }
        } catch (Exception e) {
            this.logger.warn("Failed to access the numbered headings configuration. Cause: [{}]",
                getRootCauseMessage(e));
        }
        Block parent = headerBlock.getParent();
        while (parent != null) {
            if (parent instanceof MacroMarkerBlock
                && Objects.equals(((MacroMarkerBlock) parent).getId(), PARAGRAPHS_NUMBERING_MACRO))
            {
                return true;
            }
            parent = parent.getParent();
        }

        return false;
    }
}
