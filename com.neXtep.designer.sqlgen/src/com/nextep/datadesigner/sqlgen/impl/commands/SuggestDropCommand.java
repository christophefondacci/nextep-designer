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
package com.nextep.datadesigner.sqlgen.impl.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IAttribute;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.model.IDropStrategy;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.services.IGenerationService;

/**
 * A command which returns a boolean indicating if the merger should suggest a drop according to the
 * drop strategies.<br>
 * This command expects a {@link ITypedObject} instance as a single command argument and returns a
 * boolean. If the object is not a {@link ITypedObject}, it will always return false.
 * 
 * @author Christophe Fondacci
 */
public class SuggestDropCommand implements ICommand {

	private static final Log log = LogFactory.getLog(SuggestDropCommand.class);

	/**
	 * @see com.nextep.datadesigner.model.ICommand#execute(java.lang.Object[])
	 */
	@Override
	public Object execute(Object... parameters) {
		Object obj = parameters[0];
		if (obj instanceof ITypedObject) {
			ITypedObject typedObj = (ITypedObject) obj;
			try {
				if (typedObj.getType() == IElementType.getInstance(IAttribute.TYPE_ID)) {
					return false;
				}
				IDropStrategy strategy = SQLGenPlugin.getService(IGenerationService.class)
						.getDropStrategy(typedObj.getType());
				return strategy.isDropping();
			} catch (ErrorException e) {
				log.debug("Error while retrieving drop strategy for " + obj, e);
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.ICommand#getName()
	 */
	@Override
	public String getName() {
		return "Computing drop suggestion...";
	}

}
