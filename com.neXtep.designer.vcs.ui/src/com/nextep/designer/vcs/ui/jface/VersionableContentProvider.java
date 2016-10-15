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
package com.nextep.designer.vcs.ui.jface;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerListener;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.ui.impl.TypedNode;
import com.nextep.designer.vcs.ui.model.ITypedNode;
import com.nextep.designer.vcs.ui.navigators.VersionNavigatorRoot;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * This class is the main content provider of the version navigator. It provides elements that build
 * the tree of the current workspace. Every element managed by this provider is tracked and refresh
 * is automatically managed by a smooth background refresh job.
 * 
 * @author Christophe Fondacci
 */
public class VersionableContentProvider implements ITreeContentProvider, IEventListener,
		IMarkerListener {

	private static final Log LOGGER = LogFactory.getLog(VersionableContentProvider.class);
	private StructuredViewer viewer;

	public VersionableContentProvider() {
		CorePlugin.getService(IMarkerService.class).addMarkerListener(this);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof VersionNavigatorRoot) {
			final VersionNavigatorRoot root = (VersionNavigatorRoot) parentElement;
			final Collection<?> contents = root.getContents();
			// Registering all objects so that we'll keep track of their lifecycle
			for (Object o : contents) {
				registerObject(o);
			}
			return contents.toArray();
		} else if (parentElement instanceof IVersionContainer) {
			final IVersionContainer parent = (IVersionContainer) parentElement;
			// Registering controller as model listener (backward compatibility as the controller is
			// responsible for executing save events on specific events).
			for (IVersionable<?> v : new ArrayList<IVersionable<?>>(parent.getContents())) {
				registerObject(v);
			}
			return TypedNode.buildNodesFromCollection(parent, parent.getContents(), this).toArray();
		} else if (parentElement instanceof ITypedNode) {
			return ((ITypedNode) parentElement).getChildren().toArray();
		}
		return null;
	}

	private void registerObject(Object o) {
		if (o instanceof IObservable) {
			Designer.getListenerService().registerListener(this, (IObservable) o, this);
			final ITypedObjectUIController controller = UIControllerFactory.getController(o);
			if (controller != null) {
				Designer.getListenerService().registerListener(this, (IObservable) o, controller);
			}
		}
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ITypedNode) {
			return ((ITypedNode) element).getParent();
		} else if (element instanceof IWorkspace) {
			return VersionNavigatorRoot.getInstance();
		} else if (element instanceof IVersionContainer) {
			return ((IVersionable<?>) element).getContainer();
		} else if (element instanceof IVersionable<?>) {
			final IVersionable<?> v = (IVersionable<?>) element;
			return new TypedNode(v.getType(), v.getContainer());
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IVersionContainer) {
			return true;
		} else if (element instanceof ITypedNode) {
			return true;
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
		CorePlugin.getService(IMarkerService.class).removeMarkerListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (StructuredViewer) viewer;
		Designer.getListenerService().unregisterListeners(this);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case GENERIC_CHILD_ADDED:
		case GENERIC_CHILD_REMOVED:
		case COLUMN_ADDED:
		case COLUMN_REMOVED:
		case CONSTRAINT_ADDED:
		case CONSTRAINT_REMOVED:
		case INDEX_ADDED:
		case INDEX_REMOVED:
		case TRIGGER_ADDED:
		case TRIGGER_REMOVED:
		case DATASET_ADDED:
		case DATALINE_REMOVED:
		case DATASET_REMOVED:
		case DATALINE_ADDED:
		case VERSIONABLE_ADDED:
		case VERSIONABLE_REMOVED:
		case PARTITION_ADDED:
		case PARTITION_REMOVED:
		case NAME_CHANGED:
		case MODEL_CHANGED:
		case CHECKIN:
		case CHECKOUT:
		case UPDATES_LOCKED:
		case UPDATES_UNLOCKED:
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Object " + source + " refreshed in the version navigator"); //$NON-NLS-1$ //$NON-NLS-2$
				if (source instanceof IVersionable<?>) {
					final IVersionInfo version = ((IVersionable<?>) source).getVersion();
					LOGGER.debug("  => " + version); //$NON-NLS-1$
				}
			}
			refreshElement(source);
			break;
		default:
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Object " + source + " NOT REFRESHED in the version navigator: event=" //$NON-NLS-1$ //$NON-NLS-2$
						+ event.name());
			}
		}
	}

	private void refreshElement(Object o) {
		IWorkspaceUIService workspaceService = CorePlugin.getService(IWorkspaceUIService.class);
		workspaceService.refreshNavigatorFor(o);
	}

	@Override
	public void markersChanged(final Object o, Collection<IMarker> oldMarkers,
			Collection<IMarker> newMarkers) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (viewer != null && !viewer.getControl().isDisposed()) {
					refreshElement(o);
				}
			}
		});
	}

	@Override
	public void markersReset(Collection<IMarker> allMarkers) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (viewer != null && !viewer.getControl().isDisposed()) {
					viewer.refresh(true);
				}
			}
		});
	}
}
