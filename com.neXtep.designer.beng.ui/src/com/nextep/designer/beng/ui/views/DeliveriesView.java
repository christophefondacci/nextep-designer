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
package com.nextep.designer.beng.ui.views;

import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.impl.VersionedSQLScript;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.services.IDeliveryService;
import com.nextep.designer.beng.ui.jface.DeliveriesContentProvider;
import com.nextep.designer.beng.ui.jface.DeliveriesLabelProvider;
import com.nextep.designer.beng.ui.model.DeliveryTypeItem;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * A view for deliveries navigation
 * 
 * @author Christophe Fondacci
 */
public class DeliveriesView extends ViewPart implements DropTargetListener, DragSourceListener {

	private static final Log log = LogFactory.getLog(DeliveriesView.class);
	public static final String VIEW_ID = "com.nextep.designer.beng.ui.views.DeliveriesView"; //$NON-NLS-1$
	private Tree deliveriesTree;
	private TreeViewer treeViewer;

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		deliveriesTree = new Tree(parent, SWT.MULTI);
		treeViewer = new TreeViewer(deliveriesTree);
		treeViewer.setLabelProvider(new DeliveriesLabelProvider());
		treeViewer.setContentProvider(new DeliveriesContentProvider());
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				final IStructuredSelection s = (IStructuredSelection) treeViewer.getSelection();
				if (s.size() == 1) {
					final Object o = s.getFirstElement();
					ITypedObject objectToOpen = null;
					if (o instanceof IDeliveryModule) {
						objectToOpen = (ITypedObject) o;
					} else if (o instanceof IDeliveryItem<?>) {
						Object content = ((IDeliveryItem<?>) o).getContent();
						if (content instanceof ITypedObject) {
							objectToOpen = (ITypedObject) content;
						}
					} else if (o instanceof ITypedObject) {
						objectToOpen = (ITypedObject) o;
					}
					if (objectToOpen != null) {
						UIControllerFactory.getController(objectToOpen).defaultOpen(objectToOpen);
					}
				}
			}
		});
		getSite().setSelectionProvider(treeViewer);

		// Target of drop operations
		treeViewer.addDropSupport(DND.DROP_COPY | DND.DROP_LINK | DND.DROP_MOVE,
				new Transfer[] { LocalSelectionTransfer.getTransfer() }, this);
		// We allow dragging from the navigator
		treeViewer.addDragSupport(DND.DROP_COPY | DND.DROP_LINK | DND.DROP_MOVE,
				new Transfer[] { LocalSelectionTransfer.getTransfer() }, this);
		registerContextMenu(treeViewer);

		// Registering listener on every root container of this view
		IWorkspace view = VCSPlugin.getViewService().getCurrentWorkspace();
		for (IVersionable<?> v : view.getContents()) {
			if (v.getType() == IElementType.getInstance(IVersionContainer.TYPE_ID)) {
				Designer.getListenerService().registerListener(deliveriesTree, v,
						new IEventListener() {

							@Override
							public void handleEvent(ChangeEvent event, IObservable source,
									Object data) {
								if (event == ChangeEvent.CHECKOUT
										|| event == ChangeEvent.UPDATES_LOCKED
										|| event == ChangeEvent.UPDATES_UNLOCKED) {
									refresh();
								}
							}
						});
			}
		}
		treeViewer.setInput(view);
	}

	public void refresh() {
		treeViewer.refresh();
	}

	private void registerContextMenu(ISelectionProvider provider) {
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);

		// this is to work around complaints about missing standard groups.
		contextMenu.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});

		getSite().registerContextMenu(contextMenu, provider);
		Menu menu = contextMenu.createContextMenu(deliveriesTree);
		deliveriesTree.setMenu(menu);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		deliveriesTree.setFocus();
	}

	public void refreshModule(IVersionContainer module) {
		treeViewer.refresh();
	}

	@Override
	public void dragEnter(DropTargetEvent e) {
		log.debug("DeliveriesView: Drop enter"); //$NON-NLS-1$
		for (int i = 0; i < e.dataTypes.length; i++) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(e.dataTypes[i])) {
				e.currentDataType = e.dataTypes[i];
			}
		}
	}

	@Override
	public void dragLeave(DropTargetEvent arg0) {
	}

	@Override
	public void dragOperationChanged(DropTargetEvent arg0) {
	}

	@Override
	public void dragOver(DropTargetEvent e) {
		for (int i = 0; i < e.dataTypes.length; i++) {
			final ISelection s = LocalSelectionTransfer.getTransfer().getSelection();
			if (s instanceof IStructuredSelection && !s.isEmpty()) {
				final IStructuredSelection sel = (IStructuredSelection) s;
				// Only accepting tables or containers
				final Iterator<?> selIt = sel.iterator();
				while (selIt.hasNext()) {
					Object elt = selIt.next();
					// If not a table and not a container we deny the drop action
					if (!(elt instanceof ISQLScript)
							&& (!(elt instanceof IDeliveryItem) || (elt instanceof IDeliveryModule))) {
						return;
					}
				}
				// Only accepting the drop to delivery types
				TreeItem item = (TreeItem) e.item;
				if (item != null && item.getData() instanceof DeliveryTypeItem) {
					e.currentDataType = e.dataTypes[i];
					e.detail = DND.DROP_LINK;
				} else {
					e.detail = DND.DROP_NONE;
				}
			}
		}
	}

	@Override
	public void drop(DropTargetEvent e) {

		if (e.data instanceof IStructuredSelection) {
			final IDeliveryService deliveryService = BengPlugin.getService(IDeliveryService.class);
			final IStructuredSelection s = (IStructuredSelection) e.data;
			final Iterator<?> selIt = s.iterator();
			while (selIt.hasNext()) {

				Object draggedObj = selIt.next();
				if (draggedObj instanceof ISQLScript
						|| (draggedObj instanceof IDeliveryItem && !(draggedObj instanceof IDeliveryModule))) {

					TreeItem i = (TreeItem) e.item;
					if (i != null && i.getData() instanceof DeliveryTypeItem) {
						final DeliveryTypeItem typeItem = (DeliveryTypeItem) i.getData();
						final DeliveryType type = typeItem.getType();
						final IDeliveryModule module = typeItem.getModule();

						IDeliveryItem<?> addedItem = null;
						if (draggedObj instanceof VersionedSQLScript) {
							final VersionedSQLScript vs = (VersionedSQLScript) draggedObj;
							// Dropping a script on a JDBC module require specific processing
							// as this script should be executed for all vendors. We do this
							// by setting a null vendor, interpreted by the installer as "any"
							// vendor
							DBVendor vendor = vs.getContainer().getDBVendor();
							if (vendor == DBVendor.JDBC) {
								vendor = null;
							}
							addedItem = deliveryService.createDeliveryScript(type,
									vs.getSqlScript());
							addedItem.setDBVendor(vendor);
							module.addDeliveryItem(addedItem);
						} else if (draggedObj instanceof ISQLScript) {
							// Dropping a script on a JDBC module require specific processing
							// as this script should be executed for all vendors. We do this
							// by setting a null vendor, interpreted by the installer as "any"
							// vendor
							DBVendor vendor = DBGMHelper.getCurrentVendor();
							if (vendor == DBVendor.JDBC) {
								vendor = null;
							}
							addedItem = deliveryService.createDeliveryScript(type,
									(ISQLScript) draggedObj);
							addedItem.setDBVendor(vendor);
							module.addDeliveryItem(addedItem);
						} else if (draggedObj instanceof IDeliveryItem) {
							final IDeliveryItem<?> dlv = (IDeliveryItem<?>) draggedObj;
							module.removeDeliveryItem(dlv);
							dlv.setDeliveryType(type);
							module.addDeliveryItem(dlv);
							addedItem = dlv;
						}
						treeViewer.expandToLevel(addedItem, 1);
					} else {
						log.error("Unable to drop this SQL script here, please target a valid delivery container."); //$NON-NLS-1$
					}
				} else {
					log.error("Deliveries only accept SQL scripts drag / drop sources."); //$NON-NLS-1$
				}
			}
		}
	}

	@Override
	public void dropAccept(DropTargetEvent arg0) {
	}

	@Override
	public void dragSetData(DragSourceEvent e) {

		LocalSelectionTransfer.getTransfer().setSelection(treeViewer.getSelection());
		e.doit = true;
		e.data = treeViewer.getSelection();
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		ISelection s = treeViewer.getSelection();
		if (!s.isEmpty()) {
			event.doit = true;
		}

	}

	@Override
	public void dragFinished(DragSourceEvent arg0) {
		// Nothing
		log.debug("TreeNavigator: Drag finished."); //$NON-NLS-1$
	}
}
