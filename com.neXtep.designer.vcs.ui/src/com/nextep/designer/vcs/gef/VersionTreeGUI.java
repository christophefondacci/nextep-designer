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
package com.nextep.designer.vcs.gef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.GenericSelectionProvider;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.impl.DiagramItem;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionBranch;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 */
public class VersionTreeGUI extends ControlledDisplayConnector implements IObservable, IAdaptable {

	private static final int ITEM_SPACING = 30;
	private static final int ITEM_SIZE = 45;
	private static final int DIAGRAM_MARGIN_X = 7;
	private static final int DIAGRAM_MARGIN_Y = ITEM_SIZE + DIAGRAM_MARGIN_X;
	private static final int BRANCH_Y = 5;
	private static final Log log = LogFactory.getLog(VersionTreeGUI.class);
	private GraphicalViewer viewer;
	private IVersionInfo version;
	private int mode;
	private int orientation;
	private ScalableFreeformRootEditPart root;
	private VersionTreeDiagram diagram = null;
	boolean initialized = false;
	private boolean selectable = false;
	private Observable observable;
	private List<IVersionInfo> versionTree;
	private Object model;
	/** A selection provider providing current element and selection to workbench */
	private ISelectionProvider selectionProvider = new GenericSelectionProvider();

	private static class OrderedSchedulingRule implements ISchedulingRule {

		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule instanceof OrderedSchedulingRule;
		}
	}

	/**
	 * @param versionable
	 * @param mode either SWT.HORIZONTAL or SWT.VERTICAL
	 * @param selectable indicates if the tree will be user-selectable (displays a black box around
	 *        version items when the user clicks on it
	 * @param versionTree specifies the versions to display. Set to <code>null</code> to display the
	 *        full version tree
	 */
	public VersionTreeGUI(IVersionable<?> versionable, int mode, boolean selectable,
			List<IVersionInfo> versionTree) {
		super(null, null);
		this.version = versionable.getVersion();
		this.orientation = mode;
		this.selectable = selectable;
		observable = new Observable() {};
		this.versionTree = versionTree;
	}

	public VersionTreeGUI(IVersionable<?> versionable, int mode, boolean selectable) {
		super(null, null);
		this.version = versionable.getVersion();
		this.mode = mode;
		observable = new Observable() {};
		this.selectable = selectable;
	}

	/**
	 * @param versionable
	 * @param mode either SWT.HORIZONTAL or SWT.VERTICAL
	 */
	public VersionTreeGUI(IVersionInfo version, int mode) {
		super(null, null);
		this.version = version;
		this.mode = mode;
		observable = new Observable() {};
	}

	/**
	 * This method is thread-safe
	 * 
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(Object model) {
		if (getSWTConnector() == null || model == this.model || model == null) {
			return;
		}
		// Unregistering any previous model listeners
		if (this.model != null && this.model instanceof IObservable) {
			Designer.getListenerService().unregisterListener((IObservable) this.model, this);
		}
		// Setting current model
		this.model = model;
		// Registering listeners
		if (model instanceof IObservable) {
			Designer.getListenerService().registerListener(this, (IObservable) model, this);
		}
		IVersionInfo newVersion = null;
		if (model instanceof IVersionable<?>) {
			newVersion = ((IVersionable<?>) model).getVersion();
		} else if (model instanceof IVersionInfo) {
			newVersion = (IVersionInfo) model;
		}
		this.version = newVersion;
		// Updating current selection
		selectionProvider.setSelection(buildSelection());
		scheduleRefreshJob();
	}

	private void scheduleRefreshJob() {
		// Calling the thread-safe method
		Job j = new Job("Refreshing version-tree information...") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					versionTree = null;
					initializeGraphicalViewer();
				} catch (RuntimeException e) {
					log.error("Failed to display version tree", e);
					return new Status(Status.ERROR, "com.neXtep.designer.vcs.ui",
							"Failed to display version tree", e);
				}
				return Status.OK_STATUS;
			}
		};
		j.setRule(new OrderedSchedulingRule());
		j.schedule();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		// Creating graphical GEF viewer
		viewer = new ScrollingGraphicalViewer();
		Control c = viewer.createControl(parent);
		GridData d = new GridData();
		d.grabExcessHorizontalSpace = true;
		d.grabExcessVerticalSpace = true;
		d.horizontalAlignment = GridData.FILL;
		d.verticalAlignment = GridData.FILL;
		c.setLayoutData(d);
		viewer.setEditPartFactory(new VersionEditPartFactory(selectable));
		viewer.getControl().setBackground(ColorConstants.listBackground);
		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().setSelectionProvider(viewer);
		// hookGraphicalViewer();

		root = new ScalableFreeformRootEditPart();
		viewer.setRootEditPart(root);
		viewer.setSelectionManager(new CustomSelectionManager(this));
		List<String> zoomLevels = new ArrayList<String>(3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);
		viewer.getControl().setSize(100, 100);
		initializeGraphicalViewer();
		viewer.getControl().addDisposeListener(this);
		// viewer.getControl().addMouseListener(this);
		viewer.getControl().addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				refreshConnector();
			}
		});
		// Scroll-wheel Zoom
		viewer.setEditDomain(new DefaultEditDomain(null));
		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1),
				MouseWheelZoomHandler.SINGLETON);

		return viewer.getControl();
	}

	private IVersioningService getVersioningService() {
		return VCSPlugin.getService(IVersioningService.class);
	}

	/**
	 * This method builds the version tree from the current defined version model and attaches it to
	 * the graphical viewer. This method is thread-safe
	 */
	@SuppressWarnings("unchecked")
	private synchronized void initializeGraphicalViewer() {
		if (viewer == null || viewer.getControl() == null)
			return;
		// Loading the version hierarchy
		if (versionTree == null && version != null
				&& version.getReference().getReferenceId() != null) {
			versionTree = getVersioningService().listVersions(version.getReference());
		}
		final Map<String, Dimension> branchTextLength = new HashMap<String, Dimension>();
		// Few checks should be made in the display thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				if (viewer == null || (viewer != null && viewer.getControl() == null)) {
					return;
				}
				// Empty version use case
				if (version == null) {
					viewer.setContents(null);
					initialized = false;
					return;
				}
				// Orientating the tree from our actual control bounds
				if (orientation == SWT.NONE) {
					Rectangle r = viewer.getControl().getBounds();
					if (r.width > r.height) {
						mode = SWT.HORIZONTAL;
					} else {
						mode = SWT.VERTICAL;
					}
				} else {
					mode = orientation;
				}
				// Activating the zoom action handler
				IHandlerService service = (IHandlerService) VCSUIPlugin.getDefault().getWorkbench()
						.getActiveWorkbenchWindow().getActivePage().getActivePart().getSite()
						.getService(IHandlerService.class);
				service.activateHandler("com.neXtep.designer.vcs.ui.zoomIn", new ActionHandler(
						new ZoomInAction(root.getZoomManager())));

				// Computing branch names length from hierarchy (we need UI thread for TextExtent)
				if (versionTree != null) {
					// Browsing version tree to get all branches
					for (IVersionInfo v : versionTree) {
						if (v.getBranch() != null) {
							final String name = v.getBranch().getName();
							// Building text extent if not already done
							if (branchTextLength.get(name) == null) {
								final Dimension d = TextUtilities.INSTANCE.getTextExtents(name,
										FontFactory.FONT_BOLD);
								// Contributing to map
								branchTextLength.put(name, d);
							}
						}
					}
				}

			}
		});
		// Rechecking nullity to really exit
		if (version == null) {
			return;
		}
		if (versionTree != null) {
			Collections.sort(versionTree);
		}

		diagram = new VersionTreeDiagram();
		diagram.setCurrent(version);
		Map<IVersionBranch, Point> branchPositions = new HashMap<IVersionBranch, Point>();
		int freePosition = DIAGRAM_MARGIN_X;
		int currentX = 0, currentY = 0;
		IDiagramItem currentVersionItem = null;
		// Checking null version tree
		for (IVersionInfo v : versionTree != null ? versionTree
				: (List<IVersionInfo>) Collections.EMPTY_LIST) {
			if (v.isDropped()) {
				continue;
			}
			// Setting version successors
			if (v.getPreviousVersion() != null && versionTree != null
					&& versionTree.contains(v.getPreviousVersion())) {
				diagram.addVersionSuccessor(v.getPreviousVersion(), v);
			}
			if (v.getMergedFromVersion() != null && versionTree != null
					&& versionTree.contains(v.getMergedFromVersion())) {
				diagram.addVersionSuccessor(v.getMergedFromVersion(), v);
			}

			// Positioning
			Point position = branchPositions.get(v.getBranch());
			if (position == null) {
				int y = DIAGRAM_MARGIN_Y;
				if (v.getPreviousVersion() != null && versionTree != null
						&& versionTree.contains(v.getPreviousVersion())) {
					IDiagramItem prevItem = diagram.getItem(v.getPreviousVersion());
					if (prevItem != null) {
						y = ((mode == SWT.VERTICAL) ? prevItem.getYStart() : prevItem.getXStart())
								+ ITEM_SIZE + ITEM_SPACING;
					}
				}
				position = new Point(freePosition/* +ITEM_SIZE/2 */, y/* +ITEM_SIZE/2 */);
				// Calculating branch text length for item spacing
				final Dimension d = branchTextLength.get(v.getBranch().getName());
				freePosition += ITEM_SPACING + Math.max(ITEM_SIZE, d == null ? 0 : d.width);
				branchPositions.put(v.getBranch(), position);
			}
			IDiagramItem item = new DiagramItem(v, mode == SWT.VERTICAL ? position.x : position.y,
					mode == SWT.VERTICAL ? position.y : position.x);
			item.setHeight(ITEM_SIZE);
			item.setWidth(ITEM_SIZE);
			diagram.addItem(item);
			diagram.attachVersion(item, v);
			if (v.equals(version)) {
				currentVersionItem = item;
			}
			// Saving current version position for focus
			if (version == null) {
				return;
			}
			if (version.equals(v)) {
				currentX = position.x;
				currentY = position.y;
			}

			position.y += ITEM_SPACING + ITEM_SIZE;
		}
		// Setting branches
		for (IVersionBranch b : branchPositions.keySet()) {
			IDiagramItem item = new DiagramItem(new BranchReferenceAdapter(b),
					mode == SWT.VERTICAL ? branchPositions.get(b).x : BRANCH_Y,
					mode == SWT.VERTICAL ? BRANCH_Y : branchPositions.get(b).x);
			Dimension d = branchTextLength.get(b.getName());
			if (d == null) {
				d = new Dimension(100, 20);
			}
			item.setWidth(d.width);
			item.setHeight(ITEM_SIZE);
			diagram.addItem(item);
		}

		// Following is UI-related, so we ensure a synched display thread
		final IDiagramItem currentItem = currentVersionItem;
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				if (diagram != null) {
					viewer.setContents(diagram);
				}
			}
		});
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				org.eclipse.draw2d.geometry.Rectangle r = new org.eclipse.draw2d.geometry.Rectangle();
				r.x = 0; // Math.max(0, x-(ITEM_SIZE+ITEM_SPACING)*2);
				r.y = 0; // Math.max(0,y-(ITEM_SIZE+ITEM_SPACING)*5);
				r.width = (ITEM_SIZE + ITEM_SPACING) * 4;
				r.height = (ITEM_SIZE + ITEM_SPACING) * 9;
				// zoomTo(r);
				GraphicalEditPart e = (GraphicalEditPart) getGraphicalViewer()
						.getEditPartRegistry().get(currentItem);
				if (e != null) {
					FigureCanvas c = (FigureCanvas) getGraphicalViewer().getControl();
					double z = root.getZoomManager().getZoom();
					if (c != null && e.getFigure() != null) {
						c.scrollSmoothTo((int) (z * e.getFigure().getBounds().x), (int) (z * e
								.getFigure().getBounds().y));
					}
				}
			}
		});

		initialized = true;
	}

	public void zoomFit() {
		if (viewer != null && viewer.getControl() != null) {
			Rectangle r = viewer.getControl().getBounds();
			if (r.width != 0 && r.height != 0) {
				root.getZoomManager().setZoomAsText(ZoomManager.FIT_ALL);
				root.getZoomManager().setZoomAsText(ZoomManager.FIT_ALL);
			}
		}
	}

	private void zoomTo(org.eclipse.draw2d.geometry.Rectangle rect) {
		if (viewer != null && viewer.getControl() != null) {
			Rectangle r = viewer.getControl().getBounds();
			if (r.width != 0 && r.height != 0) {
				root.getZoomManager().setZoom(0.7d);
				root.getZoomManager().setViewLocation(
						new org.eclipse.draw2d.geometry.Point(rect.x, rect.y));
			}
		}
	}

	public void zoomIn() {
		if (viewer != null && viewer.getControl() != null) {
			Rectangle r = viewer.getControl().getBounds();
			if (r.width != 0 && r.height != 0 && root.getZoomManager().canZoomIn()) {
				root.getZoomManager().zoomIn();
			}
		}
	}

	public void zoomOut() {
		if (viewer != null && viewer.getControl() != null) {
			Rectangle r = viewer.getControl().getBounds();
			if (r.width != 0 && r.height != 0 && root.getZoomManager().canZoomOut()) {
				root.getZoomManager().zoomOut();
			}
		}
	}

	/**
	 * @return the currently selected object in the tree if this GUI has the selectable flag set to
	 *         <code>true</code> or the currently edited version otherwise.
	 * @see com.nextep.datadesigner.gui.model.IConnector#getModel()
	 */
	@Override
	public Object getModel() {
		if (selectable) {
			if (viewer.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				if (sel.size() > 0 && sel.getFirstElement() instanceof EditPart) {
					EditPart part = (EditPart) sel.getFirstElement();
					if (part.getModel() instanceof IDiagramItem) {
						return (IVersionInfo) ((IDiagramItem) part.getModel()).getItemModel();
					}
				}
			}
			return null;
		} else {
			return diagram;
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		if (viewer != null) {
			return viewer.getControl();
		} else {
			return null;
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Version tree viewer";
	}

	/**
	 * Thread-safe
	 * 
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case CHECKIN:
			scheduleRefreshJob();
			break;
		}
		refreshConnector();

	}

	public GraphicalViewer getGraphicalViewer() {
		return viewer;
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#addListener(com.nextep.datadesigner.model.IEventListener)
	 */
	@Override
	public void addListener(IEventListener listener) {
		Designer.getListenerService().registerListener(this, observable, listener);
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#getListeners()
	 */
	@Override
	public Collection<IEventListener> getListeners() {
		return observable.getListeners();
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#notifyListeners(com.nextep.datadesigner.model.ChangeEvent,
	 *      java.lang.Object)
	 */
	@Override
	public void notifyListeners(ChangeEvent event, Object o) {
		if (observable != null) {
			observable.notifyListeners(event, o);
		}
		selectionProvider.setSelection(buildSelection());
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#removeListener(com.nextep.datadesigner.model.IEventListener)
	 */
	@Override
	public void removeListener(IEventListener listener) {
		Designer.getListenerService().unregisterListener(observable, listener);
	}

	public void setVersionTree(List<IVersionInfo> versionTree) {
		this.versionTree = versionTree;
		initializeGraphicalViewer();
		refreshConnector();
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == ZoomManager.class) {
			return root.getZoomManager();
		}
		return null;
	}

	/**
	 * @return the {@link ISelectionProvider} which can provide and notify selection listeners of
	 *         selections occuring within the version tree
	 */
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	/**
	 * Builds current selection. The version tree selection is composed of the current model element
	 * version from which the tree is generated and optionally the selected version in the tree when
	 * the control is in the <i>selectable</i> state.
	 * 
	 * @return the current version tree selection
	 */
	private ISelection buildSelection() {
		Collection<IVersionInfo> versions = new ArrayList<IVersionInfo>();
		if (version != null) {
			versions.add(version);
			if (selectable) {
				IVersionInfo selectedVersion = (IVersionInfo) getModel();
				if (selectedVersion != null) {
					versions.add(selectedVersion);
				}
			}
		}
		final ISelection selection = new StructuredSelection(versions.toArray());
		return selection;
	}
}
