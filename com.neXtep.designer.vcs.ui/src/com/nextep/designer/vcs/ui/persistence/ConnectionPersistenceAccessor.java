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
package com.nextep.designer.vcs.ui.persistence;

import java.util.Collection;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IPersistenceAccessor;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ITargetSet;

public class ConnectionPersistenceAccessor implements IPersistenceAccessor<IConnection> {

	@Override
	public void delete(IConnection element) {
		final ITargetSet targetSet = VCSPlugin.getViewService().getCurrentViewTargets();
		// Ensuring connection is no longer in current target set
		final Collection<IConnection> conn = targetSet.getConnections();
		if (conn.contains(element)) {
			targetSet.removeConnection(element);
		}
		// Saves the target set
		CorePlugin.getPersistenceAccessor().save(targetSet);
	}

	@Override
	public boolean isHandledForLoad(IElementType typeToLoad, ITypedObject... parents) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IConnection> load(IElementType typeToLoad, ITypedObject... parents) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<IConnection> loadAll(IElementType typeToLoad) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(IConnection element) {
		final ITargetSet targetSet = VCSPlugin.getViewService().getCurrentViewTargets();
		if (!targetSet.getConnections().contains(element)) {
			targetSet.addConnection(element);
		}
		// Saves the target set
		CorePlugin.getPersistenceAccessor().save(targetSet);
	}

}
