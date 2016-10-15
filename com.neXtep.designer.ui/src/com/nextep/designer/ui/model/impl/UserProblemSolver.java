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
package com.nextep.designer.ui.model.impl;

import java.util.Arrays;
import java.util.List;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IProblemSolver;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.model.base.RunnableWithReturnedValue;
import com.nextep.designer.ui.wizards.ResolveProblemsWizard;

public class UserProblemSolver implements IProblemSolver {

	@Override
	public boolean solve(IMarker... problemMarkers) {
		return solve(Arrays.asList(problemMarkers));
	}

	@Override
	public boolean solve(final List<IMarker> problemMarkers) {
		RunnableWithReturnedValue<Boolean> runnable = new RunnableWithReturnedValue<Boolean>() {

			@Override
			public void run() {
				ResolveProblemsWizard wiz = new ResolveProblemsWizard(problemMarkers);
				WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(), wiz);
				dlg.setBlockOnOpen(true);
				// Always validating for unit tests
				if (Designer.isUnitTest()) {
					returnedValue = true;
					return;
				}
				dlg.open();
				returnedValue = dlg.getReturnCode() == Window.OK;
			}
		};
		Display.getDefault().syncExec(runnable);
		if (!runnable.returnedValue) {
			throw new CancelException(UIMessages.getString("wizard.problems.cancel")); //$NON-NLS-1$
		}
		return true;
	}

}
