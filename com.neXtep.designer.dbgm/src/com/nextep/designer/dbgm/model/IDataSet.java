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
package com.nextep.designer.dbgm.model;

import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.LoadingMethod;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.vcs.model.IRepositoryFile;

/**
 * A data set corresponds to a set of reference data.
 * 
 * @author Christophe Fondacci
 */
public interface IDataSet extends ITypedObject, IdentifiedObject, INamedObject, IObservable,
		ILockable<IDataSet>, IReferencer, IReferenceable, IStoreable {

	public static final String TYPE_ID = "DATASET"; //$NON-NLS-1$

	/**
	 * Retrieves the table for which this dataset has been defined.<br>
	 * This is a convenience method which uses the ReferenceManager to retrieve the concrete
	 * IBasicTable instance from a reference lookup. A dataset is attached to a table through a soft
	 * reference.<br>
	 * 
	 * @return the table on which this data set has been defined
	 */
	IBasicTable getTable();

	/**
	 * Defines the table for which this dataset is defined. This is a convenience method which
	 * stores the reference of the table rather than the table itself since a data set is linked to
	 * a table through a soft reference.
	 * 
	 * @param table table for which this set of data applies
	 */
	void setTable(IBasicTable table);

	/**
	 * @return the reference of the table for which this data set is defined
	 */
	IReference getTableReference();

	/**
	 * Defines the reference of the table for which this data set is defined.
	 * 
	 * @param tableRef the new table reference.
	 */
	void setTableReference(IReference tableRef);

	/**
	 * Retrieves the columns referenced by this datatype. Columns must all refer to the same table
	 * for the dataset to be consistent. The <code>isConsistent</code> method will be invoked on
	 * generation to ensure consistency.
	 * 
	 * @return the columns referenced by this dataset
	 */
	List<IBasicColumn> getColumns();

	/**
	 * Retrieves columns soft references.
	 * 
	 * @see IDataSet#getColumns()
	 * @return a list of column references
	 */
	List<IReference> getColumnsRef();

	/**
	 * Adds the given table column to this data set. Any existing data line of this data set will be
	 * updated to insert the new column. This method should throw an exception if the column is not
	 * consistent with this data set table.<br>
	 * <br>
	 * Adding a column to a data set means that this data set will define values for this columns in
	 * its data lines.<br>
	 * <br>
	 * Any previously existing line of this set will not be altered. The default column value (or
	 * <code>null</code> if not specified) will be used during generation. <br>
	 * A not null column providing no default value will raise an exception if this set is not
	 * empty.
	 * 
	 * @param column column to add to this data set.
	 */
	void addColumn(IBasicColumn column);

	/**
	 * Same as the {@link IDataSet#addColumn(IBasicColumn)} method using column soft references
	 * instead of {@link IBasicColumn}.
	 * 
	 * @param columnRef reference of the column to add
	 */
	void addColumnRef(IReference columnRef);

	/**
	 * Inserts a column at the given position of this data set. Any existing data line of this data
	 * set will be updated to insert the new column. This method should throw an exception if the
	 * column is not consistent with this data set table.<br>
	 * <br>
	 * Adding a column to a data set means that this data set will define values for this columns in
	 * its data lines.<br>
	 * <br>
	 * Any previously existing line of this set will not be altered. The default column value (or
	 * <code>null</code> if not specified) will be used during generation. <br>
	 * A not null column providing no default value will raise an exception if this set is not
	 * empty.
	 * 
	 * @param index index at which the column will be inserted
	 * @param column column to add to this data set
	 */
	void insertColumn(int index, IBasicColumn column);

	/**
	 * Removes the specified column from this data set. Any existing data line of this data set will
	 * <b>NOT</b> be altered. Anyway only columns from the <code>getColumns()</code> method of this
	 * set will be used for generation.
	 * 
	 * @param column column to remove
	 * @return <code>true</code> if the column existed in this data set
	 */
	boolean removeColumn(IBasicColumn column);

	/**
	 * Removes the column at the specified index from this data set. Any existing data line of this
	 * data set will <b>NOT</b> be altered. Anyway only columns from the <code>getColumns()</code>
	 * method of this set will be used for generation.
	 * 
	 * @param index index of the column to remove, starting at 0
	 * @return <code>true</code> if the index existed in this data set
	 */
	boolean removeColumn(int index);

	/**
	 * Defines whether this data set generates loadable files or standard insert / update / delete
	 * SQL queries
	 * 
	 * @param fileBased
	 */
	void setFileGenerated(boolean fileGeneration);

	/**
	 * @return whether this data set generates loadable files or standard insert / update / delete
	 *         SQL queries. Returns <code>true</code> for file generation <code>false</code> for SQL
	 *         generation.
	 */
	boolean isFileGenerated();

	/**
	 * Defines the loading method for this dataset. Loading method will have no effect for SQL-based
	 * datasets.
	 * 
	 * @param method file loading method
	 * @see IDataSet#setFileGenerated(boolean)
	 */
	void setLoadingMethod(LoadingMethod method);

	/**
	 * @return the {@link LoadingMethod} of this data set for file-generated data sets.
	 */
	LoadingMethod getLoadingMethod();

	/**
	 * @return the character sequences delimiting 2 fields in the resulting loadable file of a
	 *         dataset.(only for file-generated data sets)
	 * @see IDataSet#setFileGenerated(boolean)
	 */
	String getFieldsTermination();

	/**
	 * Defines the character sequences delimiting 2 fields in the resulting loadable file of a
	 * dataset.(only for file-generated data sets)
	 * 
	 * @param s the field termination delimiter
	 * @see IDataSet#setFileGenerated(boolean)
	 */
	void setFieldsTermination(String s);

	/**
	 * Defines the character sequence which encapsulates fields value in the generated file
	 * resulting from this dataset
	 * 
	 * @param s character enclosing fields
	 */
	void setFieldsEnclosure(String s);

	/**
	 * @return the character sequence which encapsulates fields value in the generated file
	 *         resulting from this dataset
	 */
	String getFieldsEnclosure();

	/**
	 * Defines whether the enclosure is optional. Optional enclosures will only enclosed fields
	 * which need it (like character fields) and leave the others.
	 * 
	 * @param optional is the enclosure optional
	 */
	void setOptionalEnclosure(boolean optional);

	/**
	 * @return whether the enclosure is optional. Optional enclosures will only enclosed fields
	 *         which need it (like character fields) and leave the others.
	 */
	boolean isOptionalEnclosure();

	/**
	 * Adds a file which contains data to this dataset, thus transforming this dataset into a
	 * file-based data set if it has no previous datafile defined.<br>
	 * File-based data set will not be able to add / remove data lines and any line information will
	 * be ignored during generation.
	 * 
	 * @param file repository file containing the data of this set
	 */
	void addDataFile(IRepositoryFile file);

	/**
	 * @return the files which contains the data of this set. Empty list will be returned for
	 *         lines-based data sets.
	 */
	List<IRepositoryFile> getDataFiles();

	/**
	 * Removes the specified datafile from this dataset. This operation will transform the dataset
	 * to a line-based dataset if it has no more files defines after having removed the specified
	 * one.
	 * 
	 * @param file datafile to remove
	 */
	void removeDataFile(IRepositoryFile file);

	/**
	 * Defines the mask of this column for file-based datasets. This mask will be used to parse
	 * datafile information back to SQL datatypes (such as dates).
	 * 
	 * @param colRef column reference
	 * @param mask load mask
	 */
	void setColumnMask(IReference colRef, String mask);

	/**
	 * @param colRef column reference
	 * @return the mask used to load this column (for file-based data sets)
	 */
	String getColumnMask(IReference colRef);

	/**
	 * @return all defined masks for all data sets columns
	 */
	Map<IReference, String> getColumnMasks();

	/**
	 * Defines the current last row identifier of this data set.
	 * 
	 * @param currentRowId current row identifier
	 */
	void setCurrentRowId(long currentRowId);

	/**
	 * Retrieves the current max row identifier of this data set
	 * 
	 * @return the identifier of the row which has been inserted last
	 */
	long getCurrentRowId();
}
