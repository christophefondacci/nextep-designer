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
/**
 *
 */
package com.nextep.designer.beng.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.map.MultiValueMap;
import com.neXtep.shared.model.ArtefactType;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.model.IArtefact;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.model.IExternalFile;
import com.nextep.designer.beng.services.BENGServices;
import com.nextep.designer.beng.services.CreateDeliveryDescriptorCommand;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * A deploy unit is a specific container which corresponds to a unit of deployement. This container
 * has got predefined sub containers which should not be extended. Each of these subcontainers will
 * be mapped to a specific package source folder when deployed.
 * 
 * @author Christophe Fondacci
 */
public class DeliveryModule extends IDNamedObservable implements IDeliveryModule {

	// private static final Log log = LogFactory.getLog(DeliveryModule.class);

	private IVersionInfo fromRelease;
	private IVersionInfo targetRelease;
	private IReference moduleRef;
	private List<IDeliveryItem<?>> items;
	private MultiValueMap itemsTypeMap;
	private boolean isAdmin = false;
	private List<IVersionInfo> hDependencies;
	private boolean firstRelease = false;
	private Set<IArtefact> artefacts;
	private List<IExternalFile> externalFiles;
	private IConnection refDBConnection;
	private boolean isUniversal;
	private DBVendor vendor;

	public DeliveryModule(IVersionContainer module, IVersionInfo fromRelease,
			IVersionInfo targetRelease) {
		this();
		setFromRelease(fromRelease);
		setTargetRelease(targetRelease);
		setModuleRef(VersionHelper.getVersionable(module).getReference());
		// setName(module.getName().toLowerCase()+"_"+targetRelease.getLabel());
	}

