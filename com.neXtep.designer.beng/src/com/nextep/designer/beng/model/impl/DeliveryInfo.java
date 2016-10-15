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
package com.nextep.designer.beng.model.impl;

import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.vcs.model.IVersionInfo;

public class DeliveryInfo extends IDNamedObservable implements Comparable<Object>, IDeliveryInfo {

	private IVersionInfo targetRelease;
	private IVersionInfo sourceRelease;
	public void setTargetRelease(IVersionInfo release) {
		this.targetRelease = release;
	}
	@Override
	public IVersionInfo getTargetRelease() {
		return targetRelease;
	}
	public void setSourceRelease(IVersionInfo release) {
		this.sourceRelease = release;
	}
	@Override
	public IVersionInfo getSourceRelease() {
		return sourceRelease;
	}
//	@Override
//	public IReference getReference() {
//		if(targetRelease==null) return null;
//		return targetRelease.getReference();
//	}
	@Override
	public int compareTo(Object o1) {
		if(o1==null) return -1;
		if(o1 instanceof DeliveryInfo) {
			final IDeliveryInfo o = (IDeliveryInfo)o1;
			if(getSourceRelease()==null) {
				if(o.getSourceRelease()!=null) {
					return -1;
				} else {
					return getTargetRelease().compareTo(o.getTargetRelease());
				}
			} else {
				return getSourceRelease().compareTo(o.getSourceRelease());
			}
		} else {
			return super.compareTo(o1);
		}
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof DeliveryInfo) {
			IDeliveryInfo other = (IDeliveryInfo)obj;
			if(this.getSourceRelease()==null) {
				if(other.getSourceRelease()!=null) {
					return false;
				}
			} else {
				if(!this.getSourceRelease().equals(other.getSourceRelease())) {
					return false;
				}
			}
			if(this.getTargetRelease()==null) {
				if(other.getTargetRelease()!=null) {
					return false;
				}
			} else {
				if(!this.getTargetRelease().equals(other.getTargetRelease())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return 1;
	}
}

