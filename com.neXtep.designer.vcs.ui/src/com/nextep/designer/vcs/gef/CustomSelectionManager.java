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
package com.nextep.designer.vcs.gef;

import org.eclipse.gef.SelectionManager;
import org.eclipse.jface.viewers.ISelection;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;

/**
 * Custom implementation of the GEF Selection managers
 * which allows notification of selection changes
 *
 * @author Christophe Fondacci
 *
 */
public class CustomSelectionManager extends SelectionManager {
	private IObservable notifier;
	public CustomSelectionManager(IObservable notifier) {
		this.notifier = notifier;
	}
	/**
	 * @see org.eclipse.gef.SelectionManager#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void setSelection(ISelection newSelection) {
		super.setSelection(newSelection);
		notifier.notifyListeners(ChangeEvent.SELECTION_CHANGED, newSelection);
	}
}
