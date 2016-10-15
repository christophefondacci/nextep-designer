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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.mysql.ui.impl;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.swt.custom.CCombo;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.mysql.services.IMySqlModelService;

/**
 * @author Christophe Fondacci
 */
public final class MySQLConverterFactory {

	private MySQLConverterFactory() {
	}

	/**
	 * Creates a new converter able to transform a charset code into a charset name
	 * 
	 * @return the corresponding new {@link IConverter}
	 */
	public static IConverter createCodeToNameCharsetConverter() {
		return new IConverter() {

			@Override
			public Object convert(Object fromObject) {
				final IMySqlModelService modelService = CorePlugin
						.getService(IMySqlModelService.class);
				return modelService.getCharsetName((String) fromObject);
			}

			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object getToType() {
				return String.class;
			}
		};
	}

	/**
	 * Creates a new converter able to transform a charset name into a charset code
	 * 
	 * @return the corresponding new {@link IConverter}
	 */
	public static IConverter createNameToCodeCharsetConverter(final CCombo collationCombo) {
		return new IConverter() {

			@Override
			public Object convert(Object fromObject) {
				final IMySqlModelService modelService = CorePlugin
						.getService(IMySqlModelService.class);
				final String charset = modelService.getCharsetFromName((String) fromObject);
				if (charset != null && collationCombo != null) {
					final String collation = modelService.getDefaultCollation(charset);
					if (collation != null) {
						collationCombo.removeAll();
						collationCombo.add(collation);
					}
				}
				return charset;
			}

			@Override
			public Object getFromType() {
				return String.class;
			}

			@Override
			public Object getToType() {
				return String.class;
			}
		};
	}
}
