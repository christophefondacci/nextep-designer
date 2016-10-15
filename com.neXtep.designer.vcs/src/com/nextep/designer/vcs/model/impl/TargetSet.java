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
package com.nextep.designer.vcs.model.impl;

import java.util.Collection;
import java.util.HashSet;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.TargetType;
import com.nextep.designer.vcs.model.ITargetSet;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * @author Christophe Fondacci
 */
public class TargetSet extends Observable implements ITargetSet {

	private Collection<IConnection> targets;
	private IWorkspace view;
	private UID id;

	public TargetSet() {
		targets = new HashSet<IConnection>(); // new HashMap<TargetType,IConnection>();
	}

	/**
	 * @see com.nextep.designer.vcs.model.ITargetSet#addTarget(com.nextep.designer.core.model.IConnection)
	 */
	@Override
	public void addConnection(IConnection target) {
		targets.add(target);
		notifyListeners(ChangeEvent.CONNECTION_ADDED, target);
	}

	/**
	 * @see com.nextep.designer.vcs.model.ITargetSet#getTargets()
	 */
	@Override
	public Collection<IConnection> getConnections() {
		return targets;
	}

	/**
	 * @see com.nextep.designer.vcs.model.ITargetSet#removeTarget(com.nextep.designer.core.model.IConnection)
	 */
	@Override
	public void removeConnection(IConnection target) {
		targets.remove(target);
		notifyListeners(ChangeEvent.CONNECTION_REMOVED, target);
	}

	/**
	 * @see com.nextep.designer.vcs.model.ITargetSet#setTargets(java.util.Set)
	 */
	@Override
	public void setConnections(Collection<IConnection> targets) {
		this.targets = targets;
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#getUID()
	 */
	@Override
	public UID getUID() {
		return id;
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#setUID(com.nextep.datadesigner.model.UID)
	 */
	@Override
	public void setUID(UID id) {
		this.id = id;
	}

	protected long getId() {
		if (id == null) {
			return 0;
		}
		return id.rawId();
	}

	protected void setId(long id) {
		setUID(new UID(id));
	}

	/**
	 * @see com.nextep.designer.vcs.model.ITargetSet#getTarget(com.nextep.designer.core.model.TargetType)
	 */
	@Override
	public Collection<IConnection> getTarget(TargetType targetType) {
		return targets;
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public IWorkspace getView() {
		return view;
	}

	@Override
	public void setView(IWorkspace view) {
		this.view = view;
	}
}
