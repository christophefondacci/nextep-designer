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
package com.nextep.designer.synch.ui.controls;

import java.util.Collection;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.ui.SynchUIImages;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.synch.ui.SynchUIPlugin;
import com.nextep.designer.synch.ui.services.ISynchronizationUIService;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;

public class SynchSelectionControl extends ContributionItem {

	private ToolItem item;
	boolean unselectAction = true;
	private ISynchronizationListener listener;

	public SynchSelectionControl() {
		registerSynchListener();
	}

	public SynchSelectionControl(String id) {
		super(id);
		registerSynchListener();
	}

	private void registerSynchListener() {
		listener = new ISynchronizationListener() {

			@Override
			public void scopeChanged(ComparisonScope scope) {
				unselectAction = true;
				updateControl();
			}

			@Override
			public void newSynchronization(ISynchronizationResult synchronizationResult) {
				unselectAction = true;
				updateControl();
			}
		};
		getSynchronizationService().addSynchronizationListener(listener);
	}

	@Override
	public void fill(ToolBar parent, int index) {
		item = new ToolItem(parent, SWT.PUSH);
		// Updating appearance
		updateControl();
		// Listening to command execution
		item.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				unselectAction = !unselectAction;
				// Executing selection
				selectAll(unselectAction);
				// Switching controls to selection status
				updateControl();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void updateControl() {
		if (item != null) {
			item.setImage(unselectAction ? SynchUIImages.UNSELECT_ALL : SynchUIImages.SELECT_ALL);
			item.setToolTipText(SynchUIMessages.getString(
					unselectAction ? "command.tooltip.unselectAll" : "command.tooltip.selectAll")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * (Un)Selects all synchronization items.
	 * 
	 * @param selected true to select, false to unselect
	 */
	private void selectAll(boolean selected) {
		IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService()
				.getActivePart();
		ISynchronizationResult result = part.getAdapter(ISynchronizationResult.class);
		if (result != null) {
			final Collection<IComparisonItem> items = result.getComparedItems();
			final ISynchronizationUIService synchService = getSynchronizationService();
			final ComparedElement selection = selected ? ComparedElement.SOURCE
					: ComparedElement.TARGET;
			// Selecting all items from the selection iteratively
			for (IComparisonItem item : items) {
				synchService.selectProposal(item, selection, false);
			}
		}
		if (part instanceof CommonNavigator) {
			((CommonNavigator) part).getCommonViewer().refresh();
		}
	}

	private ISynchronizationUIService getSynchronizationService() {
		return SynchUIPlugin.getService(ISynchronizationUIService.class);
	}

	@Override
	public void dispose() {
		getSynchronizationService().removeSynchronizationListener(listener);
		super.dispose();
	}
}
