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
package com.nextep.datadesigner.gui.impl.rcp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.model.ITypePersister;
import com.nextep.datadesigner.model.ITypedObject;

/**
 * A factory for persisters of IEditorInput.
 *
 * @author Christophe Fondacci
 *
 */
public class PersisterFactory {

	/** Our logger */
	private static final Log log = LogFactory.getLog(PersisterFactory.class);

	/**
	 * Retrieves the element type persister.
	 *
	 * @param typedObject the typed object to persist
	 * @return a valid {@link ITypePersister} persister object which can
	 * 		   persist the given typed object, or <code>null</code> if
	 * 		   no persister has been found.
	 */
	public static ITypePersister getPersister(ITypedObject typedObject) {
		if(typedObject==null) return null;
		IConfigurationElement conf = Designer.getInstance().getExtension("com.neXtep.designer.ui.typePersister", "typeId", typedObject.getType().getId());
		try {
			ITypePersister persister = (ITypePersister)conf.createExecutableExtension("class"); //controllers.get(type);
			persister.setModel(typedObject);
			log.debug("PersisterFactory: Loaded type persister for <" + typedObject.getType().getId() + ">");
			return persister;
		} catch(Exception e) {
			log.debug("No persister has been found for element type <" + typedObject.getType().getId() + ">");
			return null;
		}
	}
}
