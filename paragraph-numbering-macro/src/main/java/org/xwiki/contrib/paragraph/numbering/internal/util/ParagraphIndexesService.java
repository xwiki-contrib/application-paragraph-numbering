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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.paragraph.numbering.internal.ParagraphsNumberingMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.TransformationContext;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.xwiki.contrib.paragraph.numbering.internal.ParagraphsNumberingMacro.CONTEXT_INDEXES;

/**
 * Initialize and retrieve the paragraph indices for the current {@link TransformationContext}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component(roles = ParagraphIndexesService.class)
@Singleton
public class ParagraphIndexesService
{
    @Inject
    private Execution execution;

    /**
     * @param context the current {@link TransformationContext}
     * @return the indices for a given {@link TransformationContext}, for instance {@code [1,3,10]}
     */
    public int[] getIndices(TransformationContext context)
    {
        return (int[]) this.execution.getContext().getProperty(computeKey(context));
    }

    /**
     * Initialize the paragraph indices for the current {@link TransformationContext}, starting at the provided start
     * indices.
     *
     * @param start the start indices, must be a list of integers separated with dots (for instance {@code 1.3.10})
     * @param context the current {@link TransformationContext}
     * @return the last computed indices, to be used as a continuation for the next {@link ParagraphsNumberingMacro}
     *     execution
     * @throws MacroExecutionException in case of error when start is malformed (empty and not conforming to the
     *     expected format)
     */
    public int[] initializeIndexes(String start, TransformationContext context)
        throws MacroExecutionException
    {
        int[] startIndexes;
        String key = computeKey(context);
        ExecutionContext executionContext = this.execution.getContext();
        if (start != null) {
            startIndexes = parseStartParameter(start);
            executionContext.setProperty(key, startIndexes);
        } else {
            if (executionContext.getProperty(key) == null) {
                executionContext.setProperty(key, new int[] { 1 });
                startIndexes = new int[] { 1 };
            } else {
                startIndexes = (int[]) executionContext.getProperty(key);
            }
        }
        return startIndexes;
    }

    private String computeKey(TransformationContext context)
    {
        return String.format("%d.%s", System.identityHashCode(context), CONTEXT_INDEXES);
    }

    private int[] parseStartParameter(String start) throws MacroExecutionException
    {
        if (isEmpty(start)) {
            throw invalidStartParameterFormat(start);
        }
        String[] segments = start.split("\\.");
        int[] numbers = new int[segments.length];

        try {
            for (int i = 0; i < segments.length; i++) {
                numbers[i] = Integer.parseInt(segments[i]);

                if (numbers[i] < 0) {
                    throw new NumberFormatException();
                }
            }
        } catch (NumberFormatException e) {
            throw invalidStartParameterFormat(start);
        }
        return numbers;
    }

    private MacroExecutionException invalidStartParameterFormat(String start)
    {
        // TODO: i18n
        return new MacroExecutionException(
            String.format("Invalid start format [%s]. A list of integers separated with dots is expected", start));
    }
}
