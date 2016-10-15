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
package com.nextep.designer.sqlgen.services.impl;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.IDropStrategy;
import com.nextep.datadesigner.sqlgen.model.IGenerationSubmitter;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.datadesigner.sqlgen.strategies.NoDropStrategy;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.services.IDatatypeService;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.ISQLCommandWriter;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.model.ISqlScriptBuilder;
import com.nextep.designer.sqlgen.model.impl.DB2SQLCommandWriter;
import com.nextep.designer.sqlgen.model.impl.DefaultSQLCommandWriter;
import com.nextep.designer.sqlgen.model.impl.GenerationResult;
import com.nextep.designer.sqlgen.model.impl.MSSQLCommandWriter;
import com.nextep.designer.sqlgen.model.impl.OracleSQLCommandWriter;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;
import com.nextep.designer.sqlgen.services.IGenerationListener;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * Default {@link IGenerationService} implementation.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class GenerationService implements IGenerationService {

	private final static Log log = LogFactory.getLog(GenerationService.class);
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_hhmmss");
	private static final int PROGRESS_WORK_BLOCKS = 50;

	private final static String GENERATOR_EXTENSION_ID = "com.neXtep.designer.sqlgen.sqlGenerator"; //$NON-NLS-1$
	private static final String SUBMITTER_EXTENSION_ID = "com.neXtep.designer.sqlgen.sqlSubmitter"; //$NON-NLS-1$
	public static final String DROP_STRATEGY_EXTENSION_ID = "com.neXtep.designer.sqlgen.dropStrategy"; //$NON-NLS-1$
	private final static String CONTRIBUTION_ID = "sqlScriptBuilder"; //$NON-NLS-1$

	private IDatatypeService datatypeService;

	@Override
	public ISqlScriptBuilder getSqlScriptBuilder(DBVendor vendor) {
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(
				GENERATOR_EXTENSION_ID, CONTRIBUTION_ID, "vendor", vendor.name()); //$NON-NLS-1$
		// If we could not find a specific vendor definition we look for generic
		// builder
		if (elts == null || elts.isEmpty()) {
			elts = Designer.getInstance().getExtensions(GENERATOR_EXTENSION_ID, CONTRIBUTION_ID,
					"vendor", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		ISqlScriptBuilder builder = null;
		for (IConfigurationElement elt : elts) {
			try {
				builder = (ISqlScriptBuilder) elt.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				log.error("Problems while instantiating the SQL script builder: " + e.getMessage(), //$NON-NLS-1$
						e);
			}
		}
		// JDBC fallback when no explicit definition found
		if (builder == null && vendor != DBVendor.JDBC) {
			return getSqlScriptBuilder(DBVendor.JDBC);
		} else {
			return builder;
		}
	}

	@Override
	public IGenerationSubmitter getGenerationSubmitter(DBVendor vendor) {
		final String generationMethod = SQLGenUtil
				.getPreference(PreferenceConstants.GENERATOR_METHOD);
		String submitterVendor = DBVendor.JDBC.name();
		// Vendor specific method
		if (!DBVendor.JDBC.name().equals(generationMethod)) {
			// Checking vendor binary definition
			final String binary = SQLGenUtil
					.getPreference(PreferenceConstants.GENERATOR_BINARY_PREFIX
							+ vendor.name().toLowerCase());
			if (binary != null && !"".equals(binary.trim())) { //$NON-NLS-1$
				submitterVendor = vendor.name();
			} else {
				log.warn(SQLGenMessages.getString("service.generation.preferredSubmitterNotFound")); //$NON-NLS-1$
			}
		}
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(
				SUBMITTER_EXTENSION_ID, "vendor", submitterVendor); //$NON-NLS-1$
		if (!elts.isEmpty()) {
			IConfigurationElement elt = elts.iterator().next();
			try {
				IGenerationSubmitter s = (IGenerationSubmitter) elt
						.createExecutableExtension("class"); //$NON-NLS-1$
				return s;
			} catch (CoreException e) {
				throw new ErrorException(
						SQLGenMessages.getString("service.generation.submitterInstantiationError"), e); //$NON-NLS-1$
			}
		}
		throw new ErrorException(SQLGenMessages.getString("service.generation.noSubmitterError")); //$NON-NLS-1$
	}

	@Override
	public ISQLParser getSQLParser(DBVendor vendor) {
		return GeneratorFactory.getSQLParser(vendor);
	}

	@Override
	public ISQLParser getCurrentSQLParser() {
		ISQLParser parser = getSQLParser(DBGMHelper.getCurrentVendor());
		if (parser == null) {
			parser = getSQLParser(DBVendor.JDBC);
		}
		return parser;
	}

	@Override
	public String getGeneratorBinaryName(DBVendor vendor) {
		final String binary = SQLGenUtil.getPreference(PreferenceConstants.GENERATOR_BINARY_PREFIX
				+ vendor.name().toLowerCase());

		if (binary == null || "".equals(binary.trim())) { //$NON-NLS-1$
			return DBGMHelper.getCurrentVendor().getDefaultExecutableName();
		}

		return binary;
	}

	@Override
	public IDropStrategy getDropStrategy(IElementType type) {
		return getDropStrategy(type, DBGMHelper.getCurrentVendor());
	}

	@Override
	public IDropStrategy getDropStrategy(IElementType type, DBVendor vendor) {
		final String preferenceKey = PreferenceConstants.DROP_STRATEGY_PREFIX
				+ type.getId().toLowerCase();
		String strategyId = SQLGenUtil.getPreference(preferenceKey);
		IDropStrategy strategy = getAvailableDropStrategies(type, vendor).get(strategyId);
		if (strategy == null) {
			strategyId = SQLGenUtil.getDefaultPreference(preferenceKey);
			strategy = getAvailableDropStrategies(type, vendor).get(strategyId);
			// Should never happen, but just in case we return the no drop
			// strategy
			if (strategy == null) {
				strategy = new NoDropStrategy();
			}
			SQLGenUtil.setPreference(preferenceKey, strategy.getId());
			log.warn(MessageFormat.format(SQLGenMessages.getString("dropStrategy.resetToDefault"),
					type.getId()));
		}
		strategy.setVendor(vendor);
		return strategy;
	}

	@Override
	public Map<String, IDropStrategy> getAvailableDropStrategies(IElementType type) {
		return getAvailableDropStrategies(type, DBGMHelper.getCurrentVendor());
	}

	@Override
	public Map<String, IDropStrategy> getAvailableDropStrategies(IElementType type, DBVendor vendor) {
		// // Building our list from cache
		// Map<String,IDropStrategy> availableStrategies =
		// dropStrategiesCache.get(type);
		// if(availableStrategies!=null) {
		// return availableStrategies;
		// }
		// If not in cache we load it
		Collection<IConfigurationElement> confs = Designer.getInstance().getExtensions(
				DROP_STRATEGY_EXTENSION_ID, "typeId", type.getId());
		// Building our returned list
		Map<String, IDropStrategy> availableStrategies = new HashMap<String, IDropStrategy>();

		if (confs != null && !confs.isEmpty()) {
			// Fetching results, first loop for vendor specific configuration
			for (IConfigurationElement elt : confs) {
				// Checking the vendor code of the strategy
				if (vendor.name().equals(elt.getAttribute("databaseVendor"))) {
					final IDropStrategy dropStrategy = buildDropStrategy(elt);
					availableStrategies.put(dropStrategy.getId(), dropStrategy);
				}
			}
			// Second loop to fill undefined strategies with default
			for (IConfigurationElement elt : confs) {
				final String strategyVendor = elt.getAttribute("databaseVendor");
				// Only considering default strategies
				if (strategyVendor == null || "".equals(strategyVendor.trim())) {
					// We need to build it to get its id
					final IDropStrategy dropStrategy = buildDropStrategy(elt);
					if (!availableStrategies.containsKey(dropStrategy.getId())) {
						availableStrategies.put(dropStrategy.getId(), dropStrategy);
					}
				}
			}
		}
		// Adding common strategies
		IDropStrategy noDrop = new NoDropStrategy();
		if (!availableStrategies.containsKey(noDrop.getId())) {
			availableStrategies.put(noDrop.getId(), noDrop);
		}
		// dropStrategiesCache.put(type, availableStrategies);
		return availableStrategies;
	}

	/**
	 * Builds a drop strategy from an appropriate {@link IConfigurationElement}
	 * obtained from the dropStrategy extension point.
	 * 
	 * @param elt
	 *            {@link IConfigurationElement} to build strategy from
	 * @return the {@link IDropStrategy}
	 * @throws ErrorException
	 *             whenever Eclipse had problems to instantiate the drop
	 *             strategy implementation
	 */
	private static IDropStrategy buildDropStrategy(IConfigurationElement elt) {
		try {
			IDropStrategy dropStrategy = (IDropStrategy) elt.createExecutableExtension("class");
			String defaultVal = elt.getAttribute("default");
			dropStrategy.setDefault(defaultVal != null && Boolean.parseBoolean(defaultVal));
			return dropStrategy;
		} catch (CoreException e) {
			log.error("Error while instantiating drop strategy", e);
			throw new ErrorException(e);
		}
	}

	@Override
	public IDropStrategy getDefaultDropStrategy(IElementType type) {
		return getDefaultDropStrategy(type, DBGMHelper.getCurrentVendor());
	}

	@Override
	public IDropStrategy getDefaultDropStrategy(IElementType type, DBVendor vendor) {
		final Map<String, IDropStrategy> availableStrategies = getAvailableDropStrategies(type,
				vendor);
		IDropStrategy defaultStrategy = null;
		for (IDropStrategy strategy : availableStrategies.values()) {
			if (strategy.isDefault()) {
				defaultStrategy = strategy;
				break;
			}
		}
		if (defaultStrategy == null) {
			defaultStrategy = new NoDropStrategy();
		}
		return defaultStrategy;
	}

	@Override
	public ISQLCommandWriter getSQLCommandWriter(DBVendor vendor) {
		if (vendor == null) {
			return new DefaultSQLCommandWriter(DBVendor.JDBC);
		}
		switch (vendor) {
		case DB2:
			return new DB2SQLCommandWriter();
		case ORACLE:
			return new OracleSQLCommandWriter();
		case MSSQL:
			return new MSSQLCommandWriter();
		default:
			return new DefaultSQLCommandWriter(vendor);
		}
	}

	/**
	 * @see DBGMHelper#getCurrentVendor()
	 */
	@Override
	public ISQLCommandWriter getCurrentSQLCommandWriter() {
		return getSQLCommandWriter(DBGMHelper.getCurrentVendor());
	}

	@Override
	public String getNewLine() {
		String newlineCharacter = SQLGenUtil.getPreference(PreferenceConstants.SQL_SCRIPT_NEWLINE);
		if (newlineCharacter == null || "".equals(newlineCharacter)) {
			newlineCharacter = "\r\n";
		}
		return newlineCharacter;
	}

	@Override
	public void generate(IGenerationListener listener, IProgressMonitor monitor,
			ITypedObject... objects) {
		// Calling generic method with null vendor
		generate(DBGMHelper.getCurrentVendor(), listener, monitor, objects);
	}

	@Override
	public void generate(DBVendor vendor, IGenerationListener listener,
			IProgressMonitor progressMonitor, ITypedObject... objects) {

		// Preparing our monitors so that we can notify work
		SubMonitor monitor = SubMonitor.convert(progressMonitor, 100);
		monitor.beginTask(SQLGenMessages.getString("sqlgen.action.generate"), objects.length); //$NON-NLS-1$

		// Safety : reset datatypes
		datatypeService.reset();

		IGenerationResult result = batchGenerate(monitor.newChild(70), vendor, "showddl_"
				+ dateFormatter.format(new Date()), null, Arrays.asList(objects));

		monitor.subTask(SQLGenMessages.getString("sqlgen.action.generate.build")); //$NON-NLS-1$
		final List<ISQLScript> scripts = result.buildScript();

		// Creating resulting script
		final ISQLScript fullScript = new SQLScript(ScriptType.CUSTOM);
		fullScript.setName("showddl_" + dateFormatter.format(new Date()));
		for (ISQLScript s : scripts) {
			fullScript.appendScript(s);
		}
		monitor.done();
		listener.generationSucceeded(fullScript);
	}

	@Override
	public void generateIncrement(IGenerationListener listener, IProgressMonitor mon,
			IVersionInfo fromVersion, IVersionInfo toVersion) {
		final DBVendor vendor = CorePlugin.getService(IWorkspaceService.class)
				.getCurrentWorkspace().getDBVendor();
		generateIncrement(vendor, listener, mon, fromVersion, toVersion);
	}

	@Override
	public void generateIncrement(DBVendor vendor, IGenerationListener listener,
			IProgressMonitor mon, IVersionInfo fromVersion, IVersionInfo toVersion) {

		// First adapting our monitor
		final IProgressMonitor monitor = SubMonitor.convert(mon,
				SQLGenMessages.getString("service.generation.incremental"), 3); //$NON-NLS-1$

		// Configuring output script
		final ISQLScript fullScript = CorePlugin.getTypedObjectFactory().create(ISQLScript.class);
		fullScript.setDirectory(SQLGenUtil.getPreference(PreferenceConstants.TEMP_FOLDER));
		fullScript.setExternal(true);
		fullScript.setName("showddl_" + dateFormatter.format(new Date())); //$NON-NLS-1$
		// Generating objects
		final IElementType type = toVersion.getReference().getType();
		final ISQLGenerator generator = GeneratorFactory.getGenerator(type, vendor);
		if (generator != null) {
			monitor.subTask(SQLGenMessages.getString("service.generation.compare")); //$NON-NLS-1$

			// Getting the merger for this element type and REPOSITORY scope
			IMerger m = MergerFactory.getMerger(type, ComparisonScope.REPOSITORY);

			// If we have anything
			if (m != null) {

				// Comparing the 2 versions
				IComparisonItem item = m.compare(toVersion.getReference(), toVersion, fromVersion,
						true);
				m.merge(item, null, null);
				monitor.worked(1);
				monitor.subTask(SQLGenMessages.getString("service.generation.sql")); //$NON-NLS-1$

				// Generating the resulting SQL
				IGenerationResult r = generator.generateIncrementalSQL(item);
				monitor.worked(1);

				// If the generation returned something
				if (r != null) {
					monitor.subTask(SQLGenMessages.getString("service.generation.assemble")); //$NON-NLS-1$

					// Then we assemble all scripts together
					List<ISQLScript> scripts = r.buildScript();
					if (scripts != null) {
						for (ISQLScript s : scripts) {
							fullScript.appendScript(s);
						}
					}
				}
				monitor.done();

				// Now we notify our listener
				listener.generationSucceeded(fullScript);
			}
		}
	}

	@Override
	public <T extends ITypedObject> IGenerationResult batchGenerate(IProgressMonitor myMonitor,
			DBVendor vendor, String scriptName, String scriptDesc, Collection<T> items) {
		IProgressMonitor monitor = SubMonitor.convert(myMonitor, items.size());

		// Preparing our resulting structure
		IGenerationResult result = new GenerationResult();
		result.setName(scriptName);
		result.setDescription(scriptDesc);

		// Generating container contents
		int work = 0;
		for (final T typedItem : items) {

			// Building qualified name of generated object
			String name = "<unknown>";

			// Specific processing for comparison items
			if (typedItem instanceof IComparisonItem) {
				if (((IComparisonItem) typedItem).getSource() != null) {
					if (((IComparisonItem) typedItem).getSource() instanceof INamedObject) {
						name = ((INamedObject) ((IComparisonItem) typedItem).getSource()).getName();
					} else {
						name = ((IComparisonItem) typedItem).getSource().toString();
					}
				} else {
					name = "<null>";
				}
			} else {
				if (typedItem instanceof INamedObject) {
					name = ((INamedObject) typedItem).getName();
				}
			}

			// We finally got the name, so inject in our label translation
			if (++work == PROGRESS_WORK_BLOCKS) {
				final String fullName = MessageFormat.format(
						SQLGenMessages.getString("service.generation.generating"), typedItem
								.getType().getName().toLowerCase(), name.toUpperCase());
				// And displaying task name
				monitor.subTask(fullName);
				monitor.worked(work);
				work = 0;
			}

			// Generating the element by retrieving its SQL generator
			ISQLGenerator g = GeneratorFactory.getGenerator(typedItem, vendor);
			if (g != null) {

				// Assigning current vendor
				g.setVendor(vendor);

				// Incremental or full SQL generation
				if (typedItem instanceof IComparisonItem) {
					final IGenerationResult incrementalResult = g
							.generateIncrementalSQL((IComparisonItem) typedItem);
					result.integrate(incrementalResult);
				} else {
					final IGenerationResult fullResult = g.generateFullSQL(typedItem);
					result.integrate(fullResult);
				}
			}

		}

		// We generated everything so we return our main result
		return result;
	}

	public void setDatatypeService(IDatatypeService datatypeService) {
		this.datatypeService = datatypeService;
	}

	public IDatatypeService getDatatypeService() {
		return datatypeService;
	}
}
