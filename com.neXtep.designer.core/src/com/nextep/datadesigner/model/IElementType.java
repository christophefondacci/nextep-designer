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
package com.nextep.datadesigner.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.impl.NamedObjectHelper;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IResourceLocator;
import com.nextep.designer.core.model.ResourceConstants;
import com.nextep.designer.core.preferences.DesignerCoreConstants;
import com.nextep.designer.core.services.ICoreService;

/**
 * Defines the type of an element. This class is used by {@link ITypedObject} objects which defines
 * typed object.<br>
 * <br> {@link IElementType} is the root of the neXtep extension mechanism as the core plugin only knows
 * how to manage typed objects. Typed objects user interfaces are defined in the core.ui plugin
 * "com.neXtep.designer.ui". Every specific behaviours of typed objects are defined in contributors
 * plugin.<br> {@link IElementType} should be defined through the "elementType" extension point and
 * should never be instantiated directly.
 * 
 * @author Christophe Fondacci
 */
public class IElementType implements INamedObject {

	private static final Log log = LogFactory.getLog(IElementType.class);
	private static Map<String, IElementType> definedTypes = new HashMap<String, IElementType>();

	/** A static collection of images to dispose */
	private NamedObjectHelper nameHelper = null;
	private String categoryTitle = null;
	private IResourceLocator categoryIcon = null;
	private IResourceLocator icon = null;
	private IResourceLocator tinyIcon = null;
	private IResourceLocator tinyCategoryIcon;
	private String id;
	private Class<? extends ITypedObject> typeInterface;
	private Map<DBVendor, String> dbTypes;

