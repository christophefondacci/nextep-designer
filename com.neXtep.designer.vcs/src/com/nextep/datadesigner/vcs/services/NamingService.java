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
package com.nextep.datadesigner.vcs.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.NamingPattern;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.INamingPattern;
import com.nextep.designer.vcs.model.INamingVariableProvider;

/**
 * This service is used to name an element.
 * 
 * @author Christophe
 */
public class NamingService {

	private static NamingService instance = null;
	private static final String EXTENSION_ID = "com.neXtep.designer.vcs.namingVariableProvider";
	private Map<IElementType, INamingPattern> patternsMap;
	private boolean validated = false;
	private static final Log log = LogFactory.getLog(NamingService.class);

	private NamingService() {
	}

	/**
	 * Initializes the patterns from database
	 */
	private void initialize() {
		patternsMap = new HashMap<IElementType, INamingPattern>();
		List<? extends INamingPattern> patterns = CorePlugin.getIdentifiableDao().loadAll(
				NamingPattern.class);
		for (INamingPattern p : patterns) {
			patternsMap.put(p.getRelatedType(), p);
		}
		validated = true;
	}

	/**
	 * Invalidates the pattern list (will fire revalidation next time it is needed).
	 */
	public void invalidate() {
		validated = false;
	}

	public static NamingService getInstance() {
		if (instance == null) {
			instance = new NamingService();
		}
		return instance;
	}

	public String getName(ITypedObject t) {
		// Checks the cache state, re-initialize pattern list if needed
		if (!validated) {
			initialize();
		}
		INamingPattern pattern = patternsMap.get(t.getType());
		if (pattern != null) {
			Map<String, String> variables = computeVariables(t);
			return compilePattern(pattern.getPattern(), variables);
		} else {
			// TODO: maybe interesting to raise a checked exception here
			return null;
		}
	}

	/**
	 * A convenience method which sets the name of the given object.
	 * 
	 * @param n
	 */
	public void adjustName(INamedObject n) {
		if (n instanceof ITypedObject) {
			try {
				final String name = getName((ITypedObject) n);
				if (name != null && !"".equals(name.trim())) {
					n.setName(name.trim());
				}
			} catch (RuntimeException e) {
				log.warn("Unable to adjust the name of " + n.getName(), e);
			}
		}
	}

	/**
	 * Computes a map of all variables defined for the specified object.
	 * 
	 * @param t element being named
	 * @return a map whose keys are the variable names and values are the corresponding string
	 *         replacement value
	 */
	private Map<String, String> computeVariables(ITypedObject t) {
		Map<String, String> varMap = new HashMap<String, String>();
		for (INamingVariableProvider p : listProviders()) {
			if (p.isActiveFor(t)) {
				varMap.put(p.getVariableName(), p.getVariableValue(t));
			}
		}
		return varMap;
	}

	/**
	 * Compiles a naming pattern with the variables map provided.
	 * 
	 * @param pattern string pattern to process
	 * @param varMap map of all defined variables to replace
	 * @return the computed string where variables tags are substituted with the appropriate
	 *         variable value
	 */
	private String compilePattern(String pattern, Map<String, String> varMap) {
		String result = new String(pattern);
		for (String var : varMap.keySet()) {
			result = result.replaceAll("\\{" + var + "\\}", notNull(varMap.get(var)));
		}
		return result;
	}

	/*
	 * FIXME [BGA]: This method, along with all the others notNull methods dispatched in the
	 * application's classes should be centralized in some kind of Helper class.
	 */
	private String notNull(String s) {
		return (null == s ? "" : s);
	}

	/**
	 * @return the list of all defined variable providers
	 */
	public Collection<INamingVariableProvider> listProviders() {
		List<INamingVariableProvider> variableProviders = new ArrayList<INamingVariableProvider>();
		// Loading providers
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(EXTENSION_ID,
				"class", "*");
		for (IConfigurationElement elt : elts) {
			try {
				final INamingVariableProvider provider = (INamingVariableProvider) elt
						.createExecutableExtension("class");
				variableProviders.add(provider);
			} catch (CoreException e) {
				log.warn("Problems while loading a naming variable provider, skipping.");
			}
		}
		return variableProviders;
	}

	public List<? extends INamingPattern> listPatterns() {
		return CorePlugin.getIdentifiableDao().loadAll(NamingPattern.class);
	}
}
