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
package com.nextep.designer.vcs.ui.jface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.vcs.impl.VersionBranch;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;

public class VersionInfoContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		IVersionInfo version = null;
		if (inputElement instanceof IVersionable) {
			version = ((IVersionable<?>) inputElement).getVersion();
		} else if (inputElement instanceof IVersionInfo) {
			version = (IVersionInfo) inputElement;
		}
		// Reversing version list so last version appear last
		Set<IVersionInfo> releaseList = new HashSet<IVersionInfo>();
		fillVersionList(releaseList, version);
		releaseList.add(VersionFactory.buildVersionInfo(-1, -1, -1, -1, VersionBranch
				.getDefaultBranch(), IVersionStatus.NOT_VERSIONED, version.getReference(), null));

		// Creating list
		List<IVersionInfo> sortedList = new ArrayList<IVersionInfo>(releaseList);
		Collections.sort(sortedList, new Comparator<IVersionInfo>() {

			@Override
			public int compare(IVersionInfo o1, IVersionInfo o2) {
				if (o1.getStatus() == IVersionStatus.NOT_VERSIONED) {
					return -1;
				} else if (o2.getStatus() == IVersionStatus.NOT_VERSIONED) {
					return 1;
				} else {
					return (int) (o2.getCreationDate().getTime() - o1.getCreationDate().getTime());
				}
			}
		});
		return sortedList.toArray();
	}

	private void fillVersionList(Collection<IVersionInfo> list, IVersionInfo version) {
		if (version != null) {
			list.add(version);
			fillVersionList(list, version.getPreviousVersion());
			fillVersionList(list, version.getMergedFromVersion());
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
