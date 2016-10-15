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

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 *
 */
public class VersionManagerHandler extends AbstractVersionHandler {

	/**
	 * @see com.nextep.designer.vcs.ui.handlers.AbstractVersionHandler#isEnabled(com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	protected boolean isEnabled(IVersionable<?> v) {
		return true;
	}

	/**
	 * @see com.nextep.designer.vcs.ui.handlers.AbstractVersionHandler#versionAction(com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	protected Object versionAction(IVersionable<?> v) {
		try {
			VCSUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("com.neXtep.designer.vcs.ui.VersionInfo", v.getUID().toString(),IWorkbenchPage.VIEW_ACTIVATE);
		} catch( PartInitException e) {
			throw new ErrorException(e);
		}
		return null;
	}

}
