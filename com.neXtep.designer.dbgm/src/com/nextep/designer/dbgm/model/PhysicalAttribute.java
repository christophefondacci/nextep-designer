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
package com.nextep.designer.dbgm.model;

import java.util.Collection;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;

/**
 * This enumerated type describes the physical attributes of a database object.
 * 
 * @author Christophe Fondacci
 */
public enum PhysicalAttribute implements INamedObject, IObservable {

	/*
	 * FIXME [BGA]: The physical attributes should be ventilated by database vendor so that they can
	 * be properly handled in the vendor specific UI layers.
	 */
	PCT_FREE("PCTFREE", "Percentage of blocks left empty for future updates"),
	PCT_USED("PCTUSED", "Percentage of blocks above which the block is considered full"),
	INIT_TRANS("INITRANS", "Number of update transaction entries for which space is initially reserved in the data block header"),
	MAX_TRANS("MAXTRANS", "Number of transaction entries that can concurrently use data in the data block");

	private String name;
	private String desc;

	private PhysicalAttribute(String name, String desc) {
		this.name = name;
		this.desc = desc;
	}

	@Override
	public String getDescription() {
		return desc;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setDescription(String description) {
		this.desc = description;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void addListener(IEventListener listener) {
	}

	@Override
	public Collection<IEventListener> getListeners() {
		return null;
	}

	@Override
	public void notifyListeners(ChangeEvent event, Object o) {
	}

	@Override
	public void removeListener(IEventListener listener) {
	}

}
