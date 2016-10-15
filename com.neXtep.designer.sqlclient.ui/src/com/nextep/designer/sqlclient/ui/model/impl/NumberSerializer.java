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
package com.nextep.designer.sqlclient.ui.model.impl;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import com.nextep.designer.sqlclient.ui.model.ISQLTypeSerializer;

public class NumberSerializer implements ISQLTypeSerializer {

	@Override
	public Object deserialize(String valueString) throws ParseException {
		if (valueString == null || "".equals(valueString.trim())) { //$NON-NLS-1$
			return null;
		}
		return NumberFormat.getNumberInstance(Locale.ENGLISH).parse(valueString);
	}

	@Override
	public String serialize(Object value) {
		return value == null ? "" : value.toString();
	}

}
