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
package com.nextep.designer.sqlgen.ui.editors.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.projection.Segment;
import org.eclipse.jface.text.rules.IToken;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IProcedureParameter;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.IVariable;
import com.nextep.datadesigner.dbgm.model.IVariableContainer;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.LightProcedure;
import com.nextep.datadesigner.sqlgen.impl.PackagePrototypeMatcher;
import com.nextep.datadesigner.sqlgen.impl.Prototype;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.sqlgen.model.IPrototype;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.dbgm.sql.TableAlias;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.helper.DatatypeHelper;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.services.ICaptureService;
import com.nextep.designer.sqlgen.ui.SQLGenImages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class SQLCompletionProcessor implements IContentAssistProcessor {

	private static final Log LOGGER = LogFactory.getLog(SQLCompletionProcessor.class);

	protected IContextInformationValidator fValidator = new Validator();
	private Object editedObject;
	private ISQLParser parser;
	private Map<String, IColumnable> tablesMap;
	private boolean specEditor = false;
	private MultiValueMap proceduresMap;
	private Collection<IPackage> packageList;

	// private Map<String,IBasicColumn> columnsMap;
	public SQLCompletionProcessor(Object o) {
		this(o, false);
	}

	public SQLCompletionProcessor(Object o, boolean specEditor) {
		proceduresMap = new MultiValueMap();
		packageList = new ArrayList<IPackage>();
		this.specEditor = specEditor;
		this.editedObject = o;
		// Registering listener for model switch (checkout / checkin)
		// TODO: implement a more generic model switch
		if (editedObject instanceof IObservable) {
			Designer.getListenerService().registerListener(this, (IObservable) editedObject,
					new IEventListener() {

						@Override
						public void handleEvent(ChangeEvent event, IObservable source, Object data) {
							if (source != editedObject && source != null) {
								editedObject = source;
							}
						}
					});
		}
		parser = GeneratorFactory.getSQLParser(DBGMHelper.getCurrentVendor());
		// Initializing tables
		Job j = new Job("Computing completion proposals...") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				List<IVersionable<?>> vTables = new ArrayList<IVersionable<?>>();
				List<IVersionable<?>> vProcs = new ArrayList<IVersionable<?>>();
				List<IVersionable<?>> vPackages = new ArrayList<IVersionable<?>>();
				boolean failed = true;
				if (editedObject instanceof IConnection) {
					// If we have a connection we try to extract proposals form the connection
					final ICaptureService captureService = CorePlugin
							.getService(ICaptureService.class);
					try {
						// Getting proposals
						final Collection<IVersionable<?>> dbObjects = captureService
								.getContentsForCompletion((IConnection) editedObject, monitor);
						// Processing fetched objects to compute proposals structures
						for (IVersionable<?> dbObject : dbObjects) {
							if (dbObject.getType() == IElementType.getInstance(IBasicTable.TYPE_ID)
									|| dbObject.getType().equals(
											IElementType.getInstance(IView.TYPE_ID))) {
								vTables.add(dbObject);
							} else if (dbObject.getType() == IElementType
									.getInstance(IProcedure.TYPE_ID)) {
								vProcs.add(dbObject);
							} else if (dbObject.getType() == IElementType
									.getInstance(IPackage.TYPE_ID)) {
								vPackages.add(dbObject);
							}
						}
						failed = false;
					} catch (RuntimeException e) {
						LOGGER.error(
								"Unable to fetch completion proposals from database : "
										+ e.getMessage(), e);
					}
				}
				// If we failed or if not in connected mode, we compute proposals from workspace
				if (failed) {
					vTables = VersionHelper.getAllVersionables(VersionHelper.getCurrentView(),
							IElementType.getInstance(IBasicTable.TYPE_ID));
					vTables.addAll(VersionHelper.getAllVersionables(VersionHelper.getCurrentView(),
							IElementType.getInstance(IView.TYPE_ID)));
					vProcs = VersionHelper.getAllVersionables(VersionHelper.getCurrentView(),
							IElementType.getInstance(IProcedure.TYPE_ID));
					vPackages = VersionHelper.getAllVersionables(VersionHelper.getCurrentView(),
							IElementType.getInstance(IPackage.TYPE_ID));
				}
				tablesMap = new HashMap<String, IColumnable>();
				for (IVersionable<?> v : vTables) {
					ITypedObject t = (ITypedObject) v.getVersionnedObject().getModel();
					tablesMap.put(((INamedObject) t).getName().toUpperCase(), (IColumnable) t);
				}
				if (editedObject instanceof ITrigger) {
					if (((ITrigger) editedObject).getTriggableRef().getType() == IElementType
							.getInstance(IBasicTable.TYPE_ID)) {
						try {
							final IBasicTable t = (IBasicTable) VersionHelper
									.getReferencedItem(((ITrigger) editedObject).getTriggableRef());
							tablesMap.put("NEW", t); //$NON-NLS-1$
							tablesMap.put("OLD", t); //$NON-NLS-1$
						} catch (ErrorException e) {
							LOGGER.warn("Unable to locate trigger parent: " + e.getMessage(), e);
						}
					}
				}
				// Getting procedure
				for (IVersionable<?> v : vProcs) {
					proceduresMap.put("", (IProcedure) v.getVersionnedObject().getModel()); //$NON-NLS-1$
				}
				for (IVersionable<?> v : vPackages) {
					final IPackage pkg = (IPackage) v.getVersionnedObject().getModel();
					packageList.add(pkg);
					if (!pkg.isParsed()) {
						DBGMHelper.parse(pkg);
					}
					final Collection<IProcedure> procs = pkg.getProcedures();
					if (procs != null) {
						for (IProcedure proc : new ArrayList<IProcedure>(procs)) {
							proceduresMap.put(pkg.getName().toUpperCase(), proc);
							if (editedObject == pkg) {
								proceduresMap.put("", proc); //$NON-NLS-1$
							}
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		j.schedule();

	}

	/**
	 * Simple content assist tip closer. The tip is valid in a range of 5 characters around its
	 * popup location.
	 */
	protected static class Validator implements IContextInformationValidator,
			IContextInformationPresenter {

		protected int fInstallOffset;

		public boolean isContextInformationValid(int offset) {
			return Math.abs(fInstallOffset - offset) < 5;
		}

		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			fInstallOffset = offset;
		}

		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		try {
			return doComputeCompletionProposals(viewer, offset);
		} catch (RuntimeException e) {
			// Here we catch everything because completion processor should never
			// fail up to the user
			LOGGER.error("Problems while computing completions proposals : " + e.getMessage(), e);
			return null;
		}
	}

	private ICompletionProposal[] doComputeCompletionProposals(ITextViewer viewer, int offset) {
		List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
		try {
			int prefixStart = getPrefixStart(viewer, offset);
			String prefix = viewer.getDocument().get(prefixStart, offset - prefixStart)
					.toUpperCase();
			// Have we got a variable prefix ?
			// if(prefixStart>0 &&
			// parser.getVarSeparator().equals(viewer.getDocument().get(prefixStart-1, 1))) {

			// } else {
			// If not we might be in a SQL statement, so we parse
			DMLParseResult r = parseSQL(viewer, offset);
			if (r != null) {
				// Are we in a section which should display table proposals (FROM)
				if (matchTableSegment(offset, r)) {
					final List<String> tabNames = new ArrayList<String>(tablesMap.keySet());
					Collections.sort(tabNames);
					for (String s : tabNames) {
						if (s.toUpperCase().startsWith(prefix)) {
							ITypedObject tab = (ITypedObject) tablesMap.get(s);
							String realName = ((INamedObject) tab).getName();
							result.add(new CompletionProposal(realName, prefixStart, offset
									- prefixStart, realName.length(), ImageFactory.getImage(tab
									.getType().getTinyIcon()), realName + " " //$NON-NLS-1$
									+ tab.getType().getName(), null, "")); //$NON-NLS-1$
						}
					}
				} else if (".".equals(viewer.getDocument().get(prefixStart - 1, 1))) { //$NON-NLS-1$
					// We are after a '.' so we try to locate the corresponding table alias
					String alias = getLastWord(viewer, prefixStart - 1).toUpperCase();
					TableAlias a = r.getTableAlias(alias);
					if (("NEW".equals(alias) || "OLD".equals(alias)) //$NON-NLS-1$ //$NON-NLS-2$
							&& (editedObject instanceof ITrigger)) {
						a = new TableAlias(alias);
						a.setTable(tablesMap.get(alias));
					}
					if (a != null && a.getTable() != null) {
						addColumnsProposal(result, a, prefix, prefixStart, offset, false);
					}
					addPackageProcProposals(viewer, prefix, prefixStart, offset, result);
				} else {
					// Otherwise we propose all columns from all known tables of the statement
					if (r.getFromTables().size() > 0) {
						for (TableAlias a : r.getFromTables()) {
							if (a.getTable() != null) {
								addColumnsProposal(result, a, prefix, prefixStart, offset, true);
							}
						}
					}
					// And we propose database function prototypes as well
					List<String> funcs = parser.getTypedTokens().get(ISQLParser.FUNC);
					for (String f : (funcs == null ? (List<String>) Collections.EMPTY_LIST : funcs)) {
						if (f.startsWith(prefix)) {
							final String func = f.toLowerCase() + "()"; //$NON-NLS-1$
							result.add(new CompletionProposal(func, prefixStart, offset
									- prefixStart, func.length() - 1, DBGMImages.ICON_FUNC, func
									+ " - Database function", null, "")); //$NON-NLS-2$
						}
					}
					addPackageProcProposals(viewer, prefix, prefixStart, offset, result);
				}
				if (editedObject instanceof IPackage) {
					final IPackage pkg = (IPackage) editedObject;
					// First saving contents to not alter package state
					DBGMHelper.parse(pkg, viewer.getDocument().get());
					addPackageInnerProposals(pkg, prefix, result, offset, prefixStart);
				}
			} else {
				List<IPrototype> prototypes = new ArrayList<IPrototype>();
				// We may have a package prefix, adding procs here
				boolean pkgAdded = false;
				if (".".equals(viewer.getDocument().get(prefixStart - 1, 1))) { //$NON-NLS-1$
					addPackageProcProposals(viewer, prefix, prefixStart, offset, result);
					pkgAdded = true;
				}
				// Specification additions
				if (editedObject instanceof IPackage) {
					final IPackage pkg = (IPackage) editedObject;
					// First saving contents to not alter package state
					DBGMHelper.parse(pkg, viewer.getDocument().get());
					addPackageInnerProposals(pkg, prefix, result, offset, prefixStart);
					// Building specification proposals
					for (IProcedure p : pkg.getProcedures()) {
						prototypes.add(new Prototype(p.getName(),
								"Insert procedure specification for '" + p.getName() + "'", p //$NON-NLS-2$
										.getHeader(), p.getHeader().length(),
								new PackagePrototypeMatcher()));
					}
				}
				if (!pkgAdded) {
					addPackageProcProposals(viewer, prefix, prefixStart, offset, result);
				}
				// We are not in a SQL statement so we propose SQL prototypes
				prototypes.addAll(parser.getPrototypes());
				for (IPrototype p : prototypes) {
					if (p.getName().toUpperCase().startsWith(prefix)) {
						// Retrieving context
						Object contextualEntity = (editedObject instanceof IPackage) ? ((IPackage) editedObject)
								.getParseData().getEntity(offset) : null;
						if (contextualEntity == null || specEditor) {
							contextualEntity = editedObject;
						}
						if (p.getPrototypeMatcher().match(contextualEntity) > 0) {
							result.add(new CompletionProposal(p.getTemplate(), prefixStart, offset
									- prefixStart, p.getCursorPosition(), SQLGenImages.ICON_HINT, p
									.getName() + " - " + p.getDescription(), null, p.getTemplate())); //$NON-NLS-1$
						}
					}
				}
				// We are not in a SQL statement, so we add DML hints
				List<String> dmlStmts = parser.getTypedTokens().get(ISQLParser.DML);
				for (String s : dmlStmts) {
					if (s.startsWith(prefix)) {
						final String func = s.toLowerCase();
						// IContextInformation info = new
						// ContextInformation("ContextDisplayString","InformationDisplayString");
						result.add(new CompletionProposal(func, prefixStart, offset - prefixStart,
								func.length(), SQLGenImages.ICON_HINT, func + " - SQL statement",
								null, "")); //$NON-NLS-1$
					}
				}
				// And we add datatypes proposals
				List<String> datatypes = parser.getTypedTokens().get(ISQLParser.DATATYPE);
				for (String s : datatypes) {
					if (s.startsWith(prefix)) {
						final String type = s;
						// Instantiating datatype
						final IDatatype d = new Datatype(s);
						result.add(new CompletionProposal(type, prefixStart, offset - prefixStart,
								type.length(), DatatypeHelper.getDatatypeIcon(d), type
										+ " - Datatype", null, "")); //$NON-NLS-2$
					}
				}
			}

			// }
		} catch (BadLocationException e) {
			// Logging any bad location error only for debug
			LOGGER.debug("Error while generating completion proposals", e);
		}
		// Returning the array of proposals
		return result.toArray(new ICompletionProposal[result.size()]);
	}

	private void addPackageProcProposals(ITextViewer viewer, String prefix, int prefixStart,
			int offset, List<ICompletionProposal> result) throws BadLocationException {
		// Adding proposals for packages / procs
		if (".".equals(viewer.getDocument().get(prefixStart - 1, 1))) { //$NON-NLS-1$
			final String last = getLastWord(viewer, prefixStart - 1);
			Collection procs = proceduresMap.getCollection(last.toUpperCase());
			if (procs != null) {
				for (Object o : procs) {
					IProcedure p = (IProcedure) o;
					if (p.getName().toUpperCase().startsWith(prefix)) {
						result.add(new CompletionProposal(p.getName(), prefixStart, offset
								- prefixStart, p.getName().length(), ImageFactory.getImage(p
								.getType().getTinyIcon()), p.getName() + " - procedure", null, "")); //$NON-NLS-2$
					}
				}
			}
		} else {
			Collection procs = proceduresMap.getCollection(""); //$NON-NLS-1$
			if (procs != null) {
				for (Object o : procs) {
					IProcedure p = (IProcedure) o;
					if (p.getName().toUpperCase().startsWith(prefix)) {
						result.add(new CompletionProposal(p.getName(), prefixStart, offset
								- prefixStart, p.getName().length(), ImageFactory.getImage(p
								.getType().getTinyIcon()), p.getName() + " - procedure", null, "")); //$NON-NLS-2$
					}
				}
			}
			for (IPackage p : packageList) {
				if (p.getName().toUpperCase().startsWith(prefix)) {
					result.add(new CompletionProposal(p.getName(), prefixStart, offset
							- prefixStart, p.getName().length(), ImageFactory.getImage(p.getType()
							.getTinyIcon()), p.getName() + " - package", null, "")); //$NON-NLS-2$
				}
			}
		}
	}

	/**
	 * Adds proposal from the specified package. This includes global package variables, parameter
	 * declaration of the procedure and procedure inner variable declaration.
	 * 
	 * @param pkg
	 * @param prefix
	 * @param result
	 * @param offset
	 * @param prefixStart
	 */
	private void addPackageInnerProposals(IPackage pkg, String prefix,
			List<ICompletionProposal> result, int offset, int prefixStart) {
		// Getting entity
		Object entity = pkg.getParseData().getEntity(offset);
		if (entity instanceof LightProcedure) {
			final LightProcedure p = (LightProcedure) entity;
			addParametersProposal(p, prefix, result, offset, prefixStart);
		}
		IVariableContainer varContainer = null;
		if (entity instanceof IVariableContainer) {
			// Adding proposals from the entity which contains proposals
			varContainer = (IVariableContainer) entity;
			addVariablesProposal(varContainer, prefix, result, offset, prefixStart);
		}

		// Adding package variables
		addVariablesProposal(pkg, prefix, result, offset, prefixStart);

	}

	/**
	 * Indicates if the given offset is inside a table declaration segment.
	 * 
	 * @param offset offset to check
	 * @param r the result of the parse of the SQL statement at this offset
	 * @return <code>true</code> if the offset is inside a table declaration segment, else
	 *         <code>false</code>.
	 */
	private boolean matchTableSegment(int offset, DMLParseResult r) {
		if (r == null) {
			return false;
		}
		for (Segment s : r.getTableSegments()) {
			if (s.includes(offset)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds the column proposals from the given table alias to the list of
	 * {@link ICompletionProposal} using the specified parameters.
	 * 
	 * @param result the array of {@link ICompletionProposal} to fill with column proposals
	 * @param a the table alias to use for retrieving columns definitions
	 * @param prefix the prefix already entered by the user which should match the start of the
	 *        column name being added.
	 * @param prefixStart the prefix offset
	 * @param offset the caret offset
	 * @param appendAlias whether or not the method should append any existing table alias before
	 *        the column name
	 */
	private void addColumnsProposal(List<ICompletionProposal> result, TableAlias a, String prefix,
			int prefixStart, int offset, boolean appendAlias) {
		if (a != null && a.getTable() != null) {
			for (IBasicColumn c : a.getTable().getColumns()) {
				if (c.getName().toUpperCase().startsWith(prefix)) {
					String proposal = (a.getTableAlias() != null && appendAlias ? a.getTableAlias()
							+ "." : "") //$NON-NLS-1$ //$NON-NLS-2$
							+ c.getName();
					result.add(new CompletionProposal(proposal, prefixStart, offset - prefixStart,
							proposal.length(),
							DatatypeHelper.getDatatypeIcon(c.getDatatype(), true), c.getName()
									+ " - Column of '" + c.getParent().getName() + "'", null, "")); //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
		// Adding sequences names
		List<IVersionable<?>> seqV = VersionHelper.getAllVersionables(
				VersionHelper.getCurrentView(), IElementType.getInstance(ISequence.TYPE_ID));
		for (IVersionable<?> seq : seqV) {
			final String name = seq.getName();
			if (name.toUpperCase().startsWith(prefix)) {
				result.add(new CompletionProposal(name, prefixStart, offset - prefixStart, name
						.length(), ImageFactory.getImage(seq.getType().getIcon()), name + " - " //$NON-NLS-1$
						+ seq.getType().getName(), null, "")); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Adds variable proposals from the given variable container.
	 * 
	 * @param varContainer the variable container from which we extract the proposals
	 * @param prefix prefix of the user-typed text
	 * @param result collection of ICompletionProposal to fill in
	 * @param offset offset at which the proposal should be inserted
	 * @param prefixStart the start of the prefix (semi-entered text to complete)
	 */
	private void addVariablesProposal(IVariableContainer varContainer, String prefix,
			List<ICompletionProposal> result, int offset, int prefixStart) {
		for (IVariable v : varContainer.getVariables()) {
			if (v.getName().toUpperCase().startsWith(prefix)) {
				result.add(new CompletionProposal(v.getName().toLowerCase(), prefixStart, offset
						- prefixStart, v.getName().length(), SQLGenImages.ICON_PUBLIC_FIELD, v
						.getName().toLowerCase()
						+ " " //$NON-NLS-1$
						+ v.getDatatypeName()
						+ " - Variable of "
						+ ((INamedObject) varContainer).getName() + "", null, "")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * Adds variable proposals from the given variable container.
	 * 
	 * @param varContainer the variable container from which we extract the proposals
	 * @param prefix prefix of the user-typed text
	 * @param result collection of ICompletionProposal to fill in
	 * @param offset offset at which the proposal should be inserted
	 * @param prefixStart the start of the prefix (semi-entered text to complete)
	 */
	private void addParametersProposal(IProcedure procedure, String prefix,
			List<ICompletionProposal> result, int offset, int prefixStart) {
		for (IProcedureParameter p : procedure.getParameters()) {
			if (p.getName().toUpperCase().startsWith(prefix)) {
				result.add(new CompletionProposal(p.getName().toLowerCase(), prefixStart, offset
						- prefixStart, p.getName().length(), SQLGenImages.ICON_PUBLIC_FIELD,
						p.getName().toLowerCase()
								+ " " //$NON-NLS-1$
								+ p.getDatatype().getName()
								+ " - Argument of " + procedure.getName() + "", null, "")); //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		IContextInformation[] result = new IContextInformation[5];
		for (int i = 0; i < result.length; i++)
			result[i] = new ContextInformation("context " + i, //$NON-NLS-1$
					"info " + i); //$NON-NLS-1$
		return result;

	}

	/**
	 * Retrieves the prefix from the given offset
	 * 
	 * @param viewer text viewer
	 * @param offset offset at which the content assist has been triggerred
	 * @return the string prefix
	 * @throws BadLocationException
	 */
	private int getPrefixStart(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		if (doc == null || offset > doc.getLength())
			return -1;

		int length = 0;
		try {
			while (--offset >= 0 && Character.isJavaIdentifierPart(doc.getChar(offset)))
				length++;

			return offset + 1;
		} catch (BadLocationException e) {
			LOGGER.debug("Error while retrieving completion prefix: BadLocation.");
			return -1;
		}

	}

	/**
	 * Retrieves the last word immediately before the specified offset in the text viewer. Any space
	 * will be ignored.
	 * 
	 * @param viewer viewer to look into
	 * @param offset offset to start the search from.
	 * @return the last word.
	 */
	private String getLastWord(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		if (doc == null || offset > doc.getLength())
			return ""; //$NON-NLS-1$

		int length = 0;
		try {
			boolean wordStarted = false;
			while (--offset >= 0
					&& (!wordStarted || wordStarted
							&& Character.isJavaIdentifierPart(doc.getChar(offset)))) {
				// We continue on spaces
				if (' ' == doc.getChar(offset)) {
					continue;
				} else if (',' == doc.getChar(offset)) {
					int lastWordStart = getPrefixStart(viewer, offset - 1);
					return getLastWord(viewer, lastWordStart);
				} else if (!wordStarted) {
					wordStarted = true;
				}
				length++;
			}

			return doc.get(offset + 1, length);
		} catch (BadLocationException e) {
			LOGGER.debug("Error while retrieving completion prefix: BadLocation.");
			return ""; //$NON-NLS-1$
		}
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.', '(', ':', ',', '=' };
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return new char[] { '#' };
	}

	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}

	public String getErrorMessage() {
		return null;
	}

	public void resyncPackage() {
	}

	/**
	 * This method parses the SQL statement defined at the current start offset. The method will
	 * retrieve any SQL statement which encapsulate the start offset, parse it and return the result
	 * of this parse for completion proposals.
	 * 
	 * @param viewer viewer of the document to parse
	 * @param start start offset
	 * @return a {@link DMLParseResult} which contains information about the parse of the found SQL
	 *         statement, or <code>null</code> if no SQL statement has been found from the given
	 *         start offset.
	 */
	private DMLParseResult parseSQL(ITextViewer viewer, int start) {
		// Retrieving the corresponding statement start
		IDocument doc = new Document();
		doc.set(viewer.getDocument().get() + " "); //$NON-NLS-1$

		FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(doc);
		try {
			IRegion lastSemicolonRegion = finder.find(start - 1, ";", false, false, false, false); //$NON-NLS-1$
			if (lastSemicolonRegion == null) {
				lastSemicolonRegion = new Region(0, 1);
			}
			IRegion selectRegion = finder.find(lastSemicolonRegion.getOffset(),
					"SELECT|INSERT|UPDATE|DELETE", true, false, false, true); //$NON-NLS-1$

			IRegion endSemicolonRegion = finder.find(start == doc.getLength() ? start - 1 : start,
					";", true, false, false, false); //$NON-NLS-1$
			if (endSemicolonRegion == null) {
				endSemicolonRegion = new Region(doc.getLength() - 1, 0);
			}
			if (selectRegion == null || lastSemicolonRegion == null || endSemicolonRegion == null) {
				return null;
			}
			// The select must be found after the first semicolon, else it is not the
			// same SQL statement
			if (selectRegion.getOffset() >= lastSemicolonRegion.getOffset()
					&& endSemicolonRegion.getOffset() >= selectRegion.getOffset()) {
				DMLScanner scanner = new DMLScanner(parser);
				scanner.setRange(doc, selectRegion.getOffset(), endSemicolonRegion.getOffset()
						- selectRegion.getOffset());
				IToken token = scanner.nextToken();
				DMLParseResult result = new DMLParseResult();
				Stack<DMLParseResult> stack = new Stack<DMLParseResult>();
				Map<Segment, DMLParseResult> results = new HashMap<Segment, DMLParseResult>();
				while (!token.isEOF()) {
					// Counting parenthethis
					if (token == DMLScanner.LEFTPAR_TOKEN) {
						result.parCount++;
					} else if (token == DMLScanner.RIGHTPAR_TOKEN) {
						result.parCount--;
					}

					if (token == DMLScanner.SELECT_TOKEN) { // && (result.tableSegStart>0 ||
						// result.whereSegStart>0)) {
						stack.push(result);
						result = new DMLParseResult();
						result.stackStart = scanner.getTokenOffset();
					} else if (token == DMLScanner.RIGHTPAR_TOKEN && result.parCount < 0) { // &&
						// stack.size()>0)
						// {
						results.put(new Segment(result.stackStart, scanner.getTokenOffset()
								- result.stackStart), result);
						result = stack.pop();
					} else if (token == DMLScanner.INSERT_TOKEN) {
						result.ignoreInto = false;
					} else if (token == DMLScanner.FROM_TOKEN || token == DMLScanner.UPDATE_TOKEN
							|| (token == DMLScanner.INTO_TOKEN && !result.ignoreInto)) {
						result.ignoreInto = true;
						// We have a table segment start
						result.tableSegStart = scanner.getTokenOffset();
						result.tableStartToken = token;
					} else if (token == DMLScanner.WORD_TOKEN && result.tableSegStart > 0) {
						// We are in a table segment so we instantiate appropriate table references
						// and aliases
						// in the parse result
						if (result.lastAlias == null) {
							// This is a new table definition, we add it
							result.lastAlias = new TableAlias(doc.get(scanner.getTokenOffset(),
									scanner.getTokenLength()).toUpperCase());
							result.lastAlias
									.setTable(tablesMap.get(result.lastAlias.getTableName()));
							result.addFromTable(result.lastAlias);
						} else if (result.lastAlias.getTableAlias() == null) {
							// This is an alias of a defined table
							final String alias = doc.get(scanner.getTokenOffset(),
									scanner.getTokenLength());
							final List<String> reservedWords = parser.getTypedTokens().get(
									ISQLParser.DML);
							if (!reservedWords.contains(alias.toUpperCase())) {
								result.lastAlias.setAlias(alias);
							} else {
								result.lastAlias = null;
							}
						}
					} else if (token == DMLScanner.COMMA_TOKEN) {
						// On a comma, we reset any table reference
						result.lastAlias = null;
					} else if (token == DMLScanner.DML_TOKEN) {
						result.lastAlias = null;
						if (result.tableSegStart != -1) {
							int tableSegEnd = scanner.getTokenOffset();
							result.addTableSegment(new Segment(result.tableSegStart, tableSegEnd
									- result.tableSegStart));
							result.tableSegStart = -1;
						}
					} else if (result.tableSegStart != -1
							&& ((result.tableStartToken == DMLScanner.FROM_TOKEN && token == DMLScanner.WHERE_TOKEN)
									|| (result.tableStartToken == DMLScanner.UPDATE_TOKEN && token == DMLScanner.SET_TOKEN) || (result.tableStartToken == DMLScanner.INTO_TOKEN && token == DMLScanner.LEFTPAR_TOKEN))) {
						// We have matched a table segment end, so we close the segment
						// and we add it to the parse result's table segments
						int tableSegEnd = scanner.getTokenOffset();
						result.addTableSegment(new Segment(result.tableSegStart, tableSegEnd
								- result.tableSegStart));
						result.tableSegStart = -1;
						if (token == DMLScanner.WHERE_TOKEN) {
							result.whereSegStart = scanner.getTokenOffset()
									+ scanner.getTokenLength();
						}
					}
					token = scanner.nextToken();
				}
				// If the table segment is still opened, we close it at the end of the SQL statement
				if (result.tableSegStart > -1) {
					int tableSegEnd = endSemicolonRegion.getOffset();
					result.addTableSegment(new Segment(result.tableSegStart, tableSegEnd
							- result.tableSegStart + 1));
				}
				// Locating the appropriate result
				for (Segment s : results.keySet()) {
					if (s.getOffset() <= start && s.getOffset() + s.getLength() > start) {
						return results.get(s);
					}
				}
				return result;
			}
		} catch (BadLocationException e) {
			LOGGER.debug("Problems while retrieving SQL statement");
		}
		return null;
	}

}
