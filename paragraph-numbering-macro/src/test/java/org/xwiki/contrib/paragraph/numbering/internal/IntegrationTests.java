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
import java.util.Map;

import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheManager;
import org.xwiki.contrib.numbered.headings.internal.DefaultHeadersNumberingCacheManager;
import org.xwiki.contrib.paragraph.numbering.internal.util.ExecutionContextService;
import org.xwiki.rendering.test.integration.TestDataParser;
import org.xwiki.rendering.test.integration.junit5.RenderingTests;
import org.xwiki.skinx.SkinExtension;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link TestDataParser}.
 *
 * @version $Id$
 * @since 1.0
 */
@AllComponents
//@RenderingTests.Scope(value = "paragraphs-numbering/numbering_toc")
public class IntegrationTests implements RenderingTests
{
    @RenderingTests.Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        componentManager.registerMockComponent(SkinExtension.class, "ssrx");
        componentManager.registerMockComponent(SkinExtension.class, "ssx");
        componentManager.registerMockComponent(SkinExtension.class, "jsrx");
        CacheManager cacheManager = componentManager.registerMockComponent(CacheManager.class);

        ExecutionContextService executionContextService =
            componentManager.registerMockComponent(ExecutionContextService.class);
        when(executionContextService.isExporting()).thenReturn(false);

        Cache<DefaultHeadersNumberingCacheManager.CachedValue> cache = mock(Cache.class);
        Map<String, DefaultHeadersNumberingCacheManager.CachedValue> map = new HashMap<>();
        doAnswer(invocation -> {
            map.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(cache).set(any(), any());
        doAnswer(invocation -> map.get(invocation.getArgument(0))).when(cache).get(any());
        when(cacheManager.<DefaultHeadersNumberingCacheManager.CachedValue>createNewCache(any())).thenReturn(cache);
    }
}
