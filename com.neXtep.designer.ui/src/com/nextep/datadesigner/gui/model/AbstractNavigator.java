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
package com.nextep.datadesigner.gui.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.service.GUIService;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IInvokable;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * This class has been created to define common implementations of both <code>TypedNavigator</code>
 * and <code>UntypedNavigator</code>. Its only purpose is to avoid code duplicates and to ensure
 * fast development of new connectors.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractNavigator extends
		ListeningControlledConnector<TreeItem, INavigatorConnector> implements INavigatorConnector,
		IEventListener, SelectionListener {

	/** The list of child connectors of this navigator */
	private List<INavigatorConnector> connectors; // = new ArrayList<INavigatorConnector>();
	/** Initialization status */
	protected boolean initialized = false;
	/** Our SWT item connector */
	private TreeItem item;
	/** Our SWT tree which might be needed for null item parents */
	private Tree tree;
	/** The parent connector of this connector */
	private INavigatorConnector parent;
	/** A flag to avoir navigator refresh, settable through activateRefresh */
	private static boolean noRefresh = false;
	private static boolean noRegister = false;
	private boolean isBuilt = false;

	protected AbstractNavigator(IObservable model, ITypedObjectUIController controller) {
		super(model, controller);
		isBuilt = true;
		connectors = new ArrayList<INavigatorConnector>();
	}

	/**
	 * Initialization of the connector. This method will be called after SWT control creation. It
	 * will resursively loop on each defined child connectors and create them by calling the
	 * <code>createConnector</code> method.
	 */
	public void initialize() {
		initializeChildConnectors();
		// Since this method is always called after SWT control
		// creation, we register the dispose listener of the
		// new SWT control.
		this.getSWTConnector().addDisposeListener(this);
		// Looping on child connector to initialize them
		for (INavigatorConnector c : this.getConnectors()) {
			createConnector(c, -1);
		}
		initialized = true;
		registerNavigator();
	}

	/**
	 * This method registers navigator for the "link with editor" machanism. Implementors may
	 * override by an empty method to avoid linking (there should be 1 link by model object).
	 */
	protected void registerNavigator() {
		// Registering navigator
		if (!noRegister) {
			GUIService.registerNavigator(this, getSWTConnector().getParent());
		}
	}

	/**
	 * Deactivates regitration of link navigator <=> model
	 */
	protected static void deactivateRegister() {
		noRegister = true;
	}

	/**
	 * Activates registration of link navigator <=> model
	 */
	protected static void activateRegister() {
		noRegister = false;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#isInitialized()
	 */
	@Override
	public final boolean isInitialized() {
		return initialized;
	}

	/**
	 * Defines the default action of the connector. This method will generally be called when the
	 * user double clicks on the TreeItem corresponding to this connector.
	 * 
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#defaultAction()
	 */
	@Override
	public void defaultAction() {
		// IDisplayConnector displayConnector =
		// ControllerFactory.getController(this.getType()).initializeEditor(this.getModel());
		try {
			// Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("com.nextep.datadesigner.gui.impl.rcp.RCPViewWrapper",
			// this.getType().getName()+"_"+((IdentifiedObject)this.getModel()).getUID().toString(),
			// IWorkbenchPage.VIEW_ACTIVATE);
			if (this.getModel() instanceof ITypedObject) {
				CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(getEditorInput(), getEditorId());
			}
		} catch (PartInitException e) {
			throw new ErrorException(e);
		}
		// Designer.getInstance().getGUI().plugItemInEditorFolder(displayConnector);
	}

	/**
	 * @deprecated use {@link ITypedObjectUIController#getEditorId()} instead
	 */
	@Deprecated
	protected final String getEditorId() {
		return getController().getEditorId();
	}

	/**
	 * @return the editor input of the corresponding navigator editor
	 * @deprecated use {@link ITypedObjectUIController#getEditorInput(ITypedObject)} instead
	 */
	@Deprecated
	protected final IEditorInput getEditorInput() {
		return getController().getEditorInput((ITypedObject) this.getModel());
	}

	/**
	 * Default management of add connector (will sort child connectors)
	 */
	public void addConnector(INavigatorConnector c) {
		addConnector(c, true);
	}

	/**
	 * Adds a connector as a child of the current connector. This method allow callers
	 * 
	 * @param c child connector to add
	 * @param sort should we sort the child connectors before adding
	 */
	public void addConnector(final INavigatorConnector c, boolean sort) {
		connectors.add(c);
		c.setParent(this);
		if (sort) {
			sortConnectors(connectors);
		}
		if (initialized) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					createConnector(c, connectors.indexOf(c));
					// Expanding the item so that the new item will be visible
					if (autoExpand()) {
						getSWTConnector().setExpanded(true);
					}
				}
			});
		}
	}

	/**
	 * This method indicates if the navigator should autoexpand when child navigators are added.
	 * 
	 * @return a boolean indicating whether this navigator expands automatically or not.
	 */
	protected boolean autoExpand() {
		if (noRefresh) {
			return false;
		} else {
			return true;
		}
	}

	protected void sortConnectors(List<INavigatorConnector> conn) {
		Collections.sort(conn);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#createConnector(org.eclipse.swt.widgets.TreeItem,
	 *      int)
	 */
	public TreeItem create(TreeItem parent, int treeIndex) {
		TreeItem item = createSWTConnector(parent, treeIndex);
		item.addDisposeListener(this);
		Designer.getListenerService().activateListeners(this);
		return item;
	}

	/**
	 * This method creates a new connector.
	 * 
	 * @param c the connector to create
	 * @param index index at which the treeitem should be created
	 */
	protected abstract void createConnector(INavigatorConnector c, int index);

	/**
	 * Default management of remove connector
	 */
	public void removeConnector(final INavigatorConnector c) {
		if (c == null) {
			return;
		}
		if (c.getSWTConnector() != null) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					c.getSWTConnector().dispose();
				}
			});
		}
		connectors.remove(c);
	}

	/**
	 * Default management of getConnectors
	 */
	public final List<INavigatorConnector> getConnectors() {
		return connectors;
	}

	public INavigatorConnector getConnector(Object model) {
		for (INavigatorConnector c : connectors) {
			if (c.getModel() == model) {
				return c;
			}
		}
		return null;
		// throw new NoSuchConnectorException("No navigator exists for the specified model.");
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(INavigatorConnector o) {
		final String title = this.getTitle();
		if (title != null) {
			return this.getTitle().compareTo(o.getTitle());
		} else {
			return o.getTitle() != null ? 1 : 0;
		}
	}

	/**
	 * Implements the DEFAULT behaviour for SWT control disposal, which is to unregister the
	 * navigator from the model listeners. <br>
	 * <br>
	 * There might be other actions to perform by extending this method, like removing cached data,
	 * maps, etc.
	 * 
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent event) {
		// Unregistering
		GUIService.unregisterNavigator(this, getSWTConnector().getParent());
		super.widgetDisposed(event);
		// If the related model displayed by the navigator is observable...
		if (this.getModel() instanceof IObservable) {
			// then we will not listener anymore because SWT controls
			// which displays the model are disposed
			Designer.getListenerService().unregisterListener((IObservable) this.getModel(), this);
		}
		// Removing from parent
		if (getParent() != null) {
			getParent().getConnectors().remove(this);
		}

	}

	protected void addContributorActions(Menu menu) {
		Collection<IConfigurationElement> conf = Designer.getInstance().getExtensions(
				"com.neXtep.designer.ui.typeAction", "typeId", this.getType().getId());
		for (IConfigurationElement e : conf) {
			String label = e.getAttribute("label");
			String iconPath = e.getAttribute("icon");
			IInvokable invoker = null;
			try {
				invoker = (IInvokable) e.createExecutableExtension("actionInvoker");
			} catch (CoreException ex) {
				throw new ErrorException(ex);
			}
			Image icon = null;
			if (iconPath != null && !"".equals(iconPath)) {
				icon = CoreUiPlugin.getImageDescriptor(iconPath).createImage();
			}
			MenuItem item = new MenuItem(menu, SWT.NONE);
			item.setText(label);
			item.setImage(icon);
			item.setData(invoker);
			item.addSelectionListener(this);
		}
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() instanceof MenuItem) {
			MenuItem item = (MenuItem) e.getSource();

			((IInvokable) item.getData()).invoke(this.getModel());
		}
	}

	/**
	 * Default implementation returning the name of a {@link INamedObject} model. This method
	 * assumes the model is a named object. Other implementation must override.
	 * 
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		if (getModel() != null) {
			return notNull(((INamedObject) getModel()).getName());
		} else {
			return "";
		}
	}

	/**
	 * Default implementation of connector refresh which set the connector's title and dispatches
	 * the refresh to all sub connectors.
	 * 
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#refreshConnector()
	 */
	public void refreshConnector() {
		if (noRefresh)
			return;
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				if (getSWTConnector() != null && !getSWTConnector().isDisposed()) {
					getSWTConnector().setText(getTitle());
					if (isInitialized()) {
						for (INavigatorConnector c : getConnectors()) {
							c.refreshConnector();
						}
					}
				}
			}
		});
	}

	/**
	 * Creates the SWT tree connector given its parent. This method <b>must not</b> bother with
	 * child connector creation and must not call the <code>createSWTConnector</code> method on its
	 * own child connectors. The children SWT connector creation will be performed by abstract
	 * implementors <code>TypedNavigator</code> or <code>UntypedNavigator</code>. <br>
	 * All implementors should extend one of these 2 class.
	 * 
	 * @param parent parent item in the tree
	 * @param treeIndex TODO
	 */
	protected TreeItem createSWTConnector(TreeItem parent, int treeIndex) {
		if (parent != null) {
			if (treeIndex == -1) {
				item = new TreeItem(parent, SWT.NONE);
			} else {
				item = new TreeItem(parent, SWT.NONE, treeIndex);
			}
		} else if (tree != null) {
			if (treeIndex == -1) {
				item = new TreeItem(tree, SWT.NONE);
			} else {
				item = new TreeItem(tree, SWT.NONE, treeIndex);
			}
		} else {
			throw new ErrorException(
					"Trying to create a tree item without a parent item AND no tree defined.");
		}
		item.setData(this);
		if (getConnectorIcon() != null) {
			item.setImage(getConnectorIcon());
		}
		return item;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getSWTConnector()
	 */
	public TreeItem getSWTConnector() {
		return item;
	}

	/**
	 * Default implementation which assumes we have a typed object model and returns the
	 * corresponding type. Non typed model connectors must override
	 * 
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getType()
	 */
	@Override
	public IElementType getType() {
		return ((ITypedObject) getModel()).getType();
	}

	/**
	 * Default implementation which returns the type icon.
	 * 
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return ImageFactory.getImage(getType().getIcon());
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getParent()
	 */
	@Override
	public INavigatorConnector getParent() {
		return parent;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#setParent(com.nextep.datadesigner.gui.model.INavigatorConnector)
	 */
	@Override
	public void setParent(INavigatorConnector parent) {
		this.parent = parent;
	}

	public static void deactivateRefresh() {
		noRefresh = true;
	}

	public static void activateRefresh() {
		noRefresh = false;
	}

	/**
	 * This extension re-initializes all child connectors completely
	 */
	@Override
	public void setModel(Object model) {
		super.setModel(model);
		if (!isBuilt)
			return;
		// Removing all children
		if (getConnectors() != null) {
			// Ensuring UI thread since we may remove UI components here
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					for (INavigatorConnector nav : new ArrayList<INavigatorConnector>(
							getConnectors())) {
						removeConnector(nav);
					}

				}
			});
		}
		if (model != null) {
			if (connectors == null) {
				connectors = new ArrayList<INavigatorConnector>();
			}
			try {
				deactivateRefresh();
				// Creating children in UI thread
				getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						initializeChildConnectors();
					}
				});
			} finally {
				activateRefresh();
			}

		}
	}

	/**
	 * This method should be implemented to add child connectors to the current connector. It is
	 * very important that all navigators which have children add them through this method for
	 * proper refresh on check out / undo checkout actions.
	 */
	public void initializeChildConnectors() {
	}

	protected Display getDisplay() {
		return Display.getDefault();
	}
}
