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
package com.nextep.designer.sqlgen.mysql.generator;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.LoadingMethod;
import com.nextep.datadesigner.sqlgen.impl.DatafileGeneration;
import com.nextep.datadesigner.sqlgen.impl.generator.DataSetGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.IDatafileGeneration;
import com.nextep.designer.dbgm.mergers.DataSetMerger;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.mysql.model.IMySQLTable;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IRepositoryFile;

public class MySQLDatasetGenerator extends DataSetGenerator {

	@Override
	protected IGenerationResult generateDatafilesDiff(IComparisonItem result) {
		IDataSet src = (IDataSet) result.getSource();
		// IDataSet tgt = (IDataSet)result.getTarget();

		IDatafileGeneration fileGen = new DatafileGeneration(src);
		fileGen.setControlFileHeader(getLoadStatement(src));
		for (IComparisonItem item : result.getSubItems(DataSetMerger.CATEG_DATAFILES)) {
			if (item.getDifferenceType() != DifferenceType.EQUALS
					&& item.getSource() instanceof IRepositoryFile) {
				fileGen.addDataFile((IRepositoryFile) item.getSource());
			}
		}
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDatafileGeneration(new DatabaseReference(src.getType(), src.getTable().getName()),
				fileGen);
		return r;
	}

	@Override
	protected IGenerationResult generateDatafilesFull(IDataSet set) {
		IDatafileGeneration fileGen = new DatafileGeneration(set);
		fileGen.setControlFileHeader(getLoadStatement(set));
		for (IRepositoryFile f : set.getDataFiles()) {
			fileGen.addDataFile(f);
		}

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDatafileGeneration(new DatabaseReference(set.getType(), set.getTable().getName()),
				fileGen);
		return r;
	}

	private String getLoadStatement(IDataSet set) {
		StringBuffer buf = new StringBuffer(100);
		if (set.getLoadingMethod() == LoadingMethod.TRUNCATE) {
			buf.append("set foreign_key_checks=0;" + NEWLINE);
			buf.append("TRUNCATE TABLE " + set.getTable().getName() + ";" + NEWLINE);
			buf.append("set foreign_key_checks=1;" + NEWLINE);
		}
		if (set.getTable() != null) {
			IMySQLTable t = (IMySQLTable) set.getTable();
			if (t.getCharacterSet() != null && !"".equals(t.getCharacterSet().trim())) {
				buf.append("set character_set_database=" + t.getCharacterSet() + ";" + NEWLINE);
			}
		}
		buf.append("LOAD DATA LOCAL INFILE '$file' ");
		switch (set.getLoadingMethod()) {
		case REPLACE:
			buf.append(LoadingMethod.REPLACE.name().toLowerCase() + " ");
			break;
		case APPEND:
			buf.append("IGNORE ");
			break;
		}
		buf.append("INTO TABLE " + set.getTable().getName() + " ");
		buf.append("FIELDS TERMINATED BY '" + set.getFieldsTermination() + "' "
				+ (set.isOptionalEnclosure() ? "OPTIONALLY " : "") + "ENCLOSED BY '"
				+ (set.getFieldsEnclosure() == null ? "" : set.getFieldsEnclosure())
				+ "' LINES TERMINATED BY '\\n' (");
		boolean first = true;
		for (IBasicColumn c : set.getColumns()) {
			if (first) {
				first = false;
			} else {
				buf.append(", ");
			}
			buf.append(c.getName());
		}
		buf.append(");" + NEWLINE);
		return buf.toString();
	}
}
