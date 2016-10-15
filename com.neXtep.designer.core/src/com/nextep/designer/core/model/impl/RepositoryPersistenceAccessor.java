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
package com.nextep.designer.core.model.impl;

import java.util.Collection;
import java.util.Collections;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IPersistenceAccessor;

public class RepositoryPersistenceAccessor implements IPersistenceAccessor<ITypedObject> {

	private final static Log LOGGER = LogFactory.getLog(RepositoryPersistenceAccessor.class);
	private static IPersistenceAccessor<ITypedObject> instance = null;

	private RepositoryPersistenceAccessor() {
	}

	public static IPersistenceAccessor<ITypedObject> getInstance() {
		if (instance == null) {
			instance = new RepositoryPersistenceAccessor();
		}
		return instance;
	}

	@Override
	public void delete(ITypedObject element) {
		if (element instanceof IdentifiedObject) {
			CorePlugin.getIdentifiableDao().delete((IdentifiedObject) element);
		} else {
			LOGGER.error(CoreMessages.getString("repository.persistence.skippedDelete") //$NON-NLS-1$
					+ NameHelper.getQualifiedName(element));
		}
	}

	@Override
	public boolean isHandledForLoad(IElementType typeToLoad, ITypedObject... parents) {
		return false;
	}

	@Override
	public Collection<ITypedObject> load(IElementType typeToLoad, ITypedObject... parents) {
		return Collections.emptyList();
	}

	@Override
	public Collection<ITypedObject> loadAll(IElementType typeToLoad) {
		return Collections.emptyList();
	}

	@Override
	public void save(ITypedObject element) {
		// Saving if we are not already
		if (!CorePlugin.getIdentifiableDao().isPersisting()) {
			if (element instanceof IdentifiedObject) {
				CorePlugin.getIdentifiableDao().save((IdentifiedObject) element);
			} else {
				LOGGER.error(CoreMessages.getString("repository.persistence.skippedSave") //$NON-NLS-1$
						+ NameHelper.getQualifiedName(element));
			}
		}
	}

}
