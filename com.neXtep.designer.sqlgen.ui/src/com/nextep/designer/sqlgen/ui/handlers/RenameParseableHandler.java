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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.services.IParsingService;
import com.nextep.designer.vcs.ui.handlers.RenameHandler;

/**
 * A specific handler for renaming procedures as the SQL source of procedure needs to be updated
 * according to the new name defined.
 * 
 * @author Christophe Fondacci
 */
public class RenameParseableHandler extends RenameHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final String newName = (String) super.execute(event);
		if (newName != null) {
			ISelection s = HandlerUtil.getCurrentSelection(event);
			if (s instanceof IStructuredSelection && !s.isEmpty()) {
				final IStructuredSelection sel = (IStructuredSelection) s;
				Object o = sel.getFirstElement();
				if (o instanceof IParseable) {
					final IParseable parseable = (IParseable) o;
					IParsingService parsingService = getParsingService();
					parsingService.rename(parseable, newName);
				}
			}
		}
		return newName;
	}

	private IParsingService getParsingService() {
		return DbgmPlugin.getService(IParsingService.class);
	}
}
