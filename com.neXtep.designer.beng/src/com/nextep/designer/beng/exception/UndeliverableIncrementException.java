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
package com.nextep.designer.beng.exception;

import java.util.List;
import com.nextep.designer.beng.model.IDeliveryIncrement;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.vcs.model.IVersionInfo;

public class UndeliverableIncrementException extends Exception {

	private boolean isUniversalException = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = 3830495064197254923L;
	private IDeliveryIncrement increment;
	private IVersionInfo lastResolvedRelease;
	private List<IDeliveryInfo> resolvedIncrements;

	public UndeliverableIncrementException(IDeliveryIncrement increment,
			List<IDeliveryInfo> resolvedIncrements, IVersionInfo lastRelease) {
		this.increment = increment;
		this.resolvedIncrements = resolvedIncrements;
		this.lastResolvedRelease = lastRelease;
	}

	@Override
	public String getMessage() {
		final StringBuilder buf = new StringBuilder();
		buf.append("Cannot find a delivery combination to upgrade ");
		buf.append(increment.getModule() != null ? increment.getModule().getName() : "Unknown");
		buf.append(" from ");
		buf.append(increment.getFromRelease() == null ? "[Scratch]" : increment.getFromRelease()
				.getLabel());
		buf.append(" to ");
		buf.append(increment.getToRelease().getLabel());
		buf.append(" :\n");
		buf.append("Resolved increments are :\n");
		for (IDeliveryInfo i : resolvedIncrements) {
			buf.append(" - " + i.getSourceRelease() + " -> " + i.getTargetRelease() + "\n");
		}
		buf.append("=> Missing delivery which could upgrade to " + lastResolvedRelease);
		return buf.toString();
	}

	public IDeliveryIncrement getDeliveryIncrement() {
		return increment;
	}

	public boolean isUniversal() {
		return isUniversalException;
	}

	public void setUniversal(boolean universalException) {
		this.isUniversalException = universalException;
	}
}
