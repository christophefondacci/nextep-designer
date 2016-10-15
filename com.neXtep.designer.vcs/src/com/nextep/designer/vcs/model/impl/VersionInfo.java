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

import java.util.Date;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.IVersionBranch;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;

/**
 * {@link IVersionInfo} implementation. Any construction of an {@link IVersionInfo} instance should
 * be performed through the {@link VersionFactory} static factory which will handle all version
 * creations.<br>
 * The public empty constructor should never be called directly.
 * 
 * @author Christophe
 */
public class VersionInfo extends Observable implements IVersionInfo {

	// private static final Log log = LogFactory.getLog(VersionInfo.class);

	private UID id;
	private IReference versionReference;
	private int majorRelease;
	private int minorRelease;
	private int iteration;
	private int patch;
	private int revision;
	private IVersionBranch branch;
	private IVersionStatus status;
	private IVersionInfo previousVersion;
	private IVersionInfo mergeFromVersion;
	private IActivity activity;
	private IRepositoryUser user;
	private Date creationDate;
	private Date updateDate;
	private long updateRevision = 1;
	private boolean dropped = false;
	private Long versionTag;

	/**
	 * Hibernate empty constructor
	 */
	public VersionInfo() {
		this.majorRelease = 1;
		this.minorRelease = 0;
		this.iteration = 0;
		this.patch = 0;
		this.status = IVersionStatus.NOT_VERSIONED;
		this.user = VersionHelper.getCurrentUser();
		versionTag = VersionHelper.computeVersion(this);
	}

	public int getIteration() {
		return iteration;
	}

	public IVersionInfo getPreviousVersion() {
		return previousVersion;
	}

