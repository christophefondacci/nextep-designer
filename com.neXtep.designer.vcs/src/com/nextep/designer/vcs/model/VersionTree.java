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
package com.nextep.designer.vcs.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a version tree hierarchy. It should be used when
 * manipulating global version trees (for display or merge operations for
 * example) <br>
 * <br>
 * Be careful: this class is a tree AND a tree item
 * 
 * @author Christophe Fondacci
 * 
 */
public class VersionTree {

	private IVersionInfo versionInfo = null;
	private VersionTree parent = null;
	private List<VersionTree> children = null;
	private final Log log = LogFactory.getLog(VersionTree.class);

	public VersionTree(IVersionInfo versionInfo, VersionTree parent) {
		this.versionInfo = versionInfo;
		this.parent = parent;
		this.children = new ArrayList<VersionTree>();
		if (parent != null) {
			parent.addItem(this);
		}
	}

	/**
	 * @return version information of this tree node
	 */
	public IVersionInfo getVersionInfo() {
		return versionInfo;
	}

	/**
	 * As said earlier, tree items are a VersionTree instance.
	 * 
	 * @return the tree items connected to this tree
	 */
	public List<VersionTree> getItems() {
		return children;
	}

	/**
	 * Adds a new item connected to this tree. When an item is connected to this
	 * tree, his parent will automatically be set to this tree.
	 * 
	 * @param item
	 *            item to connect to this tree
	 */
	public void addItem(VersionTree item) {
		if (item != null) {
			if (versionInfo.equals(item.getVersionInfo())) {
				children.add(item);
				item.setParent(this);
			} else {
				log.debug("VersionTree: Trying to add incompatible item to the tree"); //$NON-NLS-1$
			}
		} else {
			log.debug("VersionTree: Trying to add a null object"); //$NON-NLS-1$
		}
	}

	/**
	 * @return the parent item / tree, could be null for a root item
	 */
	public VersionTree getParent() {
		return parent;
	}

	/**
	 * Protected method that defines the parent item of a tree
	 * 
	 * @param parent
	 *            parent item to connect to
	 */
	protected void setParent(VersionTree parent) {
		this.parent = parent;
	}
}
