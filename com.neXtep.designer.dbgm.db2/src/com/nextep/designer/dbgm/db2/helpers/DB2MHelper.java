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
package com.nextep.designer.dbgm.db2.helpers;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bruno Gautier
 */
public final class DB2MHelper {

	public final static BigDecimal DATATYPE_SMALLINT_MAX_VALUE = new BigDecimal("32767"); //$NON-NLS-1$
	public final static BigDecimal DATATYPE_INTEGER_MAX_VALUE = new BigDecimal("2147483647"); //$NON-NLS-1$
	public final static BigDecimal DATATYPE_BIGINT_MAX_VALUE = new BigDecimal("9223372036854775807"); //$NON-NLS-1$

	/**
	 * Checks if the specified <code>BigDecimal</code> value is a maximum value of a
	 * <code>DECIMAL(x,0)</code> data type.<br>
	 * This method will not consider a value smaller than the <code>BIGINT</code> data type maximum
	 * value as a potential <code>DECIMAL</code> maximum value.
	 * 
	 * @param maxValue
	 * @return
	 */
	public static boolean isMaximumDecimalValue(BigDecimal maxValue) {
		if (maxValue != null && maxValue.compareTo(DATATYPE_BIGINT_MAX_VALUE) > 0) {
			Pattern p = Pattern.compile("[^9]+"); //$NON-NLS-1$
			Matcher m = p.matcher(maxValue.toString());
			return !m.find();
		}

		return false;
	}

}
