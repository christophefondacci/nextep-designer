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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.XYAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.gef.commands.ConnectionCreateCommand;
import com.nextep.designer.dbgm.gef.commands.ForeignKeyReconnectCommand;
import com.nextep.designer.dbgm.gef.figures.EditableLabel;
import com.nextep.designer.dbgm.gef.figures.TableFigure;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class DiagramItemPart extends AbstractGraphicalEditPart implements NodeEditPart,
		IEventListener, IModelOriented<Object> {

	private static final Log log = LogFactory.getLog(DiagramItemPart.class);
	private static final String UNRESOLVED_TEXT = "<Unresolved table>";
	private IDiagramItem item;
	private IBasicTable table;

	public DiagramItemPart(IDiagramItem item) {
		this.item = item;
		IReference ref = item.getItemReference();
		try {
			table = (IBasicTable) VersionHelper.getReferencedItem(ref);// .getVersionnedObject().getModel();
		} catch (ErrorException e) {
			log.error(e.getMessage());
			table = null;
		}
		setModel(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void registerModel() {
		super.registerModel();
		getViewer().getEditPartRegistry().put(item.getItemModel(), this);
	}

	@Override
	protected void unregisterModel() {
		super.unregisterModel();
		getViewer().getEditPartRegistry().remove(item.getItemModel());
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			Designer.getListenerService().registerListener(this, ((IObservable) getModel()), this);
			if (table != null) {
				Designer.getListenerService().registerListener(this, table, this);
			}
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			Designer.getListenerService().unregisterListener(((IObservable) getModel()), this);
			if (table != null) {
				Designer.getListenerService().unregisterListener(table, this);
			}
		}
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		EditableLabel label = new EditableLabel(table == null ? UNRESOLVED_TEXT : table.getName());
		TableFigure tableFigure = new TableFigure(label);
		return tableFigure;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	@Override
	protected List<?> getModelChildren() {
		// if(item.getItemModel()/*.getVersionnedObject().getModel()*/ instanceof IBasicTable) {
		if (table != null) {
			return table.getColumns();
		}

		// }
		return super.getModelChildren();
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getContentPane()
	 */
	@Override
	public IFigure getContentPane() {
		TableFigure figure = (TableFigure) getFigure();
		return figure.getColumnsFigure();
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		// allow removal of the associated model element
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new DiagramItemEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new DiagramItemXYLayoutPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new GraphicalNodeEditPolicy() {

			@Override
			protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
				ConnectionCreateCommand cmd = (ConnectionCreateCommand) request.getStartCommand();
				cmd.setEnd((IDiagramItem) getModel());
				return cmd;
			}

			@Override
			protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
				if (getModel() != null) {
					ConnectionCreateCommand cmd = new ConnectionCreateCommand(
							(IDiagramItem) getModel());
					request.setStartCommand(cmd);
					return cmd;
				}
				// If no model, no creation
				return null;
			}

			@Override
			protected Command getReconnectSourceCommand(ReconnectRequest request) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected Command getReconnectTargetCommand(ReconnectRequest request) {
				ForeignKeyConstraint fk = (ForeignKeyConstraint) request.getConnectionEditPart()
						.getModel();
				ForeignKeyReconnectCommand cmd = new ForeignKeyReconnectCommand(fk);
				cmd.setNewRemote((IDiagramItem) getModel());

				return cmd;
			}

		});
		// installEditPolicy(EditPolicy.LAYOUT_ROLE, new
		// DiagramXYLayoutPolicy(item.getParentDiagram()));

	}

	protected void refreshVisuals() {
		// notify parent container of changed position & location
		// if this line is removed, the XYLayoutManager used by the parent container
		// (the Figure of the ShapesDiagramEditPart), will not know the bounds of this figure
		// and will not draw it correctly.
		Rectangle bounds = new Rectangle(item.getXStart(), item.getYStart(), item.getWidth(),
				item.getHeight());
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), bounds);

		TableFigure fig = (TableFigure) getFigure();
		fig.getNameLabel().setText(table == null ? UNRESOLVED_TEXT : table.getName());
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
	 */
	@Override
	protected List<ForeignKeyConstraint> getModelSourceConnections() {
		if (table == null)
			return Collections.emptyList();
		List<ForeignKeyConstraint> fkeys = new ArrayList<ForeignKeyConstraint>();
		for (IKeyConstraint k : table.getConstraints()) {
			switch (k.getConstraintType()) {
			case FOREIGN:
				ForeignKeyConstraint fk = (ForeignKeyConstraint) k;
				// Checking nullity because this method is invoked while removing items (because of
				// the setSelected() call)
				try {
					final IKeyConstraint remote = fk.getRemoteConstraint();
					if (remote != null
							&& getViewer().getEditPartRegistry().get(remote.getConstrainedTable()) != null) {
						fkeys.add((ForeignKeyConstraint) k);
					}
				} catch (UnresolvedItemException e) {
					log.error(e);
				}
				break;
			}
		}
		return fkeys;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
	 */
	@Override
	protected List<ForeignKeyConstraint> getModelTargetConnections() {
		if (table == null)
			return Collections.emptyList();
		List<ForeignKeyConstraint> fkeys = new ArrayList<ForeignKeyConstraint>();
		List<IVersionable<?>> tables = VersionHelper.getAllVersionables(VCSPlugin.getViewService()
				.getCurrentWorkspace(), IElementType.getInstance("TABLE")); //$NON-NLS-1$
		for (IVersionable<?> v : tables) {
			// Checking nullity because this method is invoked while removing items (because of the
			// setSelected() call)
			if (getViewer().getEditPartRegistry().get(v) != null) {
				for (IKeyConstraint c : ((IBasicTable) v).getConstraints()) {
					if (c.getConstraintType() == ConstraintType.FOREIGN) {
						fkeys.add((ForeignKeyConstraint) c);
					}
				}
			}
		}
		List<ForeignKeyConstraint> connections = new ArrayList<ForeignKeyConstraint>();
		// for(IDiagramItem i : item.getParentDiagram().getItems()) {
		// Object model = VersionHelper.getReferencedItem(i.getItemReference());
		// if(i instanceof IBasicTable) {
		// }
		// List<IVersionable<?>> fkeys =
		// VersionHelper.getAllVersionables(VersionHelper.getCurrentView(),
		// IElementType.getInstance("FOREIGN_KEY"));
		for (ForeignKeyConstraint fk : fkeys) {
			if (fk.getRemoteConstraint() != null
					&& fk.getRemoteConstraint().getConstrainedTable() == table) {
				connections.add(fk);
			}
		}
		return connections;

	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());// getAnchor(connection, true);
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return getAnchor(request, true);
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure()); // getAnchor(connection, false);
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return getAnchor(request, false);
	}

	private ConnectionAnchor getAnchor(Request request, boolean source) {
		if (request instanceof ReconnectRequest) {
			return getAnchor(((ReconnectRequest) request).getConnectionEditPart(), source);
		}
		return new ChopboxAnchor(getFigure());
	}

	private ConnectionAnchor getAnchor(ConnectionEditPart connection, boolean source) {
		PolylineConnection c = (PolylineConnection) connection.getFigure();
		if (source) {
			Point p = new Point(c.getStart());
			getFigure().translateToAbsolute(p);
			return new XYAnchor(p); // ItemAnchor(getFigure(),c.getStart());
		} else {
			Point p = new Point(c.getEnd());
			getFigure().translateToAbsolute(p);
			return new XYAnchor(p); // ItemAnchor(getFigure(),c.getEnd());
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (getParent() == null)
			return;
		refreshChildren();
		refreshVisuals();
		switch (event) {
		case CONSTRAINT_ADDED:
		case CONSTRAINT_REMOVED:
			getParent().refresh();
			// refreshSourceConnections();
			// refreshTargetConnections();
			// refreshSourceConnections();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class key) {
		if (key == IVersionable.class) {
			return VersionHelper.getVersionable(((IDiagramItem) getModel()).getItemModel());
		}
		return super.getAdapter(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setModel(Object model) {
		if (model instanceof IBasicTable) {
			// Removing listeners on previous model
			if (getModel() != null) {
				Designer.getListenerService().unregisterListener(((IObservable) getModel()), this);
			}
			if (table != null) {
				Designer.getListenerService().unregisterListener(table, this);
			}
			this.table = (IBasicTable) model;
			if (getViewer() != null && getViewer().getEditPartRegistry() != null) {
				getViewer().getEditPartRegistry().put(model, this);
			}
			// Registering listeners on new model
			Designer.getListenerService().registerListener(this, ((IObservable) getModel()), this);
			if (table != null) {
				Designer.getListenerService().registerListener(this, table, this);
			}
		} else {
			super.setModel(model);
		}

	}

	@Override
	public void setSelected(int value) {
		// When selecting a table, we highlight parent and child tables

		// Enlighting children
		List<ForeignKeyConstraint> fks = getModelTargetConnections();
		final Map<?, ?> partRegistry = getViewer().getEditPartRegistry();
		for (ForeignKeyConstraint fk : fks) {
			DiagramItemPart p = (DiagramItemPart) partRegistry.get(fk.getConstrainedTable());
			ConnectionEditPart c = (ConnectionEditPart) partRegistry.get(fk);
			if (value != EditPart.SELECTED_NONE) {
				if (p != null && p.getFigure() != null) {
					p.getFigure().setBackgroundColor(FontFactory.DIAGRAM_CHILD_TABLE_COLOR);
				}
				if (c != null && c.getFigure() != null) {
					((PolylineConnection) c.getFigure()).setLineWidth(2);
				}
			} else {
				if (p != null && p.getFigure() != null) {
					p.getFigure().setBackgroundColor(FontFactory.LIGHT_YELLOW);
				}
				if (c != null && c.getFigure() != null) {
					((PolylineConnection) c.getFigure()).setLineWidth(1);
				}
			}
		}

		// Enlighting parents
		fks = getModelSourceConnections();
		for (ForeignKeyConstraint fk : fks) {
			DiagramItemPart p = (DiagramItemPart) partRegistry.get(fk.getRemoteConstraint()
					.getConstrainedTable());
			ConnectionEditPart c = (ConnectionEditPart) partRegistry.get(fk);
			if (value != EditPart.SELECTED_NONE) {
				if (p != null && p.getFigure() != null) {
					p.getFigure().setBackgroundColor(FontFactory.DIAGRAM_PARENT_TABLE_COLOR);
				}
				if (c != null && c.getFigure() != null) {
					((PolylineConnection) c.getFigure()).setLineWidth(2);
				}
			} else {
				if (p != null && p.getFigure() != null) {
					p.getFigure().setBackgroundColor(FontFactory.LIGHT_YELLOW);
				}
				if (c != null && c.getFigure() != null) {
					((PolylineConnection) c.getFigure()).setLineWidth(1);
				}
			}
		}

		// TODO Auto-generated method stub
		super.setSelected(value);
	}

	@Override
	public void performRequest(Request req) {
		if (req.getType() == RequestConstants.REQ_OPEN) {
			UIControllerFactory.getController(item.getItemReference().getType()).defaultOpen(
					(ITypedObject) item.getItemModel());
		}
	}
}
