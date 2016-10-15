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
package com.nextep.designer.sqlgen.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.designer.sqlgen.ui.impl.SQLSearchQuery;

public class SearchSQLHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchSite site = HandlerUtil.getActiveSite(event);
		ISelectionProvider provider = site.getSelectionProvider();
		ISelection sel = provider.getSelection();
		if (sel == null || sel.isEmpty())
			return null;
		if (sel instanceof ITextSelection) {
			ITextSelection textSel = (ITextSelection) sel;
			NewSearchUI.runQueryInBackground(new SQLSearchQuery(textSel.getText(), false, true));
		} else if (sel instanceof IStructuredSelection) {
			final IStructuredSelection s = (IStructuredSelection) sel;
			if (s.getFirstElement() instanceof IDatabaseObject<?>) {
				final IDatabaseObject<?> dbObj = (IDatabaseObject<?>) s.getFirstElement();
				// Running search query
				NewSearchUI.runQueryInBackground(new SQLSearchQuery(dbObj.getName(), false, true));
			}
		}
		return null;
	}

}
