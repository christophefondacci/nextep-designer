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

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IImportPolicy;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 *
 */
public class ContainerMerger extends MergerWithChildCollections {
	private static final Log log = LogFactory.getLog(ContainerMerger.class);
	public static final String ATTR_SHORTNAME = "Short name";
	public static final String CATEGORY_CONTENTS = "Contents";
	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable, com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IVersionContainer srcContainer = (IVersionContainer)source;
		IVersionContainer tgtContainer = (IVersionContainer)target;

		IComparisonItem result = new ComparisonResult(source,target,getMergeStrategy().getComparisonScope());
		compareName(result, srcContainer, tgtContainer);
		result.addSubItem(new ComparisonAttribute(ATTR_SHORTNAME,srcContainer == null ? null : srcContainer.getShortName(), tgtContainer == null ? null : tgtContainer.getShortName()));
		listCompare(CATEGORY_CONTENTS, result,
				srcContainer == null ? Collections.EMPTY_LIST : srcContainer.getContents(),
				tgtContainer == null ? Collections.EMPTY_LIST : tgtContainer.getContents()
			);
		return result;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object, com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IVersionContainer c = (IVersionContainer)target;
		log.debug("Starting container <" + c.getName() +"> fill");
		fillName(result, c);
		if(c.getName() == null || "".equals(c.getName())) {
			return null;
		}
		c.setShortName(getStringProposal(ATTR_SHORTNAME, result));
		save(c);
		// Building merged contents
		List<?> contents = getMergedList(CATEGORY_CONTENTS, result, activity);
//		// Removing all contents first
//		log.debug("Removing container contents...");
//		for(IVersionable<?> v : c.getContents()) {
//			VersionHelper.removeVersionable(v);
//		}
		// Adding versionable to our container.
		// Since the container should be empty, we define an "add only" policy
		log.debug("Adding merged contents");
		IImportPolicy policy = new ImportPolicyAddOnly();
		for(Object item : contents) {
			IVersionable<?> v = (IVersionable<?>)item;
			if(v != null) {
//				c.addVersionable(v, policy);
				save(v);
				c.getContents().add(v);
				v.setContainer(c);
			}
		}
		save(c);
		log.info("Merged container <" + c.getName() +">: Success!");
		return c;
	}
//	/**
//	 * @see com.nextep.designer.vcs.model.IMerger#merge(com.nextep.designer.vcs.model.IReferenceable, com.nextep.designer.vcs.model.IReferenceable)
//	 */
//	@Override
//	public void merge(IComparisonItem item) {
//		// TODO Auto-generated method stub
//
//	}

}
