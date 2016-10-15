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
package com.nextep.designer.dbgm.mergers;

import java.io.IOException;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.dbgm.model.TriggerTime;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonAttributeIgnoreFirstLine;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.impl.MergerWithMultilineAttributes;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * @author Christophe Fondacci
 *
 */
public class TriggerMerger extends MergerWithMultilineAttributes {

	public static final String ATTR_TIME = "Trigger time";
	public static final String ATTR_ON_INSERT = "Insert";
	public static final String ATTR_ON_UPDATE = "Update";
	public static final String ATTR_ON_DELETE = "Delete";
	public static final String ATTR_USER_DEFINED = "User-defined";
	public static final String ATTR_TRIGGABLE = "Triggerred element";
	public static final String ATTR_SQL = "SQL Definition";
	
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object, com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		ITrigger tgt = (ITrigger)target;
		fillName(result, tgt);
		// Filling indexed table reference
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),getMergeStrategy().getComparisonScope());
		IReference r = (IReference)m.buildMergedObject(result.getSubItems(ATTR_TRIGGABLE).iterator().next(), activity);
		tgt.setTriggableRef(r);
		// Filling trigger time / events
		final String trigTime = getStringProposal(ATTR_TIME, result);
		if(trigTime!=null && !"".equals(trigTime)) {
			tgt.setTime(TriggerTime.valueOf(trigTime));
		} else {
			tgt.setTime(null);
		}
		
		tgt.getEvents().clear();
		if(Boolean.valueOf(getStringProposal(ATTR_ON_INSERT, result))) {
			tgt.addEvent(TriggerEvent.INSERT);
		}
		if(Boolean.valueOf(getStringProposal(ATTR_ON_UPDATE, result))) {
			tgt.addEvent(TriggerEvent.UPDATE);
		}
		if(Boolean.valueOf(getStringProposal(ATTR_ON_DELETE, result))) {
			tgt.addEvent(TriggerEvent.DELETE);
		}
		// User-defined flag
		tgt.setCustom(Boolean.valueOf(getStringProposal(ATTR_USER_DEFINED, result)));
		// SQL Source
		tgt.setSourceCode(mergeAttribute(result, ATTR_SQL));
		// Object is fully filled 
		return tgt;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable, com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IComparisonItem result = new ComparisonResult(source,target,getMergeStrategy().getComparisonScope());
		ITrigger src = (ITrigger)source;
		ITrigger tgt = (ITrigger)target;
		// Comparing name / description
		compareName(result, src, tgt);
		// Comparing indexed table
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),getMergeStrategy().getComparisonScope());
		result.addSubItem(ATTR_TRIGGABLE,m.compare(src == null ? null : src.getTriggableRef(), tgt == null ? null : tgt.getTriggableRef()));
		// Time / events
		if(getMergeStrategy().getComparisonScope()==ComparisonScope.REPOSITORY || (src!=null && !src.isCustom())) {
			result.addSubItem(new ComparisonAttribute(ATTR_TIME,src == null ? null : src.getTime() == null ? "" : src.getTime().name(), tgt == null ? null : tgt.getTime() == null ? "" : tgt.getTime().name()));
			result.addSubItem(new ComparisonAttribute(ATTR_ON_INSERT,src == null ? null : eventToBoolStr(src,TriggerEvent.INSERT), tgt == null ? null : eventToBoolStr(tgt,TriggerEvent.INSERT)));
			result.addSubItem(new ComparisonAttribute(ATTR_ON_UPDATE,src == null ? null : eventToBoolStr(src,TriggerEvent.UPDATE), tgt == null ? null : eventToBoolStr(tgt,TriggerEvent.UPDATE)));
			result.addSubItem(new ComparisonAttribute(ATTR_ON_DELETE,src == null ? null : eventToBoolStr(src,TriggerEvent.DELETE), tgt == null ? null : eventToBoolStr(tgt,TriggerEvent.DELETE)));
		}
		// User-defined flag
		result.addSubItem(new ComparisonAttribute(ATTR_USER_DEFINED,src == null ? null : Boolean.toString(src.isCustom()), tgt == null ? null : Boolean.toString(tgt.isCustom())));
		// SQL source
		final String sqlSource = (src != null && src.getSql()!=null? cleanSourceCode(src.getSql()) : null);
		result.addSubItem(new ComparisonAttributeIgnoreFirstLine(ATTR_SQL,src!=null?sqlSource:null,tgt!=null?cleanSourceCode(tgt.getSql()):null));
		if(src !=null && tgt != null) {
			try {
				addLineByLineSubItems(result, null, source, target);
			} catch(IOException e) {
				throw new ErrorException(e);
			}
		}
		// Returning comparison result
		return result;
	}
	/**
	 * This method is an entry-point for class-extensions willing to perform
	 * some String manipulation on the trigger's source code before comparing
	 * it. For example: a MySQL extension may parse the trigger code to remove
	 * trailing ';' and code comments before comparing.
	 * 
	 * @param originalSource original trigger source code
	 * @return the cleaned source to use for comparison
	 */
	protected String cleanSourceCode(String originalSource) {
		// Only processing non-repository scope
		if(getMergeStrategy().getComparisonScope()!=ComparisonScope.REPOSITORY) {
			return originalSource.replace("\r", "").replaceAll("( )+\n", "\n").trim();
		} else {
			return originalSource;
		}
	}

	private String eventToBoolStr(ITrigger t, TriggerEvent event) {
		return Boolean.toString(t.getEvents().contains(event));
	}

	@Override
	public void addLineByLineSubItems(IComparisonItem result,
			IReferenceable ancestor, IReferenceable source,
			IReferenceable target) throws IOException {
		// TODO : need to factorize multiline SQL-based mergers using the ISqlBased interface
		final ITrigger src = (ITrigger)source;
		final ITrigger tgt = (ITrigger)target;
		final ITrigger anc = (ITrigger)ancestor;
		
		final String srcText = src == null ? "" : cleanSourceCode(src.getSql());
		final String tgtText = tgt == null ? "" : cleanSourceCode(tgt.getSql());
		final String ancText = anc== null ? null : cleanSourceCode(anc.getSql());
		
		List<IComparisonItem> items = MergeUtils.mergeCompare(srcText, tgtText,ancText);
		addMergedSubItems(result, ATTR_SQL, items);
	}

	@Override
	protected void cleanLineByLineSubItems(IComparisonItem result) {
		removeMergedSubItems(result, ATTR_SQL);
	}
}
