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
package com.nextep.designer.vcs.gef;

import java.util.List;
import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.vcs.impl.VersionedDiagram;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * @author Christophe Fondacci
 *
 */
public class VersionTreeDiagram extends VersionedDiagram {

	private MultiValueMap sourceConnMap;
	private MultiValueMap targetConnMap;
	private IVersionInfo currentVersion;
	public VersionTreeDiagram() {
		super();
		setName("VersionTree");
		sourceConnMap = new MultiValueMap();
		targetConnMap = new MultiValueMap();
	}
	public void addVersionSuccessor(IVersionInfo source, IVersionInfo target) {
		VersionConnection conn = new VersionConnection(source,target);
		sourceConnMap.put(source,conn);
		targetConnMap.put(target,conn);
	}
	@SuppressWarnings("unchecked")
	public List<VersionConnection> getVersionSuccessors(IVersionInfo source) {
		return (List<VersionConnection>)sourceConnMap.get(source);
	}
	@SuppressWarnings("unchecked")
	public List<VersionConnection> getVersionPredecessors(IVersionInfo target) {
		return (List<VersionConnection>)targetConnMap.get(target);
	}
	public void setCurrent(IVersionInfo i) {
		this.currentVersion=i;
	}
	public boolean isCurrent(IVersionInfo i) {
		return currentVersion.equals(i);
	}
}