	public DeliveryModule() {
		items = new ArrayList<IDeliveryItem<?>>();
		itemsTypeMap = new MultiValueMap();
		artefacts = new HashSet<IArtefact>();
		externalFiles = new ArrayList<IExternalFile>();
		nameHelper.setFormatter(IFormatter.LOWERCASE);
		hDependencies = new ArrayList<IVersionInfo>();
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#addDeliveryItem(com.nextep.designer.beng.model.IDeliveryItem)
	 */
	@Override
	public void addDeliveryItem(IDeliveryItem<?> item) {
		if (items.add(item)) {
			itemsTypeMap.put(item.getDeliveryType(), item);
			item.setParentModule(this);
			// Collections.sort(items);
			notifyListeners(ChangeEvent.ITEM_ADDED, item);
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#getDeliveryItems()
	 */
	@Override
	public List<IDeliveryItem<?>> getDeliveryItems() {
		return items;
	}

	/**
	 * Hibernate setter for items list
	 * 
	 * @param items
	 */
	protected void setDeliveryItems(List<IDeliveryItem<?>> items) {
		this.items = items;
		itemsTypeMap.clear();
		for (IDeliveryItem<?> i : items) {
			itemsTypeMap.put(i.getDeliveryType(), i);
			i.setParentModule(this);
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#getFromRelease()
	 */
	@Override
	public IVersionInfo getFromRelease() {
		return fromRelease;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#getTargetRelease()
	 */
	@Override
	public IVersionInfo getTargetRelease() {
		return targetRelease;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#removeDeliveryItem(com.nextep.designer.beng.model.IDeliveryItem)
	 */
	@Override
	public void removeDeliveryItem(IDeliveryItem<?> item) {
		if (items.remove(item)) {
			// item.setParentModule(null);
			itemsTypeMap.remove(item.getDeliveryType(), item);
			item.setParentModule(null);
			// item.setParentModule(null);
			notifyListeners(ChangeEvent.ITEM_REMOVED, item);
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#setFromRelease(com.nextep.designer.vcs.model.IVersionInfo)
	 */
	@Override
	public void setFromRelease(IVersionInfo fromRelease) {
		if (this.fromRelease != fromRelease) {
			this.fromRelease = fromRelease;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#setTargetRelease(com.nextep.designer.vcs.model.IVersionInfo)
	 */
	@Override
	public void setTargetRelease(IVersionInfo targetRelease) {
		if (this.targetRelease != targetRelease) {
			this.targetRelease = targetRelease;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#generateArtefact(java.lang.String)
	 */
	@Override
	public void generateArtefact(String directoryTarget) {
		// Delegating
		BENGServices.generateArtefact(this, directoryTarget);
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#getContent()
	 */
	@Override
	public IDeliveryModule getContent() {
		return this;
	}

	/**
	 * @see com.nextep.datadesigner.impl.NamedObservable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		if (o instanceof IDeliveryModule) {
			IDeliveryModule otherModule = (IDeliveryModule) o;
			if (getFromRelease() == null) {
				return -1;
			} else {
				return getFromRelease().compareTo(otherModule.getFromRelease());
			}
		}
		return -1;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#getDeliveryType()
	 */
	@Override
	public DeliveryType getDeliveryType() {
		return DeliveryType.CUSTOM;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#getModuleRef()
	 */
	@Override
	public IReference getModuleRef() {
		return moduleRef;
	}

	public IVersionContainer getModule() {
		return (IVersionContainer) VersionHelper.getReferencedItem(moduleRef);
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#setModuleRef(com.nextep.designer.vcs.model.IVersionContainer)
	 */
	@Override
	public void setModuleRef(IReference moduleRef) {
		this.moduleRef = moduleRef;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#setDeliveryType(com.nextep.designer.beng.model.DeliveryType)
	 */
	@Override
	public void setDeliveryType(DeliveryType type) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#getDeliveries(com.nextep.designer.beng.model.DeliveryType)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<IDeliveryItem<?>> getDeliveries(DeliveryType type) {
		Object o = itemsTypeMap.get(type);
		if (o != null) {
			return (List<IDeliveryItem<?>>) o;
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#generateDescriptor()
	 */
	@Override
	public String generateDescriptor() {
		return (String) new CreateDeliveryDescriptorCommand(this).execute();
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#getArtefactName()
	 */
	@Override
	public String getArtefactName() {
		return getName();
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryItem#getArtefactType()
	 */
	@Override
	public ArtefactType getArtefactType() {
		return ArtefactType.DELIVERY;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#isAdmin()
	 */
	@Override
	public boolean isAdmin() {
		return isAdmin;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#setAdmin(boolean)
	 */
	@Override
	public void setAdmin(boolean admin) {
		if (this.isAdmin != admin) {
			this.isAdmin = admin;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#addDependency(com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public void addDependency(IVersionInfo release) {
		hDependencies.add(release);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#getDependencies()
	 */
	@Override
	public List<IVersionInfo> getDependencies() {
		return hDependencies;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#removeDependency(com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public void removeDependency(IVersionInfo release) {
		hDependencies.remove(release);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * Hibernate setter for the dependencies list.
	 * 
	 * @param dependencies
	 */
	protected void setHibernateDependencies(List<IVersionInfo> dependencies) {
		this.hDependencies = dependencies;
	}

	protected List<IVersionInfo> getHibernateDependencies() {
		return hDependencies;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#isFirstRelease()
	 */
	@Override
	public boolean isFirstRelease() {
		return firstRelease;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#setFirstRelease(boolean)
	 */
	@Override
	public void setFirstRelease(boolean firstRelease) {
		this.firstRelease = firstRelease;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#addArtefact(com.nextep.designer.beng.model.IArtefact)
	 */
	@Override
	public void addArtefact(IArtefact artefact) {
		if (artefacts.add(artefact)) {
			artefact.setDelivery(this);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#getArtefacts()
	 */
	@Override
	public Set<IArtefact> getArtefacts() {
		return artefacts;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#removeArtefact(com.nextep.designer.beng.model.IArtefact)
	 */
	@Override
	public void removeArtefact(IArtefact artefact) {
		if (artefacts.remove(artefact)) {
			CorePlugin.getIdentifiableDao().delete(artefact);
			artefact.setDelivery(null);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	protected void setArtefacts(Set<IArtefact> artefacts) {
		this.artefacts = artefacts;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#setArtefacts(java.util.Collection)
	 */
	@Override
	public void setArtefacts(Collection<IArtefact> artefacts) {
		for (IArtefact a : new ArrayList<IArtefact>(this.artefacts)) {
			if (a.getType() == com.nextep.designer.beng.model.ArtefactMode.AUTO) {
				this.artefacts.remove(a);
				CorePlugin.getIdentifiableDao().delete(a);
			}
		}
		this.artefacts.addAll(artefacts);
		for (IArtefact a : this.artefacts) {
			a.setDelivery(this);
		}
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);

	}

	@Override
	public void addExternalFile(IExternalFile f) {
		if (externalFiles.add(f)) {
			f.setPosition(externalFiles.size() - 1);
			f.setDelivery(this);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#getExternalFiles()
	 */
	@Override
	public List<IExternalFile> getExternalFiles() {
		return externalFiles;
	}

	protected void setExternalFiles(List<IExternalFile> files) {
		this.externalFiles = files;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#removeExternalFile(com.nextep.designer.beng.model.IExternalFile)
	 */
	@Override
	public void removeExternalFile(IExternalFile f) {
		int index = externalFiles.indexOf(f);
		if (externalFiles.remove(f)) {
			CorePlugin.getIdentifiableDao().delete(f);
			for (IExternalFile e : externalFiles) {
				if (e.getPosition() > index) {
					e.setPosition(e.getPosition() - 1);
				}
			}
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#getReferenceConnection()
	 */
	@Override
	public IConnection getReferenceConnection() {
		return refDBConnection;
	}

	/**
	 * @see com.nextep.designer.beng.model.IDeliveryModule#setReferenceConnection(com.nextep.designer.core.model.IConnection)
	 */
	@Override
	public void setReferenceConnection(IConnection conn) {
		if (conn != this.refDBConnection) {
			this.refDBConnection = conn;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public boolean isUniversal() {
		return isUniversal;
	}

	@Override
	public void setUniversal(boolean universal) {
		this.isUniversal = universal;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public IDeliveryModule getParentModule() {
		return null;
	}

	@Override
	public void setParentModule(IDeliveryModule module) {

	}

	@Override
	public DBVendor getDBVendor() {
		return vendor;
	}

	@Override
	public void setDBVendor(DBVendor vendor) {
		this.vendor = vendor;
	}
}
