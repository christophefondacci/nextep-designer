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
package com.neXtep.shared.model;

/**
 * @author Christophe Fondacci
 *
 */
public interface ITagNames {

	/** Delivery related tags */
	public static final String DELIVERY			="delivery";
	public static final String DELIVERY_REF_UID	="refUID";
	public static final String DELV_ATTR_ADMIN	="admin";
	public static final String DELV_ATTR_FIRST	="first";
	public static final String DELIVERY_ITEM	="deliveryItem";
	public static final String DELV_DEPENDENCIES="dependencies";
	public static final String DELV_REQUIREMENTS="requirements";
	public static final String DELV_DEP_MODULE	="module";
	public static final String DELV_DBVENDOR	="dbVendor";
	/** Release related tags */
	public static final String RELEASE			="release";
	public static final String REL_ATTR_MODE	="mode";
	public static final String REL_VAL_MODE_RANGE="range";
	public static final String REL_VAL_MODE_STRICT="strict";
	public static final String FROM_RELEASE		="initial";
	public static final String CREATE_RELEASE	="create";
	public static final String TARGET_RELEASE	="target";
	public static final String REL_ATTR_MAJOR	="major";
	public static final String REL_ATTR_MINOR	="minor";
	public static final String REL_ATTR_ITERATION="iteration";
	public static final String REL_ATTR_PATCH	="patch";
	public static final String REL_ATTR_REVISION="revision";
	
	/** File related tags */
	public static final String PATH				="path";
	/** Check related tags */
	public static final String CHECK_RELEASE	="checkRelease";
	public static final String CHECK_OBJ		="object";
	
	/** Control tags */
	public static final String ATTR_NAME		="name";
	public static final String ATTR_ARTEFACT	="artefact";
	public static final String ATTR_ARTEFACT_TYPE="artefactType";
	public static final String ATTR_ARTEFACT_VENDOR="vendor";
	public static final String CATEGORY			="category";
	public static final String ATTR_TYPE		="type";
}
