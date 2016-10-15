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
package com.nextep.designer.vcs.model.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.VersioningOperation;

/**
 * Default implementation of the {@link IVersioningOperationContext}.
 * 
 * @author Christophe Fondacci
 */
public class VersioningOperationContext implements IVersioningOperationContext {

	private Collection<IVersionable<?>> versionables;
	private IActivity activity;
	private Map<IVersionable<?>, IVersionInfo> versionableVersionMap = new HashMap<IVersionable<?>, IVersionInfo>();
	private VersioningOperation operation;

	public VersioningOperationContext(VersioningOperation operation,
			Collection<IVersionable<?>> versionables) {
		this.versionables = versionables;
		this.operation = operation;
	}

	@Override
	public Collection<IVersionable<?>> getVersionables() {
		return versionables;
	}

	@Override
	public IActivity getActivity() {
		return activity;
	}

	@Override
	public void setActivity(IActivity activity) {
		this.activity = activity;
	}

	@Override
	public void setTargetVersionInfo(IVersionable<?> element, IVersionInfo version) {
		versionableVersionMap.put(element, version);
	}

	@Override
	public IVersionInfo getTargetVersionInfo(IVersionable<?> element) {
		return versionableVersionMap.get(element);
	}

	@Override
	public VersioningOperation getVersioningOperation() {
		return operation;
	}
}
