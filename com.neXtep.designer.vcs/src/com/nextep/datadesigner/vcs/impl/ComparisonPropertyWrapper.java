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
package com.nextep.datadesigner.vcs.impl;

import java.util.List;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.impl.StringAttribute;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IProperty;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IComparisonProperty;

/**
 * A wrapper property which uses a comparison source item to provide the property from comparison
 * attributes.
 * 
 * @author Christophe Fondacci
 */
public class ComparisonPropertyWrapper extends Observable implements IComparisonProperty {

	private IComparisonItem item;
	private ComparedElement comparedElement;
	private String name;

	public ComparisonPropertyWrapper(String name, IComparisonItem item) {
		this(name, item, ComparedElement.SOURCE);
	}

	public ComparisonPropertyWrapper(String name, IComparisonItem item,
			ComparedElement comparedElement) {
		this.item = item;
		this.name = name;
		this.comparedElement = comparedElement;
	}

	/**
	 * @see com.nextep.datadesigner.model.IProperty#getChildren()
	 */
	@Override
	public List<IProperty> getChildren() {
		return ComparisonPropertyProvider.getChildProperties(item);
	}

	/**
	 * @see com.nextep.datadesigner.model.IProperty#getName()
	 */
	@Override
	public String getName() {
		if (item instanceof ComparisonAttribute) {
			return ((ComparisonAttribute) item).getName();
		} else if (item instanceof ComparisonResult) {
			ComparisonAttribute attr = (ComparisonAttribute) item.getAttribute(Merger.ATTR_NAME);
			final IReferenceable comparedElt = comparedElement.get(item);
			if (attr != null) {
				StringAttribute strAttr = (StringAttribute) comparedElement.get(attr);
				if (strAttr == null || "".equals(strAttr.getValue())) {
					strAttr = (StringAttribute) comparedElement.getOther(attr);
				}
				return strAttr == null ? "" : strAttr.getValue();
			} else if (comparedElt instanceof IReference) {
				// Trying to resolve reference
				try {
					IReferenceable r = VersionHelper.getReferencedItem((IReference) comparedElement
							.get(item));
					if (r instanceof INamedObject) {
						return "[ref] " + ((INamedObject) r).getName();
					} else {
						return "[ref] uid=" + r.getReference().getReferenceId();
					}
				} catch (UnresolvedItemException e) {
					return "[unresolved ref]" + ((IReference) item.getSource()).getReferenceId();
				} catch (ErrorException e) {
					return "Unable to extract name from this property";
				}
			} else if (comparedElt instanceof INamedObject) {
				try {
					return ((INamedObject) comparedElt).getName();
				} catch (ErrorException e) {
					return "[Error: " + e.getMessage() + "]";
				}
			}
		}
		return name;
	}

	/**
	 * @see com.nextep.datadesigner.model.IProperty#getValue()
	 */
	@Override
	public String getValue() {
		final IReferenceable elt = comparedElement.get(item);
		return getElementValue(elt);
	}

	private String getElementValue(IReferenceable elt) {
		if (elt instanceof StringAttribute) {
			return ((StringAttribute) elt).getValue();
		} else if (elt instanceof INamedObject) {
			return ((INamedObject) elt).getName();
		} else {
			return "";
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
	 */
	@Override
	public IElementType getType() {
		return item.getType();
	}

	@Override
	public String getComparedValue() {
		final IReferenceable elt = comparedElement.getOther(item);
		return getElementValue(elt);
	}

	@Override
	public DifferenceType getDifferenceType() {
		return item.getDifferenceType();
	}

}
