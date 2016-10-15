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
package com.nextep.designer.sqlgen.oracle.generator;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.LoadingMethod;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.sqlgen.impl.DatafileGeneration;
import com.nextep.datadesigner.sqlgen.impl.generator.DataSetGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.IDatafileGeneration;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.mergers.DataSetMerger;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IRepositoryFile;

/**
 * An extension of the base {@link DataSetGenerator} for specific file-based data sets support
 * through SQL*Loader
 * 
 * @author Christophe
 */
public class OracleDatasetGenerator extends DataSetGenerator {

	@Override
	protected IGenerationResult generateDatafilesDiff(IComparisonItem result) {
		IDataSet src = (IDataSet) result.getSource();
		// IDataSet tgt = (IDataSet)result.getTarget();

		IDatafileGeneration fileGen = new DatafileGeneration(src);
		fileGen.setControlFileHeader(getControlString(src));
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
		fileGen.setControlFileHeader(getControlString(set));
		for (IRepositoryFile f : set.getDataFiles()) {
			fileGen.addDataFile(f);
		}

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDatafileGeneration(new DatabaseReference(set.getType(), set.getTable().getName()),
				fileGen);
		return r;
	}

	protected String getControlString(IDataSet set) {
		StringBuffer buf = new StringBuffer(100);
		buf.append("load data \n");
		buf.append(set.getLoadingMethod().equals(LoadingMethod.INSERT) ? "" : set
				.getLoadingMethod().name().toLowerCase()
				+ " ");
		buf.append("into table " + set.getTable().getName() + "\n");
		buf.append("fields terminated by \"" + escapeStr(set.getFieldsTermination()) + "\"\n");
		buf.append(set.isOptionalEnclosure() ? "optionally " : "");
		buf.append("enclosed by \"" + escapeStr(set.getFieldsEnclosure())
				+ "\" trailing nullcols (\n");
		boolean first = true;
		for (IBasicColumn c : set.getColumns()) {
			if (first) {
				buf.append("    ");
				first = false;
			} else {
				buf.append(",   ");
			}
			buf.append(c.getName() + " " + getLoaderType(set, c) + '\n');
		}
		buf.append(")" + "\n");
		return buf.toString();
	}

	private String escapeStr(String s) {
		String esc = s.replace("\\", "\\\\");
		esc = esc.replace("\"", "\\\"");
		return esc;
	}

	private String getLoaderType(IDataSet set, IBasicColumn c) {
		IDatatype d = c.getDatatype();
		IDatatypeProvider provider = DBGMHelper.getDatatypeProvider(DBVendor.ORACLE);
		if (d.getName().toUpperCase().contains("DATE")
				|| d.getName().toUpperCase().contains("TIME")) {
			String mask = set.getColumnMask(c.getReference());
			return "date \"" + escapeStr(mask) + "\"";
		} else if (provider.listStringDatatypes().contains(d.getName().toUpperCase())) {
			return "char";
		} else {
			return "decimal external";
		}
	}
}
