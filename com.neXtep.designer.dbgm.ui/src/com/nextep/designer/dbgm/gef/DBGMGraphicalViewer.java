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
/**
 * 
 */
package com.nextep.designer.dbgm.gef;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.internal.InternalGEFPlugin;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;

/**
 * @author Christophe Fondacci
 *
 */
public class DBGMGraphicalViewer extends ViewPart {

	private static final Log log = LogFactory.getLog(DBGMGraphicalViewer.class);
	private GraphicalViewer viewer;
	private FlyoutPaletteComposite splitter;
	private PaletteViewerProvider provider;
	
	public GraphicalViewer getGraphicalViewer() {
		return viewer;
	}
	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		splitter = new FlyoutPaletteComposite(parent, SWT.NONE, getSite().getPage(),
				getPaletteViewerProvider(), getPalettePreferences());
		viewer = new ScrollingGraphicalViewer();
		viewer.createControl(parent);
		viewer.setEditPartFactory(new DBGMEditPartFactory());
		viewer.getControl().setBackground(ColorConstants.listBackground);
		getSite().setSelectionProvider(viewer);
//		hookGraphicalViewer();
		initializeGraphicalViewer();
	}
	/**
	 * By default, this method returns a FlyoutPreferences object that stores the flyout
	 * settings in the GEF plugin.  Sub-classes may override.
	 * @return	the FlyoutPreferences object used to save the flyout palette's preferences 
	 */
	protected FlyoutPreferences getPalettePreferences() {
		return FlyoutPaletteComposite
				.createFlyoutPreferences(InternalGEFPlugin.getDefault().getPluginPreferences());
	}
	protected void initializeGraphicalViewer() {
		//Retrieving selected diagram
        log.debug("DBGMGraphicalEditor: Retrieving selected model");
        ISelection s = this.getSite().getWorkbenchWindow().getSelectionService().getSelection();
        if(s instanceof IStructuredSelection && !s.isEmpty()) {
            IStructuredSelection sel = (IStructuredSelection)s;
            if(sel.getFirstElement() instanceof ITypedObject) {
            	ITypedObject model = (ITypedObject)sel.getFirstElement();
            	if(model.getType() == IElementType.getInstance("DIAGRAM")) {
            		viewer.setContents(model);
            	} else {
            		log.debug("Selection is not a diagram.");
            	}
            } else {
            	log.debug("Untyped object selected");
            }
        } else {
        	log.debug("Unstructured selection");
        }
	}
	/**
	 * Returns the palette viewer provider that is used to create palettes for the view and
	 * the flyout.  Creates one if it doesn't already exist.
	 * 
	 * @return	the PaletteViewerProvider that can be used to create PaletteViewers for
	 * 			this editor
	 * @see	#createPaletteViewerProvider()
	 */
	protected final PaletteViewerProvider getPaletteViewerProvider() {
		if (provider == null)
			provider = createPaletteViewerProvider();
		return provider;
	}
	/**
	 * Creates a PaletteViewerProvider that will be used to create palettes for the view
	 * and the flyout.
	 * 
	 * @return	the palette provider
	 */
	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(new EditDomain());
	}
	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
		
	}

}
