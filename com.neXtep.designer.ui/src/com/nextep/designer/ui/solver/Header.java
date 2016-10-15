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
package com.nextep.designer.ui.solver;

import org.eclipse.swt.nebula.widgets.compositetable.AbstractNativeHeader;
import org.eclipse.swt.widgets.Composite;
import com.nextep.designer.ui.UIMessages;

public class Header extends AbstractNativeHeader {

	public Header(Composite parent, int style) {
		super(parent, style);
		// setLayout(new GridRowLayout(new int[] { 160, 100 }, false));
		setWeights(new int[] { 160, 100 });
		setColumnText(new String[] {
				UIMessages.getString("wizard.problems.problemColumnTitle"), UIMessages.getString("wizard.problems.hintColumnTitle") }); //$NON-NLS-1$ //$NON-NLS-2$
		// Label problem = new Label(this, SWT.NULL);
		//		problem.setText(UIMessages.getString("wizard.problems.problemColumnTitle")); //$NON-NLS-1$
		//		new Label(this, SWT.NULL).setText(UIMessages.getString("wizard.problems.hintColumnTitle")); //$NON-NLS-1$
	}

}
