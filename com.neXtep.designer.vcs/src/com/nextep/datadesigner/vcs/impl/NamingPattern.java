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
package com.nextep.datadesigner.vcs.impl;

import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.vcs.model.INamingPattern;
import com.nextep.designer.vcs.model.IVersionContainer;

public class NamingPattern extends IDNamedObservable implements INamingPattern {

	private String pattern;
	private IVersionContainer container;
	private IElementType type;
	
	@Override
	public String getPattern() {
		return pattern;
	}

	@Override
	public IVersionContainer getRelatedContainer() {
		return container;
	}

	@Override
	public IElementType getRelatedType() {
		return type;
	}

	@Override
	public void setPattern(String pattern) {
		if(pattern!=this.pattern) {
			this.pattern = pattern;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setRelatedContainer(IVersionContainer container) {
		if(container!=this.container) {
			this.container = container;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setRelatedType(IElementType type) {
		if(type!=this.type) {
			this.type = type;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	protected String getHibernateType() {
		if(type==null) return null;
		return type.getId();
	}
	protected void setHibernateType(String type) {
		if(type!=null) {
			this.type = IElementType.getInstance(type);
		}
	}
}
