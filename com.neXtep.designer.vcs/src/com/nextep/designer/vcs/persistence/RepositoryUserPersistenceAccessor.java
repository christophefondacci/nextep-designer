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
package com.nextep.designer.vcs.persistence;

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.RepositoryUser;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.ICheckedObject;
import com.nextep.designer.core.model.IPersistenceAccessor;
import com.nextep.designer.vcs.model.IRepositoryUser;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class RepositoryUserPersistenceAccessor implements IPersistenceAccessor<IRepositoryUser> {

	private static final Log LOGGER = LogFactory.getLog(RepositoryUserPersistenceAccessor.class);

	@Override
	public void delete(IRepositoryUser element) {
		CorePlugin.getIdentifiableDao().delete(element);
	}

	@Override
	public boolean isHandledForLoad(IElementType typeToLoad, ITypedObject... parents) {
		return parents.length == 0;
	}

	@Override
	public Collection<IRepositoryUser> load(IElementType typeToLoad, ITypedObject... parents) {
		throw new ErrorException("Unsupported load operation"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IRepositoryUser> loadAll(IElementType typeToLoad) {
		return (Collection<IRepositoryUser>) CorePlugin.getIdentifiableDao().loadAll(
				RepositoryUser.class);
	}

	@Override
	public void save(IRepositoryUser element) {
		try {
			// Checking user consistency before saving
			((ICheckedObject) element).checkConsistency();
		} catch (InconsistentObjectException ioe) {
			LOGGER.warn("User was not saved: " + ioe.getMessage());
			return;
		}
		CorePlugin.getIdentifiableDao().save(element);
	}

}
