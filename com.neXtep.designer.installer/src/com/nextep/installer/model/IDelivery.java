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
package com.nextep.installer.model;

import java.util.Collection;
import java.util.List;

/**
 * @author Christophe Fondacci
 *
 */
public interface IDelivery {

	/**
	 * Defines the release from which this delivery applies.
	 * 
	 * @param fromRel
	 */
	public void setFromRelease(IRelease fromRel);
	/**
	 * @return the release from which this delivery applies
	 */
	public IRelease getFromRelease();
	public void setRangeCheck(boolean rangeCheck);
	public boolean checkRange();
	/**
	 * Defines the release which is deployed by this delivery
	 * @param toRel
	 */
	public void setRelease(IRelease toRel);
	/**
	 * @return the release deployed by this delivery
	 */
	public IRelease getRelease();
	/**
	 * Adds an artefact to this delivery.
	 * @param artefact
	 */
	public void addArtefact(IArtefact artefact);
	/**
	 * @return the list of all artefacts of this delivery
	 * in the order they have been added.
	 */
	public List<IArtefact> getArtefacts();
	/**
	 * Defines the name of this delivery
	 * @param name
	 */
	public void setName(String name);
	/**
	 * @return the name of this delivery
	 */
	public String getName();
	/**
	 * @return the unique reference ID of the module updated
	 * by this delivery 
	 */
	public long getRefUID();
	/**
	 * Defines the unique reference ID of the module updated
	 * by this delivery.
	 * @param refUID
	 */
	public void setRefUID(long refUID);
	/**
	 * @return a list of deliveries on which this delivery depend
	 */
	public List<IDelivery> getDependencies();
	/**
	 * Adds a delivery dependency to this delivery
	 * @param dependentDelivery
	 */
	public void addDependency(IDelivery dependentDelivery);
	public Collection<ICheck> getChecks();
	public void addCheck(ICheck check);
	public boolean isAdmin();
	/**
	 * Defines the database vendor of this delivery.
	 * Depending on the vendor, installer will call different
	 * binaries to publish module contents.
	 * 
	 * @param vendor database vendor for which this delivery has
	 *        been designed.
	 */
	public void setDBVendor(DBVendor vendor);
	/**
	 * @return the database vendor for which this delivery
	 * 		   has been designed.
	 */
	public DBVendor getDBVendor();
	/**
	 * Is this delivery the first one to be deployed for this
	 * module? If yes, the initial release of this delivery
	 * will be defined assuming it is the one in place.
	 * @return boolean indicating if this is the first release
	 */
	public boolean isFirstRelease();
	/**
	 * Defines if this delivery is the first release to be deployed
	 * from nextep installer.
	 * @param firstRelease
	 */
	public void setFirstRelease(boolean firstRelease);
	/**
	 * Adds a required delivery to this delivery. 
	 * 
	 * @param reqDelivery required delivery to add
	 * @see IRequiredDelivery
	 */
	public void addRequiredDelivery(IRequiredDelivery reqDelivery);
	/**
	 * @return the list of required deliveries
	 */
	public List<IRequiredDelivery> getRequiredDeliveries();
	/**
	 * Removes a required delivery from this delivery
	 * @param reqDelivery required delivery to remove
	 */
	public void removeRequiredDelivery(IRequiredDelivery reqDelivery);
}
