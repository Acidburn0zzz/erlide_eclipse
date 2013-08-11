/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.erlide.ui.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Viewer filter used in selection dialogs.
 */
public class TypedViewerFilter extends ViewerFilter {

    private final Class<?>[] fAcceptedTypes;
    private final Object[] fRejectedElements;

    /**
     * Creates a filter that only allows elements of gives types.
     * 
     * @param acceptedTypes
     *            The types of accepted elements
     */
    public TypedViewerFilter(final Class<?>[] acceptedTypes) {
        this(acceptedTypes, null);
    }

    /**
     * Creates a filter that only allows elements of gives types, but not from a
     * list of rejected elements.
     * 
     * @param acceptedTypes
     *            Accepted elements must be of this types
     * @param rejectedElements
     *            Element equals to the rejected elements are filtered out
     */
    public TypedViewerFilter(final Class<?>[] acceptedTypes,
            final Object[] rejectedElements) {
        assertThat(acceptedTypes, is(not(nullValue())));
        fAcceptedTypes = acceptedTypes;
        fRejectedElements = rejectedElements;
    }

    /**
     * @see ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(final Viewer viewer, final Object parentElement,
            final Object element) {
        if (fRejectedElements != null) {
            for (int i = 0; i < fRejectedElements.length; i++) {
                if (element.equals(fRejectedElements[i])) {
                    return false;
                }
            }
        }
        for (int i = 0; i < fAcceptedTypes.length; i++) {
            if (fAcceptedTypes[i].isInstance(element)) {
                return true;
            }
        }
        return false;
    }

}
