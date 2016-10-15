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
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.model.IPersistenceAccessor;
import com.nextep.designer.util.Assert;

/**
 * This {@link IPersistenceAccessor} acts as a delegate for all others. It uses the
 * <code>com.neXtep.designer.core.persistenceAccessor</code> extension point to load and register
 * any defined persistence accessor during its initialization.<br>
 * All methods implementing {@link IPersistenceAccessor} will then route to the appropriate
 * {@link IPersistenceAccessor} defined through the extensions and may raise {@link ErrorException}
 * when an appropriate persistance accessor cannot be found.
 * 
 * @author Christophe Fondacci
 */
public class PersistenceAccessorDelegate implements IPersistenceAccessor<ITypedObject> {

	private static final String EXTENSION_ID = "com.neXtep.designer.core.persistenceAccessors"; //$NON-NLS-1$
	private Map<IElementType, IPersistenceAccessor<ITypedObject>> typeAccessorMap;
	private static final Log LOGGER = LogFactory.getLog(PersistenceAccessorDelegate.class);
	private static IPersistenceAccessor<ITypedObject> instance = null;

	@SuppressWarnings("unchecked")
	private PersistenceAccessorDelegate() {
		typeAccessorMap = new HashMap<IElementType, IPersistenceAccessor<ITypedObject>>();
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(EXTENSION_ID,
				"class", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		for (IConfigurationElement elt : elts) {
			try {
				// Building accessor
				final IPersistenceAccessor<ITypedObject> accessor = (IPersistenceAccessor<ITypedObject>) elt
						.createExecutableExtension("class"); //$NON-NLS-1$
				// Retrieving registered type
				final String typeId = elt.getAttribute("typeId"); //$NON-NLS-1$
				final IElementType type = IElementType.getInstance(typeId);
				// Registering
				typeAccessorMap.put(type, accessor);
			} catch (CoreException e) {
				LOGGER.error(CoreMessages.getString("persistance.loadExtensionError"), e); //$NON-NLS-1$
			}
		}
	}

	public static IPersistenceAccessor<ITypedObject> getInstance() {
		if (instance == null) {
			instance = new PersistenceAccessorDelegate();
		}
		return instance;
	}

	@Override
	public void delete(ITypedObject element) {
		Assert.notNull(element, CoreMessages.getString("persistance.cannotDeleteNull")); //$NON-NLS-1$
		getPersistanceAccessor(element.getType()).delete(element);
	}

	/**
	 * Provides a safe, non-null persistance accessor which can handle the specified type. All
	 * checks are made and runtime exceptions may be raised when accessor cannot be found.
	 * 
	 * @param type the type to retrieve persistance accessor for
	 * @return a valid corresponding {@link IPersistenceAccessor}
	 */
	private IPersistenceAccessor<ITypedObject> getPersistanceAccessor(IElementType type) {
		Assert.notNull(type, CoreMessages.getString("persistance.invalidType")); //$NON-NLS-1$
		IPersistenceAccessor<ITypedObject> accessor = typeAccessorMap.get(type);
		// Assert.notNull(accessor, MessageFormat.format(CoreMessages
		//				.getString("persistance.noAccessorFound"), type.getName())); //$NON-NLS-1$
		return accessor;
	}

	@Override
	public boolean isHandledForLoad(IElementType typeToLoad, ITypedObject... parents) {
		final IPersistenceAccessor<ITypedObject> accessor = getPersistanceAccessor(typeToLoad);
		return accessor.isHandledForLoad(typeToLoad, parents);
	}

	@Override
	public Collection<ITypedObject> load(IElementType typeToLoad, ITypedObject... parents) {
		final IPersistenceAccessor<ITypedObject> accessor = getPersistanceAccessor(typeToLoad);
		if (accessor == null) {
			return Collections.emptyList();
		} else {
			return accessor.load(typeToLoad, parents);
		}
	}

	@Override
	public Collection<ITypedObject> loadAll(IElementType typeToLoad) {
		final IPersistenceAccessor<ITypedObject> accessor = getPersistanceAccessor(typeToLoad);
		return accessor.loadAll(typeToLoad);
	}

	@Override
	public void save(ITypedObject element) {
		Assert.notNull(element, CoreMessages.getString("persistance.cannotSaveNull")); //$NON-NLS-1$
		IPersistenceAccessor<ITypedObject> accessor = getPersistanceAccessor(element.getType());
		// Compatibility bridge for repository persistence by default
		if (accessor == null) {
			accessor = RepositoryPersistenceAccessor.getInstance();
		}
		accessor.save(element);
	}

}
