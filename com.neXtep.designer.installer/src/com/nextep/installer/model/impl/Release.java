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

import com.nextep.installer.model.IRelease;

/**
 * @author Christophe Fondacci
 */
public class Release implements IRelease, Comparable<IRelease> {

	private int major, minor, iteration, patch, revision;
	private long id;

	public Release() {
	}

	public Release(int major, int minor, int iteration, int patch, int revision) {
		setMajor(major);
		setMinor(minor);
		setIteration(iteration);
		setPatch(patch);
		setRevision(revision);
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#getIteration()
	 */
	public int getIteration() {
		return iteration;
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#getMajor()
	 */
	public int getMajor() {
		return major;
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#getMinor()
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#getPatch()
	 */
	public int getPatch() {
		return patch;
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#getRevision()
	 */
	public int getRevision() {
		return revision;
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#setIteration(int)
	 */
	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#setMajor(int)
	 */
	public void setMajor(int major) {
		this.major = major;
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#setMinor(int)
	 */
	public void setMinor(int minor) {
		this.minor = minor;
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#setPatch(int)
	 */
	public void setPatch(int patch) {
		this.patch = patch;
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#setRevision(int)
	 */
	public void setRevision(int revision) {
		this.revision = revision;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "" + major + "." + minor + "." + iteration + "." + patch
				+ (revision > 0 ? "_" + revision : "");
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(IRelease o) {
		if (o instanceof IRelease) {
			long currRel = getReleaseNumber(this);
			long compRel = getReleaseNumber((IRelease) o);
			return (int) (currRel - compRel);
		} else if (o == null) {
			return 1;
		}

		return 1;
	}

	private static long getReleaseNumber(IRelease r) {
		return r.getRevision() + r.getPatch() * 100 + r.getIteration() * 10000 + r.getMinor()
				* 1000000 + r.getMajor() * 100000000;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IRelease) {
			IRelease rel = (IRelease) obj;
			return major == rel.getMajor() && minor == rel.getMinor()
					&& iteration == rel.getIteration() && patch == rel.getPatch()
					&& revision == rel.getRevision();
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return toString().hashCode();
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#getId()
	 */
	public long getId() {
		return id;
	}

	/**
	 * @see com.neXtep.installer.model.IRelease#setId(long)
	 */
	public void setId(long id) {
		this.id = id;
	}
}
