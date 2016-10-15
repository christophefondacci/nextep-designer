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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.MatchHeightAction;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.gef.action.PrintFitPageRetargetAction;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.DbgmUIPlugin;
import com.nextep.designer.dbgm.ui.DiagramEditorInput;
import com.nextep.designer.vcs.model.IDiagram;

/**
 * @author Christophe Fondacci
 */
public class DBGMGraphicalEditor extends GraphicalEditorWithFlyoutPalette implements IEventListener {

	// private final static Log log = LogFactory.getLog(DBGMGraphicalEditor.class);
	private static PaletteRoot PALETTE_ROOT;
	private IDiagram diagram;
	/** GEF Selection provider */
	private ISelectionProvider selProvider;
	/** Our overview outline */
	private IContentOutlinePage outlinePage;
	/** Ruler encapsulating our graphical control */
	private RulerComposite rulerComp;
	/** Dirty flag for outter diagram interactions like auto layout */
	private boolean dirty = false;
	public final static String EDITOR_ID = "com.neXtep.designer.dbgm.ui.diagramEditor"; //$NON-NLS-1$

	public DBGMGraphicalEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
	 */
	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		getGraphicalViewer().setEditPartFactory(new DBGMEditPartFactory());
		ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
		getGraphicalViewer().setRootEditPart(root);

		// Adding menu management
		DBGMGraphicalContextMenuProvider menuProvider = new DBGMGraphicalContextMenuProvider(
				getGraphicalViewer(), getActionRegistry());
		getGraphicalViewer().setContextMenu(menuProvider);
		getSite().registerContextMenu("com.neXtep.designer.dbgm.ui.gef", menuProvider, selProvider); //$NON-NLS-1$

		// Contributing zoom actions
		List<String> zoomLevels = new ArrayList<String>(3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);
		IAction zoomIn = new ZoomInAction(root.getZoomManager());
		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		getActionRegistry().registerAction(zoomIn);
		getActionRegistry().registerAction(zoomOut);

		// Contributing format actions (rulers, grid, snap)
		// IAction showRulers = new ToggleRulerVisibilityAction(getGraphicalViewer());
		// getActionRegistry().registerAction(showRulers);

		IAction snapAction = new ToggleSnapToGeometryAction(getGraphicalViewer());
		getActionRegistry().registerAction(snapAction);

		IAction showGrid = new ToggleGridAction(getGraphicalViewer());
		getActionRegistry().registerAction(showGrid);
		getCommandStack().addCommandStackEventListener(new CommandStackEventListener() {

			@Override
			public void stackChanged(CommandStackEvent event) {
				firePropertyChange(PROP_DIRTY);
			}

		});

