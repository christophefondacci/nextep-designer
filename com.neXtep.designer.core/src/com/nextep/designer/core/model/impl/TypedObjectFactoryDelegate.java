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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.util.Assert;

public class TypedObjectFactoryDelegate implements ITypedObjectFactory {

	private static final String EXTENSION_ID = "com.neXtep.designer.core.factories"; //$NON-NLS-1$
	private Map<String, Collection<FactoryWithInterface>> typeFactoryMap;
	private static final Log LOGGER = LogFactory.getLog(TypedObjectFactoryDelegate.class);

	private class FactoryWithInterface extends MultiKey {

		/** Serialization UID */
		private static final long serialVersionUID = -8844147595016276024L;

		public FactoryWithInterface(ITypedObjectFactory factory,
				Class<? extends ITypedObject> providedInterface) {
			super(factory, providedInterface);
		}

		public ITypedObjectFactory getFactory() {
			return (ITypedObjectFactory) getKeys()[0];
		}

		@SuppressWarnings("unchecked")
		public Class<? extends ITypedObject> getInterface() {
			return (Class<? extends ITypedObject>) getKeys()[1];
		}
	}

	public TypedObjectFactoryDelegate() {
		typeFactoryMap = new HashMap<String, Collection<FactoryWithInterface>>();
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(EXTENSION_ID,
				"class", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		for (IConfigurationElement elt : elts) {
			try {
				// Building factory
				final ITypedObjectFactory factory = (ITypedObjectFactory) elt
						.createExecutableExtension("class"); //$NON-NLS-1$
				// Retrieving registered type
				final String typeId = elt.getAttribute("typeId"); //$NON-NLS-1$
				final String context = elt.getAttribute("context"); //$NON-NLS-1$
				final IElementType type = IElementType.getInstance(typeId);
				final FactoryWithInterface fwi = new FactoryWithInterface(factory, type
						.getInterface());
				// Registering
				Collection<FactoryWithInterface> factories = typeFactoryMap.get(context);
				if (factories == null) {
					factories = new LinkedList<FactoryWithInterface>();
					typeFactoryMap.put(context, factories);
				}
				factories.add(fwi);
			} catch (CoreException e) {
				LOGGER.error(CoreMessages.getString("factory.loadExtensionError"), e); //$NON-NLS-1$
			}
		}
	}

	@Override
	public <T extends ITypedObject> T create(Class<T> classToCreate) {
		return getFactory(classToCreate).create(classToCreate);
	}

	private ITypedObjectFactory getFactory(Class<? extends ITypedObject> clazz) {
		Assert.notNull(clazz, CoreMessages.getString("factory.nullTypeError")); ////$NON-NLS-1$
		final List<String> contexts = Arrays.asList(Designer.getInstance().getContext(), "", null); //$NON-NLS-1$
		for (String context : contexts) {
			ITypedObjectFactory factory = getFactory(context, clazz);
			if (factory != null) {
				return factory;
			}
		}
		throw new ErrorException(MessageFormat.format(CoreMessages
				.getString("factory.noFactoryFound"), clazz.getName())); //$NON-NLS-1$
	}

	private ITypedObjectFactory getFactory(String context,
			Class<? extends ITypedObject> classToCreate) {
		final Collection<FactoryWithInterface> factories = typeFactoryMap.get(context);
		if (factories == null) {
			return null;
		}
		for (FactoryWithInterface fwi : factories) {
			if (fwi.getInterface().isAssignableFrom(classToCreate)) {
				return fwi.getFactory();
			}
		}
		return null;
	}
}
