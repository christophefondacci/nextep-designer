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
package com.nextep.datadesigner.sqlgen.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.ICheckedObject;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.ISQLCommandWriter;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.model.impl.GenerationResult;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Abstract class for generators which provides default generation behaviour for
 * incremental generation. It handles simple incremental scenarios resolution
 * and delegates the only DIFFER comparison case to an abstract dedicated
 * method.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public abstract class SQLGenerator implements ISQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(SQLGenerator.class);
	protected static final String INDENTATION = "          "; //$NON-NLS-1$
	// private static final int MAX_LOOPS = 2000;

	private DBVendor vendor;
	private ISQLCommandWriter sqlCommandWriter;
	public String NEWLINE = "\r\n"; //$NON-NLS-1$

	public SQLGenerator() {
		NEWLINE = CorePlugin.getService(IGenerationService.class).getNewLine();
	}

	@Override
	public final IGenerationResult generateIncrementalSQL(IComparisonItem result) {
		if (result == null)
			return null;
		IReferenceable r = result.getMergeInfo().getMergeProposal();

		// If all sub elements of the target are proposals, we got a target
		// proposal
		if (result.getDifferenceType() != DifferenceType.EQUALS
				&& result.getScope() != ComparisonScope.REPOSITORY /*
																	 * &&
																	 * r==null
																	 * /*result
																	 * .
																	 * getTarget
																	 * ()
																	 */
				&& result.getSource() != null) {
			if (MergeUtils.isSelected(result, ComparedElement.TARGET)) {
				r = result.getTarget();
			}
		}
		switch (result.getDifferenceType()) {
		case MISSING_TARGET:
			// We generate only if source is selected
			if (r == result.getSource()) {
				try {
					checkConsistency(result.getSource());
				} catch (ErrorException e) {
					// Error occurred while generating
					LOGGER.error(
							"Skipping generation of inconsistent element : "
									+ e.getMessage(), e);
					return null;
				}
				try {
					return generateFullSQL(result.getSource());
				} catch (RuntimeException e) {
					// Error occurred while generating
					LOGGER.error(
							"Generator generated runtime exception : "
									+ e.getMessage(), e);
					return null;
				}
			} else {
				return null;
			}
		case DIFFER:
			// TODO check if OK to replace comparison source
			// Checking proposal...
			if (result.getTarget() == r
					&& result.getScope() != ComparisonScope.REPOSITORY) { // &&
				// (result.getScope()==ComparisonScope.DATABASE))
				// {
				// //
				// ||
				// result.getScope()==ComparisonScope.ALL))
				// {
				return null;
			}
			// else if(r!=result.getSource()) {
			// // Getting merger
			// IMerger m = MergerFactory.getMerger(result.getType(),
			// ComparisonScope.DATABASE);
			// if(m!=null) {
			// try {
			// Object mergeResult = m.buildMergedObject(result, null);
			// result.setSource((IReferenceable)mergeResult);
			// } catch( Exception e ) {
			// log.warn("Skipping user-defined merge of " +
			// result.getType().getName() + " '" +
			// ((INamedObject)result.getSource()).getName() +
			// "', attempting to generate from repository...");
			// }
			// }
			// }
			checkConsistency(result.getSource());
			try {
				return generateDiff(result);
			} catch (RuntimeException e) {
				// Error occurred while generating
				LOGGER.error(
						"Generator generated runtime exception : "
								+ e.getMessage(), e);
				return null;
			}
		case MISSING_SOURCE:
			// We drop only if selected
			if (r == result.getSource()) {
				try {
					return generateDrop(result.getTarget());
				} catch (RuntimeException e) {
					// Error occurred while generating
					LOGGER.error(
							"Generator generated runtime exception : "
									+ e.getMessage(), e);
					return null;
				}
			} else {
				return null;
			}
		case EQUALS:
		default:
			return null;
		}
	}

	/**
	 * Method which generates the SQLScript to upgrade a target element which
	 * differ from the source element. This abstract class assures implementors
	 * that a difference has been found and must be generated.
	 * 
	 * @param result
	 *            the comparison information containing merge data
	 * @return the SQL script that upgrades the target element to the source
	 *         element
	 */
	public abstract IGenerationResult generateDiff(IComparisonItem result);

	@Override
	public IGenerationResult generateDrop(Object model) {
		final IGenerationService generationService = SQLGenPlugin
				.getService(IGenerationService.class);
		return generationService.getDropStrategy(
				((ITypedObject) model).getType(), getVendor()).generateDrop(
				this, model, getVendor());
	}

	// @SuppressWarnings("unused")
	// protected ISQLScript computeGenerationScript(IGenerationResult
	// currentGeneration,String name,
	// Collection<IGenerationResult> contentsGeneration) {
	// SQLWrapperScript s = new SQLWrapperScript(name,"");
	// // A set of all processed results
	// Set<IGenerationResult> processedResults = new
	// HashSet<IGenerationResult>();
	// // A copy of our input collection which we will modify (delete processed
	// entries
	// Collection<IGenerationResult> contents = new
	// ArrayList<IGenerationResult>(contentsGeneration);
	// int loopCount = 0;
	// while(!contents.isEmpty() && loopCount < MAX_LOOPS) {
	// IGenerationResult toProcess = null;
	// for(IGenerationResult c : contents) {
	// if(c.getPreconditions().isEmpty()) {
	// toProcess = c;
	// break;
	// } else {
	//
	// }
	// }
	// loopCount++;
	// }
	// return s;
	// }

	/**
	 * This method is a helper which helps generating sub items incremental
	 * scripts.
	 * 
	 * @param category
	 *            the category of the child comparison item to generate
	 * @param result
	 *            the current comparison result for the generator
	 * @param type
	 *            the child item type
	 * @param resolveDependencies
	 *            indicates whether the integration will resolve dependencies
	 */
	protected IGenerationResult generateTypedChildren(String category,
			IComparisonItem result, IElementType type,
			boolean resolveDependencies) {
		// Getting generator
		ISQLGenerator generator = getGenerator(type);
		// Delegating
		return generateChildren(category, result, generator,
				resolveDependencies);
	}

	/**
	 * This method is a helper which helps generating sub items incremental
	 * scripts.
	 * 
	 * @param category
	 *            the category of the child comparison item to generate
	 * @param result
	 *            the current comparison result for the generator
	 * @param generator
	 *            the generator to use for child generation
	 * @param resolveDependencies
	 *            indicates whether the integration will resolve dependencies
	 */
	protected IGenerationResult generateChildren(String category,
			IComparisonItem result, ISQLGenerator generator,
			boolean resolveDependencies) {
		List<IGenerationResult> childGenerations = new ArrayList<IGenerationResult>();
		// Getting sub items
		List<IComparisonItem> items = result.getSubItems(category);
		if (items == null) {
			return null;
			// // We try to look for an attribute instead of list
			// IComparisonItem attr = result.getAttribute(category);
			// if(attr == null) {
			// return null;
			// } else {
			// // If we find an attribute we convert it to a list of 1 element
			// items = new ArrayList<IComparisonItem>();
			// items.add(attr);
			// }
		}
		// Parsing comparison column items
		ISQLGenerator itemGenerator = generator;
		for (IComparisonItem colItem : items) {
			if (generator == null) {
				itemGenerator = getGenerator(colItem.getType());
			}
			// Generating child
			IGenerationResult childGeneration = itemGenerator
					.generateIncrementalSQL(colItem);
			childGenerations.add(childGeneration);
		}
		return integrateAll(childGenerations, resolveDependencies);
	}

	/**
	 * Generates the list of children element using their default preconfigured
	 * generator.
	 * 
	 * @param children
	 *            list of children to generate
	 * @param resolveDependencies
	 *            should we resolve the dependencies of the resulting
	 *            {@link IGenerationResult} when assembling everything
	 *            altogether.
	 * @return a generation result for this whold generation
	 */
	public IGenerationResult generateChildren(
			Collection<? extends ITypedObject> children,
			boolean resolveDependencies) {
		return generateChildren(children, null, resolveDependencies);
	}

	/**
	 * Generates the list of children element using the specified generator for
	 * all children
	 * 
	 * @param children
	 *            list of children to generate
	 * @param g
	 *            generator to use
	 * @param resolveDependencies
	 *            should we resolve the dependencies of the resulting
	 *            {@link IGenerationResult} when assembling everything
	 *            altogether.
	 * @return a generation result for this whold generation
	 * @see SQLGenerator#generateChildren(Collection, boolean)
	 */
	public IGenerationResult generateChildren(
			Collection<? extends ITypedObject> children, ISQLGenerator g,
			boolean resolveDependencies) {
		List<IGenerationResult> childGenerations = new ArrayList<IGenerationResult>();

		for (ITypedObject child : children) {
			// Getting generator
			ISQLGenerator generator = (g == null ? getGenerator(child.getType())
					: g);
			// Generating children
			if (child instanceof IComparisonItem) {
				// Generating incremental and adding if there is a non null
				// generation result
				IGenerationResult childGeneration = generator
						.generateIncrementalSQL((IComparisonItem) child);
				childGenerations.add(childGeneration);
			} else {
				// Generating incremental and adding to global result
				IGenerationResult childGeneration = generator
						.generateFullSQL(child);
				childGenerations.add(childGeneration);
			}
		}
		// Integrating
		return integrateAll(childGenerations, resolveDependencies);
	}

	/**
	 * Integrates a collection of generation results into a single generation
	 * result. Dependencies may be resolved depending on the resolveDependency
	 * flag. Null generation results will be filtered.
	 * 
	 * @param name
	 *            name of the resulting generation result
	 * @param generations
	 *            a collection of all generation results to integrate
	 * @param resolveDependencies
	 *            a flag indicating if the integration should resolve
	 *            dependencies. It consists in ordering the results depending on
	 *            their dependencies (i.e. to ensure tables are created before
	 *            indexes, etc.)
	 * @return a single generation result integrating the collection of results
	 */
	public static IGenerationResult integrateAll(String name,
			List<IGenerationResult> generations, boolean resolveDependencies) {
		// Initializing generation result
		IGenerationResult generation = new GenerationResult(name);
		// Removing null entries
		List<IGenerationResult> nonNullResults = new ArrayList<IGenerationResult>();
		for (IGenerationResult r : generations) {
			if (r != null) {
				nonNullResults.add(r);
			}
		}
		// Resolving by ordering generations (see the GenerationResult
		// comparator implementation)
		List<IGenerationResult> resolvedResults = new ArrayList<IGenerationResult>();
		if (resolveDependencies) {
			// Try / catch safe blocks added for DES-925 problems
			try {
				Collections.sort(nonNullResults, NameComparator.getInstance());
			} catch (RuntimeException e) {
				LOGGER.warn(
						"Unable to sort generation results by name: " + e.getMessage(), e); //$NON-NLS-1$
			}
			try {
				Collections.sort(nonNullResults);
			} catch (RuntimeException e) {
				LOGGER.warn(
						"Unable to sort generation results by natural comparison: " //$NON-NLS-1$
								+ e.getMessage(), e);
			}
			// Since comparator of IGenerationResult is not transitive
			// the sort might not resolve all dependencies
			// Hashing results by db ref
			Map<DatabaseReference, IGenerationResult> refMap = new HashMap<DatabaseReference, IGenerationResult>();
			for (IGenerationResult result : nonNullResults) {
				for (DatabaseReference generatedRef : result
						.getGeneratedReferences()) {
					refMap.put(generatedRef, result);
				}
			}
			// Resolving
			for (IGenerationResult result : nonNullResults) {
				if (!resolvedResults.contains(result)) {
					processPreconditions(result, resolvedResults, refMap,
							new ArrayList<IGenerationResult>());
				}
			}
		} else {
			resolvedResults = nonNullResults;
		}
		// Integrating children generations
		for (IGenerationResult r : resolvedResults) {
			generation.integrate(r);
		}
		return generation;
	}

	public static void processPreconditions(IGenerationResult result,
			List<IGenerationResult> resolvedResults,
			Map<DatabaseReference, IGenerationResult> refMap,
			Collection<IGenerationResult> stack) {
		if (result.getPreconditions().isEmpty()) {
			resolvedResults.add(result);
		} else {
			// We have a deadloop here, so we return
			if (stack.contains(result)) {
				LOGGER.warn("Circular dependencies found, generation order may not be accurate.");
				return;
			}
			stack.add(result);
			for (DatabaseReference ref : result.getPreconditions()) {
				IGenerationResult precondResult = refMap.get(ref);
				if (!resolvedResults.contains(precondResult)
						&& precondResult != null && precondResult != result) {
					try {
						processPreconditions(precondResult, resolvedResults,
								refMap, stack);
					} catch (StackOverflowError e) {
						LOGGER.info(result.getName());
						throw e;
					}
				}
			}
			stack.remove(result);
			resolvedResults.add(result);
		}
	}

	/**
	 * Performs an unnamed integration of all specified scrips
	 * 
	 * @see SQLGenerator#integrateAll(String, List, boolean)
	 * @param generations
	 *            collection of generation results to integrate
	 * @param resolveDependencies
	 *            indicates whether the integration will resolve dependencies
	 * @return a single generation result integrating all specified generations
	 */
	public static IGenerationResult integrateAll(
			List<IGenerationResult> generations, boolean resolveDependencies) {
		return integrateAll(null, generations, resolveDependencies);
	}

	/**
	 * This method will simply check if the only difference in this comparison
	 * item is the name. This could be used to generate the "ALTER ... RENAME"
	 * statements
	 * 
	 * @param item
	 *            the comparison item to check
	 * @return <code>true</code> if the only difference is the name, else
	 *         <code>false</code>
	 */
	protected boolean isRenamedOnly(IComparisonItem item) {
		boolean renamedOnly = false;
		for (IComparisonItem i : item.getSubItems()) {
			if (i instanceof ComparisonAttribute
					&& Merger.ATTR_NAME.equals(((ComparisonAttribute) i)
							.getName())) {
				if (i.getDifferenceType() == DifferenceType.DIFFER) {
					renamedOnly = true;
				}
			} else {
				if (i.getDifferenceType() != DifferenceType.EQUALS) {
					return false;
				}
			}
		}
		return renamedOnly;
	}

	protected boolean isRenamed(IComparisonItem item) {
		IComparisonItem attrName = item.getAttribute(Merger.ATTR_NAME);
		if (attrName instanceof ComparisonAttribute) {
			return (attrName.getDifferenceType() == DifferenceType.DIFFER);
		}
		return false;
	}

	/**
	 * Checks the consistency of this object if it implements the
	 * {@link ICheckedObject} interface. This method is called before
	 * generating.
	 * 
	 * @param o
	 */
	private void checkConsistency(Object o) {
		if (!(o instanceof ICheckedObject)) {
			return;
		}
		ICheckedObject c = (ICheckedObject) o;
		try {
			c.checkConsistency();
		} catch (InconsistentObjectException e) {
			throw new ErrorException(
					NameHelper.getQualifiedName(c)
							+ " appear to be inconsistent. Fix it before generating. Reason: "
							+ e.getReason());
		}
	}

	/**
	 * Adds all provided scripts to a source script, using commas to separate
	 * one script content from another. A header and footer may be provided
	 * which will encapsulate the script enumeration. This method is used to
	 * append several child scripts to a global ALTER SQL command.
	 * 
	 * @param source
	 *            source script which will be modified by appending the scripts
	 *            list.
	 * @param header
	 *            header to append to the source script before appending the
	 *            scripts list
	 * @param footer
	 *            footer to append to the source script after appending the
	 *            scripts list
	 * @param scripts
	 *            scripts list to append to the source, using a comma to
	 *            separate script contents
	 */
	protected void addCommaSeparatedScripts(ISQLScript source, String header,
			String footer, List<ISQLScript> scripts) {
		if (scripts.size() == 0)
			return;
		boolean first = true;
		source.appendSQL(header);
		for (ISQLScript script : scripts) {
			source.appendSQL(NEWLINE).appendSQL(INDENTATION);
			// Adding format & comma on every item but the first
			if (!first) {
				source.appendSQL(","); //$NON-NLS-1$
			} else {
				source.appendSQL(" "); //$NON-NLS-1$
				first = false;
			}
			// Appending script
			source.appendSQL(script.getSql());
		}
		source.appendSQL(NEWLINE);
		if (footer != null && !"".equals(footer)) { //$NON-NLS-1$
			source.appendSQL(footer).appendSQL(NEWLINE);
		}
	}

	@Override
	public void setVendor(DBVendor vendor) {
		this.vendor = vendor;
		initSQLCommandWriter();
	}

	@Override
	public DBVendor getVendor() {
		return vendor;
	}

	/**
	 * This method initialize the SQL command writer according to the database
	 * vendor supplied to this SQL generator.
	 */
	private void initSQLCommandWriter() {
		this.sqlCommandWriter = SQLGenPlugin.getService(
				IGenerationService.class).getSQLCommandWriter(getVendor());
	}

	/**
	 * Returns the SQL command writer associated with this SQL generator.
	 * 
	 * @return a {@link ISQLCommandWriter}
	 */
	protected ISQLCommandWriter getSQLCommandWriter() {
		if (sqlCommandWriter == null) {
			initSQLCommandWriter();
		}
		return sqlCommandWriter;
	}

	/**
	 * A helper method which returns the desired {@link ISQLGenerator} for the
	 * specified {@link IElementType}.
	 * 
	 * @param t
	 *            the {@link IElementType} for which you want a SQL generator
	 * @return the {@link ISQLGenerator}
	 */
	protected ISQLGenerator getGenerator(IElementType t) {
		// TODO: Warning, this code retrieves the "global" vendor defined for
		// the view, instead of
		// local module vendor
		final ISQLGenerator generator = GeneratorFactory.getGenerator(t,
				getVendor());
		generator.setVendor(getVendor());
		return generator;
	}

	/**
	 * Generates the SQL code which prompts the specified message to the user
	 * during execution
	 * 
	 * @return the prompt command to use for this vendor (for generic
	 *         generation)
	 * @see ISQLCommandWriter#promptMessage(String)
	 */
	protected String prompt(String message) {
		/*
		 * For a quicker migration this method has been updated to use the
		 * ISQLCommandWriter interface, but subclasses should call
		 * getSQLCommandWriter().promptMessage(String) instead.
		 */
		return getSQLCommandWriter().promptMessage(message);
	}

	/**
	 * Helper method to append a statement delimiter followed by a new line
	 * character to the last statement appended to the specified SQLScript. This
	 * method does not check if the SQL script actually contains a statement to
	 * close.
	 * 
	 * @param script
	 *            a script with an unclosed statement at the end of the script.
	 * @return the specified script with a statement delimiter appended at the
	 *         end.
	 * @see ISQLCommandWriter#closeStatement()
	 */
	protected ISQLScript closeLastStatement(ISQLScript script) {
		/*
		 * For a quicker migration this method has been updated to use the
		 * ISQLCommandWriter interface, but subclasses should call
		 * getSQLCommandWriter().closeStatement() instead.
		 */
		return script.appendSQL(getSQLCommandWriter().closeStatement());
	}

	protected ISQLParser getParser() {
		return GeneratorFactory.getSQLParser(getVendor());
	}

	protected IProgressMonitor getMonitor() {
		return new NullProgressMonitor();
	}

	/**
	 * Convenience method to get a new <code>ISQLScript</code> instance and set
	 * its name, description and type attributes. This method uses the proper
	 * {@link ITypedObjectFactory} to create a new <code>ISQLScript</code>
	 * instance.
	 * 
	 * @param name
	 *            the name of the script
	 * @param description
	 *            the description of the script
	 * @param type
	 *            the type of the script, must correspond to one of the values
	 *            of the {@link ScriptType} enumeration
	 * @return a new {@link ISQLScript} initialized with the specified
	 *         attributes
	 */
	protected ISQLScript getSqlScript(String name, String description,
			ScriptType type) {
		ISQLScript script = CorePlugin.getTypedObjectFactory().create(
				ISQLScript.class);
		script.setName(name);
		script.setDescription(description);
		script.setScriptType(type);
		return script;
	}

	/**
	 * Convenience method for escaping names
	 * 
	 * @param name
	 *            name to escape
	 * @return the escaped name
	 */
	protected String escape(String name) {
		return getSQLCommandWriter().escapeDbObjectName(name);
	}

	/**
	 * Provides a name to use for SQL generation, which handles proper schema
	 * location by performing a lookup in the container hierarchy.
	 * 
	 * @param dbObject
	 *            the {@link IDatabaseObject} to generate a name for
	 * @return the proper name which might be prefixed by a schema
	 */
	protected String getName(IDatabaseObject<?> dbObject) {
		return getName(dbObject.getName(), dbObject);
	}

	/**
	 * Provides a name to use for SQL generation using the given name as the
	 * database object name and the given database object for schema location.
	 * 
	 * @param name
	 *            name of the database object to generate
	 * @param forObject
	 *            the {@link IDatabaseObject} to use for locating the schema of
	 *            this object
	 * @return the SQL generation name of the object, optionally schema-prefixed
	 */
	protected String getName(String name, IDatabaseObject<?> forObject) {
		// Check if we have a versionable
		IVersionable<?> versionable = VersionHelper.getVersionable(forObject);
		String schemaPrefix = "";
		// If yes, we check the owning container prefix
		if (versionable != null) {
			// Requesting the schema of this versionable
			final String schema = getSchema(versionable.getContainer());
			// If something has been defined we update the prefix
			if (schema != null) {
				schemaPrefix = schema + ".";
			}
		}
		return schemaPrefix + name;
	}

	private String getSchema(IVersionContainer v) {
		// If null we are on top of hierarchy and no schema was found
		if (v == null) {
			return null;
		} else {
			// Extracting defined schema
			final String schema = v.getSchemaName();
			// If something is defined
			if (schema != null && !"".equals(schema.trim())) {
				// We return it
				return schema;
			} else {
				// Otherwise we go up in the container hierarchy
				IVersionable<?> versionable = VersionHelper.getVersionable(v);
				// If we have a parent container
				if (versionable != null) {
					// We return the schema for this
					return getSchema(versionable.getContainer());
				} else {
					return null;
				}
			}
		}
	}

	/**
	 * Helper method to check emptiness of a script by safely checking that :<br>
	 * - It is not null<br>
	 * - It is not empty after trimming<br>
	 * 
	 * @param s
	 *            string to check
	 * @return <code>true</code> if empty or <code>null</code>, else
	 *         <code>false</code>
	 */
	protected boolean isEmpty(String s) {
		return s == null || "".equals(s.trim()); //$NON-NLS-1$
	}

	/**
	 * Convenience method to return an empty string if the specified string is
	 * <code>null</code>.
	 * 
	 * @param s
	 *            a <code>String</code>
	 * @return an empty string if the specified string is <code>null</code>, the
	 *         same string otherwise.
	 */
	protected String notNull(String s) {
		return (s == null ? "" : s); //$NON-NLS-1$
	}

}
