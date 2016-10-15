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
package com.nextep.designer.dbgm.helpers;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.nextep.datadesigner.exception.ErrorException;

public final class ConversionHelper {

	/*
	 * TODO [BGA] This date format should be retrieved from the preferences
	 */
	private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final NumberFormat NUMBER_FORMAT = NumberFormat
			.getNumberInstance(Locale.ENGLISH);

	private ConversionHelper() {
	}

	public static Date getDate(Object value) {
		Date d;
		if (value == null) {
			d = null;
		} else if (value instanceof Date) {
			d = (Date) value;
		} else {
			try {
				String strDate = value.toString();
				if (strDate.length() > 19) {
					strDate = strDate.substring(0, 19);
				}
				d = DATE_FORMAT.parse(strDate);
			} catch (ParseException e) {
				throw new ErrorException("Problems while parsing the date " + value + ": "
						+ e.getMessage(), e);
			}
		}
		return d;
	}

	public static Number getNumber(Object value) {
		Number n;
		if (value == null) {
			n = null;
		} else if (value instanceof Number) {
			n = (Number) value;
		} else {
			try {
				n = NUMBER_FORMAT.parse(value.toString());
			} catch (ParseException e) {
				throw new ErrorException("Unable to parse number " + value.toString() + ": "
						+ e.getMessage(), e);
			}
		}
		return n;
	}
}
