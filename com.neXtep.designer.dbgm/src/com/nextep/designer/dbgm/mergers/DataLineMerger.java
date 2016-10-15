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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerWithChildCollections;
import com.nextep.designer.dbgm.model.IColumnValue;
import com.nextep.designer.dbgm.model.IDataLine;
import com.nextep.designer.dbgm.model.impl.DataLine;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class DataLineMerger extends MergerWithChildCollections {

	public static final String ATTR_POSITION = "Line number";
	public static final String ATTR_CONTENTS = "Data line contents";

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IDataLine line = (IDataLine) target;
		line.setRowId(Long.valueOf(getStringProposal(ATTR_POSITION, result)));
		List<?> values = (List<?>) getMergedList(ATTR_CONTENTS, result, activity);
		// Adding column values
		for (Object o : values) {
			IColumnValue val = (IColumnValue) o;
			line.addColumnValue(val);
		}
		// Returning the filled dataline object
		return line;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		IDataLine sourceLine = (IDataLine) source;
		IDataLine targetLine = (IDataLine) target;
		// Adding line number attribute
		result.addSubItem(new ComparisonAttribute(ATTR_POSITION, sourceLine != null ? String
				.valueOf(sourceLine.getRowId()) : null, targetLine != null ? String
				.valueOf(targetLine.getRowId()) : null));
		// Adding line contents
		listCompare(ATTR_CONTENTS, result, sourceLine == null ? Collections.EMPTY_LIST
				: adapt(sourceLine.getColumnValues()), targetLine == null ? Collections.EMPTY_LIST
				: adapt(targetLine.getColumnValues()));
		return result;
	}

	private List<IColumnValue> adapt(Collection<IColumnValue> vals) {
		List<IColumnValue> adaptedVals = new ArrayList<IColumnValue>(vals.size());
		for (IColumnValue val : vals) {
			adaptedVals.add(new ColumnValueReferenceableAdapter(val));
		}
		return adaptedVals;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#createTargetObject(com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object createTargetObject(IComparisonItem result, IActivity mergeActivity) {
		return new DataLine();
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#copyWhenUnchanged()
	 */
	@Override
	protected boolean copyWhenUnchanged() {
		return true;
	}

	/**
	 * An wrapper adapter which makes a column value referenceable. The referenceable interface is
	 * needed for merge. This adapter returns the column reference as the reference of the column
	 * value. It works only because references for merge are only used in the scope of the parent
	 * object (here: a data line)
	 * 
	 * @author Christophe Fondacci
	 */
	private class ColumnValueReferenceableAdapter implements IColumnValue, IReferenceable {

		private IColumnValue val;

		public ColumnValueReferenceableAdapter(IColumnValue val) {
			this.val = val;
		}

		@Override
		public IBasicColumn getColumn() {
			return val.getColumn();
		}

		@Override
		public String getStringValue() {
			return val.getStringValue();
		}

		@Override
		public Object getValue() {
			return val.getValue();
		}

		@Override
		public void setValue(Object value) {
			val.setValue(value);
		}

		@Override
		public UID getUID() {
			return val.getUID();
		}

		@Override
		public void setUID(UID id) {
			val.setUID(id);

		}

		@Override
		public IElementType getType() {
			return val.getType();
		}

		@Override
		public IReference getReference() {
			final IReference colRef = val.getColumnRef();
			IReference ref = new Reference(getType(), null, null);
			ref.setUID(colRef.getUID());
			ref.setVolatile(false);
			return ref;
		}

		@Override
		public void setReference(IReference ref) {
			// Read-only reference, doing nothing

		}

		/**
		 * @see com.nextep.datadesigner.model.IObservable#addListener(com.nextep.datadesigner.model.IEventListener)
		 */
		@Override
		public void addListener(IEventListener listener) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see com.nextep.datadesigner.model.IObservable#getListeners()
		 */
		@Override
		public Collection<IEventListener> getListeners() {
			// TODO Auto-generated method stub
			return null;
		}

		/**
		 * @see com.nextep.datadesigner.model.IObservable#notifyListeners(com.nextep.datadesigner.model.ChangeEvent,
		 *      java.lang.Object)
		 */
		@Override
		public void notifyListeners(ChangeEvent event, Object o) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see com.nextep.datadesigner.model.IObservable#removeListener(com.nextep.datadesigner.model.IEventListener)
		 */
		@Override
		public void removeListener(IEventListener listener) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see com.nextep.designer.dbgm.model.IColumnValue#getColumnRef()
		 */
		@Override
		public IReference getColumnRef() {
			return val.getColumnRef();
		}

		public void setColumn(IBasicColumn column) {
			val.setColumn(column);
		}

		public void setColumnRef(IReference r) {
			val.setColumnRef(r);
		}

		public void setDataLine(IDataLine line) {
			val.setDataLine(line);
		}

		public IDataLine getDataLine() {
			return val.getDataLine();
		}

	}

}
