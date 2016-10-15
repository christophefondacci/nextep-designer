/*******************************************************************************
 * Copyright (c) 2011 neXtep Software and contributors.
 * All rights reserved.
 *
 * This file is part of neXtep designer.
 *
 * NeXtep designer is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 *
 * NeXtep designer is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.ui.decorators;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.designer.dbgm.ui.DBGMImages;

/**
 * An example showing how to control when an element is decorated. This example decorates only
 * elements that are instances of IResource and whose attribute is 'Read-only'.
 * 
 * @see ILightweightLabelDecorator
 */
public class ForeignKeyColumnsDecorator implements ILightweightLabelDecorator {

	/**
	 * The image description used in <code>addOverlay(ImageDescriptor, int)</code>
	 */
	private ImageDescriptor descriptor;
	private final static int quadrant = IDecoration.BOTTOM_LEFT;

	public ForeignKeyColumnsDecorator() {
		descriptor = ImageDescriptor.createFromImage(DBGMImages.DECORATOR_FK);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 * org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IBasicColumn) {
			final IBasicColumn column = (IBasicColumn) element;
			final IColumnable parent = ((IBasicColumn) element).getParent();
			if (parent instanceof IBasicTable) {
				boolean needsDecoration = false;
				for (IKeyConstraint key : ((IBasicTable) parent).getConstraints()) {
					switch (key.getConstraintType()) {
					case FOREIGN:
						if (key.getConstrainedColumnsRef().contains(column.getReference())) {
							needsDecoration = true;
							break;
						}
					}
				}
				if (needsDecoration) {
					decoration.addOverlay(descriptor, quadrant);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.
	 * ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 * java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.
	 * ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}
}
