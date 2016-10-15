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
package com.nextep.designer.sqlgen.ui.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.sqlgen.model.ISQLParser;

/**
 * @author Christophe Fondacci
 *
 */
public class SQLAttributeManager {

	public static final String DDL 		= ISQLParser.DDL;
	public static final String DML 		= ISQLParser.DML;
	public static final String FUNC		= ISQLParser.FUNC;
	public static final String VAR 		= ISQLParser.VAR;
	public static final String DATATYPE	= ISQLParser.DATATYPE;
	public static final String SPECIAL	= ISQLParser.SPECIAL;
	public static final String STRING 	= SQLPartitionScanner.STRING;
	public static final String COMMENT 	= SQLPartitionScanner.COMMENT;
	public static final String PROMPT 	= SQLPartitionScanner.PROMPT;
	public static final String DEFAULT 	= "DEFAULT";


	public static TextAttribute getAttribute(String sqlKey) {
		if(DDL.equals(sqlKey)) {
			return new TextAttribute(FontFactory.DDL_COLOR,null,SWT.BOLD);
		} else if(STRING.equals(sqlKey)) {
			return new TextAttribute(FontFactory.QUOTE_COLOR);
		} else if(PROMPT.equals(sqlKey)) {
			return new TextAttribute(FontFactory.PROMPT_COLOR);
		} else if(COMMENT.equals(sqlKey)) {
			return new TextAttribute(FontFactory.COMMENT_COLOR);
		} else if(DML.equals(sqlKey)) {
			return new TextAttribute(FontFactory.DML_COLOR,null,SWT.BOLD);
		} else if(FUNC.equals(sqlKey)) {
			return new TextAttribute(FontFactory.FUNC_COLOR,null,SWT.ITALIC);
		} else if(VAR.equals(sqlKey)) {
			return new TextAttribute(FontFactory.PLVAR_COLOR,null,SWT.ITALIC);
		} else if(DATATYPE.equals(sqlKey)) {
			return new TextAttribute(FontFactory.DATATYPE_COLOR,null,SWT.BOLD);
		} else if(SPECIAL.equals(sqlKey)) {
			return new TextAttribute(FontFactory.QUOTE_COLOR,null,SWT.BOLD);
		}
		return new TextAttribute(FontFactory.BLACK);
	}
}
