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
package com.nextep.designer.core.model;

import java.util.List;

/**
 * This interface describes a marker. A marker is a typed information related to an object of the
 * workspace, optionally having attributes such as location, and optionally capable of providing
 * actions.
 * 
 * @author Christophe
 */
public interface IMarker {

	final String ATTR_LINE = "line"; //$NON-NLS-1$
	final String ATTR_COL = "col"; //$NON-NLS-1$
	final String ATTR_EXTERNAL_TYPE = "type"; //$NON-NLS-1$
	final String ATTR_CONTEXT = "context"; //$NON-NLS-1$

	/**
	 * @return the kind of marker
	 */
	MarkerType getMarkerType();

	/**
	 * @return the message to be displayed when listing this marker
	 */
	String getMessage();

	/**
	 * @param attr attribute name
	 * @return a custom attribute of this marker
	 */
	Object getAttribute(String attr);

	/**
	 * Defines an attribute for this marker
	 * 
	 * @param attrName attribute name
	 * @param value attribute value
	 */
	void setAttribute(String attrName, Object value);

	/**
	 * @return the object for which this marker has been defined
	 */
	Object getRelatedObject();

	/**
	 * Defines the related object of this marker
	 * 
	 * @param o new related object
	 */
	void setRelatedObject(Object o);

	/**
	 * Retrieves the list of all available hints which could solve this problem.
	 * 
	 * @return available {@link IMarkerHint} which could solve this problem
	 */
	List<IMarkerHint> getAvailableHints();

	/**
	 * Adds the specified hint as an option to fix this problem
	 * 
	 * @param hint a {@link IMarkerHint} which could be applied on the object to fix this marker
	 */
	void addAvailableHint(IMarkerHint hint);

	/**
	 * Retrieves the currently selected hint resolving this problem
	 * 
	 * @return the current {@link IMarkerHint}
	 */
	IMarkerHint getSelectedHint();

	/**
	 * Defines the selected hint. The selected hint is the one that will get executed when the user
	 * 
	 * @param hint
	 */
	void setSelectedHint(IMarkerHint hint);

	/**
	 * Defines the icon of this marker. The icon is used by the problem solver.
	 * 
	 * @param icon marker large (32x32) icon
	 */
	void setIcon(IResourceLocator icon);

	/**
	 * Retrieves this marker's icon (may return null).
	 * 
	 * @return a 32x32 marker icon
	 */
	IResourceLocator getIcon();
}
