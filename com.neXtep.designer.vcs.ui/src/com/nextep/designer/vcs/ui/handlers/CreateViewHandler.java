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
/**
 *
 */
package com.nextep.designer.vcs.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.ui.impl.ExceptionHandler;
import com.nextep.designer.vcs.ui.services.VersionUIHelper;

/**
 * @author Christophe Fondacci
 */
public class CreateViewHandler extends AbstractHandler {

	/**
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// Saving current view
		IWorkspace currentView = VersionHelper.getCurrentView();
		try {
			IWorkspace newView = (IWorkspace) UIControllerFactory.getController(
					IElementType.getInstance(IWorkspace.TYPE_ID)).newInstance(null);
			// If not cancelled we create the view
			CorePlugin.getIdentifiableDao().save(newView);
			VersionUIHelper.changeView(newView.getUID());
		} catch (CancelException e) {
			VersionHelper.setCurrentView(currentView);
			ExceptionHandler.handle(e);
		}
		// TODO Auto-generated method stub
		return null;
	}

}
