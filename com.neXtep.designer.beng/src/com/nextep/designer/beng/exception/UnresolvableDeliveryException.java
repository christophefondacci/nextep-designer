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
package com.nextep.designer.beng.exception;

import java.util.List;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;

/**
 * @author Christophe Fondacci
 */
public class UnresolvableDeliveryException extends ErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6333759697976095507L;
	private List<ContainerInfo> conflictingDeliveries;

	public UnresolvableDeliveryException(List<ContainerInfo> conflictingDeliveries) {
		super("Unresolved delivery modules");
		this.conflictingDeliveries = conflictingDeliveries;
	}

	public String getMessage() {
		if (conflictingDeliveries != null && !conflictingDeliveries.isEmpty()) {
			final StringBuilder buf = new StringBuilder();
			buf.append("Too many matching modules found, please specify 'module.ref' on the command line. Found modules :\n");
			for (ContainerInfo m : conflictingDeliveries) {
				buf.append("  - [ref=" + m.getReference().getUID() + "] " + m.getName()
						+ " - Last release is " + m.getRelease().getLabel() + " from "
						+ m.getRelease().getUser().getName() + " on "
						+ m.getRelease().getCreationDate() + "\n");
			}
			return buf.toString();
		} else {
			return "No matching modules found.";
		}
	}
}
