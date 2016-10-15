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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.vcs.ui.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IComparisonListener;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.compare.IComparisonEditorProvider;
import com.nextep.designer.vcs.ui.views.DiffPreviewView;

/**
 * A comparison listener which shows the comparison view when a new comparison is triggerred.
 * 
 * @author Christophe Fondacci
 */
public class ShowViewComparisonListener implements IComparisonListener {

	private static final Log log = LogFactory.getLog(ShowViewComparisonListener.class);

	@Override
	public void newComparison(final String description, final IComparisonItem... comparisonItems) {
		try {
			if (PlatformUI.getWorkbench() == null
					|| PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null
					|| PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() == null) {
				log.error(VCSUIMessages.getString("comparison.view.unableToShow")); //$NON-NLS-1$
				return;
			}
			IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(DiffPreviewView.VIEW_ID);
			if (view instanceof IComparisonListener) {
				((IComparisonListener) view).newComparison(description, comparisonItems);
			}
			// If only one element to compare, we open the default comparison editor on it
			if (comparisonItems.length == 1) {
				final IComparisonItem compItem = comparisonItems[0];
				final IComparisonEditorProvider provider = VCSUIPlugin.getComparisonUIManager()
						.getComparisonEditorProvider(compItem.getType());
				VCSUIPlugin.getComparisonUIManager().openComparisonEditor(compItem, provider);
			}
		} catch (PartInitException e) {
			log.error(VCSUIMessages.getString("comparison.view.showViewError"), e); //$NON-NLS-1$
		}
	}

}
