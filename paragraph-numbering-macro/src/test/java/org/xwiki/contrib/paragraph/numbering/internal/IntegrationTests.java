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

import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.runner.RunWith;
import org.xwiki.contrib.paragraph.numbering.internal.util.MacroIdGenerator;
import org.xwiki.rendering.test.integration.RenderingTestSuite;
import org.xwiki.rendering.test.integration.TestDataParser;
import org.xwiki.skinx.SkinExtension;
import org.xwiki.test.jmock.MockingComponentManager;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link TestDataParser}.
 *
 * @version $Id$
 * @since 1.0
 */
@RunWith(RenderingTestSuite.class)
public class IntegrationTests
{
    @RenderingTestSuite.Initialized
    public void initialize(MockingComponentManager componentManager) throws Exception
    {
        Mockery mockery = new JUnit4Mockery();

        final SkinExtension ssrxMock = componentManager.registerMockComponent(mockery, SkinExtension.class, "ssrx");

        MacroIdGenerator macroIdGenerator = componentManager.registerMockComponent(mockery, MacroIdGenerator.class);

        mockery.checking(new Expectations()
        {
            {
                allowing(macroIdGenerator).generateId(with(any(String.class)));
                will(returnValue("generated-id"));
            }
        });

        mockery.checking(new Expectations()
        {
            {
                String cssPath = "macroedit.css";
                allowing(ssrxMock).use(with(cssPath));
                allowing(ssrxMock).use(with(cssPath), with(any(Map.class)));
            }
        });
    }
}
