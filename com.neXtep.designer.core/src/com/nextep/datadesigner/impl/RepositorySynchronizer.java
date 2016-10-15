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
package com.nextep.datadesigner.impl;

import org.hibernate.Session;
import com.nextep.datadesigner.exception.OutOfDateObjectException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ISharedRepositoryObject;
import com.nextep.datadesigner.model.IdentifiedObject;

public class RepositorySynchronizer {

	public static void synchronize(IdentifiedObject o, Session session)
			throws OutOfDateObjectException {
		if (o instanceof ISharedRepositoryObject) {
			final ISharedRepositoryObject sharedObject = (ISharedRepositoryObject) o;
			if (sharedObject.isOutOfDate()
					&& session != HibernateUtil.getInstance().getSandBoxSession()) {
				sharedObject.resyncWithRepository();
				if (sharedObject instanceof IObservable) {
					((IObservable) sharedObject).notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				}
				throw new OutOfDateObjectException(o, "Object out of date  with repository");
			}
		}
	}

	public static void upgrade(IdentifiedObject o) {
		if (o instanceof ISharedRepositoryObject) {
			((ISharedRepositoryObject) o).incrementRevision();
		}
	}
}
