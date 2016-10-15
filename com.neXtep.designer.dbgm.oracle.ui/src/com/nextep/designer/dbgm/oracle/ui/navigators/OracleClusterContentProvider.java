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
package com.nextep.designer.dbgm.oracle.ui.navigators;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.vcs.ui.impl.TypedNode;
import com.nextep.designer.vcs.ui.model.ITypedNode;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

public class OracleClusterContentProvider implements ITreeContentProvider, IEventListener {

	private final static Log LOGGER = LogFactory.getLog(OracleClusterContentProvider.class);

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IOracleCluster) {
			final IOracleCluster cluster = (IOracleCluster) parentElement;
			final Collection<Object> elts = new ArrayList<Object>();
			final Collection<IOracleClusteredTable> clusteredTabs = cluster.getClusteredTables();
			if (clusteredTabs != null) {
				for (IOracleClusteredTable t : clusteredTabs) {
					try {
						IBasicTable table = (IBasicTable) VersionHelper.getReferencedItem(t
								.getTableReference());
						elts.add(table);
					} catch (RuntimeException e) {
						LOGGER.warn(
								MessageFormat.format(
										DBOMUIMessages.getString("navigator.cluster.tabError"), //$NON-NLS-1$
										e.getMessage()), e);
					}
				}
				return TypedNode.buildNodesFromCollection(cluster,
						(Collection<? extends ITypedObject>) elts, this).toArray();
			}
		} else if (parentElement instanceof ITypedNode) {
			return ((ITypedNode) parentElement).getChildren().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IOracleCluster) {
			return true;
		} else if (element instanceof ITypedNode) {
			return !((ITypedNode) element).getChildren().isEmpty();
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
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		CorePlugin.getService(IWorkspaceUIService.class).refreshNavigatorFor(source);
	}

}
