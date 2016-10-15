package com.nextep.designer.dbgm.postgre.parser;

import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.designer.dbgm.model.base.AbstractTypedSqlParser;

/**
 * A small extension of the default procedure paser in order to include
 * procedure arguments from the name
 * 
 * @author cfondacci
 * 
 */
public class PostgreSqlProcedureParser extends AbstractTypedSqlParser {

	@Override
	protected String getNameDelimiterEndTag() {
		return "\\s+(.*)RETURNS";
	}
	
	@Override
	public String parseName(String sql) {
		final String name = super.parseName(sql);
		if(name!=null) {
			return name.trim();
		} else {
			return null;
		}
	}

	@Override
	public IParseData parse(IParseable p, String contentsToParse) {
		// No default parsing support
		return null;
	}

	@Override
	protected String getNameDelimiterTag() {
		return "procedure|function";
	}

}
