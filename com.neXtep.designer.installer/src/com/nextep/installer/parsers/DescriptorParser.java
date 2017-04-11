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
package com.nextep.installer.parsers;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.neXtep.shared.model.ArtefactType;
import com.neXtep.shared.model.ITagNames;
import com.nextep.installer.exception.ParseException;
import com.nextep.installer.factories.InstallerFactory;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.IDatabaseObjectCheck;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IRelease;
import com.nextep.installer.model.IRequiredDelivery;
import com.nextep.installer.model.impl.Artefact;
import com.nextep.installer.model.impl.DBObject;
import com.nextep.installer.model.impl.Delivery;
import com.nextep.installer.model.impl.Release;
import com.nextep.installer.model.impl.RequiredDelivery;

/**
 * This class builds a IDelivery object from a neXtep XML delivery file.
 * 
 * @author Christophe Fondacci
 */
public class DescriptorParser {

	public static IDelivery buildDescriptor(String path, File file, DBVendor vendor)
			throws ParseException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			return parseDocument(path, doc, vendor);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return null;
	}

	private static IDelivery parseDocument(String path, Document doc, DBVendor vendor)
			throws ParseException {
		// Retrieving root element
		Element elt = doc.getDocumentElement();
		boolean isAdmin = "true".equals(elt.getAttribute(ITagNames.DELV_ATTR_ADMIN)); //$NON-NLS-1$
		// Building delivery
		IDelivery delivery = new Delivery(isAdmin);
		// Filling name and ref
		try {
			delivery.setName(elt.getAttribute(ITagNames.ATTR_NAME));
			delivery.setRefUID(Long.valueOf(elt.getAttribute(ITagNames.DELIVERY_REF_UID))
					.longValue());
			// Setting database vendor, preserving 1.0.0 compatibility
			String parsedVendor = elt.getAttribute(ITagNames.DELV_DBVENDOR);
			if (!"".equals(parsedVendor) && parsedVendor != null) { //$NON-NLS-1$
				delivery.setDBVendor(DBVendor.valueOf(parsedVendor));
			}
			// First release ?
			String first = elt.getAttribute(ITagNames.DELV_ATTR_FIRST);
			if (first != null && !"".equals(first)) { //$NON-NLS-1$
				delivery.setFirstRelease(new Boolean(first).booleanValue());
			}
		} catch (NumberFormatException e) {
			throw new ParseException(e);
		}
		// Retrieving release node
		NodeList releaseElts = elt.getElementsByTagName(ITagNames.RELEASE);
		if (releaseElts == null || releaseElts.getLength() == 0) {
			throw new ParseException("Unable to find release information.");
		}
		Element releaseElt = (Element) releaseElts.item(0);
		fillReleaseInfo(delivery, releaseElt);
		// Filling dependencies
		NodeList depElts = elt.getElementsByTagName(ITagNames.DELV_DEPENDENCIES);
		if (depElts != null && depElts.getLength() > 0) {
			fillDependencies(path, delivery, (Element) depElts.item(0));
		}
		// Filling requirements
		NodeList reqElts = elt.getElementsByTagName(ITagNames.DELV_REQUIREMENTS);
		if (reqElts != null && reqElts.getLength() > 0) {
			fillRequirements(path, delivery, (Element) reqElts.item(0));
		}
		// Filling defined artefacts
		fillCategories(path, delivery, elt);
		// Filling checks
		fillChecks(delivery, elt, vendor);
		return delivery;
	}

	private static void fillDependencies(String path, IDelivery delivery, Element depElt)
			throws ParseException {
		NodeList nl = depElt.getElementsByTagName(ITagNames.DELV_DEP_MODULE);
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element moduleElt = (Element) nl.item(i);
				boolean isAdmin = Boolean.getBoolean(moduleElt
						.getAttribute(ITagNames.DELV_ATTR_ADMIN));
				IDelivery depDelivery = new Delivery(isAdmin);
				depDelivery.setName(moduleElt.getAttribute(ITagNames.ATTR_NAME));
				try {
					depDelivery.setRefUID(Long.valueOf(
							moduleElt.getAttribute(ITagNames.DELIVERY_REF_UID)).longValue());
				} catch (Exception e) {
					throw new ParseException(e);
				}
				depDelivery.setRelease(getRelease(moduleElt));
				fillArtefacts(path + File.separator + "dependencies", depDelivery, moduleElt); //$NON-NLS-1$
				delivery.addDependency(depDelivery);
			}
		}
	}

	/**
	 * Fills required deliveries information
	 * 
	 * @param path
	 * @param delivery
	 * @param reqElt
	 * @throws ParseException
	 */
	private static void fillRequirements(String path, IDelivery delivery, Element reqElt)
			throws ParseException {
		NodeList nl = reqElt.getElementsByTagName(ITagNames.DELIVERY);
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element moduleElt = (Element) nl.item(i);

				IRequiredDelivery reqDelivery = new RequiredDelivery();
				reqDelivery.setName(moduleElt.getAttribute(ITagNames.ATTR_NAME));
				delivery.addRequiredDelivery(reqDelivery);
			}
		}
	}

	/**
	 * Fills release information to the delivery. The XML element corresponds to the "releases" tag.
	 * 
	 * @param delivery processed delivery
	 * @param releaseElt releases tag
	 * @throws ParseException
	 */
	private static void fillReleaseInfo(IDelivery delivery, Element releaseElt)
			throws ParseException {
		// Release mode
		delivery.setRangeCheck(ITagNames.REL_VAL_MODE_RANGE.equals(releaseElt
				.getAttribute(ITagNames.REL_ATTR_MODE)));
		// Processing from release
		NodeList nl = releaseElt.getElementsByTagName(ITagNames.FROM_RELEASE);
		if (nl != null && nl.getLength() > 0) {
			// We only pick the first
			delivery.setFromRelease(getRelease((Element) nl.item(0)));
		}
		// Processing target release
		nl = releaseElt.getElementsByTagName(ITagNames.TARGET_RELEASE);
		if (nl != null && nl.getLength() > 0) {
			// We only pick the first
			delivery.setRelease(getRelease((Element) nl.item(0)));
		}
	}

	/**
	 * Builds a release from the given node. Proper release attributes are expected in the specified
	 * node.
	 * 
	 * @param node node which contains the release attributes
	 * @return the corresponding release.
	 * @throws ParseException
	 */
	private static IRelease getRelease(Element node) throws ParseException {
		try {
			int major = Integer.valueOf(node.getAttribute(ITagNames.REL_ATTR_MAJOR)).intValue();
			int minor = Integer.valueOf(node.getAttribute(ITagNames.REL_ATTR_MINOR)).intValue();
			int iter = Integer.valueOf(node.getAttribute(ITagNames.REL_ATTR_ITERATION)).intValue();
			int patch = Integer.valueOf(node.getAttribute(ITagNames.REL_ATTR_PATCH)).intValue();
			int rev = Integer.valueOf(node.getAttribute(ITagNames.REL_ATTR_REVISION)).intValue();
			IRelease rel = new Release();
			rel.setMajor(major);
			rel.setMinor(minor);
			rel.setIteration(iter);
			rel.setPatch(patch);
			rel.setRevision(rev);
			return rel;

		} catch (NumberFormatException e) {
			throw new ParseException(e);
		}
	}

	/**
	 * Fills information retrieved from the categories tag
	 * 
	 * @param path relative path to use as a prefix for artefacts location
	 * @param delivery delivery to fill
	 * @param rootElement parent XML element which contains categories
	 */
	private static void fillCategories(String path, IDelivery delivery, Element rootElement) {
		// Retrieving all categories
		NodeList nl = rootElement.getElementsByTagName(ITagNames.CATEGORY);
		if (nl != null && nl.getLength() > 0) {
			// Parsing all categories
			for (int i = 0; i < nl.getLength(); i++) {
				Element categoryElt = (Element) nl.item(i);
				// Retrieving category path
				String relativePath = categoryElt.getAttribute(ITagNames.PATH);
				fillArtefacts(path + File.separator + relativePath, delivery, categoryElt);
			}
		}
	}

	/**
	 * Fills artefacts to a delivery
	 * 
	 * @param path relative path where the artefacts are located (used as a prefix to artefact
	 *        physical location).
	 * @param delivery delivery to fill with artefacts
	 * @param parentElt parent XML element which contains artefact declaration.
	 */
	private static void fillArtefacts(String path, IDelivery delivery, Element parentElt) {
		// Retrieving delivery items
		NodeList itemNodes = parentElt.getElementsByTagName(ITagNames.DELIVERY_ITEM);
		if (itemNodes != null && itemNodes.getLength() > 0) {
			for (int j = 0; j < itemNodes.getLength(); j++) {
				Element deliveryElt = (Element) itemNodes.item(j);
				// Building the corresponding artefact
				IArtefact a = new Artefact();
				a.setType(ArtefactType.valueOf(deliveryElt
						.getAttribute(ITagNames.ATTR_ARTEFACT_TYPE)));
				a.setRelativePath(path);
				a.setFilename(deliveryElt.getAttribute(ITagNames.ATTR_ARTEFACT));
				final String vendor = deliveryElt.getAttribute(ITagNames.ATTR_ARTEFACT_VENDOR);
				if (vendor != null && !"".equals(vendor.trim())) { //$NON-NLS-1$
					a.setDBVendor(DBVendor.valueOf(deliveryElt
							.getAttribute(ITagNames.ATTR_ARTEFACT_VENDOR)));
				}
				delivery.addArtefact(a);
			}
		}
	}

	private static void fillChecks(IDelivery delivery, Element rootElt, DBVendor vendor) {
		NodeList nl = rootElt.getElementsByTagName(ITagNames.CHECK_RELEASE);
		if (nl == null || nl.getLength() == 0) {
			return;
		}
		for (int i = 0; i < nl.getLength(); i++) {
			Element checkElt = (Element) nl.item(i);
			// Retrieving object check definitions
			NodeList objElts = checkElt.getElementsByTagName(ITagNames.CHECK_OBJ);
			if (objElts == null || objElts.getLength() == 0) {
				continue;
			}
			// Initializing our rule
			IDatabaseObjectCheck objCheck = InstallerFactory
					.buildDatabaseObjectCheckerFor(vendor == null ? delivery.getDBVendor() : vendor);
			for (int j = 0; j < objElts.getLength(); j++) {
				Element objElt = (Element) objElts.item(j);
				// Building db object from XML
				DBObject dbObj = new DBObject(
						convertType(objElt.getAttribute(ITagNames.ATTR_TYPE)),
						objElt.getAttribute(ITagNames.ATTR_NAME));
				objCheck.addObject(dbObj);
			}
			// Registering rule to delivery
			delivery.addCheck(objCheck);
		}
	}

	/**
	 * Converts a release type check to a database type. TODO: connect this to a vendor dependent
	 * conversion as it might differ from a vendor to another.
	 * 
	 * @param releaseType object type defined in descriptor
	 * @return the type to check in database
	 */
	private static String convertType(String releaseType) {
		if ("USER_COLLECTION".equals(releaseType)) { //$NON-NLS-1$
			return "TYPE"; //$NON-NLS-1$
		} else if ("USER_TYPE".equals(releaseType)) { //$NON-NLS-1$
			return "TYPE"; //$NON-NLS-1$
		} else if ("SQL_VIEW".equals(releaseType)) { //$NON-NLS-1$
			return "VIEW"; //$NON-NLS-1$
		} else {
			return releaseType;
		}
	}

}
