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

import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.core.model.impl.ReferenceManager;

/**
 * The default IReferenceable implementation. Implementation of {@link IReferenceable} interface
 * should delegate reference management to this class to ensure {@link ReferenceManager} will
 * register all references correctly.
 * 
 * @author Christophe Fondacci
 */
public class Referenceable implements IReferenceable {

	private IReference ref;
	private IReferenceable referenceable;

	public Referenceable(Object referenceable) {
		if (referenceable instanceof IReferenceable) {
			this.referenceable = (IReferenceable) referenceable;
		}
	}

	@Override
	public IReference getReference() {
		return ref;
	}

	@Override
	public void setReference(IReference ref) {
		this.ref = ref;
		// if(referenceable != null) {
		// CorePlugin.getService(IReferenceManager.class).reference(ref, referenceable);
		// }
	}

}
