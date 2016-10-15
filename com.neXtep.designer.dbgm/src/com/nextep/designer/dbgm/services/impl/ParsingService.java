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
package com.nextep.designer.dbgm.services.impl;

import java.text.MessageFormat;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.DBGMMessages;
import com.nextep.datadesigner.dbgm.impl.ParseData;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.model.ITypedSqlParser;
import com.nextep.designer.dbgm.services.IParsingService;
import com.nextep.designer.util.Assert;

public class ParsingService implements IParsingService {

	private static final String EXTENSION_ID_SQL_TYPED_PARSER = "com.neXtep.designer.dbgm.sqlTypedParser"; //$NON-NLS-1$
	private final static Log log = LogFactory.getLog(ParsingService.class);

	private ITypedSqlParser getParser(IElementType type, DBVendor vendor) {
		if (type != null) {
			Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(
					EXTENSION_ID_SQL_TYPED_PARSER, "typeId", type.getId()); //$NON-NLS-1$
			if (elts != null) {
				for (IConfigurationElement elt : elts) {
					try {
						final String parserVendor = elt.getAttribute("vendor"); //$NON-NLS-1$
						if ((vendor == null && ("".equals(parserVendor) || parserVendor == null)) //$NON-NLS-1$
								|| (vendor != null && vendor.name().equals(parserVendor))) {
							ITypedSqlParser parser = (ITypedSqlParser) elt
									.createExecutableExtension("class"); //$NON-NLS-1$
							return parser;
						}
					} catch (CoreException e) {
						throw new ErrorException(e);
					}
				}
			} else {
				log.warn(MessageFormat.format(
						DBGMMessages.getString("service.parsing.noParser"), type.getName())); //$NON-NLS-1$
			}
		}
		return null;
	}

	private ITypedSqlParser getParser(IElementType type) {
		ITypedSqlParser parser = getParser(type, DBGMHelper.getCurrentVendor());
		if (parser == null) {
			parser = getParser(type, null);
		}
		return parser;
	}

	@Override
	public String getRenamedSql(IElementType sqlType, String sqlToRename, String newName) {
		final ITypedSqlParser parser = getParser(sqlType);
		String renamedSql = sqlToRename;
		if (parser != null) {
			renamedSql = parser.rename(sqlToRename, newName);
		}
		return renamedSql;
	}

	@Override
	public void parse(IParseable p) {
		parse(p, p.getSql());
	}

	@Override
	public void parse(IParseable p, String parseSource) {
		Assert.notNull(p, DBGMMessages.getString("service.parsing.nullParseable")); //$NON-NLS-1$
		final ITypedSqlParser parser = getParser(p.getType());
		if (parser != null) {

			IParseData parseData = null;
			// A parse problem should not be fired to the world
			try {
				parseData = parser.parse(p, parseSource);
			} catch (RuntimeException e) {
				log.error(
						MessageFormat.format(
								DBGMMessages.getString("service.parsing.parseException"), NameHelper.getQualifiedName(p), e.getMessage()), e);
			}
			// Ensuring a non-null parse data
			if (parseData == null) {
				parseData = new ParseData();
			}
			p.setParsed(true);
			p.setParseData(parseData);
		}
	}

	@Override
	public String parseName(IParseable parseable) {
		return parseName(parseable.getType(), parseable.getSql());
	}

	@Override
	public String parseName(IElementType type, String sql) {
		final ITypedSqlParser parser = getParser(type);
		if (parser != null) {
			return parser.parseName(sql);
		}
		return null;
	}

	@Override
	public void rename(IParseable parseable, String newName) {
		final ITypedSqlParser parser = getParser(parseable.getType());
		if (parser != null) {
			parser.rename(parseable, newName);
			// Explicit save as SQL-based elements do not fire a save unless the user
			// explicitly use the SAVE action
			CorePlugin.getPersistenceAccessor().save(parseable);
		}
	}

}
