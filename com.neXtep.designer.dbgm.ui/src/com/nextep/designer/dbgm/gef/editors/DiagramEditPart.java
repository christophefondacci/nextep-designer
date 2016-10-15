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
package com.nextep.designer.dbgm.gef.editors;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.rulers.RulerProvider;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.gef.figures.SchemaFigure;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;

/**
 * @author Christophe Fondacci
 */
public class DiagramEditPart extends AbstractGraphicalEditPart implements IEventListener {

	private IDiagram diagram;
	private boolean isModifiable = false;

	public DiagramEditPart(IDiagram diagram) {
		this.diagram = diagram;
		Designer.getListenerService().registerListener(this, diagram, this);
		this.setModel(diagram);
		isModifiable = Designer.checkIsModifiable(diagram, false);
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			Designer.getListenerService().registerListener(this, diagram, this);
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			Designer.getListenerService().unregisterListener(diagram, this);
			Designer.getListenerService().unregisterListeners(this);
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		Figure f = new SchemaFigure();
		f.setLayoutManager(new FreeformLayout()); // XYLayout());
		return f;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	@Override
	protected List<IDiagramItem> getModelChildren() {
		IDiagram diag = (IDiagram) getModel();
		return new ArrayList<IDiagramItem>(diag.getItems());
		// List<IBasicTable> tables = new ArrayList<IBasicTable>();
		// for(IDiagramItem i : diag.getItems()) {
		// if(i.getItemModel() instanceof IBasicTable) {
		// tables.add((IBasicTable)i.getItemModel());
		// }
		// }
		// return tables;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		if (isModifiable) {
			installEditPolicy(EditPolicy.COMPONENT_ROLE, new DiagramItemEditPolicy());
			installEditPolicy(EditPolicy.LAYOUT_ROLE, new DiagramXYLayoutPolicy(diagram));
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#refresh()
	 */
	@Override
	public void refresh() {
		super.refresh();
		for (Object o : this.getChildren()) {
			EditPart e = (EditPart) o;
			e.setParent(this);
			e.refresh();
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (source != diagram && source instanceof IDiagram) {

			diagram = (IDiagram) source;
			for (Object o : new ArrayList<Object>(this.getChildren())) {
				removeChild((EditPart) o);
			}
			setModel(diagram);
			// RootEditPart root = getRoot();
			// root.setContents(null);
			// root.setContents(this);

		}
		// switch(event) {
		// case ITEM_REMOVED:
		// removeChild((EditPart)getViewer().getEditPartRegistry().get(data));
		// break;
		// // case ITEM_ADDED:
		// // EditPart childPart = (EditPart)getViewer().getEditPartRegistry().get(data);
		// //
		// }
		if (isModifiable != VersionHelper.ensureModifiable(diagram, false)) {
			refreshEditPolicies();
		}
		refresh();
		// refreshChildren();
		// for(Object o : this.getChildren()) {
		// EditPart e = (EditPart)o;
		// e.refresh();
		// // if(e.getModel() == data) {
		// // e.refresh();
		// // }
		// }
		// // refreshTargetConnections();
		// refreshVisuals();
	}

	@Override
	public Object getAdapter(Class key) {
		if (key == SnapToHelper.class) {
			List snapStrategies = new ArrayList();
			Boolean val = (Boolean) getViewer()
					.getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
			if (val != null && val.booleanValue())
				snapStrategies.add(new SnapToGuides(this));
			val = (Boolean) getViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
			if (val != null && val.booleanValue())
				snapStrategies.add(new SnapToGeometry(this));
			val = (Boolean) getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
			if (val != null && val.booleanValue())
				snapStrategies.add(new SnapToGrid(this));

			if (snapStrategies.size() == 0)
				return null;
			if (snapStrategies.size() == 1)
				return snapStrategies.get(0);

			SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()];
			for (int i = 0; i < snapStrategies.size(); i++)
				ss[i] = (SnapToHelper) snapStrategies.get(i);
			return new CompoundSnapToHelper(ss);
		}
		return super.getAdapter(key);
	}

	private void refreshEditPolicies() {
		removeEditPolicy(EditPolicy.COMPONENT_ROLE);
		removeEditPolicy(EditPolicy.LAYOUT_ROLE);
		isModifiable = VersionHelper.ensureModifiable(diagram, false);
		createEditPolicies();
	}

	@Override
	protected void refreshVisuals() {
		// ConnectionLayer cLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
		// cLayer.setConnectionRouter(new ShortestPathConnectionRouter(getFigure()));
		super.refreshVisuals();
	}

	@Override
	protected void addChild(EditPart child, int index) {
		super.addChild(child, index);
	}
}
