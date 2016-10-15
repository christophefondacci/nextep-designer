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
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.gui.VersionViewRulesNewEditor;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.IMarkerService;

/**
 * @author Christophe Fondacci
 */
public class EditViewHandler extends AbstractHandler {

	private class MyWizard extends Wizard {

		public MyWizard() {
			addPage(new VersionViewRulesNewEditor(VersionHelper.getCurrentView()));
			setWindowTitle("View contents edition");
		}

		@Override
		public boolean performFinish() {
			return true;
		}

	}

	/**
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		// Saving our view ID
		UID viewID = VersionHelper.getCurrentView().getUID();

		WizardDialog d = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell(), new MyWizard());
		d.setTitle("View edition");
		d.setBlockOnOpen(true);
		d.open();

		// Saving changes
		CorePlugin.getIdentifiableDao().save(VersionHelper.getCurrentView());
		CorePlugin.getService(IMarkerService.class).computeAllMarkers();
		return null;
	}

}
