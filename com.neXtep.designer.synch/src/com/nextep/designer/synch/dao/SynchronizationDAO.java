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
package com.nextep.designer.synch.dao;

import java.util.Collection;
import java.util.Collections;
import com.nextep.datadesigner.sqlgen.impl.SynchronizationFilter;
import com.nextep.datadesigner.sqlgen.model.ISynchronizationFilter;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This class contains some data-access specific method.
 * 
 * @author Christophe Fondacci
 */
public class SynchronizationDAO {

	/**
	 * Retrieves all {@link ISynchronizationFilter} for the given {@link IVersionContainer}.
	 * 
	 * @param c container to get synchronization filters for
	 * @return a collection of {@link ISynchronizationFilter}
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ISynchronizationFilter> getFilters(IVersionContainer c) {
		IVersionable<IVersionContainer> v = VersionHelper.getVersionable(c);
		if (v == null || v.getReference() == null || v.getReference().getUID() == null)
			return Collections.emptyList();
		Collection<ISynchronizationFilter> filters = (Collection<ISynchronizationFilter>) CorePlugin.getIdentifiableDao().loadForeignKey(SynchronizationFilter.class,
						v.getReference().getUID(), "containerRef", false);
		if (filters == null) { return Collections.emptyList(); }
		return filters;
	}
}
