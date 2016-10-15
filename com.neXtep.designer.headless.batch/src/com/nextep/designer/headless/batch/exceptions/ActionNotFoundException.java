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
package com.nextep.designer.headless.batch.exceptions;

import java.text.MessageFormat;
import com.nextep.designer.headless.batch.BatchMessages;
import com.nextep.designer.headless.exceptions.BatchException;

/**
 * @author Christophe Fondacci
 */
public class ActionNotFoundException extends BatchException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5523628860669253948L;

	public ActionNotFoundException(String actionId) {
		super(MessageFormat.format(BatchMessages.getString("exception.actionNotFound"), //$NON-NLS-1$
				actionId));
	}
}
