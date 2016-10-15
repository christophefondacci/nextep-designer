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
package com.nextep.designer.sqlgen.ui.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.gui.editors.IAnnotatedInput;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.dbgm.sql.TextPosition;
import com.nextep.designer.sqlgen.ui.PackageEditorInput;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.sqlgen.ui.editors.SQLEditor;
import com.nextep.designer.sqlgen.ui.editors.SQLMultiEditor;
import com.nextep.designer.sqlgen.ui.impl.GlobalTextProvider;
import com.nextep.designer.sqlgen.ui.model.ITypedObjectTextProvider;
import com.nextep.designer.util.ValuedRunnable;

/**
 * @author Christophe Fondacci
 */
public class SQLEditorUIServices extends Observable {

	private final List<IBreakpoint> breakpoints;
	private static SQLEditorUIServices instance = null;
	private final static Log log = LogFactory.getLog(SQLEditorUIServices.class);

	private SQLEditorUIServices() {
		breakpoints = new ArrayList<IBreakpoint>();
	}

	public static SQLEditorUIServices getInstance() {
		if (instance == null) {
			instance = new SQLEditorUIServices();
		}
		return instance;
	}

	public void addBreakpoint(IBreakpoint bp) {
		breakpoints.add(bp);
		notifyListeners(ChangeEvent.BREAKPOINT_ADDED, bp);
	}

	public List<IBreakpoint> getBreakpoints() {
		return breakpoints;
	}

	public void removeBreakpoint(IBreakpoint bp) {
		breakpoints.remove(bp);
		notifyListeners(ChangeEvent.BREAKPOINT_REMOVED, bp);
	}

	public void toggleBreakpoint(IBreakpoint bp) {
		for (IBreakpoint b : breakpoints) {
			if (b.getTarget() == bp.getTarget() && b.getLine() == bp.getLine()) {
				removeBreakpoint(b);
				return;
			}
		}
		addBreakpoint(bp);
	}

	/**
	 * @return the proposal provider to use when looking for SQL proposals
	 */
	public ITypedObjectTextProvider getTypedObjectTextProvider() {
		// For now we don't handle refresh and we cache everything so
		// we instantiate a new provider each time.
		// TODO: handle refresh and provide the same provider each time
		// if(proposalProvider==null) {
		// proposalProvider = new GlobalTextProvider();
		// }
		return new GlobalTextProvider();
	}

	/**
	 * Opens the editor for a procedure contained inside a {@link IPackage}.
	 * This method will open the package editor and set the appropriate focus on
	 * the section defining the specified {@link IProcedure}.
	 * 
	 * @param pkg
	 *            the {@link IPackage} which contains the procedure
	 * @param proc
	 *            the {@link IProcedure} of this package to open
	 */
	public void openPackageProcedureEditor(IPackage pkg, IProcedure proc) {
		try {
			IEditorPart editor = PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage()
					.openEditor(new PackageEditorInput(pkg),
							"com.neXtep.designer.sqlgen.ui.packageEditor");
			TextPosition p = pkg.getParseData().getPosition(proc);
			if (p != null) {
				if (editor instanceof SQLMultiEditor) {
					((SQLMultiEditor) editor).getCurrentEditor().setHighlightRange(p.offset,
							p.length, true);
				} else {
					((AbstractTextEditor) editor).setHighlightRange(p.offset, p.length, true);
				}
			}
		} catch (PartInitException e) {
			throw new ErrorException(e);
		}
	}

	/**
	 * Retrieves compilation markers for a given object
	 * 
	 * @see SQLEditorUIServices#getCompilationMarkersFor(ITypedObject,
	 *      IDocument, String)
	 * @param o
	 *            object to retrieve compilation info for
	 * @param doc
	 *            document on which annotations will be displayed
	 * @return a map of annotation positions
	 */
	public Map<Annotation, Position> getCompilationMarkersFor(ITypedObject o, IDocument doc) {
		return getCompilationMarkersFor(o, doc, null);
	}

	/**
	 * Retrieves the compilation markers for a given object
	 * 
	 * @param o
	 *            a {@link ITypedObject} element to provide markers for
	 * @param doc
	 *            the document in which this object is displayed (used to
	 *            compute positions)
	 * @return a map of annotations positions
	 */
	public Map<Annotation, Position> getCompilationMarkersFor(ITypedObject o, IDocument doc,
			String externalType) {
		Map<Annotation, Position> annMap = new HashMap<Annotation, Position>();
		// Retrieving markers for our typed element
		Collection<IMarker> markers = Designer.getMarkerProvider().getMarkersFor(o);
		// No markers => exiting
		if (markers == null || markers.isEmpty())
			return null;
		// Processing markers and converting them to annotations
		for (IMarker marker : markers) {
			int line = 1;
			int col = 1;
			if (marker.getAttribute(IMarker.ATTR_LINE) != null) {
				line = (Integer) marker.getAttribute(IMarker.ATTR_LINE);
				if (line == 0)
					line = 1;
			}
			if (marker.getAttribute(IMarker.ATTR_COL) != null) {
				col = (Integer) marker.getAttribute(IMarker.ATTR_COL);
				if (col == 0)
					col = 1;
			}
			try {
				int offset = doc.getLineOffset(line - 1) + col - 1;
				// We stop the marker at the next statement delimiter
				int end = getNextSeparator(doc, offset);
				try {
					end = doc.getLineOffset(line);
				} catch (BadLocationException e) {
					log.debug("Unable to reach next line for SQL error annotation"); //$NON-NLS-1$
				}

				String annType = null;
				// Using eclipse built-in markers.
				if (marker.getMarkerType() == MarkerType.ERROR) {
					annType = "org.eclipse.ui.workbench.texteditor.error";
				} else {
					annType = "org.eclipse.ui.workbench.texteditor.warning";
				}
				// Only filling markers when the external type matches or when
				// no external type has
				// been provided
				if (externalType == null
						|| externalType.equals(marker.getAttribute(IMarker.ATTR_EXTERNAL_TYPE))) {
					// Filling our map
					annMap.put(new Annotation(annType, true, marker.getMessage()), new Position(
							offset, end - offset));
				}
			} catch (BadLocationException e) {
				// Non blocking error
				log.debug("Bad location while processing compilation markers", e);
			}
		}
		return annMap;
	}

