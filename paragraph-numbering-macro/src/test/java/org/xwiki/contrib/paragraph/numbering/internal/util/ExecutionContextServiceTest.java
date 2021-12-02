package org.xwiki.contrib.paragraph.numbering.internal.util;/*
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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import com.xpn.xwiki.XWikiContext;

import static com.xpn.xwiki.XWikiContext.EXECUTIONCONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test of {@link ExecutionContextService}.
 *
 * @version $Id$
 * @since 1.0
 */
@ComponentTest
class ExecutionContextServiceTest
{
    @InjectMockComponents
    private ExecutionContextService executionContextService;

    @MockComponent
    private Execution execution;

    @Mock
    private ExecutionContext executionContext;

    @Mock
    private XWikiContext xWikiContext;

    @ParameterizedTest
    @CsvSource({ "export,true", "view,false" })
    void isExporting(String action, boolean expected)
    {
        when(this.execution.getContext()).thenReturn(this.executionContext);
        when(this.executionContext.getProperty(EXECUTIONCONTEXT_KEY)).thenReturn(this.xWikiContext);
        when(this.xWikiContext.getAction()).thenReturn(action);
        assertEquals(expected, this.executionContextService.isExporting());
    }
}