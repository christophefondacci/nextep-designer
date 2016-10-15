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
package com.nextep.designer.sqlgen.ui.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.sqlgen.ui.model.ITypedObjectTextProvider;

/**
 * One proposal provider to get them all. This provider aggregates all existing
 * providers declared through extension.
 * 
 * @author Christophe Fondacci
 *
 */
public class GlobalTextProvider implements ITypedObjectTextProvider {

	private Collection<ITypedObjectTextProvider> providers;
	private static final String EXTENSION_ID = "com.neXtep.designer.sqlgen.ui.proposalProvider";
	private static final Log log = LogFactory.getLog(GlobalTextProvider.class);
	
	public GlobalTextProvider() {
		providers = new ArrayList<ITypedObjectTextProvider>();
		// Initializing all contributed proposal providers
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(EXTENSION_ID, "class", "*");
		for(IConfigurationElement elt : elts) {
			try {
				final ITypedObjectTextProvider provider = (ITypedObjectTextProvider)elt.createExecutableExtension("class");
				providers.add(provider);
			} catch( CoreException e ) {
				log.error("Unable to instantiate a proposal provider",e);
			} catch( ClassCastException e) {
				log.error("Incorrect proposal provider class type",e);
			}
		}
	}
	@Override
	public ITypedObject getElement(String elementName) {
		for(ITypedObjectTextProvider p : providers) {
			ITypedObject obj = p.getElement(elementName);
			if(obj!=null) {
				return obj;
			}
		}
		return null;
	}

	@Override
	public boolean open(String elementName) {
		for(ITypedObjectTextProvider provider : providers) {
			if(provider.open(elementName)) {
				return true;
			}
		}
		// No provider has been able to open this element, simply return false (the contract)
		return false;
	}
//	@Override
//	public Image getImageFor(String element) {
//		for(IProposalProvider p : providers) {
//			Image img = p.getImageFor(element);
//			if(img!=null) {
//				return img;
//			}
//		}
//		return null;
//	}

	@Override
	public List<String> listProvidedElements() {
		List<String> elts = new ArrayList<String>();
		for(ITypedObjectTextProvider p : providers){
			elts.addAll(p.listProvidedElements());
		}
		return elts;
	}

}
