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
package com.nextep.designer.synch.model;

import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * This interface defines a listener on synchronization events.
 * 
 * @author Christophe Fondacci
 */
public interface ISynchronizationListener {

	/**
	 * Notifies that a new synchronization has been performed and transmit the result of the
	 * comparison which should be used as the start of the synchronization process.
	 * 
	 * @param items all {@link IComparisonItem} of the synchronization
	 */
	void newSynchronization(ISynchronizationResult synchronizationResult);

	/**
	 * Notifies all listeners that the synchronization scope has changed.
	 * 
	 * @param scope new synchronization scope
	 */
	void scopeChanged(ComparisonScope scope);
}
