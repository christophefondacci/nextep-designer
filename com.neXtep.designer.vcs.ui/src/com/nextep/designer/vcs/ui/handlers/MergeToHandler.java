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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * @author Christophe Fondacci
 *
 */
public class MergeToHandler extends AbstractHandler {
	private static final String EXTENSION_ID = "com.neXtep.designer.vcs.SelectionInvoker";

	/**
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Object model = UIHelper.getSelectedSingleModel(window);
		if(model instanceof IVersionContainer) {
			IVersionable<IVersionContainer> v = VersionHelper.getVersionable((IVersionContainer)model);
			if(v.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
				throw new ErrorException(VCSUIMessages.getString("mergeCheckOutError"));
			}
			Designer.getInstance().invokeSelection("merge", (IVersionContainer)model);
		}
		// TODO Auto-generated method stub
		return null;
	}

}
