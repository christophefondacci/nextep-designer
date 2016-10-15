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
package com.nextep.designer.sqlclient.ui.helpers;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.eclipse.jface.preference.IPreferenceStore;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;
import com.nextep.designer.sqlclient.ui.preferences.PreferenceConstants;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public final class ExportHelper {

	private ExportHelper() {
	}

	private static DateFormat dateFormat = null;
	private static String separator, encloser, escaper, decimalSeparator;

	public static void initialize() {
		final IPreferenceStore prefs = SQLClientPlugin.getDefault().getPreferenceStore();
		final String dateFormatPattern = prefs.getString(PreferenceConstants.EXPORT_DATE_FORMAT);
		dateFormat = new SimpleDateFormat(dateFormatPattern);
		separator = prefs.getString(PreferenceConstants.EXPORT_SEPARATOR);
		encloser = prefs.getString(PreferenceConstants.EXPORT_ENCLOSER);
		escaper = prefs.getString(PreferenceConstants.EXPORT_ESCAPER);
		decimalSeparator = prefs.getString(PreferenceConstants.EXPORT_DECIMAL_SEPARATOR);
	}

	public static String buildCSVLine(ISQLRowResult row) {
		final StringBuilder buf = new StringBuilder(200);
		final List<Object> values = row.getValues();
		String separator = ""; //$NON-NLS-1$
		for (Object value : values) {
			buf.append(separator);
			if (value instanceof Time) {
				buf.append(enclose(((Time) value).toString()));
			} else if (value instanceof Date) {
				buf.append(enclose(formatDate((Date) value)));
			} else if (value instanceof Number) {
				buf.append(formatNumber((Number) value));
			} else if (value != null) {
				buf.append(enclose((value.toString())));
			}
			separator = getCSVSeparator();
		}
		buf.append(getLineTermination());
		return buf.toString();
	}

	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}

	public static DateFormat getDateFormat() {
		return dateFormat;
	}

	public static String getCSVSeparator() {
		return separator;
	}

	public static String getCSVEncloser() {
		return encloser;
	}

	public static String enclose(String s) {
		return encloser + escapeString(s) + encloser;
	}

	public static String getLineTermination() {
		return "\n"; //$NON-NLS-1$
	}

	public static String formatNumber(Number number) {
		String s = number.toString(); // numberFormat.format(number);
		if (!".".equals(decimalSeparator)) { //$NON-NLS-1$
			s = s.replace(".", decimalSeparator); //$NON-NLS-1$
			return enclose(s);
		}
		return s;
	}

	public static String escapeString(String s) {
		if (!"".equals(escaper)) { //$NON-NLS-1$
			s = s.replace(escaper, escaper + escaper).replace(encloser, escaper + encloser);
		} else if (!"".equals(encloser)) { //$NON-NLS-1$
			s = s.replace(encloser, encloser + encloser);
		}
		return s;
	}

}
