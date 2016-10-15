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
package com.nextep.designer.headless.standalone.services.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.StringAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.designer.headless.standalone.model.xml.Attribute;
import com.nextep.designer.headless.standalone.model.xml.Category;
import com.nextep.designer.headless.standalone.model.xml.Item;
import com.nextep.designer.headless.standalone.model.xml.Items;
import com.nextep.designer.headless.standalone.services.ISerializationService;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class SerializationService implements ISerializationService {

	private JAXBContext context;
	private Marshaller marshaller;

	// private Unmarshaller unmarshaller;

	/**
	 * 
	 */
	public SerializationService() {
		try {
			context = JAXBContext.newInstance(Items.class);
			marshaller = context.createMarshaller();
			// unmarshaller = context.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void serialize(List<IComparisonItem> items, OutputStream stream) {
		// Recursively creating the XML-bound beans
		final Items xmlRootItems = new Items();
		final List<Item> xmlItems = xmlRootItems.getItem();
		for (IComparisonItem item : items) {
			final Item xmlItem = buildXmlItem(item);
			xmlItems.add(xmlItem);
		}
		try {
			marshaller.marshal(xmlRootItems, stream);
		} catch (JAXBException e) {
			throw new ErrorException("Unable to generate XML: " + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	private Item buildXmlItem(IComparisonItem item) {
		Item xmlItem = new Item();
		xmlItem.setDifferenceType(item.getDifferenceType().name());
		xmlItem.setScope(item.getScope().name());
		xmlItem.setType(item.getType().getId());

		final List<Object> xmlItemChildren = xmlItem.getItemOrCategoryAndAttribute();

		final List<IComparisonItem> processed = new ArrayList<IComparisonItem>();
		final Collection<String> categories = item.getCategories();
		for (String categ : categories) {
			final Category c = new Category();
			c.setName(categ);
			final List<Item> attrs = c.getItem();
			final List<IComparisonItem> categoryItems = item.getSubItems(categ);
			for (IComparisonItem categoryItem : categoryItems) {
				final Item xmlCateogoryItem = (Item) buildXmlItem(categoryItem);
				attrs.add(xmlCateogoryItem);
				processed.add(categoryItem);
			}
		}
		if (item instanceof ComparisonAttribute) {
			final Attribute attr = new Attribute();
			final ComparisonAttribute compAttr = (ComparisonAttribute) item;
			attr.setName(compAttr.getName());
			attr.setSource(compAttr.getSource() != null ? ((StringAttribute) compAttr.getSource())
					.getValue() : null);
			attr.setTarget(compAttr.getTarget() != null ? ((StringAttribute) compAttr.getTarget())
					.getValue() : null);
			xmlItemChildren.add(attr);
		}

		for (IComparisonItem subItem : item.getSubItems()) {
			if (!processed.contains(subItem)) {
				final Item xmlSubItem = (Item) buildXmlItem(subItem);
				xmlItemChildren.add(xmlSubItem);
			}
		}

		return xmlItem;
	}

	@Override
	public IComparisonItem unserialize(InputStream stream) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void serializeFile(List<IComparisonItem> item, File f) {
		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(f));
			serialize(item, os);
		} catch (FileNotFoundException e) {
			throw new ErrorException("Problems writing to file (check permissions): " //$NON-NLS-1$
					+ e.getMessage(), e);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {

			}
		}
	}

	@Override
	public IComparisonItem unserializeFile(File f) {
		// TODO Auto-generated method stub
		return null;
	}

}
