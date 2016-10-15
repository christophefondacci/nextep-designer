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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.core.services.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.dao.ITypedObjectDAO;
import com.nextep.designer.core.dao.impl.TypedObjectDAO;
import com.nextep.designer.core.services.IDAOService;

/**
 * @author Christophe Fondacci
 */
public class DAOService implements IDAOService {

	private final static String DAO_EXTENSION_ID = "com.neXtep.designer.core.typedDao"; //$NON-NLS-1$
	private static final String PERSISTENCE_EXTENSION_ID = "com.neXtep.designer.core.persistenceFile"; //$NON-NLS-1$
	private Map<IElementType, ITypedObjectDAO<ITypedObject>> daoTypeMap = null;

	@Override
	public List<ITypedObject> loadAll(IElementType type) {
		final ITypedObjectDAO<ITypedObject> dao = getDao(type);
		return dao.loadAll();
	}

	@Override
	public ITypedObject get(IElementType type, UID id) {
		final ITypedObjectDAO<ITypedObject> dao = getDao(type);
		return dao.get(id);
	}

	@Override
	public void save(ITypedObject objectToSave) {
		final ITypedObjectDAO<ITypedObject> dao = getDao(objectToSave.getType());
		dao.save(objectToSave);
	}

	@SuppressWarnings("unchecked")
	private ITypedObjectDAO<ITypedObject> getDao(IElementType type) {
		if (daoTypeMap == null) {
			loadDaoTypeMap();
		}
		final ITypedObjectDAO<ITypedObject> dao = daoTypeMap.get(type);
		if (dao == null) {
			throw new ErrorException(MessageFormat.format(
					CoreMessages.getString("service.dao.daoNotFound"), type.getId(),
					"No extension found"));
		}
		return dao;
	}

	private void loadDaoTypeMap() {
		daoTypeMap = new HashMap<IElementType, ITypedObjectDAO<ITypedObject>>();
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(
				DAO_EXTENSION_ID, "typeId", "*"); //$NON-NLS-1$
		// Registering explicit DAO from extensions
		for (IConfigurationElement elt : elts) {
			final String typeId = elt.getAttribute("typeId");
			final IElementType type = IElementType.getInstance(typeId);
			try {
				final ITypedObjectDAO<ITypedObject> dao = (ITypedObjectDAO<ITypedObject>) elt
						.createExecutableExtension("class"); //$NON-NLS-1$
				daoTypeMap.put(type, dao);
			} catch (CoreException e) {
				throw new ErrorException(MessageFormat.format(CoreMessages
						.getString("service.dao.daoNotFound"), type.getId(), e.getMessage()), e); //$NON-NLS-1$
			}
		}
		// Registering implicit DAO from persistence file contributions
		elts = Designer.getInstance().getExtensions(PERSISTENCE_EXTENSION_ID, "typeId", "*"); //$NON-NLS-1$
		for (IConfigurationElement elt : elts) {
			final String typeId = elt.getAttribute("typeId");
			final IElementType type = IElementType.getInstance(typeId);
			// Only registering if not yet existing
			if (daoTypeMap.get(type) == null) {
				final String persistedClass = elt.getAttribute("persistedClass");
				if (persistedClass != null && !"".equals(persistedClass.trim())) {
					try {
						Class<? extends IdentifiedObject> clazz = (Class<? extends IdentifiedObject>) Class
								.forName(persistedClass);
						ITypedObjectDAO<ITypedObject> dao = new TypedObjectDAO(type, clazz);
						daoTypeMap.put(type, dao);
					} catch (ClassNotFoundException e) {
						throw new ErrorException(
								MessageFormat
										.format(CoreMessages
												.getString("service.dao.persistedClassNotFound"), persistedClass, e.getMessage()), e); //$NON-NLS-1$
					}
				}
			}
		}
	}

}
