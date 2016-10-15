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
package com.nextep.designer.beng.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.beng.model.impl.DeliveryFile;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * This interface represents a delivery module. A delivery module corresponds to the delivery
 * scripts of a container in a given release. This module is different from the delivery itself: you
 * should consider it as a workarea for assembling and building the delivery package.<br>
 * The delivery package will be generated from a delivery module by wrapping a shell script template
 * around delivery module contents.
 * 
 * @author Christophe Fondacci
 */
public interface IDeliveryModule extends IDeliveryItem<IDeliveryModule>, INamedObject, IObservable,
		IdentifiedObject {

	String TYPE_ID = "DELIVERY_MODULE"; //$NON-NLS-1$

	/**
	 * Returns the first module release which can be handled by this delivery. Full deliveries will
	 * return null to indicate no existing release is required.
	 * 
	 * @return the initial release on which this delivery can apply
	 */
	IVersionInfo getFromRelease();

	/**
	 * Sets the first release on which this delivery will be able to be deployed.
	 * 
	 * @param fromRelease release from which the delivery can start to upgrade
	 */
	void setFromRelease(IVersionInfo fromRelease);

	/**
	 * Returns the target release of this delivery. This is the release which will be installed
	 * after the delivery module has been deployed.
	 * 
	 * @return the target release of this delivery
	 */
	IVersionInfo getTargetRelease();

	/**
	 * Sets the target release of this delivery. It defines the release which will be installed
	 * after the delivery module has been deployed.
	 * 
	 * @param targetRelease installed release
	 */
	void setTargetRelease(IVersionInfo targetRelease);

	/**
	 * Adds a new delivery item to this delivery module.
	 * 
	 * @param item delivery item to include to this delivery
	 */
	void addDeliveryItem(IDeliveryItem<?> item);

	/**
	 * Removes a delivery item from this delivery module.
	 * 
	 * @param item delivery item to remove from this delivery
	 */
	void removeDeliveryItem(IDeliveryItem<?> item);

	/**
	 * @return all delivery items of this module
	 */
	List<IDeliveryItem<?>> getDeliveryItems();

	/**
	 * @return the module managed by this delivery
	 */
	IReference getModuleRef();

	/**
	 * Defines the module managed by this delivery
	 * 
	 * @param module
	 */
	void setModuleRef(IReference module);

	/**
	 * @param type type of deliveries to retrieve
	 * @return a list of delivery items of the specified type. Returned items are ordered in the way
	 *         they have been added to this module
	 */
	List<IDeliveryItem<?>> getDeliveries(DeliveryType type);

	/**
	 * Generates the descriptor of this delivery module. The descriptor will usually be a XML
	 * document which will be interpreted by the installer to analyze the module. It should contain
	 * all the information which describe this delivery module such as release information,
	 * contained items and types, sequence of installation, etc. <br>
	 * <br>
	 * Since the descriptor will depend on the installer, it is materialized here as a simple
	 * String. Default implementation will provide a XML string.
	 * 
	 * @return the descriptor of the resulting delivery module
	 */
	String generateDescriptor();

	/**
	 * @return a boolean indicating whether this module is an admin module. An admin module will be
	 *         deployed in the neXtep admin repository instead of being deployed in the target
	 *         database user.
	 */
	boolean isAdmin();

	/**
	 * Changes the admin status of this module.
	 * 
	 * @param admin admin flag for this module
	 * @see IDeliveryModule#isAdmin()
	 */
	void setAdmin(boolean admin);

	/**
	 * Lists all custom dependencies of this delivery module. Required dependency will be added to
	 * this list when generating the delivery.
	 * 
	 * @return a list of all dependency of this delivery module
	 */
	List<IVersionInfo> getDependencies();

	/**
	 * Adds a custom dependency to this module
	 * 
	 * @param module module dependency to add
	 */
	void addDependency(IVersionInfo release);

	/**
	 * Removes a dependency from this delivery.
	 * 
	 * @param module module dependency to remove.
	 */
	void removeDependency(IVersionInfo release);

	/**
	 * @return whether this delivery module is the first release to be delivered from neXtep. The
	 *         first release is a special release which will not consider the "from" release as a
	 *         requirement but will insert it instead.
	 */
	boolean isFirstRelease();

	/**
	 * Defines whether this delivery module is the first release ever deployed from neXtep designer.
	 * Default is false.
	 * 
	 * @param firstRelease
	 */
	void setFirstRelease(boolean firstRelease);

	/**
	 * Adds an artefact to this delivery module.
	 * 
	 * @param artefact artefact to add
	 * @see IArtefact
	 */
	void addArtefact(IArtefact artefact);

	/**
	 * Removes an artefact from this delivery module
	 * 
	 * @param artefact artefact to remove
	 * @see IArtefact
	 */
	void removeArtefact(IArtefact artefact);

	/**
	 * @return the list of all artefacts of this module
	 * @see IArtefact
	 */
	Set<IArtefact> getArtefacts();

	/**
	 * This method sets all artefacts of the given collection as the new artefact list of this
	 * module.
	 * 
	 * @param artefacts new artefacts collection of this module
	 */
	void setArtefacts(Collection<IArtefact> artefacts);

	/**
	 * Adds an external file to this delivery.
	 * 
	 * @see IExternalFile
	 * @param f external file to add
	 * @deprecated do not use... Replaced by {@link DeliveryFile} artefacts which are
	 *             repository-based files
	 */
	void addExternalFile(IExternalFile f);

	/**
	 * Removes this external file from this delivery.
	 * 
	 * @param f external file to remove.
	 * @deprecated do not use... Replaced by {@link DeliveryFile} artefacts which are
	 *             repository-based files
	 */
	void removeExternalFile(IExternalFile f);

	/**
	 * @return the list of external files defined for this delivery
	 * @deprecated do not use... Replaced by {@link DeliveryFile} artefacts which are
	 *             repository-based files
	 */
	List<IExternalFile> getExternalFiles();

	/**
	 * Returns the reference database connection to use when generating this module.
	 * 
	 * @return the reference database connection or <code>null</code> for repository based
	 *         deliveries.
	 */
	IConnection getReferenceConnection();

	/**
	 * Defines the reference database connection to use when generating this module.
	 * 
	 * @param conn new reference database connection
	 */
	void setReferenceConnection(IConnection conn);

	/**
	 * @return whether this module is a universal delivery. A universal delivery is capable of
	 *         upgrading ANY release to the current one.
	 */
	boolean isUniversal();

	void setUniversal(boolean universal);
}
