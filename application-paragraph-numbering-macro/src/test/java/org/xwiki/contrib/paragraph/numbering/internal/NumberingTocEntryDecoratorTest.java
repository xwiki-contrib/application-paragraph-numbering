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
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.contrib.numbered.content.headings.NumberedHeadingsConfiguration;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.HeaderBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.paragraph.numbering.internal.ParagraphsNumberingMacro.PARAGRAPHS_NUMBERING_MACRO;

/**
 * Test of {@link NumberingTocEntryDecorator}.
 *
 * @version $Id$
 * @since 1.2
 */
@ComponentTest
class NumberingTocEntryDecoratorTest
{
    @InjectMockComponents
    private NumberingTocEntryDecorator decorator;

    @MockComponent
    private NumberedHeadingsConfiguration numberedHeadingsConfiguration;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @Test
    void isNumberedInNumberedContext() throws Exception
    {
        when(this.numberedHeadingsConfiguration.isNumberedHeadingsEnabled()).thenReturn(true);
        assertFalse(this.decorator.isNumbered(new HeaderBlock(List.of(), HeaderLevel.LEVEL1)));
    }

    @Test
    void isNumberedNotInAParagraphNumberingMacro() throws Exception
    {
        when(this.numberedHeadingsConfiguration.isNumberedHeadingsEnabled()).thenReturn(false);
        assertFalse(this.decorator.isNumbered(new HeaderBlock(List.of(), HeaderLevel.LEVEL1)));
    }

    @Test
    void isNumberedConfigurationError() throws Exception
    {
        when(this.numberedHeadingsConfiguration.isNumberedHeadingsEnabled()).thenThrow(Exception.class);
        assertFalse(this.decorator.isNumbered(new HeaderBlock(List.of(), HeaderLevel.LEVEL1)));
        assertEquals("Failed to access the numbered headings configuration. Cause: [Exception: ]",
            this.logCapture.getMessage(0));
    }

    @Test
    void isNumbered() throws Exception
    {
        when(this.numberedHeadingsConfiguration.isNumberedHeadingsEnabled()).thenReturn(false);
        HeaderBlock headerBlock = new HeaderBlock(List.of(), HeaderLevel.LEVEL1);
        GroupBlock groupBlock = new GroupBlock();
        headerBlock.setParent(groupBlock);
        groupBlock.setParent(new MacroMarkerBlock(PARAGRAPHS_NUMBERING_MACRO, Map.of(), List.of(), false));
        assertTrue(this.decorator.isNumbered(headerBlock));
    }
}