	/**
	 * Retrieves the next separator position in the given document. This is used
	 * when displaying errors to underline the complete word pointed out by the
	 * error information
	 * 
	 * @param doc
	 *            document on which we work
	 * @param start
	 *            the separator will be searched from this start offset
	 * @return the offset of the next separator within the document
	 */
	private static int getNextSeparator(IDocument doc, int start) {
		try {
			char car = doc.getChar(start);
			while (start < doc.getLength() && car != ' ' && car != ';' && car != '\n'
					&& car != '\t' && car != ',') {
				car = doc.getChar(++start);
			}
			return start;
		} catch (BadLocationException e) {
			log.debug("BadLocation while retrieving next separator");
			return doc.getLength();
		}
	}

	@SuppressWarnings("unchecked")
	public void annotateInput(final IDocumentProvider docProvider, final IEditorInput input) {
		if (input instanceof IAnnotatedInput) {
			final Job j = new Job(SQLMessages.getString("annotation.job.name")) { //$NON-NLS-1$

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask(SQLMessages.getString("annotation.job.name"), 4); //$NON-NLS-1$
					final IAnnotatedInput annotatedInput = (IAnnotatedInput) input;
					final Collection<String> annotatedTypes = annotatedInput.getAnnotationTypes();
					final IDocument doc = docProvider.getDocument(input);
					final IAnnotationModel annModel = docProvider.getAnnotationModel(input);
					monitor.worked(1);
					final Collection<Annotation> toRemove = new ArrayList<Annotation>();
					synchronized (annModel) {
						Iterator<Annotation> it = annModel.getAnnotationIterator();
						while (it.hasNext()) {
							final Annotation a = it.next();
							if (annotatedTypes.contains(a.getType())) {
								toRemove.add(a);
							}
						}
						monitor.worked(1);
						Map<Annotation, Position> annMap = annotatedInput.getAnnotationMap(doc);
						monitor.worked(2);
						try {
							((IAnnotationModelExtension) annModel).replaceAnnotations(
									toRemove.toArray(new Annotation[toRemove.size()]), annMap);
						} catch (RuntimeException e) {
							// Logging a debug message only to avoid some error
							// popup to come up
							// from the background job
							log.debug(e.getMessage(), e);
						}
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			};
			j.schedule();
		}
	}

	/**
	 * A convenience method to retrieve the appropriate SQL editor ID. Wrapper
	 * scripts should be open by the {@link SQLMultiEditor} so that every
	 * wrapped script will be opened in a {@link SQLEditor} in a tab of a multi
	 * page editor.
	 * 
	 * @param input
	 *            the object being shown
	 * @return the editor id
	 */
	public static String getEditorId(Object input) {
		if (input instanceof SQLWrapperScript) {
			return SQLMultiEditor.EDITOR_ID;
		} else if (input instanceof IModelOriented<?>) {
			if (((IModelOriented<?>) input).getModel() instanceof SQLWrapperScript) {
				return SQLMultiEditor.EDITOR_ID;
			}
		}
		return SQLEditor.EDITOR_ID;
	}

	/**
	 * This method refreshed current annotation for the current visible editor.
	 * If several editors are visible, only the last one brought to top will be
	 * refreshed.
	 */
	public void annotateVisibleEditor() {
		final ValuedRunnable<IEditorPart> runnable = new ValuedRunnable<IEditorPart>() {

			@Override
			public void run() {
				IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().getActiveEditor();
				// We unwrap any multi page editor to get the current active
				// page
				if (editor instanceof MultiPageEditorPart) {
					final Object activeEditor = ((MultiPageEditorPart) editor).getSelectedPage();
					if (activeEditor instanceof IEditorPart) {
						editor = (IEditorPart) activeEditor;
					}
				}
				setValue(editor);
			}
		};
		Display.getDefault().syncExec(runnable);
		final IEditorPart editor = runnable.getValue();
		// Sometimes we won't have an editor here...
		if (editor != null) {
			final IEditorInput input = editor.getEditorInput();
			// If text base we annotate
			if (editor instanceof AbstractTextEditor) {
				IDocumentProvider provider = ((AbstractTextEditor) editor).getDocumentProvider();
				annotateInput(provider, input);
			}

		}
	}
}
