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
package com.nextep.designer.dbgm.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.LoadingMethod;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.Property;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IProperty;
import com.nextep.datadesigner.model.IPropertyProvider;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.impl.SelfControlVersionable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.mergers.DataSetMerger;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;
import com.nextep.designer.dbgm.policies.VersionPolicyDataSet;
import com.nextep.designer.vcs.model.IRepositoryFile;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Default implementation of the {@link IDataSet} interface
 * 
 * @author Christophe Fondacci
 */
public class DataSet extends SelfControlVersionable<IDataSet> implements IDataSet {

	// private IBasicTable table;
	private IReference tableRef;

	private List<IReference> columnRefs;
	private boolean fileGenerated = false;
	private LoadingMethod method = LoadingMethod.INSERT;
	private String fieldsTermination = ";";
	private String fieldsEnclosure = "\"";
	private boolean optionalEnclosure;
	private List<IRepositoryFile> datafiles;
	private Map<IReference, String> columnMasks;
	private IStorageHandle handle;
	private long currentRowId = 1;

	public DataSet(IBasicTable table) {
		this();
		setTable(table);
	}

	/**
	 * Creates an empty dataset
	 */
	public DataSet() {
		columnRefs = new ArrayList<IReference>();
		datafiles = new ArrayList<IRepositoryFile>();
		columnMasks = new HashMap<IReference, String>();
		setVersionPolicy(new VersionPolicyDataSet());
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataSet#getColumns()
	 */
	@Override
	public List<IBasicColumn> getColumns() {
		List<IBasicColumn> cols = new ArrayList<IBasicColumn>();
		IVersionable<IBasicTable> t = (IVersionable<IBasicTable>) VersionHelper
				.getVersionable((IBasicTable) VersionHelper.getReferencedItem(getTableReference()));
		for (IReference r : columnRefs) {
			cols.add((IBasicColumn) t.getReferenceMap().get(r));
		}
		return cols;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataSet#getTable()
	 */
	@Override
	public IBasicTable getTable() {
		return (IBasicTable) VersionHelper.getReferencedItem(tableRef);
	}

	public void setTable(IBasicTable table) {
		if (table != null) {
			this.tableRef = VersionHelper.getVersionable(table).getVersion().getReference();
			// table.addDataSet(this);
		}
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataSet#addColumn(com.nextep.datadesigner.dbgm.model.IBasicColumn)
	 */
	@Override
	public void addColumn(IBasicColumn column) {
		if (column == null) {
			throw new ErrorException("Trying to add a null column to a dataset.");
		}
		// Validating creation
		canInsert(column, true);
		// Adding column
		addColumnRef(column.getReference());
	}

	/**
	 * Determine if this data set can insert this column. The method will validate that we are not
	 * trying to insert a "NOT NULL" column which does not specifies a default expression while
	 * already having existing data lines.
	 * 
	 * @param column column eligible for insert
	 * @param raise should this method raise an exception if no insert is possible
	 * @return a boolean indicating the insert ability
	 */
	protected boolean canInsert(IBasicColumn column, boolean raise) {
		// if (!lines.isEmpty()) {
		// if (column.isNotNull()
		// && ("".equals(column.getDefaultExpr()) || column.getDefaultExpr() == null)) {
		// if (raise) {
		// throw new ErrorException(
		// "Can only add a non-defaulted NOT NULL column on an empty data set");
		// } else {
		// return false;
		// }
		// }
		// }
		return true;
	}

	/**
	 * Determine whether we can remove this dataset column or not. So far, the very basic
	 * implementation will grant column removal on empty data sets only (i.e. no data lines).
	 * 
	 * @param column column to remove
	 * @return <code>true</code> if ok to remove, else <code>false</code>
	 */
	protected boolean canRemove(IBasicColumn column) {
		// if (!lines.isEmpty()) {
		// throw new ErrorException(DBGMMessages.getString("datasetMustBeEmpty"));
		// }
		return true;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataSet#insertColumn(int,
	 *      com.nextep.datadesigner.dbgm.model.IBasicColumn)
	 */
	@Override
	public void insertColumn(int index, IBasicColumn column) {
		// Validating ability
		canInsert(column, true);
		// Effectively adding our column
		columnRefs.add(index, column.getReference());
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataSet#removeColumn(com.nextep.datadesigner.dbgm.model.IBasicColumn)
	 */
	@Override
	public boolean removeColumn(IBasicColumn column) {
		if (canRemove(column)) {
			final boolean removed = columnRefs.remove(column.getReference());
			notifyListeners(ChangeEvent.COLUMN_REMOVED, column);
			return removed;
		}
		return false;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataSet#removeColumn(int)
	 */
	@Override
	public boolean removeColumn(int index) {
		if (columnRefs.size() > index) {
			columnRefs.remove(index);
			return true;
		}
		return false;
	}

	/**
	 * Hibernate setter for columns
	 * 
	 * @param columns
	 */
	protected void setColumnsRef(List<IReference> columns) {
		this.columnRefs = columns;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataSet#getTableReference()
	 */
	@Override
	public IReference getTableReference() {
		return tableRef;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataSet#setTableReference(com.nextep.datadesigner.impl.Reference)
	 */
	@Override
	public void setTableReference(IReference tableRef) {
		this.tableRef = tableRef;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		List<IReference> refs = new ArrayList<IReference>();
		for (IReference r : getColumnsRef()) {
			refs.add(r);
		}
		refs.add(getTableReference());
		return refs;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataSet#getColumnsRef()
	 */
	@Override
	public List<IReference> getColumnsRef() {
		return columnRefs;
	}

	/**
	 * @see com.nextep.designer.dbgm.model.IDataSet#addColumnRef(com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public void addColumnRef(IReference columnRef) {
		// If everything's ok, we add
		if (columnRefs.add(columnRef)) {
			notifyListeners(ChangeEvent.COLUMN_ADDED,
					(IBasicColumn) VersionHelper.getReferencedItem(columnRef));
		}
	}

	@Override
	public String getFieldsEnclosure() {
		return fieldsEnclosure;
	}

	@Override
	public String getFieldsTermination() {
		return fieldsTermination;
	}

	@Override
	public LoadingMethod getLoadingMethod() {
		return method;
	}

	@Override
	public boolean isFileGenerated() {
		return fileGenerated;
	}

	@Override
	public boolean isOptionalEnclosure() {
		return optionalEnclosure;
	}

	@Override
	public void setFieldsEnclosure(String s) {
		if (s != fieldsEnclosure) {
			this.fieldsEnclosure = s;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setFieldsTermination(String s) {
		this.fieldsTermination = s;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setFileGenerated(boolean fileGeneration) {
		this.fileGenerated = fileGeneration;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setLoadingMethod(LoadingMethod method) {
		this.method = method;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public void setOptionalEnclosure(boolean optional) {
		this.optionalEnclosure = optional;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	protected void setHibFileGenerated(String bool) {
		setFileGenerated("Y".equals(bool));
	}

	protected String getHibFileGenerated() {
		return isFileGenerated() ? "Y" : "N";
	}

	protected void setHibOptional(String bool) {
		setOptionalEnclosure("Y".equals(bool));
	}

	protected String getHibOptional() {
		return isOptionalEnclosure() ? "Y" : "N";
	}

	@Override
	public List<IRepositoryFile> getDataFiles() {
		return datafiles;
	}

	/**
	 * Hibernate list setter
	 * 
	 * @param files
	 */
	protected void setDataFiles(List<IRepositoryFile> files) {
		this.datafiles = files;
	}

	@Override
	public void addDataFile(IRepositoryFile file) {
		datafiles.add(file);
		notifyListeners(ChangeEvent.GENERIC_CHILD_ADDED, file);
	}

	@Override
	public void removeDataFile(IRepositoryFile file) {
		datafiles.remove(file);
		notifyListeners(ChangeEvent.GENERIC_CHILD_REMOVED, file);
	}

	@Override
	public String getColumnMask(IReference colRef) {
		if (colRef == null)
			return null;
		return columnMasks.get(colRef);
	}

	@Override
	public Map<IReference, String> getColumnMasks() {
		return columnMasks;
	}

	@Override
	public void setColumnMask(IReference colRef, String mask) {
		columnMasks.put(colRef, mask);
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	protected void setColumnMasks(Map<IReference, String> map) {
		this.columnMasks = map;
	}

	@Override
	public IStorageHandle getStorageHandle() {
		return handle;
	}

	@Override
	public void setStorageHandle(IStorageHandle handle) {
		this.handle = handle;
	}

	@Override
	public long getCurrentRowId() {
		return currentRowId;
	}

	@Override
	public void setCurrentRowId(long currentRowId) {
		this.currentRowId = currentRowId;
		notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	@Override
	public Object getAdapter(Class adapter) {
		// We intercept the property provider so that we do not trigger data set comparison which
		// would cause the dataset to be loaded locally.
		if (adapter == IPropertyProvider.class) {
			return new IPropertyProvider() {

				@Override
				public void setProperty(IProperty property) {
				}

				@Override
				public List<IProperty> getProperties() {
					final List<IProperty> props = new ArrayList<IProperty>();
					props.add(new Property(DataSetMerger.ATTR_NAME, getName()));
					props.add(new Property(DataSetMerger.ATTR_DESC, getDescription()));
					props.add(new Property(DataSetMerger.ATTR_TABLE, getTable().getName()));
					return props;
				}
			};
		}
		return super.getAdapter(adapter);
	}
}
