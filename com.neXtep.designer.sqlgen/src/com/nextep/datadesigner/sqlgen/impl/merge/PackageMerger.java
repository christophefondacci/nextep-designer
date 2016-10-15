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
package com.nextep.datadesigner.sqlgen.impl.merge;

import java.io.IOException;
import java.util.List;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.vcs.impl.ComparisonAttributeIgnoreFirstLine;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerWithMultilineAttributes;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 *
 */
public class PackageMerger extends MergerWithMultilineAttributes {

	public static final String ATTR_SPEC = "Spec";
	public static final String ATTR_BODY = "Body";
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object, com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		IPackage pkg = (IPackage)target;
		
		// Filling name properties
		fillName(result, pkg);
		// Filling source code
		if(result.getSource()!=null && result.getTarget()!=null && result.getDifferenceType()!=DifferenceType.EQUALS ) {
			pkg.setSpecSourceCode(mergeAttribute(result, ATTR_SPEC)); // , ancestor==null ? "" : ancestor.getSpecSourceCode(),pkg.getName()));
			pkg.setBodySourceCode(mergeAttribute(result, ATTR_BODY)); //, ancestor == null ? "" : ancestor.getBodySourceCode(),pkg.getName()));
		} else {
			pkg.setSpecSourceCode(getStringProposal(ATTR_SPEC, result));
			pkg.setBodySourceCode(getStringProposal(ATTR_BODY, result));
		}
		
		// Returning filled package
		return pkg;
	}


	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable, com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IPackage src = (IPackage)source;
		IPackage tgt = (IPackage)target;
		
		IComparisonItem result = new ComparisonResult(source,target,getMergeStrategy().getComparisonScope());
		compareName(result, src, tgt);
		
		// Triming each line
		String sourceSpec = null; 
		String sourceBody = null;
		String targetSpec = null;
		String targetBody = null;
		if(getMergeStrategy().getComparisonScope()!=ComparisonScope.REPOSITORY) {
			sourceSpec = (src != null && src.getSpecSourceCode()!= null? src.getSpecSourceCode().replace("\r", "").replaceAll("(\\s)+\n", "\n").trim() : null);
			sourceBody = (src != null && src.getBodySourceCode()!=null? src.getBodySourceCode().replace("\r", "").replaceAll("(\\s)+\n", "\n").trim() : null);
			// We should process target streams as well for repository comparison equality (will do nothing when target is database since no \r)
			targetSpec = (tgt != null && tgt.getSpecSourceCode()!= null? tgt.getSpecSourceCode().replace("\r", "").replaceAll("(\\s)+\n", "\n").trim() : null);
			targetBody = (tgt != null && tgt.getBodySourceCode()!= null? tgt.getBodySourceCode().replace("\r", "").replaceAll("(\\s)+\n", "\n").trim() : null);
		} else {
			sourceSpec = (src != null ? src.getSpecSourceCode() : null);
			sourceBody = (src != null ? src.getBodySourceCode() : null);
			targetSpec = (tgt != null ? tgt.getSpecSourceCode() : null);
			targetBody = (tgt != null ? tgt.getBodySourceCode() : null);
		}
		
		// Comparing body and specs
		result.addSubItem(new ComparisonAttributeIgnoreFirstLine(ATTR_SPEC,src != null ? sourceSpec : null, tgt != null ? targetSpec : null));
		if(src!=null && sourceBody != null && sourceBody.contains("wrapped") && getMergeStrategy().getComparisonScope()==ComparisonScope.DATABASE) {
			return result;
		}
		result.addSubItem(new ComparisonAttributeIgnoreFirstLine(ATTR_BODY,src != null ? sourceBody : null, tgt != null ? targetBody : null));
		if(src!=null && target!=null) {
			try {
				addLineByLineSubItems(result, null, source, target);
			} catch(IOException e) {
				throw new ErrorException(e);
			}
		}
		// Returning
		return result;
	}

	@Override
	public void addLineByLineSubItems(IComparisonItem result, IReferenceable ancestor,
			IReferenceable source, IReferenceable target) throws IOException {
		final IPackage srcPkg = (IPackage)source;
		final IPackage tgtPkg = (IPackage)target;
		final IPackage ancPkg = (IPackage)ancestor;
	    	List<IComparisonItem> srcItems = MergeUtils.mergeCompare(srcPkg == null ? "" : srcPkg.getSpecSourceCode(), tgtPkg == null ? "" : tgtPkg.getSpecSourceCode(), ancPkg ==null ? null : ancPkg.getSpecSourceCode());
	    	addMergedSubItems(result, ATTR_SPEC, srcItems);
	    	List<IComparisonItem> bodyItems = MergeUtils.mergeCompare(srcPkg == null ? "" : srcPkg.getBodySourceCode(), tgtPkg == null ? "" : tgtPkg.getBodySourceCode(), ancPkg == null ? null : ancPkg.getBodySourceCode());
	    	addMergedSubItems(result, ATTR_BODY, bodyItems);
	}


	@Override
	protected void cleanLineByLineSubItems(IComparisonItem result) {
		removeMergedSubItems(result, ATTR_SPEC);
		removeMergedSubItems(result, ATTR_BODY);
	}


}