	/**
	 * Hibernate setter
	 * 
	 * @param info
	 */
	public void setPreviousVersion(IVersionInfo info) {
		this.previousVersion = info;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	public String getLabel() {
		return majorRelease + "." + minorRelease + "." + iteration + "." + patch //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ (revision > 0 ? "_" + revision : ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public int getMajorRelease() {
		return majorRelease;
	}

	public int getMinorRelease() {
		return minorRelease;
	}

	public int getPatch() {
		return patch;
	}

	public void setIteration(int iteration) {
		if (this.iteration != iteration) {
			this.iteration = iteration;
			versionTag = VersionHelper.computeVersion(this);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	public void setMajorRelease(int majorRelease) {
		if (this.majorRelease != majorRelease) {
			this.majorRelease = majorRelease;
			versionTag = VersionHelper.computeVersion(this);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	public void setMinorRelease(int minorRelease) {
		if (this.minorRelease != minorRelease) {
			this.minorRelease = minorRelease;
			versionTag = VersionHelper.computeVersion(this);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	public void setPatch(int patch) {
		if (this.patch != patch) {
			this.patch = patch;
			versionTag = VersionHelper.computeVersion(this);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	public IVersionBranch getBranch() {
		return branch;
	}

	public String toString() {
		return getLabel() + " " + getStatus().name(); //$NON-NLS-1$
	}

	public boolean equals(Object o) {
		if (o instanceof IVersionInfo) {
			IVersionInfo v = (IVersionInfo) o;
			return ((v.getStatus() == this.getStatus())
					&& (v.getMajorRelease() == this.getMajorRelease())
					&& (v.getMinorRelease() == this.getMinorRelease())
					&& (v.getIteration() == this.getIteration())
					&& (v.getPatch() == this.getPatch())
					&& (v.getBranch() != null && this.getBranch() != null
							&& v.getBranch().getName() != null && v.getBranch().getName()
							.equals(this.getBranch().getName()))
					&& (v.getRevision() == this.getRevision()) && equalsRefId(this.getReference(),
					v.getReference()));
		} else {
			return false;
		}
	}

	private boolean equalsRefId(IReference ref1, IReference ref2) {
		if (ref1.getReference().getReferenceId() != null
				&& ref2.getReference().getReferenceId() != null) {
			return ref1.getReference().getReferenceId()
					.equals(ref2.getReference().getReferenceId());
		} else {
			return ref1.getReference().equals(ref2.getReference());
		}
	}

	@Override
	public int hashCode() {
		// return ((getReference()==null || getReference().getUID()==null ? "null" :
		// getReference().getUID().toString()) + getLabel()).hashCode();
		// return 1;
		if (getReference() == null || getReference().getUID() == null) {
			return 1;
		} else {
			return getReference().getUID().toString().hashCode();
		}
	}

	@Override
	public IVersionStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(IVersionStatus status) {
		this.status = status;
		setUpdateDate(new Date());
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setBranch(IVersionBranch newBranch) {
		this.branch = newBranch;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	public UID getUID() {
		return id;
	}

	/**
	 * Defines a new unique identifier of this object
	 * 
	 * @param id new identifier for the object
	 */
	protected void setId(long id) {
		// log.debug("Called setId with id="+id);
		this.id = new UID(id);
	}

	protected long getId() {
		if (id == null) {
			return 0;
		}
		return id.rawId();
	}

	public IReference getReference() {
		return versionReference;
	}

	/**
	 * We do not inform the ReferenceManager of VersionInfo objects
	 * 
	 * @see com.nextep.datadesigner.model.IReferenceable#setReference(com.nextep.datadesigner.model.IReference)
	 */
	public void setReference(IReference ref) {
		this.versionReference = ref;
	}

	public void setUID(UID id) {
		this.id = id;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionInfo#getActivity()
	 */
	public IActivity getActivity() {
		return this.activity;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionInfo#setActivity(com.nextep.designer.vcs.model.IActivity)
	 */
	public void setActivity(IActivity activity) {
		this.activity = activity;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IActivity#getUser()
	 */
	@Override
	public IRepositoryUser getUser() {
		return user;
	}

	protected void setUser(IRepositoryUser user) {
		this.user = user;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionInfo#getCreationDate()
	 */
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionInfo#getUpdateDate()
	 */
	@Override
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionInfo#setCreationDate(java.util.Date)
	 */
	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionInfo#setUpdateDate(java.util.Date)
	 */
	@Override
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;

	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionInfo#getMergedFromVersion()
	 */
	@Override
	public IVersionInfo getMergedFromVersion() {
		return mergeFromVersion;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionInfo#setMergedFromVersion(com.nextep.designer.vcs.model.IVersionInfo)
	 */
	@Override
	public void setMergedFromVersion(IVersionInfo mergedFromVersion) {
		this.mergeFromVersion = mergedFromVersion;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionInfo#getRevision()
	 */
	@Override
	public int getRevision() {
		return this.revision;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IVersionInfo#setRevision(int)
	 */
	@Override
	public void setRevision(int revision) {
		if (this.revision != revision) {
			this.revision = revision;
			versionTag = VersionHelper.computeVersion(this);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IVersionInfo version) {
		if (version == null)
			return 1;
		if (!version.getBranch().equals(getBranch())) {
			// Is the external version one of our predecessor ?
			if (VersionHelper.isPredecessor(this, version)) {
				return 1;
			} else if (VersionHelper.isPredecessor(version, this)) {
				// Is the current version a predecessor of the external version ?
				return -1;
			}
		}
		// In any other case (same branch, or different branch but no predecessor found), we
		// compare the version number
		return new Long(VersionHelper.computeVersion(this) - VersionHelper.computeVersion(version))
				.intValue();
	}

	@Override
	public long getUpdateRevision() {
		return updateRevision;
	}

	@Override
	public void setUpdateRevision(long revision) {
		this.updateRevision = revision;
	}

	@Override
	public boolean isDropped() {
		return dropped;
	}

	@Override
	public void setDropped(boolean dropped) {
		if (this.dropped != dropped) {
			this.dropped = dropped;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setRelease(long fullVersion, boolean notify) {
		this.revision = (int) (fullVersion / REVISION % PRECISION);
		this.patch = (int) (fullVersion / PATCH % PRECISION);
		this.iteration = (int) ((fullVersion / ITERATION) % PRECISION);
		this.minorRelease = (int) ((fullVersion / MINOR) % PRECISION);
		this.majorRelease = (int) (fullVersion / MAJOR % PRECISION);
		versionTag = VersionHelper.computeVersion(this);
		if (notify) {
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public Long getVersionTag() {
		return versionTag;
	}

	@Override
	public void setVersionTag(Long tag) {
		if ((versionTag == null && tag != null) || versionTag != tag) {
			this.versionTag = tag;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}
}
