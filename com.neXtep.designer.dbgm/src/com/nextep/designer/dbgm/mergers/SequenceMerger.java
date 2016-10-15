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

import java.math.BigDecimal;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class SequenceMerger extends Merger {

	protected static final String ATTR_START = "Start"; //$NON-NLS-1$
	protected static final String ATTR_INC = "Increment"; //$NON-NLS-1$
	protected static final String ATTR_MIN = "Min"; //$NON-NLS-1$
	protected static final String ATTR_MAX = "Max"; //$NON-NLS-1$
	protected static final String ATTR_CACHED = "Cached"; //$NON-NLS-1$
	protected static final String ATTR_CACHE_SIZE = "Cache size"; //$NON-NLS-1$
	protected static final String ATTR_CYCLE = "Cycle"; //$NON-NLS-1$
	protected static final String ATTR_ORDER = "Order"; //$NON-NLS-1$

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		ISequence seq = (ISequence) target;

		// Filling name and description attributes.
		fillName(result, seq);
		if (seq.getName() == null) {
			return null;
		}

		String start = getStringProposal(ATTR_START, result);
		String inc = getStringProposal(ATTR_INC, result);
		String min = getStringProposal(ATTR_MIN, result);
		String max = getStringProposal(ATTR_MAX, result);
		String cached = getStringProposal(ATTR_CACHED, result);
		String cacheSize = getStringProposal(ATTR_CACHE_SIZE, result);
		String cycle = getStringProposal(ATTR_CYCLE, result);
		String order = getStringProposal(ATTR_ORDER, result);

		if (start != null && !"".equals(start)) { //$NON-NLS-1$
			seq.setStart(new BigDecimal(start));
		}
		if (inc != null) {
			seq.setIncrement(Long.valueOf(inc));
		}
		if (min != null && !"".equals(min)) { //$NON-NLS-1$
			seq.setMinValue(new BigDecimal(min));
		}
		if (max != null && !"".equals(max)) { //$NON-NLS-1$
			seq.setMaxValue(new BigDecimal(max));
		}
		if (cached != null) {
			seq.setCached(Boolean.valueOf(cached));
		}
		if (cacheSize != null) {
			seq.setCacheSize(Integer.valueOf(cacheSize));
		}
		if (cycle != null) {
			seq.setCycle(Boolean.valueOf(cycle));
		}
		if (order != null) {
			seq.setOrdered(Boolean.valueOf(order));
		}

		return seq;
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		ISequence src = (ISequence) source;
		ISequence tgt = (ISequence) target;

		// Comparing name and description attributes.
		compareName(result, src, tgt);

		// Comparing sequence specific attributes.
		compareStartWithAttribute(result, src, tgt);
		compareIncrementByAttribute(result, src, tgt);
		compareMinValueAttribute(result, src, tgt);
		compareMaxValueAttribute(result, src, tgt);
		compareCachedAttribute(result, src, tgt);
		compareCycleAttribute(result, src, tgt);
		compareOrderAttribute(result, src, tgt);

		return result;
	}

	/**
	 * This method is provided as a convenience for vendor specific implementors who might want to
	 * override this method in order to perform a vendor specific comparison of the "START WITH"
	 * sequence attribute.
	 * 
	 * @param result a {@link IComparisonItem} containing the comparison information
	 * @param source a {@link ISequence} representing the source element of the comparison
	 * @param target a {@link ISequence} representing the target element of the comparison
	 */
	protected void compareStartWithAttribute(IComparisonItem result, ISequence source,
			ISequence target) {
		result.addSubItem(new ComparisonAttribute(ATTR_START, source == null ? null : strVal(source
				.getStart()), target == null ? null : strVal(target.getStart()),
				ComparisonScope.REPOSITORY));
	}

	/**
	 * This method is provided as a convenience for vendor specific implementors who might want to
	 * override this method in order to perform a vendor specific comparison of the "INCREMENT BY"
	 * sequence attribute.
	 * 
	 * @param result a {@link IComparisonItem} containing the comparison information
	 * @param source a {@link ISequence} representing the source element of the comparison
	 * @param target a {@link ISequence} representing the target element of the comparison
	 */
	protected void compareIncrementByAttribute(IComparisonItem result, ISequence source,
			ISequence target) {
		result.addSubItem(new ComparisonAttribute(ATTR_INC, source == null ? null : strVal(source
				.getIncrement()), target == null ? null : strVal(target.getIncrement())));
	}

	/**
	 * This method is provided as a convenience for vendor specific implementors who might want to
	 * override this method in order to perform a vendor specific comparison of the "MINVALUE"
	 * sequence attribute.
	 * 
	 * @param result a {@link IComparisonItem} containing the comparison information
	 * @param source a {@link ISequence} representing the source element of the comparison
	 * @param target a {@link ISequence} representing the target element of the comparison
	 */
	protected void compareMinValueAttribute(IComparisonItem result, ISequence source,
			ISequence target) {
		result.addSubItem(new ComparisonAttribute(ATTR_MIN, source == null ? null : strVal(source
				.getMinValue()), target == null ? null : strVal(target.getMinValue())));
	}

	/**
	 * This method is provided as a convenience for vendor specific implementors who might want to
	 * override this method in order to perform a vendor specific comparison of the "MAXVALUE"
	 * sequence attribute.
	 * 
	 * @param result a {@link IComparisonItem} containing the comparison information
	 * @param source a {@link ISequence} representing the source element of the comparison
	 * @param target a {@link ISequence} representing the target element of the comparison
	 */
	protected void compareMaxValueAttribute(IComparisonItem result, ISequence source,
			ISequence target) {
		result.addSubItem(new ComparisonAttribute(ATTR_MAX, source == null ? null : strVal(source
				.getMaxValue()), target == null ? null : strVal(target.getMaxValue())));
	}

	/**
	 * This method is provided as a convenience for vendor specific implementors who might want to
	 * override this method in order to perform a vendor specific comparison of the "CACHE <size>"
	 * sequence attribute.
	 * 
	 * @param result a {@link IComparisonItem} containing the comparison information
	 * @param source a {@link ISequence} representing the source element of the comparison
	 * @param target a {@link ISequence} representing the target element of the comparison
	 */
	protected void compareCachedAttribute(IComparisonItem result, ISequence source, ISequence target) {
		result.addSubItem(new ComparisonAttribute(ATTR_CACHED, source == null ? null
				: strVal(source.isCached()), target == null ? null : strVal(target.isCached())));
		result.addSubItem(new ComparisonAttribute(ATTR_CACHE_SIZE, source == null ? null
				: strVal(source.getCacheSize()), target == null ? null : strVal(target
				.getCacheSize())));
	}

	/**
	 * This method is provided as a convenience for vendor specific implementors who might want to
	 * override this method in order to perform a vendor specific comparison of the "CYCLE" sequence
	 * attribute.
	 * 
	 * @param result a {@link IComparisonItem} containing the comparison information
	 * @param source a {@link ISequence} representing the source element of the comparison
	 * @param target a {@link ISequence} representing the target element of the comparison
	 */
	protected void compareCycleAttribute(IComparisonItem result, ISequence source, ISequence target) {
		result.addSubItem(new ComparisonAttribute(ATTR_CYCLE, source == null ? null : strVal(source
				.isCycle()), target == null ? null : strVal(target.isCycle())));
	}

	/**
	 * This method is provided as a convenience for vendor specific implementors who might want to
	 * override this method in order to perform a vendor specific comparison of the "ORDER" sequence
	 * attribute.
	 * 
	 * @param result a {@link IComparisonItem} containing the comparison information
	 * @param source a {@link ISequence} representing the source element of the comparison
	 * @param target a {@link ISequence} representing the target element of the comparison
	 */
	protected void compareOrderAttribute(IComparisonItem result, ISequence source, ISequence target) {
		result.addSubItem(new ComparisonAttribute(ATTR_ORDER, source == null ? null : strVal(source
				.isOrdered()), target == null ? null : strVal(target.isOrdered())));
	}

	@Override
	protected String strVal(Object o) {
		return o == null ? null : super.strVal(o);
	}

}
