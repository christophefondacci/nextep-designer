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
package com.nextep.designer.sqlgen.ui.editors;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.dbgm.sql.TextPosition;
import com.nextep.designer.sqlgen.ui.SQLEditorInput;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.sqlgen.ui.commands.MarkOccurrencesJob;
import com.nextep.designer.sqlgen.ui.editors.sql.SQLContentOutlinePage;
import com.nextep.designer.sqlgen.ui.model.IConnectable;
import com.nextep.designer.sqlgen.ui.services.IBreakpoint;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;

/**
 * @author Christophe Fondacci
 */
public class SQLEditor extends AbstractDecoratedTextEditor implements IEventListener {

	private static final Log log = LogFactory.getLog(SQLEditor.class);
	private SQLContentOutlinePage outlinePage;
	public final static String EDITOR_ID = "com.neXtep.designer.sqlgen.ui.SQLEditor"; //$NON-NLS-1$
	private boolean isSaving = false;
	/** The projection support */
	private ProjectionSupport fProjectionSupport;
	private Collection<ProjectionAnnotation> annotations = new ArrayList<ProjectionAnnotation>();

	public SQLEditor() {
		super();
		setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope" }); //$NON-NLS-1$
		// configureInsertMode(SMART_INSERT, false);
		setDocumentProvider(new SQLDocumentProvider());
	}

