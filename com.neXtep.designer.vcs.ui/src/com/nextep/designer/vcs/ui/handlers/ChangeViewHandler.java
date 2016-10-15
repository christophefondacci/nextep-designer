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
import com.nextep.datadesigner.gui.model.InvokableController;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.ui.dialogs.ViewSelectorDialog;
import com.nextep.designer.vcs.ui.services.VersionUIHelper;

/**
 * @author Christophe Fondacci
 *
 */
public class ChangeViewHandler extends AbstractHandler {

	/**
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		new SelectorController().invoke();

		// Code if OK (else CancelException is raised)
		VersionUIHelper.changeView(VersionHelper.getCurrentView().getUID());
		// TODO Auto-generated method stub
		return null;
	}

	private class SelectorController extends InvokableController {

		/**
		 * @see com.nextep.datadesigner.model.IInvokable#invoke(java.lang.Object[])
		 */
		@Override
		public Object invoke(Object... arg) {
			ViewSelectorDialog gui = new ViewSelectorDialog() {
				/**
				 * @see com.nextep.designer.vcs.ui.dialogs.ViewSelectorDialog#handleButtonOKWidgetSelected()
				 */
				@Override
				protected void handleButtonOKWidgetSelected() {
					super.handleButtonOKWidgetSelected();
					if(isOK()) {
						this.getShell().dispose();
					}
				}
			};
			invokeGUI(gui);
			if(!gui.isOK()) {
				throw new CancelException("Change view cancelled");
			}
			// TODO Auto-generated method stub
			return null;
		}

	}
}
