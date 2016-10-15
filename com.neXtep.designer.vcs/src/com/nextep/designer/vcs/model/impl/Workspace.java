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
package com.nextep.designer.vcs.model.impl;

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
import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.IImportPolicy;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;

public class Workspace extends NamedObservable implements IWorkspace {

	// private static final Log log = LogFactory.getLog(VersionView.class);

	private UID id = null;
	private Set<IVersionable<?>> contents = null;
	private String shortName = null;
	private DBVendor viewDBVendor = DBVendor.getDefaultVendor();
	private long revision = 1;
	private boolean isImportOnOpenNeeded;
	private String schemaName;

	public Workspace(String name, String description) {
		setName(name);
		setDescription(description);
		contents = new HashSet<IVersionable<?>>();
		this.isImportOnOpenNeeded = false;
	}

	/**
	 * Hibernate empty constructor
	 */
	protected Workspace() {
		contents = new HashSet<IVersionable<?>>();
	}

	@Override
	public boolean addVersionable(IVersionable<?> versionable, IImportPolicy policy) {
		if (isOutOfDate()) {
			resyncWithRepository();
			throw new OutOfDateObjectException(this, VCSMessages.getString("view.outOfDate")); //$NON-NLS-1$
		}
		boolean isAdded = policy
				.importVersionable(versionable, this, Activity.getDefaultActivity());
		if (!isAdded) {
			throw new ErrorException(VCSMessages.getString("view.addVersionableFailed")); //$NON-NLS-1$
		}
		return true;
	}

	@Override
	public Set<IVersionable<?>> getContents() {
		return contents;
	}

	protected void setContents(Set<IVersionable<?>> contents) {
		this.contents = contents;
		for (IVersionable<?> v : contents) {
			v.setContainer(this);
		}
	}

	@Override
	public UID getUID() {
		return id;
	}

	@Override
	public long getId() {
		if (id == null) {
			return 0;
		}
		return id.rawId();
	}

	@Override
	public void setId(long id) {
		this.id = new UID(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IWorkspace && obj != null) {
			IWorkspace v = (IWorkspace) obj;
			return this.getUID() == v.getUID() || this.getUID().equals(v.getUID());
		}
		return false;
	}

	@Override
	public boolean removeVersionable(IVersionable<?> versionableToRemove) {
		if (isOutOfDate()) {
			resyncWithRepository();
			throw new OutOfDateObjectException(this, VCSMessages.getString("view.outOfDate")); //$NON-NLS-1$
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

	@Override
	public void setUID(UID id) {
		this.id = id;

	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#getModel()
	 */
	@Override
	public IVersionContainer getModel() {
		return this;
	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#lockUpdates()
	 */
	@Override
	public void lockUpdates() {
	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#unlockUpdates()
	 */
	@Override
	public void unlockUpdates() {
	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#updatesLocked()
	 */
	@Override
	public boolean updatesLocked() {
		return false;
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
		if (this.shortName != shortName) {
			this.shortName = shortName;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
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
	 * @see com.nextep.designer.vcs.model.IWorkspace#getDBVendor()
	 */
	@Override
	public DBVendor getDBVendor() {
		return viewDBVendor;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IWorkspace#setDBVendor(com.nextep.designer.core.model.DBVendor)
	 */
	@Override
	public void setDBVendor(DBVendor dbVendor) {
		if (this.viewDBVendor != dbVendor) {
			this.viewDBVendor = dbVendor;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
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

	@Override
	public long getRevision() {
		return revision;
	}

	@Override
	public void incrementRevision() {
		// A random increment prevents us from same time update between 2 clients
		setRevision(getRevision() + (long) (Math.random() * 10));

	}

	@Override
	public boolean isOutOfDate() {
		return !VersionHelper.isContainerUpToDate(this);
	}

	@Override
	public void resyncWithRepository() {
		Designer.getInstance().invokeSelection("prompt.reloadView"); //$NON-NLS-1$
	}

	@Override
	public void setRevision(long revision) {
		this.revision = revision;
	}

	@Override
	public boolean isImportOnOpenNeeded() {
		return isImportOnOpenNeeded;
	}

	@Override
	public void setImportOnOpenNeeded(boolean needsImportOnOpen) {
		this.isImportOnOpenNeeded = needsImportOnOpen;
	}

	@Override
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	@Override
	public String getSchemaName() {
		return schemaName;
	}
}
