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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.impl.StringAttribute;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.MergeInfo;

/**
 * This class provides convenience methods to process textual attributes which
 * may have multiple lines.<br>
 * The idea is to override the merging process to integrate line-by-line merge
 * facilities and to rebuild the multiline attribute from this merge
 * information.<br>
 * Line by line items are only used for precise merging of multiline attributes.
 * 
 * @author Christophe
 */
public abstract class MergerWithMultilineAttributes<T> extends Merger<T> {

	private static final Log log = LogFactory.getLog(MergerWithMultilineAttributes.class);

	/**
	 * Adds customly merged sub items corresponding to the line by line
	 * representation of the given multiline attribute.
	 * 
	 * @param result
	 *            comparison result to populate
	 * @param multilineAttributeName
	 *            name of the attribute containing multiline data
	 * @param mergedSubItems
	 *            merged comparison items representing a line by line merge of
	 *            this attribute content
	 */
	protected void addMergedSubItems(IComparisonItem result, String multilineAttributeName,
			List<IComparisonItem> mergedSubItems) {
		for (IComparisonItem subItem : mergedSubItems) {
			final IComparisonItem multiLineAttr = result.getAttribute(multilineAttributeName);
			if (multiLineAttr != null) {
				multiLineAttr.addSubItem(subItem);
			}
		}
	}

	/**
	 * Removes all sub items of the specified attribute
	 * 
	 * @param result
	 *            result to clean
	 * @param multilineAttributeName
	 *            attribute name where multiline sub items are populated
	 */
	protected void removeMergedSubItems(IComparisonItem result, String multilineAttributeName) {
		final IComparisonItem attribute = result.getAttribute(multilineAttributeName);
		// Securing this section as part of the DES-710 bugfix
		if (attribute != null) {
			final List<IComparisonItem> subItems = attribute.getSubItems();
			if (subItems != null) {
				subItems.clear();
			}
		}
	}

	/**
	 * Finds the last common ancestor version of the 2 given versionable. This
	 * method will go through the version predecessors of the 2 elements and
	 * will return the first object which matches.
	 * 
	 * @param source
	 *            source versionable element
	 * @param target
	 *            target versionable element
	 * @return the last common ancestor of the 2 elements
	 */
	@SuppressWarnings("unchecked")
	protected IReferenceable findCommonAncestor(IVersionable<?> source, IVersionable<?> target) {
		// Checking nullity
		if (source == null || target == null) {
			return null;
		}
		// Building target ancestors list
		List<IVersionInfo> targetAncestors = new ArrayList<IVersionInfo>();
		IVersionInfo version = target.getVersion();
		while (version != null) {
			targetAncestors.add(version);
			version = version.getPreviousVersion();
		}
		// Browsing source ancestors until a matching target ancestor is found
		version = source.getVersion();
		while (version != null) {
			// Have we got a match?
			if (targetAncestors.contains(version)) {
				// Then we load this ancestor
				IVersionInfo commonAncestorVersion = version;
				// We load the ancestor in a sandbox otherwise it could have the
				// same reference
				// scope which would generate TooManyReferencesException
				IVersionable<IReferenceable> commonAncestor = (IVersionable<IReferenceable>) CorePlugin
						.getIdentifiableDao().load(IVersionable.class,
								commonAncestorVersion.getUID(),
								HibernateUtil.getInstance().getSandBoxSession(), true);
				return commonAncestor.getVersionnedObject().getModel();
			}
			version = version.getPreviousVersion();
		}
		throw new ErrorException("Unable to find any common ancestor of "
				+ target.getType().getName().toLowerCase() + " <" + target.getName()
				+ ">. Merge process failed!");
	}

	@Override
	public MergeInfo merge(IComparisonItem result, IComparisonItem sourceRootDiff,
			IComparisonItem targetRootDiff) {
		// Only extending for repository merges
		if (getMergeStrategy().getComparisonScope() != ComparisonScope.REPOSITORY) {
			return super.merge(result, sourceRootDiff, targetRootDiff);
		}
		// Initializing merge information
		MergeInfo mergeInfo = super.merge(result, sourceRootDiff, targetRootDiff); // result.getMergeInfo();

		// Merging
		switch (result.getDifferenceType()) {
		case EQUALS:
			// Done by super class
			break;
		default:
			if (sourceRootDiff == null
					|| sourceRootDiff.getDifferenceType() == DifferenceType.EQUALS) {
				// Done by super class
				break;
			} else {
				if (targetRootDiff == null
						|| targetRootDiff.getDifferenceType() == DifferenceType.EQUALS) {
					// Done by super class
					break;
				} else {
					final IReferenceable ancestor = findCommonAncestor(
							(IVersionable<?>) result.getSource(),
							(IVersionable<?>) result.getTarget());
					final IReferenceable source = result.getSource();
					final IReferenceable target = result.getTarget();
					// First, we clean any previous line by line information
					cleanLineByLineSubItems(result);
					try {
						addLineByLineSubItems(result, ancestor, source, target);
					} catch (IOException e) {
						log.error(e);
					}
				}
			}
		}
		return mergeInfo;
	}

	/**
	 * Implement this method to add line by line sub items for the multiline
	 * attributes. Use the
	 * {@link MergeUtils#mergeCompare(String, String, String)} to transform your
	 * multi-line text to a list of comparison items and them integrate them by
	 * calling the
	 * {@link MergerWithMultilineAttributes#addMergedSubItems(IComparisonItem, String, List)}
	 * method.
	 * 
	 * @param result
	 *            comparison reuslt to populate
	 * @param ancestor
	 *            last common ancestor object
	 * @param source
	 *            source object
	 * @param target
	 *            target object
	 * @throws IOException
	 */
	public abstract void addLineByLineSubItems(IComparisonItem result, IReferenceable ancestor,
			IReferenceable source, IReferenceable target) throws IOException;

	/**
	 * Cleans the comparison item from its line by line attributes. The
	 * implementation should simply call
	 * {@link MergerWithMultilineAttributes#removeMergedSubItems(IComparisonItem, String)}
	 * method on every multiline attribute.
	 * 
	 * @param result
	 *            comparison item to clean
	 */
	protected abstract void cleanLineByLineSubItems(IComparisonItem result);

	/**
	 * Builds the attribute value by retrieving the line by line merged sub
	 * items.
	 * 
	 * @param result
	 *            comparison item containing the merge information
	 * @param attribute
	 *            multi-line attribute name
	 * @return the fully merged attribute value
	 */
	protected String mergeAttribute(IComparisonItem result, String attribute) {// ,
																				// String
		// ancestorVal,
		// String name) {
		final StringBuffer contents = new StringBuffer();
		boolean first = true;
		for (IComparisonItem item : result.getAttribute(attribute).getSubItems()) {
			final StringAttribute a = (StringAttribute) item.getMergeInfo().getMergeProposal();
			if (a != null && !"".equals(a.getValue())) {
				if (first) {
					first = false;
				} else {
					contents.append('\n');
				}
				contents.append(a.getValue());
			}
		}
		return contents.toString();
	}
}
