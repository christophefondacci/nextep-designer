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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.vcs.impl.DiagramItem;
import com.nextep.datadesigner.vcs.impl.VersionedDiagram;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;

/**
 * @author Christophe Fondacci
 *
 */
public class TableOutline extends Page implements IContentOutlinePage {

	private GraphicalViewer viewer;
	private ScalableFreeformRootEditPart root;
	private IBasicTable table;
	
	private static final int H_SPACING = 50;
	private static final int V_SPACING = 10;
	
	public TableOutline(IBasicTable table) {
		this.table = table;
	}
	
	/**
	 * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
        //Creating graphical GEF viewer
        viewer = new ScrollingGraphicalViewer();
        Control c = viewer.createControl(parent);
        GridData d = new GridData();
        d.grabExcessHorizontalSpace = true;
        d.grabExcessVerticalSpace = true;
        d.horizontalAlignment = GridData.FILL;
        d.verticalAlignment = GridData.FILL;
        c.setLayoutData(d);
		viewer.setEditPartFactory(new DBGMEditPartFactory());
		root = new ScalableFreeformRootEditPart();
		viewer.setRootEditPart(root);
        viewer.getControl().setBackground(ColorConstants.listBackground);
        //PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().setSelectionProvider(viewer);
//		hookGraphicalViewer();

//        viewer.setSelectionManager(new CustomSelectionManager(this));
        List<String> zoomLevels = new ArrayList<String>(3);
        zoomLevels.add(ZoomManager.FIT_ALL);
        zoomLevels.add(ZoomManager.FIT_WIDTH);
        zoomLevels.add(ZoomManager.FIT_HEIGHT);
        root.getZoomManager().setZoomLevelContributions(zoomLevels);
        viewer.getControl().setSize(100, 100);
        Job j = new Job("Building table outline...") {
        	@Override
        	protected IStatus run(IProgressMonitor monitor) {
       			initializeGraphicalViewer();
        		return Status.OK_STATUS;
        	}
        };
        j.schedule();
//        viewer.getControl().addDisposeListener(this);
//        viewer.getControl().addMouseListener(this);
        viewer.getControl().addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                zoomFit();
            }
        });
	}

	private void initializeGraphicalViewer() {
		final IDiagram d = new VersionedDiagram();
		
		int x = 1;
		int y = 1;
		int maxWidth =0;
		int maxHeight = 0;
        int leftHeight = 0;
        List<IDiagramItem> parentItems = new ArrayList<IDiagramItem>();
        List<IDiagramItem> childItems = new ArrayList<IDiagramItem>();
        List<IBasicTable> displayedTables = new ArrayList<IBasicTable>();
        displayedTables.add(table);
        // Parent tables referenced via foreign keys
		for(IKeyConstraint k : table.getConstraints()) {
			switch(k.getConstraintType()) {
			case FOREIGN:
				ForeignKeyConstraint fk = (ForeignKeyConstraint)k;
				if(fk.getRemoteConstraint()!=null) {
					IBasicTable t = fk.getRemoteConstraint().getConstrainedTable();
					if(t!=null && !displayedTables.contains(t)) {
						final IDiagramItem fkItem = createTableItem(fk.getRemoteConstraint().getConstrainedTable(),x,y);
						parentItems.add(fkItem);
						// Adjusting position variables
						y+=fkItem.getHeight()+V_SPACING;
						// MAnaging max height / width
						if(y>maxHeight) maxHeight = y;
						if(fkItem.getWidth()> maxWidth) {
							maxWidth = fkItem.getWidth();
						}
						// Registering item to diagrm
						d.addItem(fkItem);
						displayedTables.add(t);
					}
				}
				break;
			}
		}
		leftHeight = maxHeight;
		// Central table (current)
        x+=maxWidth + H_SPACING;
        y=1;
        
        IDiagramItem tabItem = new DiagramItem(VersionHelper.getVersionable(table),x,y);
        tabItem.setHeight(40 + 21 * table.getColumns().size());
        d.addItem(tabItem);
        
        // Child tables
        x+=tabItem.getWidth() + H_SPACING;
        y=1;
        
        Collection<IReferencer> children = CorePlugin.getService(IReferenceManager.class).getReverseDependencies(table);
        for(IReferencer r : children) {
        	if(r instanceof IBasicTable && !displayedTables.contains(r)) {
        		final IDiagramItem i = createTableItem((IBasicTable)r,x,y);
        		childItems.add(i);
        		y+=i.getHeight()+V_SPACING;
        		if(y>maxHeight) maxHeight = y;
        		d.addItem(i);
        		displayedTables.add((IBasicTable)r);
        	}
        }
        
        // Repositioning central table Y
        tabItem.setYStart(maxHeight/2 - tabItem.getHeight()/2);
        
        // Repositioning minimum height elements
        if(leftHeight<maxHeight) {
        	repositionItem(parentItems, leftHeight, maxHeight);
        } else {
        	repositionItem(childItems, y, maxHeight);
        }
        // Setting contents in the UI thread cause we're in a job here
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
        	@Override
        	public void run() {
        		viewer.setContents(d);
        	}
        });
        
	}
	
	private void repositionItem(Collection<IDiagramItem> items, int itemsHeight, int maxHeight) {
		int deltaY = (maxHeight - itemsHeight) / 2;
		for(IDiagramItem i : items) {
			i.setYStart(i.getYStart()+deltaY);
		}
	}
	private IDiagramItem createTableItem(IBasicTable t,int x,int y) {
		IDiagramItem i = new DiagramItem(t,x,y);
		i.setHeight(40 + 21 * t.getColumns().size());
		return i;
	}
	/**
	 * @see org.eclipse.ui.part.Page#getControl()
	 */
	@Override
	public Control getControl() {
		return viewer.getControl();
	}

	/**
	 * @see org.eclipse.ui.part.Page#setFocus()
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
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
    private void zoomFit() {
    	if(viewer != null && viewer.getControl() != null) {
	        Rectangle r = viewer.getControl().getBounds();
	        if(r.width != 0 && r.height != 0) {
	            root.getZoomManager().setZoomAsText(ZoomManager.FIT_ALL);
	            root.getZoomManager().setZoomAsText(ZoomManager.FIT_ALL);
	        }
    	}
    }

}
