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
package com.nextep.designer.core.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerHint;
import com.nextep.designer.core.model.IResourceLocator;
import com.nextep.designer.core.model.MarkerType;

public class Marker implements IMarker {

	private String message;
	private Map<String, Object> attrs;
	private Object typedObj;
	private MarkerType type;
	private List<IMarkerHint> availableHints;
	private IMarkerHint selectedHint;
	private IResourceLocator icon;

	public Marker() {
		attrs = new HashMap<String, Object>();
		availableHints = new ArrayList<IMarkerHint>();
	}

	@Override
	public Object getAttribute(String attr) {
		return attrs.get(attr);
	}

	@Override
	public MarkerType getMarkerType() {
		return type;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public Object getRelatedObject() {
		if (typedObj instanceof IModelOriented<?>) {
			return ((IModelOriented<?>) typedObj).getModel();
		}
		return typedObj;
	}

	@Override
	public void setAttribute(String attrName, Object value) {
		attrs.put(attrName, value);
	}

	@Override
	public void setRelatedObject(Object o) {
		this.typedObj = o;
	}

	@Override
	public void addAvailableHint(IMarkerHint hint) {
		availableHints.add(hint);
	}

	@Override
	public List<IMarkerHint> getAvailableHints() {
		return availableHints;
	}

	@Override
	public IMarkerHint getSelectedHint() {
		return selectedHint;
	}

	@Override
	public void setSelectedHint(IMarkerHint hint) {
		this.selectedHint = hint;
	}

	@Override
	public IResourceLocator getIcon() {
		return icon;
	}

	@Override
	public void setIcon(IResourceLocator icon) {
		this.icon = icon;
	}

	public void setMarkerType(MarkerType type) {
		this.type = type;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
