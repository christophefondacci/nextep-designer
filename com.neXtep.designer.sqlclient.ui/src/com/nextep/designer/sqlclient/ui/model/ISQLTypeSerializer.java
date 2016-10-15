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
package com.nextep.designer.sqlclient.ui.model;

import java.text.ParseException;

/**
 * A SQL type serializer can transform a string value back to its original class and can serialize a
 * value instance into a string.<br>
 * For example, a date deserializer can transform a string to a date, a number deserializer can
 * instantiate the appropriate number implementation from a string, etc.
 * 
 * @author Christophe Fondacci
 */
public interface ISQLTypeSerializer {

	/**
	 * Deserializes the specified String in a dedicated class instance.
	 * 
	 * @param valueString value to deserialized, expressed as a string
	 * @return an deserialized instance
	 */
	Object deserialize(String valueString) throws ParseException;

	String serialize(Object value);
}
