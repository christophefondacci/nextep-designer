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
package com.nextep.designer.vcs.model;

import com.nextep.datadesigner.model.IReferenceable;

/**
 * @author Christophe Fondacci
 *
 */
public class MergeInfo {

	private IReferenceable proposal;
	private IReferenceable cachedMergeProposal;
	private MergeStatus status = MergeStatus.NOT_MERGED;
	private boolean proposalDefined = false;

	/**
	 * Sets the proposal resulting from merge processing
	 *
	 * @param proposal merge proposal (source or target)
	 */
	public void setMergeProposal(IReferenceable proposal) {
		this.proposal = proposal;
		this.status = MergeStatus.MERGE_RESOLVED;
		if(!proposalDefined) {
			cachedMergeProposal = proposal;
			proposalDefined = true;
		}
	}
	/**
	 * @return the merge proposal or <code>null</code> if there is an ambiguity
	 */
	public IReferenceable getMergeProposal() {
		return proposal;
	}
	public MergeStatus getStatus() {
		return status;
	}
	public MergeStatus addStatus(MergeStatus subStatus) {
		status = status.compute(subStatus);
		return status;
	}
	public void setStatus(MergeStatus status) {
		this.status=status;
	}

	public void restoreMergeProposal() {
		this.proposal = cachedMergeProposal;
	}

}
