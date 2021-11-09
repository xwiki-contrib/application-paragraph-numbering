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

import org.mockito.Mockito;
import org.xwiki.contrib.paragraph.numbering.internal.util.MacroIdGenerator;
import org.xwiki.rendering.configuration.RenderingConfiguration;
import org.xwiki.rendering.test.integration.TestDataParser;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;
import org.xwiki.skinx.SkinExtension;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link TestDataParser}.
 *
 * @version $Id$
 * @since 1.0
 */
@AllComponents
public class IntegrationTests implements RenderingTests
{
    @RenderingTests.Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        componentManager.registerMockComponent(SkinExtension.class, "ssrx");

        MacroIdGenerator macroIdGenerator = componentManager.registerMockComponent(MacroIdGenerator.class);
        when(macroIdGenerator.generateId(anyString())).thenReturn("generated-id");

        // Spy on the rendering configuration to exclude `paragraphs-ids` from the configuration, to prevent it to be 
        // called twice, once during the macro execution and once when the transformations are called.
        RenderingConfiguration renderingConfiguration = componentManager.getInstance(RenderingConfiguration.class);
        RenderingConfiguration spy = Mockito.spy(renderingConfiguration);
        componentManager.registerComponent(RenderingConfiguration.class, spy);
        final List<String> transformationNames = spy.getTransformationNames();
        when(spy.getTransformationNames()).thenAnswer(invocationOnMock -> {
            transformationNames.remove("paragraphs-ids");
            return transformationNames;
        });
    }
}
