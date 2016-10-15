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
package com.nextep.designer.dbgm.gef;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author Christophe Fondacci
 *
 */
public class DBGMGraphicalOutline extends Page implements IContentOutlinePage {
	private ScrollableThumbnail thumbnail;
	private GraphicalViewer viewer;
	private Canvas canvas; 
	public DBGMGraphicalOutline(GraphicalViewer viewer) {
		this.viewer = viewer;
	}
	/**
	 * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		// Creation de la miniature.
		canvas = new Canvas(parent, SWT.BORDER);
		LightweightSystem lws = new LightweightSystem(canvas);

		thumbnail = new ScrollableThumbnail(
				(Viewport) ((ScalableFreeformRootEditPart) viewer
						.getRootEditPart()).getFigure());
		thumbnail.setSource(((ScalableFreeformRootEditPart) viewer
				.getRootEditPart())
				.getLayer(LayerConstants.PRINTABLE_LAYERS));

		lws.setContents(thumbnail);

		viewer.getControl().addDisposeListener( new DisposeListener() {
			//@Override
			public void widgetDisposed(DisposeEvent e) {
				if (thumbnail != null) {
					thumbnail.deactivate();
					thumbnail = null;
				}
			}
		});
		
	}

	/**
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	@Override
	public Control getControl() {
		return canvas;
	}

	/**
	 * @see org.eclipse.ui.part.Page#setFocus()
	 */
	@Override
	public void setFocus() {
		canvas.setFocus();
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	@Override
	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection selection) {
		// TODO Auto-generated method stub
		
	}
}
