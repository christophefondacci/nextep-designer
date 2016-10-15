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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.OutOfDateObjectException;
import com.nextep.datadesigner.impl.Property;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IProperty;
import com.nextep.datadesigner.model.IPropertyProvider;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IImportPolicy;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.impl.Activity;
import com.nextep.designer.vcs.policies.ContainerVersionPolicy;

/**
 * A VersionContainer object is a versionable container. It can be both
 * versioned and contains other versioned objects.
 * 
 * @author Christophe Fondacci
 */
public class VersionContainer extends SelfControlVersionable<IVersionContainer> implements
		IVersionable<IVersionContainer>, ILockable<IVersionContainer>, IVersionContainer {

	private Set<IVersionable<?>> contents = null;
	private String shortName = null;
	private DBVendor vendor;
	private String schemaName;

	public VersionContainer(String name, String description, IActivity activity) {
		nameHelper.setFormatter(IFormatter.PROPPER_LOWER);
		this.setVersionPolicy(ContainerVersionPolicy.getInstance());
		this.setName(name);
		this.setDescription(description);
		contents = new HashSet<IVersionable<?>>();
		setVersion(VersionFactory.getUnversionedInfo(
				new Reference(IElementType.getInstance(IVersionContainer.TYPE_ID), name, this),
				activity));
	}

	/**
	 * Hibernate empty constructor
	 */
	public VersionContainer() {
		nameHelper.setFormatter(IFormatter.PROPPER_LOWER);
		contents = new HashSet<IVersionable<?>>();
		this.setVersionPolicy(ContainerVersionPolicy.getInstance());
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionContainer#addVersionable(com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public boolean addVersionable(IVersionable<?> newVersionable, IImportPolicy policy) {
		if (isOutOfDate()) {
			resyncWithRepository();
			throw new OutOfDateObjectException(this, "Module was out of date");
		}
		final boolean isAdded = policy.importVersionable(newVersionable, this,
				Activity.getDefaultActivity());
		if (!isAdded) {
			throw new ErrorException(VCSMessages.getString("view.addVersionableFailed")); //$NON-NLS-1$
		}
		return true;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionContainer#removeVersionable(com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public boolean removeVersionable(IVersionable<?> versionableToRemove) {
		if (isOutOfDate()) {
			resyncWithRepository();
			throw new OutOfDateObjectException(this, "Module was out of date");
		}
		contents.remove(versionableToRemove);
		try {
			versionableToRemove.setContainer(null);
		} catch (ErrorException e) {
			// Trying to save manually
			CorePlugin.getIdentifiableDao().save(versionableToRemove);
		}
		notifyListeners(ChangeEvent.VERSIONABLE_REMOVED, versionableToRemove);
		return true;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionContainer#getContents()
	 */
	@Override
	public Set<IVersionable<?>> getContents() {
		return contents;
	}

	/**
	 * @param contents
	 */
	public void setContents(Set<IVersionable<?>> contents) {
		this.contents = contents;
		// Initializing parent container for each contained
		// versionable
		if (contents != null) {
			try {
				for (IVersionable<?> v : contents) {
					v.setContainer(this);
				}
			} catch (NullPointerException e) {
				// Workaround of Hibernate bug which may not provide
				// a working set when empty
			}
		}
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance("CONTAINER");
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionContainer#getShortName()
	 */
	@Override
	public String getShortName() {
		return shortName;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionContainer#setShortName(java.lang.String)
	 */
	@Override
	public void setShortName(String shortName) {
		this.shortName = IFormatter.UPPERCASE.format(shortName);
		notifyListeners(ChangeEvent.MODEL_CHANGED, this.shortName);
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getReferenceMap()
	 */
	@Override
	public Map<IReference, IReferenceable> getReferenceMap() {
		Map<IReference, IReferenceable> refMap = new HashMap<IReference, IReferenceable>();
		for (IVersionable<?> v : new ArrayList<IVersionable<?>>(getContents())) {
			refMap.put(v.getReference(), v);
			refMap.putAll(v.getReferenceMap());
		}
		return refMap;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		List<IReference> refs = new ArrayList<IReference>();
		for (IVersionable<?> v : getContents()) {
			refs.addAll(v.getReferenceDependencies());
		}
		return refs;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#updateReferenceDependencies(com.nextep.datadesigner.model.IReference,
	 *      com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		for (IVersionable<?> v : getContents()) {
			if (v.updateReferenceDependencies(oldRef, newRef)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extended to allow concurrent modifications by different users.
	 * 
	 * @see com.nextep.datadesigner.model.ILockable#updatesLocked()
	 */
	@Override
	public boolean updatesLocked() {
		if (getVersion() != null) { // && !version.getReference().isVolatile())
									// {
			return getVersion().getStatus() == IVersionStatus.CHECKED_IN;
		} else {
			return true;
		}
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertyProvider.class) {
			return new IPropertyProvider() {

				@Override
				public List<IProperty> getProperties() {
					List<IProperty> props = new ArrayList<IProperty>();
					props.add(new Property(ContainerMerger.ATTR_NAME, getName()));
					props.add(new Property(ContainerMerger.ATTR_SHORTNAME, getShortName()));
					props.add(new Property(ContainerMerger.ATTR_DESC, getDescription()));
					return props;
				}

				@Override
				public void setProperty(IProperty property) {
				}
			};
		}
		// TODO Auto-generated method stub
		return super.getAdapter(adapter);
	}

	@Override
	public void resyncWithRepository() {
		Designer.getInstance().invokeSelection("prompt.reloadView");
	}

	@Override
	public DBVendor getDBVendor() {
		return vendor;
	}

	@Override
	public void setDBVendor(DBVendor vendor) {
		if (vendor != this.vendor) {
			this.vendor = vendor;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setSchemaName(String schemaName) {
		if (schemaName != this.schemaName) {
			this.schemaName = schemaName;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public String getSchemaName() {
		return schemaName;
	}
}
