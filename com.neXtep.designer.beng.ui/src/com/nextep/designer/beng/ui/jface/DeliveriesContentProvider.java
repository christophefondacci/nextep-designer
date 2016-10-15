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
package com.nextep.designer.beng.ui.jface;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.services.BENGServices;
import com.nextep.designer.beng.services.IDeliveryService;
import com.nextep.designer.beng.ui.model.DeliveryRootItem;
import com.nextep.designer.beng.ui.model.DeliveryTypeItem;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * This content provider provides the content for the deliveries view. It takes a {@link IWorkspace}
 * as its input and provides 2 root nodes, one for current workspace contents, the other for older
 * deliveries. Workspace deliveries are listed in place while older ones are structured in modules,
 * versions and devliery.
 * 
 * @author Christophe Fondacci
 */
public class DeliveriesContentProvider implements ITreeContentProvider, IEventListener {

	private IWorkspace versionView;
	private TreeViewer viewer;

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IWorkspace) {
			Designer.getListenerService().unregisterListeners(this);
			versionView = (IWorkspace) newInput;
			this.viewer = (TreeViewer) viewer;
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IWorkspace) {
			return new Object[] { new DeliveryRootItem(true), new DeliveryRootItem(false) };
		} else if (inputElement instanceof DeliveryRootItem) {
			final DeliveryRootItem rootItem = (DeliveryRootItem) inputElement;
			if (rootItem.isCurrentWorkspace()) {
				// If current workspace root, we list deliveries of current root
				// modules
				final List<IDeliveryModule> modules = new ArrayList<IDeliveryModule>();
				// Getting all root containers to retrieve their delivery
				// modules
				for (IVersionable<?> v : versionView.getContents()) {
					if (v.getType() == IElementType.getInstance(IVersionContainer.TYPE_ID)) {
						// Looking for the delivery module
						final IDeliveryModule module = BENGServices
								.getCurrentDelivery((IVersionContainer) v);
						if (module != null) {
							Designer.getListenerService().registerListener(this, module, this);
							ITypedObjectUIController controller = UIControllerFactory
									.getController(module);
							Designer.getListenerService().registerListener(controller, module,
									controller);
							Designer.getListenerService().registerListener(this, v, this);
							Designer.getListenerService().registerListener(this, v.getVersion(),
									this);
							modules.add(module);
						}
					}
				}
				return modules.toArray();
			} else {
				// Getting all root containers to list them
				List<IVersionContainer> containers = new ArrayList<IVersionContainer>();
				for (IVersionable<?> v : versionView.getContents()) {
					if (v.getType() == IElementType.getInstance(IVersionContainer.TYPE_ID)) {
						containers.add((IVersionContainer) v);
					}
				}
				return containers.toArray();
			}
		} else if (inputElement instanceof IVersionContainer) {
			// For containers, we list all previous versions
			IVersionable<IVersionContainer> versionable = VersionHelper
					.getVersionable((IVersionContainer) inputElement);
			List<IVersionInfo> historyList = getVersionHistoryList(versionable.getVersion());
			return historyList.toArray();
		} else if (inputElement instanceof IVersionInfo) {
			final IDeliveryModule module = BengPlugin.getService(IDeliveryService.class)
					.loadDelivery((IVersionInfo) inputElement);
			if (module != null) {
				ITypedObjectUIController controller = UIControllerFactory
						.getController(IElementType.getInstance(IDeliveryModule.TYPE_ID));
				Designer.getListenerService().registerListener(controller, module, controller);
				return new Object[] { module };
			}
		} else if (inputElement instanceof IDeliveryModule) {
			final List<DeliveryTypeItem> items = new ArrayList<DeliveryTypeItem>();
			for (DeliveryType t : DeliveryType.values()) {
				items.add(new DeliveryTypeItem(t, (IDeliveryModule) inputElement));
			}
			return items.toArray();
		} else if (inputElement instanceof DeliveryTypeItem) {
			final DeliveryTypeItem item = (DeliveryTypeItem) inputElement;
			return item.getModule().getDeliveries(item.getType()).toArray();
		} else if (inputElement instanceof SQLWrapperScript) {
			return ((SQLWrapperScript) inputElement).getChildren().toArray();
		}
		return null;
	}

	private List<IVersionInfo> getVersionHistoryList(IVersionInfo version) {
		final List<IVersionInfo> historyList = new ArrayList<IVersionInfo>();
		final IVersionInfo previousVersion = version.getPreviousVersion();
		if (previousVersion != null) {
			historyList.add(previousVersion);
			historyList.addAll(getVersionHistoryList(previousVersion));
		}
		final IVersionInfo mergedFromVersion = version.getMergedFromVersion();
		if (mergedFromVersion != null) {
			historyList.add(mergedFromVersion);
			historyList.addAll(getVersionHistoryList(mergedFromVersion));
		}
		return historyList;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if ((element instanceof DeliveryRootItem) || (element instanceof IDeliveryModule)
				|| (element instanceof IVersionInfo) || (element instanceof IVersionContainer)) {
			return true;
		} else if (element instanceof SQLWrapperScript) {
			return !((SQLWrapperScript) element).getChildren().isEmpty();
		} else if (element instanceof DeliveryTypeItem) {
			final DeliveryTypeItem item = (DeliveryTypeItem) element;
			return !item.getModule().getDeliveries(item.getType()).isEmpty();
		}
		return false;
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case ITEM_ADDED:
			viewer.refresh(source);
			viewer.expandToLevel(data, 1);
			break;
		case ITEM_REMOVED:
			viewer.refresh(source);
			break;
		case MODEL_CHANGED:
			if (source instanceof IDeliveryModule) {
				viewer.refresh(source);
			} else {
				viewer.refresh();
			}
			break;
		}
	}

}
