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
import com.nextep.datadesigner.gui.model.InvokableController;
import com.nextep.datadesigner.vcs.gui.dialog.UserLoginGUI;

/**
 * @author Christophe Fondacci
 *
 */
public class ChangeUserHandler extends AbstractHandler {

	/**
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final UserLoginGUI userGUI = new UserLoginGUI() {
			/**
			 * @see com.nextep.datadesigner.vcs.gui.dialog.UserLoginGUI#connect()
			 */
			@Override
			protected void connect() {
				super.connect();
				if(isAuthenticated()) {
					getShell().dispose();
				}
			}
		};
		while(!userGUI.isAuthenticated()) {
			userGUI.resetShell();
			new InvokableController() {
				@Override
				public Object invoke(Object... arg) {
					invokeGUI(userGUI);
					return null;
				}
			}.invoke();
		}
		new ChangeViewHandler().execute(null);
		return null;
	}

}
