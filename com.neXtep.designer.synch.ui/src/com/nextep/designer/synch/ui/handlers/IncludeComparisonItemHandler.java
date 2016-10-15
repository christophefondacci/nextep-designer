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
package com.nextep.designer.synch.ui.handlers;

import java.util.Iterator;
import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.navigator.CommonNavigator;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.synch.ui.SynchUIPlugin;
import com.nextep.designer.synch.ui.services.ISynchronizationUIService;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.IComparisonItem;

public class IncludeComparisonItemHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			final Iterator<?> it = sel.iterator();
			// Retrieving reverse dependencies
			final MultiValueMap invRefMap = CorePlugin.getService(IReferenceManager.class)
					.getReverseDependenciesMap();
			// Selecting all items from the selection iteratively
			while (it.hasNext()) {
				Object selObject = it.next();
				if (selObject instanceof IComparisonItem) {
					final IComparisonItem item = (IComparisonItem) selObject;
					getSynchronizationService().selectProposal(item, ComparedElement.SOURCE,
							invRefMap);
					getSynchronizationService().adjustParents(item.getParent());
					if (item.getParent() != null) {
						item.getParent().getMergeInfo().setMergeProposal(null);
					}
				}
			}
			IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getPartService().getActivePart();
			if (part instanceof CommonNavigator) {
				((CommonNavigator) part).getCommonViewer().refresh();
			}
		}
		return null;
	}

	private ISynchronizationUIService getSynchronizationService() {
		return SynchUIPlugin.getService(ISynchronizationUIService.class);
	}
}
