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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.numbered.content.headings.HeadingsNumberingService;
import org.xwiki.contrib.numbered.content.toc.TocEntriesResolver;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.HeaderBlock;

/**
 * Resolve the heading list to include in a Paragraphs Numbers Table of Content.
 *
 * @version $Id$
 * @since 1.2
 */
@Component
@Singleton
@Named("paragraphs")
public class ParagraphsTocEntryResolver implements TocEntriesResolver
{
    @Inject
    @Named("paragraphs")
    private HeadingsNumberingService headerNumberingService;

    @Override
    public List<HeaderBlock> getHeaderBlocks(Block rootBlock)
    {
        return this.headerNumberingService.getHeadingsList(rootBlock);
    }
}
