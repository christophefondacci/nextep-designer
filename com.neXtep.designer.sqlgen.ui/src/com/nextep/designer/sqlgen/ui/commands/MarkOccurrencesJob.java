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
package com.nextep.designer.sqlgen.ui.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import com.nextep.designer.sqlgen.ui.editors.SQLEditor;

/**
 * This jobs finds and marks (highlighted annotations) occurrences of a text within a SQL editor.
 * The {@link SQLEditor} is in charge of triggerring this job whenever the selection changes.
 * 
 * @author Christophe Fondacci
 */
public class MarkOccurrencesJob extends Job {

	private SQLEditor editor;
	private String selection;
	private static final Log log = LogFactory.getLog(MarkOccurrencesJob.class);
	private static final String ANNOTATION_TYPE_OCCURRENCES = "com.neXtep.designer.sqlgen.ui.occurrences";

	public MarkOccurrencesJob(SQLEditor editor, String selection) {
		super("Updating occurrences...");
		this.editor = editor;
		this.selection = selection;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// Removing previous occurrences
		IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		IAnnotationModel annotationModel = editor.getDocumentProvider().getAnnotationModel(
				editor.getEditorInput());
		// We should always have an annotation model
		if (annotationModel == null) {
			log.warn("Unable to anotate SQL editor, contact neXtep Software if the problem persists.");
			return Status.CANCEL_STATUS;
		}
		// Removing
		synchronized (annotationModel) {
			Iterator<?> it = annotationModel.getAnnotationIterator();
			final Collection<Annotation> toRemove = new ArrayList<Annotation>();
			while (it.hasNext()) {
				Annotation ann = (Annotation) it.next();
				if (ANNOTATION_TYPE_OCCURRENCES.equals(ann.getType())) {
					synchronized (annotationModel) {
						toRemove.add(ann);
					}

				}
			}

			// Now looking for the text to highlight, regexp
			String source = doc.get();
			if (selection.contains("\n")) {
				return Status.OK_STATUS;
			}
			try {
				final Pattern p = Pattern.compile("(\\W|\\s|^)"
						+ FindReplaceDocumentAdapter.escapeForRegExPattern(selection.toUpperCase())
						+ "(\\W|\\s|$)");
				final Matcher m = p.matcher(source.toUpperCase());
				Map<Annotation, Position> annotationMap = new HashMap<Annotation, Position>();
				while (m.find()) {
					// Since we may have captured enclosing characters, we localize our selection
					// string inside the found pattern to properly highlight it
					String capturedText = m.group();
					int offset = capturedText.indexOf(selection.toUpperCase());
					final Position annPosition = new Position(m.start() + offset,
							selection.length());
					final Annotation annotation = new Annotation(ANNOTATION_TYPE_OCCURRENCES,
							false, selection);
					annotationMap.put(annotation, annPosition);

				}
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(
						toRemove.toArray(new Annotation[toRemove.size()]), annotationMap);
			} catch (RuntimeException e) {
				// Should be silent on errors as it can be really annoying
				log.debug("Problems while trying to mark occurrences", e);
			}
		}
		return Status.OK_STATUS;
	}

}