	/**
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeEditor()
	 */
	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setSourceViewerConfiguration(new SQLSourceViewerConfiguration(this));

	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		outlinePage = new SQLContentOutlinePage(getDocumentProvider(), this);
		if (input instanceof ISQLEditorInput) {
			// We register the editor as a listener to our model to handle
			// Model changes (checkout / checkin)
			final ISQLEditorInput<IObservable> sqlInput = (ISQLEditorInput<IObservable>) input;
			Designer.getListenerService().registerListener(this, (IObservable) sqlInput.getModel(),
					new IEventListener() {

						@Override
						public void handleEvent(final ChangeEvent event, final IObservable source,
								final Object data) {
							getSite().getShell().getDisplay().syncExec(new Runnable() {

								@Override
								public void run() {
									if (source != null) {
										switch (event) {
										case UPDATES_LOCKED:
										case UPDATES_UNLOCKED:
											log.debug("Switching SQL editor model"); //$NON-NLS-1$
											sqlInput.setModel(source);
											updateDocumentFromInput(sqlInput);
											resetDocument();
											break;
										case SOURCE_CHANGED:
										case MODEL_CHANGED:
											// We only refresh editor contents if we're not in a
											// save operation, else multi-sql content (like package)
											// might erase other part's modifications
											if (!isDirty()) {
												updateDocumentFromInput(sqlInput);
												resetDocument();
											}
											break;
										default:
											updateStateAndActions(sqlInput);
											break;
										}
									} else {
										updateStateAndActions(sqlInput);
									}
									addFolding();
									refreshAnnotations();
								}
							});
						}
					});
			setInput(input);
		} else if (input instanceof FileStoreEditorInput) {
			// Converting an external file input to a standard SQLScript / ISQLEditorInput
			FileStoreEditorInput i = (FileStoreEditorInput) input;
			ISQLScript s = new SQLScript(i.getURI().getPath());
			setInput(new SQLEditorInput(s));
		}
	}

	/**
	 * Updates the underlying editor document from the current content of the editor input.
	 * 
	 * @param sqlInput {@link ISQLEditorInput} to retrieve content from
	 */
	private void updateDocumentFromInput(ISQLEditorInput<?> sqlInput) {
		if (!isDirty()) {
			// We save current selection which would be reset by the document update
			int selectionOffset = 0;
			int length = 0;
			final ISelection sel = getSelectionProvider().getSelection();
			if (sel instanceof ITextSelection) {
				final ITextSelection textSel = (ITextSelection) sel;
				selectionOffset = textSel.getOffset();
				length = textSel.getLength();
			}
			// Updating document
			getDocumentProvider().getDocument(sqlInput).set(
					sqlInput.getSql() == null ? "" : sqlInput.getSql()); //$NON-NLS-1$
			setPartName(sqlInput.getName());
			updateStateAndActions(sqlInput);
			getDocumentProvider().changed(sqlInput);
			// Restoring selection
			if (selectionOffset > 0 || length > 0) {
				ITextSelection textSel = new TextSelection(selectionOffset, length);
				try {
					getSelectionProvider().setSelection(textSel);
				} catch (RuntimeException e) {
					log.warn("Could not restore previous selection", e);
				}
			}
		}
	}

	private void updateStateAndActions(ISQLEditorInput<?> sqlInput) {
		updateState(sqlInput);
		updateStateDependentActions();
		updateContentDependentActions();
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#dispose()
	 */
	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
		IEditorInput input = getEditorInput();
		if (input instanceof IConnectable) {
			final IConnectable connectableInput = (IConnectable) input;
			Connection conn = connectableInput.getSqlConnection();
			if (conn != null) {
				try {
					final DatabaseMetaData md = conn.getMetaData();
					log.info("Disconnecting from " + md.getURL());
					conn.close();
					connectableInput.setSqlConnection(null);
				} catch (SQLException e) {
					log.warn("Unable to close connection: " + e.getMessage(), e);
				}
			}
		}
		super.dispose();
	}

	/**
	 * @see org.eclipse.ui.editors.text.TextEditor#createActions()
	 */
	@Override
	protected void createActions() {
		super.createActions();

		IAction action = new TextOperationAction(SQLMessages.getResourceBundle(), "ContentAssist.", //$NON-NLS-1$
				this, ISourceViewer.CONTENTASSIST_PROPOSALS);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssist", action); //$NON-NLS-1$
		markAsStateDependentAction("ContentAssist", true); //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextEditor#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (IContentOutlinePage.class == adapter) {
			return outlinePage;
		}
		if (fProjectionSupport != null) {
			Object a = fProjectionSupport.getAdapter(getSourceViewer(), adapter);
			if (a != null)
				return a;
		}
		if (getDocumentProvider() != null && adapter == IAnnotationModel.class) {
			Object a = getDocumentProvider().getAnnotationModel(getEditorInput());
			if (a != null) {
				return a;
			}
		}
		return super.getAdapter(adapter);
	}

	/**
	 * @see org.eclipse.ui.editors.text.TextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		menu.add(new Separator("sql")); //$NON-NLS-1$
		menu.add(new Separator("profiling")); //$NON-NLS-1$
		menu.add(new Separator());
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, "ContentAssist"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		// We should never loose editor content, so if we need a failsafe save, we save as on the
		// filesystem
		try {
			isSaving = true;
			if (Designer.getTerminationSignal()) {
				doSaveAs();
			} else {
				super.doSave(progressMonitor);
				final IPackage pkg = getPackage();
				if (pkg != null) {
					DBGMHelper.parse(pkg);
					addFolding();
				}
				refreshAnnotations();
				if (outlinePage != null) {
					outlinePage.update();
				}
			}
			updateDocumentFromInput((ISQLEditorInput<?>) getEditorInput());
			resetDocument();
		} finally {
			isSaving = false;
		}
	}

	@Override
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		final IEditorInput input = getEditorInput();
		FileDialog d = new FileDialog(this.getSite().getShell(), SWT.SAVE);
		d.setFilterExtensions(new String[] { "*.sql" }); //$NON-NLS-1$
		d.setOverwrite(true);
		d.setFileName(input.getName());
		d.setText(SQLMessages.getString("editor.sql.saveAsSqlFilePrompt")); //$NON-NLS-1$
		String fileLocation = d.open();
		if (fileLocation != null) {
			ISQLScript s = null;
			// If our editor is an editor over a ISQLScript already external, we use it to save
			// our contents
			if (input instanceof IModelOriented<?>) {
				final Object model = ((IModelOriented<?>) input).getModel();
				if (model instanceof ISQLScript) {
					final ISQLScript script = (ISQLScript) model;
					if (script.isExternal()) {
						s = script;
					}
				}
			}
			if (s == null) {
				s = CorePlugin.getTypedObjectFactory().create(ISQLScript.class);
			}
			s.setExternal(true);
			if (fileLocation.toLowerCase().endsWith(".sql")) { ////$NON-NLS-1$
				fileLocation = fileLocation.substring(0, fileLocation.length() - 4);
			}
			File f = new File(fileLocation);
			s.setDirectory(f.getParent());
			s.setName(f.getName());
			s.setSql(getDocumentProvider().getDocument(getEditorInput()).get());
			ControllerFactory.getController(s).save(s);
			setPartName(input.getName());
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Convenience method that return the underlying package, if we are editing a package
	 * 
	 * @return
	 */
	private IPackage getPackage() {
		if (getEditorInput() instanceof ISQLEditorInput<?>) {
			if (((ISQLEditorInput<?>) getEditorInput()).getModel() instanceof IPackage) {
				return (IPackage) ((ISQLEditorInput<?>) getEditorInput()).getModel();
			}
		}
		return null;
	}

	// private void removeFolding() {
	// IAnnotationModel model= (IAnnotationModel)getAdapter(ProjectionAnnotationModel.class);
	// if (model != null) {
	// for(ProjectionAnnotation a : annotations) {
	// model.removeAnnotation(a);
	// }
	// }
	// }
	private void addFolding() {
		IAnnotationModel model = (IAnnotationModel) getAdapter(ProjectionAnnotationModel.class);
		Map<ProjectionAnnotation, Position> annMap = new HashMap<ProjectionAnnotation, Position>();
		final IPackage pkg = getPackage();
		if (model != null && pkg != null) {
			IParseData data = pkg.getParseData();
			for (IProcedure proc : pkg.getProcedures()) {
				TextPosition p = data.getPosition(proc);
				if (p != null) {
					Position pos = new Position(p.getOffset(), p.getLength());
					ProjectionAnnotation a = new ProjectionAnnotation();
					annMap.put(a, pos);
				} else {
					log.debug("WARN: Empty position for procedure: " + proc.getName()); //$NON-NLS-1$
				}
			}
			((IAnnotationModelExtension) model).replaceAnnotations(annotations.isEmpty() ? null
					: annotations.toArray(new ProjectionAnnotation[annotations.size()]), annMap);
			annotations = annMap.keySet();
		}
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createSourceViewer(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.jface.text.source.IVerticalRuler, int)
	 */
	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		fAnnotationAccess = getAnnotationAccess();
		fOverviewRuler = createOverviewRuler(getSharedColors());

		ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(),
				isOverviewRulerVisible(), styles);
		// ensure decoration support has been created and configured.
		getSourceViewerDecorationSupport(viewer);

		return viewer;
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
		fProjectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		fProjectionSupport
				.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
		fProjectionSupport
				.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
		fProjectionSupport.install();
		viewer.doOperation(ProjectionViewer.TOGGLE);
		final IPackage pkg = getPackage();
		if (pkg != null) {
			DBGMHelper.parse(pkg);
		}
		addFolding();
		installSelectionListeners();
		// Setting title
		setPartName(getEditorInput().getName());
		SQLEditorUIServices.getInstance().addListener(this);
		getVerticalRuler().getControl().addMouseListener(
				new RulerMouseListener(getVerticalRuler(), this));
		refreshAnnotations();
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (getDocumentProvider() == null)
			return;
		IAnnotationModel ann = getDocumentProvider().getAnnotationModel(getEditorInput());
		if (ann == null)
			return;

		try {
			switch (event) {
			case BREAKPOINT_ADDED:
				IBreakpoint bp = (IBreakpoint) data;
				Annotation a = new Annotation("org.eclipse.ui.workbench.texteditor.info", false, //$NON-NLS-1$
						SQLMessages.getString("editor.sql.breakpointText") + bp.getLine()); //$NON-NLS-1$
				ann.addAnnotation(a,
						new Position(getDocumentProvider().getDocument(getEditorInput())
								.getLineOffset(bp.getLine())));
				bp.setAnnotation(a);
				break;
			case BREAKPOINT_REMOVED:
				IBreakpoint b = (IBreakpoint) data;
				if (b.getAnnotation() != null) {
					ann.removeAnnotation(b.getAnnotation());
				}
				break;
			}
		} catch (BadLocationException e) {
			log.debug("BadLocation while adding breakpoint", e); //$NON-NLS-1$
		}

	}

	@Override
	public void setFocus() {
		super.setFocus();
		refreshAnnotations();
	}

	/**
	 * Refreshes markers annotations
	 */
	private void refreshAnnotations() {
		SQLEditorUIServices.getInstance().annotateInput(getDocumentProvider(), getEditorInput());
	}

	/**
	 * Installs the selection listeners on the current document / editor so that proper listeners
	 * will catch the selection events coming from the editor. Typically, this is where we add the
	 * listener which marks occurrences of the selected text.
	 */
	protected void installSelectionListeners() {
		((IPostSelectionProvider) this.getEditorSite().getSelectionProvider())
				.addPostSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						// Retriveing selected text
						ISelection s = event.getSelection();
						if (s instanceof ITextSelection) {
							final String selectedText = ((ITextSelection) s).getText();
							if (selectedText != null && !selectedText.trim().equals("")) { //$NON-NLS-1$
								MarkOccurrencesJob markerJob = new MarkOccurrencesJob(
										SQLEditor.this, selectedText);
								// Running our job
								markerJob.schedule();
							}

						}
					}
				});
	}

	private void resetDocument() {
		try {
			getDocumentProvider().resetDocument(getEditorInput());
		} catch (CoreException e) {
			log.error(SQLMessages.getString("editor.sql.modelSwitchError") //$NON-NLS-1$
					+ e.getMessage(), e);
		}
	}

	public boolean isSaving() {
		return isSaving;
	}
}