	// public static IElementType UNKNOWN_TYPE = new
	// IElementType("Unknown type","Unknown type","Unresolved items",ImageFactory.ICON_ERROR);
	/**
	 * Retrieves the instance of a given element type, given its type identifier. Element types are
	 * defined dynamically from plugin contributions.
	 * 
	 * @param typeId string identifier of the type to retrieve
	 * @return the element type
	 */
	@SuppressWarnings("unchecked")
	public static IElementType getInstance(String typeId) {
		// We first have a look in our type cache
		IElementType type = definedTypes.get(typeId);
		// If no type found we look for existing extensions
		if (type == null) {
			final ICoreService coreService = CorePlugin.getService(ICoreService.class);
			try {
				IConfigurationElement conf = Designer.getInstance().getExtension(
						"com.neXtep.designer.core.elementType", "Id", typeId); //$NON-NLS-1$ //$NON-NLS-2$
				final String name = conf.getAttribute("name"); //$NON-NLS-1$
				final String desc = conf.getAttribute("description"); //$NON-NLS-1$
				final String categ = conf.getAttribute("categoryLabel"); //$NON-NLS-1$
				final String categIconPath = conf.getAttribute("categoryIcon"); //$NON-NLS-1$
				final String typeInterfaceName = conf.getAttribute("typeInterface"); //$NON-NLS-1$
				final String iconPath = conf.getAttribute("icon"); //$NON-NLS-1$
				final String tinyIconPath = conf.getAttribute("icon_16x16"); //$NON-NLS-1$
				final String tinyCategIconPath = conf.getAttribute("categoryIcon_16x16"); //$NON-NLS-1$

				final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
				// Loading icon images
				IResourceLocator categIcon = null;
				if (categIconPath != null && !"".equals(categIconPath)) { //$NON-NLS-1$
					categIcon = coreFactory.createImageLocator(conf.getContributor().getName(),
							categIconPath);
				}
				IResourceLocator icon = null;
				if (iconPath != null && !"".equals(iconPath)) { //$NON-NLS-1$
					icon = coreFactory
							.createImageLocator(conf.getContributor().getName(), iconPath);
				}
				IResourceLocator tinyIcon = null;
				if (tinyIconPath != null && !"".equals(tinyIconPath)) { //$NON-NLS-1$
					tinyIcon = coreFactory.createImageLocator(conf.getContributor().getName(),
							tinyIconPath);
				}
				IResourceLocator tinyCategIcon = null;
				if (tinyCategIconPath != null && !"".equals(tinyCategIconPath)) { //$NON-NLS-1$
					tinyCategIcon = coreFactory.createImageLocator(conf.getContributor().getName(),
							tinyCategIconPath);
				}
				// Loading interface type descriptor
				Class<? extends ITypedObject> typeInterface = null;
				if (typeInterfaceName != null && !"".equals(typeInterfaceName)) { //$NON-NLS-1$
					typeInterface = (Class<? extends ITypedObject>) Class
							.forName(typeInterfaceName);
				}
				type = new IElementType(typeId, name, desc, icon, categ, categIcon, typeInterface,
						tinyIcon, tinyCategIcon);
				addVendorMappings(conf, type);
				log.info(MessageFormat.format(CoreMessages.getString("typeLoaded"), type.getName())); //$NON-NLS-1$
			} catch (Exception e) {
				log.error(MessageFormat.format(CoreMessages.getString("typeNotFound"), typeId), e); //$NON-NLS-1$
				final IResourceLocator errorLocator = coreService
						.getResource(ResourceConstants.ICON_ERROR);
				type = new IElementType(
						typeId,
						MessageFormat.format(CoreMessages.getString("typeUnknownLabel"), typeId), CoreMessages.getString("elementType.unknown"), errorLocator, CoreMessages.getString("typeUnknwonCategoryLabel"), errorLocator, null, errorLocator, errorLocator); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			definedTypes.put(typeId, type);
		}
		return type;
	}

	/**
	 * Defines the type vendor mappings for an element type. Called immediately after type creation.
	 * 
	 * @param conf configuration element which defines the neXtep type
	 * @param type corresponding neXtep type
	 */
	private static void addVendorMappings(IConfigurationElement conf, IElementType type) {
		IConfigurationElement[] elts = conf.getChildren("vendorMapping"); //$NON-NLS-1$
		for (IConfigurationElement elt : elts) {
			String vendor = elt.getAttribute("dbVendor"); //$NON-NLS-1$
			String typeName = elt.getAttribute("typeName"); //$NON-NLS-1$
			type.setDatabaseType(DBVendor.valueOf(vendor), typeName);
		}
	}

	/**
	 * Provides type-compatibility with an enum class
	 * 
	 * @return all defined IElementType
	 */
	public static List<IElementType> values() {
		IConfigurationElement[] contributions = Platform.getExtensionRegistry()
				.getConfigurationElementsFor("com.neXtep.designer.core.elementType"); //$NON-NLS-1$
		for (int i = 0; i < contributions.length; i++) {
			if ("elementType".equals(contributions[i].getName())) { //$NON-NLS-1$
				getInstance(contributions[i].getAttribute("Id")); //$NON-NLS-1$
			}
		}
		// Workaround to ensure VERSIONABLE element type is the first
		List<IElementType> typeList = new ArrayList<IElementType>(definedTypes.values());
		typeList.add(0, IElementType.getInstance("VERSIONABLE")); //$NON-NLS-1$
		return typeList;
	}

	IElementType(String id, String name, String description, IResourceLocator icon,
			String categoryTitle, IResourceLocator categoryIcon,
			Class<? extends ITypedObject> typeInterface, IResourceLocator tinyIcon,
			IResourceLocator tinyCategIcon) {
		this.id = id;
		this.typeInterface = typeInterface;
		this.nameHelper = new NamedObjectHelper(name, description);
		this.categoryTitle = categoryTitle;
		this.categoryIcon = categoryIcon;
		this.icon = icon;
		this.tinyIcon = tinyIcon != null ? tinyIcon : icon;
		this.tinyCategoryIcon = tinyCategIcon != null ? tinyCategIcon : categoryIcon;
		this.dbTypes = new HashMap<DBVendor, String>();
	}

	/**
	 * @return the type description
	 * @see com.nextep.datadesigner.model.INamedObject#getDescription()
	 */
	@Override
	public String getDescription() {
		return nameHelper.getDescription();
	}

	/**
	 * @return the type name
	 * @see com.nextep.datadesigner.model.INamedObject#getName()
	 */
	@Override
	public String getName() {
		return nameHelper.getName();
	}

	/**
	 * Sets this type's description
	 * 
	 * @see com.nextep.datadesigner.model.INamedObject#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		nameHelper.setDescription(description);

	}

	/**
	 * Sets this type's name
	 * 
	 * @see com.nextep.datadesigner.model.INamedObject#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		nameHelper.setName(name);
	}

	/**
	 * @return the type unique identifier
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the title of the category to which this type belong
	 */
	public String getCategoryTitle() {
		return categoryTitle;
	}

	/**
	 * @return the icon of thi element type
	 */
	public IResourceLocator getIcon() {
		if (Designer.getInstance().getPropertyBool(DesignerCoreConstants.ICON_TINY)) {
			return tinyIcon;
		} else {
			return icon;
		}
	}

	public IResourceLocator getTinyIcon() {
		return tinyIcon;
	}

	/**
	 * @return the icon of the corresponding category
	 */
	public IResourceLocator getCategoryIcon() {
		if (Designer.getInstance().getPropertyBool(DesignerCoreConstants.ICON_TINY)) {
			return tinyCategoryIcon;
		} else {
			return categoryIcon;
		}
	}

	/**
	 * @return
	 * @deprecated use getId instead
	 */
	public String getCode() {
		return getId();
	}

	/**
	 * The abstract interface which represents this type.
	 * 
	 * @return the interface which can abstract this type implementation
	 */
	public Class<? extends ITypedObject> getInterface() {
		return typeInterface;
	}

	public String toString() {
		return id;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof String) {
			return id.equals(obj);
		} else if (obj instanceof IElementType) {
			return getId().equals(((IElementType) obj).getId());
		}
		return false;
	}

	/**
	 * Retrieves the database-specific type name of this neXtep type. Type names are defined through
	 * extensions. If no database typename is provided, the neXtep type id will be returned.
	 * 
	 * @param vendor database vendor
	 * @return the database-specific type name
	 */
	public String getDatabaseType(DBVendor vendor) {
		final String type = dbTypes.get(vendor);
		if (type == null || "".equals(type.trim())) { //$NON-NLS-1$
			return getId();
		}
		return type.trim();
	}

	/**
	 * Defines the database-specific type name of this element. Defined through extensions
	 * <b>ONLY</b>. There should only be one definition per vendor, otherwise an exception will be
	 * thrown.
	 * 
	 * @param vendor database vendor
	 * @param typeName name of this neXtep type for this vendor.
	 */
	public void setDatabaseType(DBVendor vendor, String typeName) {
		if (dbTypes.get(vendor) != null) {
			throw new IllegalArgumentException(
					CoreMessages.getString("elementType.tooManyDefinitions")); //$NON-NLS-1$
		}
		dbTypes.put(vendor, typeName);
	}
}
