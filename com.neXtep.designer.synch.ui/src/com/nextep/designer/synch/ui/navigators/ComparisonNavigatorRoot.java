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
package com.nextep.designer.synch.ui.navigators;

import java.util.Collection;
import org.eclipse.core.runtime.IAdaptable;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;

public class ComparisonNavigatorRoot extends Observable implements IAdaptable,
		ISynchronizationListener {

	private static ComparisonNavigatorRoot instance = null;
	private ISynchronizationResult synchResult;

	private ComparisonNavigatorRoot() {
		// SynchUIPlugin.getSynchronizationUIService().addSynchronizationListener(this);
	}

	public static ComparisonNavigatorRoot getInstance() {
		if (instance == null) {
			instance = new ComparisonNavigatorRoot();
		}
		return instance;
	}

	public Collection<IComparisonItem> getItems() {
		return synchResult.getComparedItems();
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public void newSynchronization(ISynchronizationResult synchResult) {
		this.synchResult = synchResult;
		notifyListeners(ChangeEvent.MODEL_CHANGED, this);
	}

	public ISynchronizationResult getCurrentSynchronization() {
		return synchResult;
	}

	@Override
	public void scopeChanged(ComparisonScope scope) {
	}
}
