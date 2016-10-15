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
package com.nextep.datadesigner.sqlgen.impl.oracle;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.ParameterType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.sqlgen.impl.Package;

/**
 * @author Christophe Fondacci
 */
public class OraclePackage extends Package {

	// private static final Log log = LogFactory.getLog(OraclePackage.class);
	// private static final String PROC = "PROCEDURE";
	// private static final String FUNC = "FUNCTION";

	private IParseData parseData;
	private boolean parsed = false;
	private String[] paramTypes;
	Collection<IProcedure> procedures = new ArrayList<IProcedure>();

	public OraclePackage() {
		super();
		paramTypes = new String[ParameterType.values().length];
		int i = 0;
		for (ParameterType t : ParameterType.values()) {
			paramTypes[i++] = t.name();
		}
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.impl.Package#getProcedures()
	 */
	@Override
	public Collection<IProcedure> getProcedures() {
		if (!isParsed()) {
			DBGMHelper.parse(this);
		}
		return procedures;
	}

	public String getNextWord(IDocument doc, int start) throws BadLocationException {
		StringBuffer buf = new StringBuffer();
		boolean started = false;
		while (start < doc.getLength()) {
			if (Character.isJavaIdentifierPart(doc.getChar(start))) {
				started = true;
				buf.append(doc.getChar(start++));
			} else if (started) {
				return buf.toString();
			} else {
				start++;
			}
		}
		return buf.toString();
	}

	// protected IProcedureParameter parseParameter(IDocument doc,int start, int length) {
	// ParameterScanner scanner = new ParameterScanner();
	// scanner.setRange(doc,start,length);
	//
	// IToken token = scanner.nextToken();
	// ParameterType type = null;
	// String paramName = null;
	// String paramDatatype = null;
	// String defaultExpr = null;
	// while(!token.isEOF()) {
	// if(token.getData() instanceof ParameterType) {
	// type = (ParameterType)token.getData();
	// } else if(token == ParameterScanner.DECL_TOKEN){
	// if(type == null) {
	// paramName =
	// doc.get().substring(scanner.getTokenOffset(),scanner.getTokenOffset()+scanner.getTokenLength()).trim();
	// } else {
	// paramDatatype =
	// doc.get().substring(scanner.getTokenOffset(),scanner.getTokenOffset()+scanner.getTokenLength()).trim();
	// }
	// } else if(token == ParameterScanner.DEFAULTEXPR_TOKEN) {
	// defaultExpr =
	// doc.get().substring(scanner.getTokenOffset()+2,scanner.getTokenOffset()+scanner.getTokenLength()).trim();
	// }
	// token = scanner.nextToken();
	// }
	// // Building parameter
	// IProcedureParameter param = new ProcedureParameter(paramName,type,new
	// Datatype(paramDatatype));
	// param.setDefaultExpr(defaultExpr);
	// return param;
	// }

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IPackage#getParseData()
	 */
	@Override
	public IParseData getParseData() {
		if (!isParsed()) {
			DBGMHelper.parse(this);
		}
		return parseData;
	}

	/**
	 * @see com.nextep.datadesigner.sqlgen.model.IPackage#isParsed()
	 */
	@Override
	public boolean isParsed() {
		return parsed;
	}

	@Override
	public void setParsed(boolean parsed) {
		this.parsed = parsed;
	}

	@Override
	public void setParseData(IParseData parseData) {
		this.parseData = parseData;
	}

	@Override
	public void addProcedure(IProcedure procedure) {
		procedure.setParent(this);
		procedures.add(procedure);
	}

	@Override
	public void clearProcedures() {
		procedures.clear();
	}

	@Override
	public void setProcedures(Collection<IProcedure> procedures) {
		this.procedures = procedures;
	}

	@Override
	public String getSql() {
		return getBodySourceCode();
	}

	@Override
	public void setSql(String sql) {
		setBodySourceCode(sql);
	}
}
