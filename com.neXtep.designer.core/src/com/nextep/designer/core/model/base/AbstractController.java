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
package com.nextep.designer.core.model.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.ICheckedObject;
import com.nextep.designer.core.model.ITypedObjectController;

public abstract class AbstractController implements ITypedObjectController {

	private final static Log log = LogFactory.getLog(AbstractController.class);
	private IElementType type;

	public Object load(String className, UID id) {
		try {
			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(className);
			return CorePlugin.getIdentifiableDao().load(clazz, id);
		} catch (ClassNotFoundException e) {
			throw new ErrorException(e);
		}
	}

	@Override
	public final void setType(IElementType type) {
		this.type = type;
	}

	@Override
	public final IElementType getType() {
		return type;
	}

	@Override
	public void save(IdentifiedObject content) {
		// We do not save inconsistent checked object
		if (content instanceof ICheckedObject) {
			ICheckedObject co = (ICheckedObject) content;
			try {
				co.checkConsistency();
			} catch (InconsistentObjectException e) {
				log.debug("Inconsistent object, skipped save.");
				return;
			}
		}
		CorePlugin.getPersistenceAccessor().save((ITypedObject) content);
	}
}
