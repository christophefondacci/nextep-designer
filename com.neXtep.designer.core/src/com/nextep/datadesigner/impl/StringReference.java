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

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ReferenceContext;
import com.nextep.datadesigner.model.UID;

public class StringReference implements IReference {

	private String strRef;
	private StringUID uid;
	private class StringUID extends UID {
		private static final long serialVersionUID = -8292297529981376605L;

		public StringUID(String uid) {
			super(uid.hashCode());
		}
	}
	public StringReference(String ref) {
		this.strRef=ref;
		this.uid=new StringUID(ref);
	}
	@Override
	public String getArbitraryName() {
		return strRef;
	}

	@Override
	public IReferenceable getInstance() {
		return null;
	}

	@Override
	public UID getReferenceId() {
		return uid;
	}

	@Override
	public boolean isVolatile() {
		return true;
	}

	@Override
	public void setArbitraryName(String arbitraryName) {}

	@Override
	public void setInstance(IReferenceable referenceable) {}

	@Override
	public void setVolatile(boolean isVolatile) {}

	@Override
	public IElementType getType() {
		return null;
	}

	@Override
	public IReference getReference() {
		return this;
	}

	@Override
	public void setReference(IReference ref) {}

	@Override
	public UID getUID() {
		return getReferenceId();
	}

	@Override
	public void setUID(UID id) {}
	@Override
	public ReferenceContext getReferenceContext() {
		return null;
	}
	@Override
	public void setReferenceContext(ReferenceContext context) {}

}
