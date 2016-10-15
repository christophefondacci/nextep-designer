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
package com.nextep.designer.vcs.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.compare.IComparisonEditorProvider;

/**
 * A handler which opens comparison for the current selection with the currently defined comparison
 * editor.<br>
 * Selection must be a IComparisonItem for this handler to open the comparison, otherwise it will do
 * nothing.
 * 
 * @author Christophe Fondacci
 */
public class OpenComparisonHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection s = HandlerUtil.getCurrentSelection(event);
		if (!s.isEmpty() && s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) HandlerUtil
					.getCurrentSelection(event);
			if (sel.getFirstElement() instanceof IComparisonItem) {
				IComparisonItem compItem = (IComparisonItem) sel.getFirstElement();
				final IComparisonEditorProvider provider = VCSUIPlugin.getComparisonUIManager()
						.getComparisonEditorProvider(compItem.getType());
				VCSUIPlugin.getComparisonUIManager().openComparisonEditor(compItem, provider);
			}
		}
		return null;
	}

}
