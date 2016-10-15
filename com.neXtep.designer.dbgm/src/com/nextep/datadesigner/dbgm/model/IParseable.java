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
package com.nextep.datadesigner.dbgm.model;

import com.nextep.datadesigner.model.ITypedObject;

/**
 * This interface represents a parseable element. A parseable element generally has a plain text
 * attribute which can be parsed to fill other convenience methods.<br>
 * For example, a database package is roughly a text string. In the repository it is a
 * {@link IParseable} object which means that a call to {@link IParseable#parse()} gives you access
 * to methods which can list the contained procedures, inner variables, extract heading declaration,
 * etc. All this information is *generated* from the sql text string after a parse.
 * 
 * @author Christophe
 */
public interface IParseable extends ITypedObject, ISqlBased {

	/**
	 * Parses the source code of the parsable element to generate parse data information.<br>
	 * This is a convenience method which should call parse(String) with the default content to
	 * parse.
	 */
	// public abstract void parse();

	/**
	 * Parses the specified source code of this parseable element. In most case, prefer using
	 * {@link IParseable#parse()} method except when you need to parse an unsaved state of this
	 * element (like for completion proposals).
	 * 
	 * @param contentsToParse String to parse
	 */
	// public abstract void parse(String contentsToParse);
	/**
	 * A flag indicating if this package is parsed
	 * 
	 * @return <code>true</code> if this package has been parsed, else <code>false</code>.
	 */
	public abstract boolean isParsed();

	/**
	 * Sets the parsed status flag
	 * 
	 * @param parsed new parse status (parsed / not parsed)
	 */
	public abstract void setParsed(boolean parsed);

	/**
	 * The parse data of the last parse operation
	 * 
	 * @return the parse data
	 */
	public abstract IParseData getParseData();

	/**
	 * Sets parse information
	 * 
	 * @param parseData new parse information
	 */
	public abstract void setParseData(IParseData parseData);

}
