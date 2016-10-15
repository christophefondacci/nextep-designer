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
/**
 *
 */
package com.nextep.datadesigner.sqlgen.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.model.IDropStrategy;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.ISQLParser;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class GeneratorFactory {

	private static final Log LOGGER = LogFactory.getLog(GeneratorFactory.class);

	public static final String GENERATOR_EXTENSION_ID = "com.neXtep.designer.sqlgen.sqlGenerator"; //$NON-NLS-1$
	public static final String PARSER_EXTENSION_ID = "com.neXtep.designer.sqlgen.sqlParser"; //$NON-NLS-1$
	public static final String ATTR_VENDOR = "databaseVendor"; //$NON-NLS-1$

	private static Map<IElementType, Map<String, IDropStrategy>> dropStrategiesCache = new HashMap<IElementType, Map<String, IDropStrategy>>();
	private static Map<DBVendor, ISQLParser> parsers = new HashMap<DBVendor, ISQLParser>();
	private static List<DBVendor> failedParserVendors = new ArrayList<DBVendor>();

	/**
	 * Retrieves the correct SQL generator which is able to generate the specified model object in
	 * SQL.
	 * 
	 * @param model the model to generate
	 * @return the correct generator
	 */
	public static ISQLGenerator getGenerator(ITypedObject model, DBVendor vendor) {
		return getGenerator(model.getType(), vendor);
	}

	/**
	 * Retrieves the correct SQL generator which is able to generate the specified model object in
	 * SQL.
	 * 
	 * @param type type of the item to generate
	 * @return the appropriate generator
	 */
	public static ISQLGenerator getGenerator(IElementType type, DBVendor vendor) {
		// Loading extensiongs
		Collection<IConfigurationElement> conf = Designer.getInstance().getExtensions(
				GENERATOR_EXTENSION_ID, "sqlGenerator", "typeId", type.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		if (conf == null || conf.isEmpty()) {
			LOGGER.debug("No generator found for type <" + type.getId() + ">, will not generate.");
			// throw new ErrorException("No generator found for type <" + type.getId() + ">");
			return new NullGenerator();
		} else {
			try {
				ISQLGenerator generator = null;
				// First looking for vendor-specific generator
				for (IConfigurationElement elt : conf) {
					final String extensionVendor = elt.getAttribute(ATTR_VENDOR);
					if (vendor.name().equals(extensionVendor)) {
						generator = (ISQLGenerator) elt.createExecutableExtension("class"); //$NON-NLS-1$
						generator.setVendor(vendor);
						return generator;
					}
				}
				// Falling back here if no vendor specific extension found, looking for generic
				// support
				for (IConfigurationElement elt : conf) {
					final String extensionVendor = elt.getAttribute(ATTR_VENDOR);
					if (extensionVendor == null || "".equals(extensionVendor)) { //$NON-NLS-1$
						generator = (ISQLGenerator) elt.createExecutableExtension("class"); //$NON-NLS-1$
						generator.setVendor(vendor);
					}
				}

				// If we do not have a vendor-specific generator BUT we have a generic generator, we
				// use it
				if (generator != null) {
					return generator;
				} else {
					// We fall here if we have no generator for the current vendor AND no generic
					// generator
					LOGGER.info("No generator found for type <" + type.getId() + "> and vendor "
							+ vendor.toString() + ", will not generate.");
					return new NullGenerator();
				}
			} catch (CoreException e) {
				LOGGER.error("Error while instantiating generator for type <" + type.getId() + ">"); //$NON-NLS-2$
				throw new ErrorException(e);
			}
		}
	}

	/**
	 * @return all element types which have a SQL generator defined.
	 */
	public static List<IElementType> getGeneratedTypes() {
		Set<IElementType> generatedTypes = new HashSet<IElementType>();
		Collection<IConfigurationElement> contributions = Designer.getInstance().getExtensions(
				GENERATOR_EXTENSION_ID, "sqlGenerator", "typeId", "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (IConfigurationElement elt : contributions) {
			String typeId = elt.getAttribute("typeId"); //$NON-NLS-1$
			generatedTypes.add(IElementType.getInstance(typeId));
		}
		List<IElementType> types = new ArrayList<IElementType>(generatedTypes);
		Collections.sort(types, NameComparator.getInstance());
		return types;
	}

	/**
	 * Retrieves the appropriate parser for the given database vendor.
	 * 
	 * @param vendor vendor for which to retrieve the SQL parser
	 * @return the contributed SQL parser.
	 */
	public static ISQLParser getSQLParser(DBVendor vendor) {
		ISQLParser parser = parsers.get(vendor);
		if (null == parser) {
			if (!failedParserVendors.contains(vendor)) {
				IConfigurationElement elt = Designer.getInstance().getExtension(
						PARSER_EXTENSION_ID, "databaseVendor", vendor.name()); //$NON-NLS-1$
				if (elt != null) {
					try {
						parser = (ISQLParser) elt.createExecutableExtension("class"); //$NON-NLS-1$
						parsers.put(vendor, parser);
					} catch (CoreException e) {
						LOGGER.error("Error while instantiating SQL parser for vendor <"
								+ vendor.name() + ">"); //$NON-NLS-1$
						throw new ErrorException(e);
					}
				}
			}
		}
		if (null == parser) {
			if (vendor != DBVendor.JDBC) {
				LOGGER.warn("No SQL parser found for vendor <" + vendor.name() + ">, trying JDBC");
				if (!failedParserVendors.contains(vendor))
					failedParserVendors.add(vendor);
				return getSQLParser(DBVendor.JDBC);
			}
			throw new ErrorException("No default JDBC SQL parser found");
		}
		return parser;
	}

}
