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
package com.nextep.designer.sqlclient.ui.jface;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.eclipse.jface.viewers.ICellEditorValidator;
import com.nextep.designer.sqlclient.ui.helpers.ExportHelper;

/**
 * This validator validates that a date text entered by a user is parseable according to the date
 * format set in properties. If not, the parse error will be returned.
 * 
 * @author Christophe Fondacci
 */
public class DateCellValidator implements ICellEditorValidator {

	@Override
	public String isValid(Object value) {
		if (value instanceof String) {
			final DateFormat dateFormat = ExportHelper.getDateFormat();
			try {
				dateFormat.parse((String) value);
			} catch (ParseException e) {
				String pattern = "";
				if (dateFormat instanceof SimpleDateFormat) {
					pattern = ((SimpleDateFormat) dateFormat).toPattern();
				}
				return "Invalid date format (" + pattern + ") : " + e.getMessage();
			}
		}
		return null;
	}

}
