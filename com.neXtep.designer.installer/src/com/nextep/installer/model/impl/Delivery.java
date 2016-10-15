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
package com.nextep.installer.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.ICheck;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IRelease;
import com.nextep.installer.model.IRequiredDelivery;

/**
 * @author Christophe Fondacci
 *
 */
public class Delivery implements IDelivery {

	private IRelease fromRelease;
	private IRelease release;
	private List<IArtefact> artefacts;
	private String name;
	private long refUID;
	private boolean checkRange = false;
	private List<IDelivery> dependencies;
	private List<ICheck> checks;
	private boolean isAdmin = false;
	private boolean isFirstRelease = false;
	/** Delivery vendor, default is Oracle */
	private DBVendor vendor = DBVendor.ORACLE;
	private List<IRequiredDelivery> requiredDeliveries;
	public Delivery(boolean isAdmin) {
		artefacts =  new ArrayList<IArtefact>();
		dependencies = new ArrayList<IDelivery>();
		checks = new ArrayList<ICheck>();
		this.isAdmin = isAdmin;
		requiredDeliveries = new ArrayList<IRequiredDelivery>();
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#getFromRelease()
	 */
	public IRelease getFromRelease() {
		return fromRelease;
	}

	/**
	 * @see com.neXtep.installer.model.IDelivery#getRelease()
	 */
	public IRelease getRelease() {
		return release;
	}

	/**
	 * @see com.neXtep.installer.model.IDelivery#setFromRelease(com.neXtep.installer.model.IRelease)
	 */
	public void setFromRelease(IRelease fromRel) {
		this.fromRelease = fromRel;
	}

	/**
	 * @see com.neXtep.installer.model.IDelivery#setRelease(com.neXtep.installer.model.IRelease)
	 */
	public void setRelease(IRelease toRel) {
		this.release = toRel;
	}

	/**
	 * @see com.neXtep.installer.model.IDelivery#addArtefact(com.neXtep.installer.model.IArtefact)
	 */
	public void addArtefact(IArtefact artefact) {
		artefacts.add(artefact);
		artefact.setDelivery(this);
	}

	/**
	 * @see com.neXtep.installer.model.IDelivery#getArtefacts()
	 */
	public List<IArtefact> getArtefacts() {
		return artefacts;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#getName()
	 */
	public String getName() {
		return name;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#getRefUID()
	 */
	public long getRefUID() {
		return refUID;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#setRefUID(long)
	 */
	public void setRefUID(long refUID) {
		this.refUID = refUID;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#checkRange()
	 */
	public boolean checkRange() {
		return checkRange;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#setRangeCheck(boolean)
	 */
	public void setRangeCheck(boolean rangeCheck) {
		checkRange = rangeCheck;
		
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#addDependency(com.neXtep.installer.model.IDelivery)
	 */
	public void addDependency(IDelivery dependentDelivery) {
		dependencies.add(dependentDelivery);
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#getDependencies()
	 */
	public List<IDelivery> getDependencies() {
		return dependencies;
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if(obj instanceof IDelivery) {
			IDelivery dlv = (IDelivery)obj;
			return getRefUID() == dlv.getRefUID() && getRelease().equals(dlv.getRelease());
		}
		return false;
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (getName() + getRelease().toString()).hashCode();
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#addCheck(com.neXtep.installer.model.ICheck)
	 */
	public void addCheck(ICheck check) {
		checks.add(check);
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#getChecks()
	 */
	public Collection<ICheck> getChecks() {
		return checks;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#isAdmin()
	 */
	public boolean isAdmin() {
		return isAdmin;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#getDBVendor()
	 */
	public DBVendor getDBVendor() {
		return vendor;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#setDBVendor(com.neXtep.installer.model.DBVendor)
	 */
	public void setDBVendor(DBVendor vendor) {
		this.vendor = vendor;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#isFirstRelease()
	 */
	public boolean isFirstRelease() {
		return isFirstRelease;
	}
	/**
	 * @see com.neXtep.installer.model.IDelivery#setFirstRelease(boolean)
	 */
	public void setFirstRelease(boolean firstRelease) {
		this.isFirstRelease = firstRelease;
	}
	public void addRequiredDelivery(IRequiredDelivery reqDelivery) {
		requiredDeliveries.add(reqDelivery);
	}
	public List<IRequiredDelivery> getRequiredDeliveries() {
		return requiredDeliveries;
	}
	public void removeRequiredDelivery(IRequiredDelivery reqDelivery) {
		requiredDeliveries.remove(reqDelivery);
	}

}