		// Handling activation / deactivation on main graphical control
		Listener listener = new Listener() {

			public void handleEvent(Event event) {
				handleActivationChanged(event);
			}
		};
		getGraphicalControl().addListener(SWT.Activate, listener);
		getGraphicalControl().addListener(SWT.Deactivate, listener);
		loadProperties();
	}

	private void loadProperties() {
		// Snap to Geometry property
		getGraphicalViewer().setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, new Boolean(true));

		// Grid properties
		getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, new Boolean(true));
		// We keep grid visibility and enablement in sync
		getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, new Boolean(true));

		// Scroll-wheel Zoom
		getGraphicalViewer().setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1),
				MouseWheelZoomHandler.SINGLETON);

	}

	/**
	 * Handling activation updates from the Ruler composite
	 * 
	 * @param event
	 */
	protected void handleActivationChanged(Event event) {
		IAction copy = null;
		if (event.type == SWT.Deactivate)
			copy = getActionRegistry().getAction(ActionFactory.COPY.getId());
		if (getEditorSite().getActionBars().getGlobalActionHandler(ActionFactory.COPY.getId()) != copy) {
			getEditorSite().getActionBars()
					.setGlobalActionHandler(ActionFactory.COPY.getId(), copy);
			getEditorSite().getActionBars().updateActionBars();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action;
		action = new MatchWidthAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new MatchHeightAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		registry.registerAction(new PrintFitPageRetargetAction(this));

		// action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.LEFT);
		// registry.registerAction(action);
		// getSelectionActions().add(action.getId());
		//
		// action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.RIGHT);
		// registry.registerAction(action);
		// getSelectionActions().add(action.getId());
		//
		// action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.TOP);
		// registry.registerAction(action);
		// getSelectionActions().add(action.getId());
		//
		// action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.BOTTOM);
		// registry.registerAction(action);
		// getSelectionActions().add(action.getId());
		//
		// action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.CENTER);
		// registry.registerAction(action);
		// getSelectionActions().add(action.getId());
		//
		// action = new AlignmentAction((IWorkbenchPart)this, PositionConstants.MIDDLE);
		// registry.registerAction(action);
		// getSelectionActions().add(action.getId());
	}

	/**
	 * Encapsulating graphical viewer creation with the Ruler composite
	 */
	@Override
	protected void createGraphicalViewer(Composite parent) {
		rulerComp = new RulerComposite(parent, SWT.NONE);
		super.createGraphicalViewer(rulerComp);
		rulerComp.setGraphicalViewer((ScrollingGraphicalViewer) getGraphicalViewer());
	}

	/**
	 * Overriding to expose our encapsulating ruler
	 */
	@Override
	protected Control getGraphicalControl() {
		return rulerComp;
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		// Adding context menu
		// registerContextMenu();
		outlinePage = new DBGMGraphicalOutline(this.getGraphicalViewer());
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		if (input == null) {
			throw new PartInitException(new Status(IStatus.ERROR, DbgmUIPlugin.PLUGIN_ID,
					DBGMUIMessages.getString("editor.diagram.diagramLoadError"))); //$NON-NLS-1$
		}
		diagram = ((DiagramEditorInput) input).getDiagram();
		setPartName(input.getName());
		Designer.getListenerService().registerListener(this, diagram, this);
	}

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#initializeGraphicalViewer()
	 */
	@Override
	protected void initializeGraphicalViewer() {
		this.getGraphicalViewer().setContents(diagram);
		this.getGraphicalViewer().addDropTargetListener(
				new DiagramSelectionDropTargetListener(getGraphicalViewer(), diagram));
	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		CorePlugin.getIdentifiableDao().save(
				(IdentifiedObject) getGraphicalViewer().getContents().getModel());
		dirty = false;
		getCommandStack().markSaveLocation();
		firePropertyChange(PROP_DIRTY);
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isDirty() {
		return super.isDirty() || dirty;
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPaletteRoot()
	 */
	@Override
	protected PaletteRoot getPaletteRoot() {
		if (PALETTE_ROOT == null)
			PALETTE_ROOT = DBGMGraphicalPaletteFactory.createPalette();
		return PALETTE_ROOT;
	}

	/**
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class type) {
		if (type == ZoomManager.class) {
			return getGraphicalViewer().getProperty(ZoomManager.class.toString());
		} else if (type == IContentOutlinePage.class) {
			return outlinePage;
		}
		return super.getAdapter(type);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		super.selectionChanged(part, selection);
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) selection;
			GraphicalEditPart e = (GraphicalEditPart) getGraphicalViewer().getEditPartRegistry()
					.get(sel.getFirstElement());
			ZoomManager zoom = ((ScalableFreeformRootEditPart) getGraphicalViewer()
					.getRootEditPart()).getZoomManager();
			if (e != null) {
				getGraphicalViewer().select(e);
				FigureCanvas c = (FigureCanvas) getGraphicalViewer().getControl();
				double zoomRatio = zoom == null ? 1d : zoom.getZoom();
				if (c != null) {
					c.scrollSmoothTo((int) (zoomRatio * e.getFigure().getBounds().x),
							(int) (zoomRatio * e.getFigure().getBounds().y));
				}
			}
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (diagram.getName() != null && getPartName() != null
				&& diagram.getName().equals(getPartName())) {
			setPartName(diagram.getName());
		}
		setDirty(true);
	}
}
