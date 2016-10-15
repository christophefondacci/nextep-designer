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
package com.nextep.datadesigner.vcs.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IAdaptable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IPropertyProvider;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.services.DBVendorEvaluator;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionBranch;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.VersionPolicy;
import com.nextep.designer.vcs.policies.DefaultVersionPolicy;

/**
 * This abstract class provides default standard implementation of an object which would be self
 * controlled (the version content of the versionable is the versionable itself). <br>
 * <br>
 * Note that extensions of this class will be named (INamedObject), identified for data persistency
 * (IdentifiableObject), and observable (Observable).<br>
 * There is no need for a <code>IVersionable</code> to derive from this class, this superclass has
 * only been created to avoid code rewriting of the same default behviours. This superclass allows
 * children classes to concentrate on their own business.<br>
 * <br>
 * Note that this class provides a protected method to override the default version policy. Child
 * classes which does not want to acquire <code>DefaultPolicy</code> behaviour should call it to
 * specify a custom version policy.
 * 
 * @author Christophe Fondacci
 */
public abstract class SelfControlVersionable<T> extends NamedObservable implements IVersionable<T>,
		ILockable<T>, IReferenceable, IAdaptable {

	private static final Log log = LogFactory.getLog(SelfControlVersionable.class);
	private IVersionInfo version = null;
	private VersionPolicy policy = DefaultVersionPolicy.getInstance();
	private IVersionContainer parent = null;

	public void setVersionPolicy(VersionPolicy policy) {
		this.policy = policy;
	}

	public VersionPolicy getVersionPolicy() {
		return policy;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#checkIn(com.nextep.designer.vcs.model.IActivity)
	 */
	public IVersionable<T> checkIn(IVersioningOperationContext context) {
		policy.checkIn(this, context);
		return this;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#checkOut(com.nextep.designer.vcs.model.IActivity)
	 */
	public IVersionable<T> checkOut(IVersioningOperationContext context) {
		// Checkouting copied object
		return policy.checkOut(this, context);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#deBranch(com.nextep.designer.vcs.model.IVersionBranch)
	 */
	public void deBranch(IVersionBranch newBranch) {
		policy.debranch(this, newBranch);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#getContainer()
	 */
	public final IVersionContainer getContainer() {
		return parent;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#getId()
	 */
	public final long getId() {
		if (version == null || version.getUID() == null) {
			return 0;
		}
		return version.getUID().rawId();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#getType()
	 */
	public abstract IElementType getType();

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#getVersion()
	 */
	public final IVersionInfo getVersion() {
		return version;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#getVersionnedObject()
	 */
	public final ILockable<T> getVersionnedObject() {
		return this;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#setContainer(com.nextep.designer.vcs.model.IVersionContainer)
	 */
	public final void setContainer(IVersionContainer container) {
		if (this.parent != container) {
			this.parent = container;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#setId(long)
	 */
	public final void setId(long id) {
		// Null implementation since id is managed by version
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionable#setVersion(com.nextep.designer.vcs.model.IVersionInfo)
	 */
	public final void setVersion(IVersionInfo version) {
		this.version = version;
		// If clause for compatibility with loading versionables
		// if(version.getReference()!=null) {
		// CorePlugin.getService(IReferenceManager.class).reference(version.getReference(), this);
		// }
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#getUID()
	 */
	public final UID getUID() {
		return version == null ? null : version.getUID();
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#setUID(com.nextep.datadesigner.model.UID)
	 */
	public void setUID(UID id) {
		// No effect since the ID is managed by the VersionInfo object
		// Because of the one-to-one relationship between version and table
	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#lockUpdates()
	 */
	public final void lockUpdates() {
		if (version.getStatus() != IVersionStatus.CHECKED_IN) {
			log.error("lockUpdates - inconsistent lock with version status");
		}
		notifyListeners(ChangeEvent.UPDATES_LOCKED, this);
	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#unlockUpdates()
	 */
	public final void unlockUpdates() {
		if (version.getStatus() == IVersionStatus.CHECKED_IN) {
			log.error("unlockUpdates - inconsistent lock with version status");
		}
		notifyListeners(ChangeEvent.UPDATES_UNLOCKED, this);
	}

	/**
	 * @see com.nextep.datadesigner.model.ILockable#updatesLocked()
	 */
	public boolean updatesLocked() {
		if (version != null) { // && !version.getReference().isVolatile()) {
			// if(version.getReference().isVolatile()) {
			// return true;
			// }
			return version.getStatus().equals(IVersionStatus.CHECKED_IN)
					|| (!version.getUser().getUID().equals(VersionHelper.getCurrentUser().getUID()));
		} else {
			return true;
		}
	}

	// /**
	// * @see java.lang.Object#equals(java.lang.Object)
	// */
	// @Override
	// public boolean equals(Object obj) {
	// if(obj != null && obj.getClass() == this.getClass()) {
	// IVersionable t = (IVersionable)obj;
	// if(t.getVersion().getUID()!= null) {
	// return t.getVersion().getUID().equals(version.getUID());
	// }
	// return this==t;
	// }
	// return false;
	// }
	// /**
	// * @see java.lang.Object#hashCode()
	// */
	// @Override
	// public int hashCode() {
	// if(this.getUID() == null) {
	// return super.hashCode();
	// }
	// return getType().hashCode()*1000+(int)version.getUID().rawId();
	// }
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj instanceof IVersionable) {
			if (version != null) {
				return version.equals(((IVersionable<?>) obj).getVersion());
			} else {
				if (((IVersionable<?>) obj).getVersion() == null) {
					return this == obj;
				}
				return false;
			}
		}
		return false;

	}

	/**
	 * Hash code of a table is the hash code of its underlying version info. This contract allows
	 * correct identification of distinct table objects on version checkin / checkout. Mostly used
	 * for hibernate persistence consistency.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (version == null) {
			return super.hashCode();
		}
		return version.hashCode();

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + this.getType() + ")" + this.getName() + "(id:" + this.getUID() + ")";
	}

	public T getModel() {
		return (T) this;
	}

	public Map<IReference, IReferenceable> getReferenceMap() {
		return Collections.EMPTY_MAP;
	}

	/**
	 * @see com.nextep.datadesigner.model.IReferenceable#getReference()
	 */
	@Override
	public IReference getReference() {
		return getVersion().getReference();
	}

	/**
	 * @see com.nextep.datadesigner.model.IReferenceable#setReference(com.nextep.designer.vcs.model.VersionReference)
	 */
	@Override
	public void setReference(IReference ref) {
		log.debug("Setting reference of a SelfControlVersionable, updating version ref");
		IVersionInfo v = getVersion();
		// CorePlugin.getService(IReferenceManager.class).reference(ref, this);
		while (v != null) {
			v.setReference(ref);
			v = v.getPreviousVersion();
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IReferencer#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * This default implementation does nothing
	 * 
	 * @see com.nextep.datadesigner.model.IReferencer#updateReferenceDependencies(com.nextep.datadesigner.model.IReference,
	 *      com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		// This default implementation does nothing
		return false;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertyProvider.class) {
			return new ComparisonPropertyProvider(this);
		} else if (adapter == DBVendorEvaluator.class) {
			return DBVendorEvaluator.getInstance();
		}
		return null;
	}

	// public boolean isMergeableTo(IVersionable versionable) {
	// return this.getType().getDefaultClass().isInstance(versionable);
	// }

	@Override
	public boolean isOutOfDate() {
		boolean outOfDate = !VersionHelper.isUpToDate(this);
		if (outOfDate) {
			// Trying to wait and retry
			try {
				Thread.sleep(300);
				outOfDate = !VersionHelper.isUpToDate(this);
				// Final attempt
				if (outOfDate) {
					Thread.sleep(1000);
					outOfDate = !VersionHelper.isUpToDate(this);
				}
			} catch (InterruptedException e) {
				throw new ErrorException(e);
			}

		}
		return outOfDate;
	}

	@Override
	public long getRevision() {
		return getVersion().getUpdateRevision();
	}

	@Override
	public void setRevision(long revision) {
		getVersion().setUpdateRevision(revision);
	}

	@Override
	public void resyncWithRepository() {
		VersionHelper.refresh(this);
	}

	@Override
	public void incrementRevision() {
		// A random increment prevents us from same time update
		setRevision(getRevision() + (long) (Math.random() * 10));
	}
}
