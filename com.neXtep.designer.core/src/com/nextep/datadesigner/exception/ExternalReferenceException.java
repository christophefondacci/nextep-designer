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
package com.nextep.datadesigner.exception;

import java.text.MessageFormat;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.core.CoreMessages;

/**
 * This exception is thrown whenever an external reference is found while trying
 * to retrieve an object from its reference through the reference manager.
 * 
 * @author Christophe
 *
 */
public class ExternalReferenceException extends ErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2865243858561885080L;

	public ExternalReferenceException(IReference r) {
        // TODO [BGA]: Change the message supplied to the exception constructor to give the name
        // of the referenced object if available.
		super(MessageFormat.format(CoreMessages.getString("externalRefFound"),r.getReferenceId()));
	}
}
