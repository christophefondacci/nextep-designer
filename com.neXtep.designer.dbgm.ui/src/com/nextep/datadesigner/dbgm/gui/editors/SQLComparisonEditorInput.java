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
package com.nextep.datadesigner.dbgm.gui.editors;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.texteditor.IDocumentProvider;
import com.nextep.datadesigner.impl.StringAttribute;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.gui.rcp.ComparisonSideToSideEditor;
import com.nextep.datadesigner.vcs.gui.rcp.IComparisonItemEditorInput;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * A SQL comparison editor input takes a {@link IComparisonItem} as its source
 * of information for providing the SQL and considers that every sub item of the
 * {@link IComparisonItem} is a line of SQL text.<br>
 * It also comes with an annotation model able to highlight the editor from its
 * comparison information.<br>
 * Note that this kind of input is not saveable, nor persistable and querying
 * its model will return <code>null</code>. Since this input is always used
 * within a {@link ComparisonSideToSideEditor}, there is no image description,
 * tooltip or name.
 * 
 * @author Christophe Fondacci
 */
public class SQLComparisonEditorInput implements ISQLEditorInput<IObservable>,
		IComparisonItemEditorInput, IAnnotatedInput {

	private final static Log log = LogFactory.getLog(SQLComparisonEditorInput.class);
	private IComparisonItem comparisonItem;
	private ComparedElement comparedElement;
	private final ISQLEditorInput<? extends IObservable> wrappedInput;
	private final static String ANNOTATION_MODIFIED = "com.neXtep.designer.sqlgen.ui.diff.modified";
	private final static String ANNOTATION_NEW = "com.neXtep.designer.sqlgen.ui.diff.new";
	private final static String ANNOTATION_REMOVED = "com.neXtep.designer.sqlgen.ui.diff.removed";
	private final String attribute;

	public SQLComparisonEditorInput(ISQLEditorInput<? extends IObservable> wrappedInput) {
		this(wrappedInput, null);
	}

	/**
	 * A constructor which initializes this {@link SQLComparisonEditorInput} on
	 * a specific child item of the {@link IComparisonItemEditorInput}.
	 * 
	 * @param wrappedInput
	 *            the original editor input
	 * @param attribute
	 *            the sub attribute to consider when a {@link IComparisonItem}
	 *            is provided.
	 */
	public SQLComparisonEditorInput(ISQLEditorInput<? extends IObservable> wrappedInput,
			String attribute) {
		this.wrappedInput = wrappedInput;
		this.attribute = attribute;
	}

	@Override
	public IComparisonItem getComparisonItem() {
		return comparisonItem;
	}

	@Override
	public void setComparisonItem(IComparisonItem item) {
		if (attribute == null || item == null) {
			this.comparisonItem = item;
		} else {
			this.comparisonItem = item.getAttribute(attribute);
		}
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return wrappedInput.getImageDescriptor();
	}

	@Override
	public String getName() {
		return wrappedInput.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return wrappedInput.getToolTipText();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public String getSql() {
		StringBuffer buf = new StringBuffer(200);
		if (comparisonItem != null) {
			if (!comparisonItem.getSubItems().isEmpty()) {
				for (IComparisonItem item : comparisonItem.getSubItems()) {
					if (item instanceof ComparisonAttribute) {
						StringAttribute attr = (StringAttribute) (comparedElement.isSource() ? item
								.getSource() : item.getTarget());
						if (attr != null) {
							buf.append(attr.getValue());
							buf.append("\n");
						} else {
							buf.append("\n");
						}
					}
				}
			} else {
				buf.append(wrappedInput.getSql());
			}
		} else {
			buf.append(wrappedInput.getSql());
		}
		return buf.toString();
	}

	@Override
	public void save(IDocumentProvider provider) {

	}

	@Override
	public void setSql(String sql) {

	}

	@Override
	public IObservable getModel() {
		return wrappedInput.getModel();
	}

	@Override
	public void setModel(IObservable model) {

	}

	@Override
	public void setComparedElement(ComparedElement comparedElement) {
		this.comparedElement = comparedElement;
	}

	@Override
	public ComparedElement getComparedElement() {
		return comparedElement;
	}

	@Override
	public Map<Annotation, Position> getAnnotationMap(IDocument doc) {
		IComparisonItem item = getComparisonItem();
		final Map<Annotation, Position> toAdd = new HashMap<Annotation, Position>();

		if (item != null) {
			int line = 0;
			String annType = null;
			String lastAnnType = null;
			int startOffset = 0;
			for (IComparisonItem i : item.getSubItems()) {
				switch (i.getDifferenceType()) {
				case DIFFER:
					annType = ANNOTATION_MODIFIED;
					break;
				case MISSING_SOURCE:
					annType = ANNOTATION_REMOVED;
					break;
				case MISSING_TARGET:
					annType = ANNOTATION_NEW;
					break;
				default:
					annType = null;
					break;
				}
				if (annType != lastAnnType) {
					try {
						int endOffset = doc.getLineOffset(line);
						if (lastAnnType != null) {
							toAdd.put(new Annotation(lastAnnType, true, ""), new Position(
									startOffset, endOffset - startOffset));
						}
						startOffset = endOffset;
					} catch (BadLocationException e) {
						log.debug(e);
					}
				}
				lastAnnType = annType;
				line++;
			}
			if (annType != null) {
				toAdd.put(new Annotation(annType, true, ""), new Position(startOffset, doc
						.getLength()
						- startOffset));
			}
		}
		return toAdd;
	}

	@Override
	public Collection<String> getAnnotationTypes() {
		return Arrays.asList(ANNOTATION_NEW, ANNOTATION_REMOVED, ANNOTATION_MODIFIED);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SQLComparisonEditorInput) {
			if (getComparisonItem() == null) {
				return false;
			}
			return getComparisonItem().equals(((SQLComparisonEditorInput) obj).getComparisonItem())
					&& getComparedElement() == ((SQLComparisonEditorInput) obj)
							.getComparedElement();
		}
		return false;
	}
}
