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
package com.nextep.designer.sqlgen.db2.generator;

import java.math.BigDecimal;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.db2.parser.DB2SQLParser;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.generic.generator.SequenceGenerator;
import com.nextep.designer.sqlgen.model.IGenerationResult;

/**
 * @author Bruno Gautier
 */
public final class DB2SequenceGenerator extends SequenceGenerator {

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final ISequence seq = (ISequence) model;
		final String seqName = seq.getName();

		ISQLScript additionScript = new SQLScript(seqName, seq.getDescription(),
				getSQLCommandWriter().promptMessage("Creating sequence '" + seqName + "'..."), //$NON-NLS-1$ //$NON-NLS-2$
				ScriptType.SEQ);

		additionScript.appendSQL("CREATE SEQUENCE ").appendSQL(seqName); //$NON-NLS-1$

		String seqDatatype = getSequenceDatatype(seq);
		if (!seqDatatype.equals(DB2SQLParser.DATATYPE_INTEGER)) {
			additionScript.appendSQL(" AS ").appendSQL(seqDatatype); //$NON-NLS-1$

			if (seqDatatype.equals(DB2SQLParser.DATATYPE_DECIMAL)) {
				additionScript.appendSQL("(").appendSQL(seq.getMaxValue().precision()) //$NON-NLS-1$
						.appendSQL(",0)"); //$NON-NLS-1$
			}
		}

		appendGenericAttributesFull(additionScript, seq);

		additionScript.appendSQL(NEWLINE).appendSQL(getSQLCommandWriter().closeStatement());

		IGenerationResult genResult = GenerationFactory.createGenerationResult();
		genResult.addAdditionScript(new DatabaseReference(seq.getType(), seqName), additionScript);

		return genResult;
	}

	@Override
	protected boolean isAlterable(ISequence oldSeq, ISequence newSeq) {
		/*
		 * We check if the sequence data type has been changed, since it cannot be changed with an
		 * alter statement.
		 */
		return getSequenceDatatype(newSeq).equals(getSequenceDatatype(oldSeq));
	}

	/**
	 * Try to guess the sequence data type according to its maximal value.<br>
	 * If no maximal value has been defined, the sequence is assumed to be of the default data type
	 * <code>INTEGER</code>.
	 * 
	 * @param seq a {@link ISequence} for which we must try to guess the data type
	 * @return a {@link String} representing the specified's sequence data type
	 */
	private String getSequenceDatatype(ISequence seq) {
		final BigDecimal maxValue = seq.getMaxValue();
		// FIXME [BGA]: The IDatatypeProvider should be retrieved by using a service.
		final IDatatypeProvider provider = DBGMHelper.getDatatypeProvider(DBVendor.DB2);

		// FIXME [BGA]: There should be some way to retrieve a data type label with a method of the
		// provider, avoiding the caller to explicitly write the data type label.
		if (null == maxValue) {
			return DB2SQLParser.DATATYPE_INTEGER;
		} else if (maxValue.compareTo(provider.getDatatypeMaxSize("SMALLINT")) <= 0) { //$NON-NLS-1$
			return DB2SQLParser.DATATYPE_SMALLINT;
		} else if (maxValue.compareTo(provider.getDatatypeMaxSize("INTEGER")) <= 0) { //$NON-NLS-1$
			return DB2SQLParser.DATATYPE_INTEGER;
		} else if (maxValue.compareTo(provider.getDatatypeMaxSize("BIGINT")) <= 0) { //$NON-NLS-1$
			return DB2SQLParser.DATATYPE_BIGINT;
		} else {
			return DB2SQLParser.DATATYPE_DECIMAL;
		}
	}

}
