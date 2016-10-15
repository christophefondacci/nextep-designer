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
package com.nextep.designer.vcs.ui.compare;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.impl.StringAttribute;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.gui.rcp.ComparisonEditorInput;
import com.nextep.datadesigner.vcs.gui.rcp.ComparisonSideToSideEditor;
import com.nextep.datadesigner.vcs.gui.rcp.IComparisonItemEditorInput;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * This abstract class is provided as a base for {@link IComparisonEditorProvider} which want to
 * provide a multi-editor whose left pane will be the source and right pane will be the target
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractComparisonEditorProvider implements IComparisonEditorProvider {

	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"); //$NON-NLS-1$

	protected ITypedObject getComparedElement(IComparisonItem comparisonItem,
			ComparedElement comparedElement) {
		switch (comparedElement) {
		case SOURCE:
			return (ITypedObject) comparisonItem.getSource();
		case TARGET:
			return (ITypedObject) comparisonItem.getTarget();
		}
		return null;
	}

	/**
	 * This helper method adapts an original input to a comparison item editor input when available.
	 * 
	 * @param input original input
	 * @param compItem comparison information
	 * @param comparedElement SOURCE or TARGET
	 * @return adapted input or original when unadaptable
	 */
	protected IEditorInput adapt(IEditorInput input, IComparisonItem compItem,
			ComparedElement comparedElement) {
		final IComparisonItemEditorInput compContainer = (IComparisonItemEditorInput) input
				.getAdapter(IComparisonItemEditorInput.class);
		if (compContainer != null) {
			compContainer.setComparisonItem(compItem);
			compContainer.setComparedElement(comparedElement);
			return compContainer;
		} else {
			return input;
		}
	}

	@Override
	public IEditorInput getEditorInput(IComparisonItem compItem) {
		// Building the inputs we need to create Multi editor inputs
		final List<String> editorIds = new ArrayList<String>(2);
		final List<IEditorInput> editorInputs = new ArrayList<IEditorInput>(2);
		final List<String> messages = new ArrayList<String>();
		if (compItem.getType() == IElementType.getInstance(StringAttribute.TYPE_ID)) {
			return null;
		}
		// First (left/source) item input
		final IReferenceable source = compItem.getSource();
		if (source instanceof ITypedObject) {
			IEditorInput srcInput = getEditorInput(compItem, ComparedElement.SOURCE);
			String srcId = getEditorId(compItem, ComparedElement.SOURCE);
			if (srcInput != null && srcId != null) {
				editorInputs.add(srcInput);
				editorIds.add(srcId);
				// Non-volatile reference is a workspace reference
				if (!source.getReference().isVolatile()) {
					messages.add(MessageFormat.format(
							VCSUIMessages.getString("editor.comparison.currentElementTag"), //$NON-NLS-1$
							getQualifiedName((ITypedObject) source)));
				} else {
					final IVersionable<?> v = VersionHelper.getVersionable(source);
					if (v != null) {
						if (v.getVersion().getStatus() == IVersionStatus.NOT_VERSIONED) {
							messages.add(MessageFormat.format(
									VCSUIMessages.getString("editor.comparison.databaseElementTag"), //$NON-NLS-1$
									getTypedName((ITypedObject) source)));
						}
					}
					messages.add(getQualifiedName((ITypedObject) source));
				}
			}
		}

		// Second (right/target) item input
		final IReferenceable target = compItem.getTarget();
		if (target instanceof ITypedObject) {
			IEditorInput tgtInput = getEditorInput(compItem, ComparedElement.TARGET);
			String tgtId = getEditorId(compItem, ComparedElement.TARGET);
			if (tgtInput != null && tgtId != null) {
				editorInputs.add(tgtInput);
				editorIds.add(tgtId);
				// Non-volatile reference is a workspace reference
				if (!target.getReference().isVolatile()) {
					messages.add(MessageFormat.format(
							VCSUIMessages.getString("editor.comparison.currentElementTag"), //$NON-NLS-1$
							getQualifiedName((ITypedObject) target)));
				} else {
					final IVersionable<?> v = VersionHelper.getVersionable(target);
					if (v != null) {
						if (v.getVersion().getStatus() == IVersionStatus.NOT_VERSIONED) {
							messages.add(MessageFormat.format(
									VCSUIMessages.getString("editor.comparison.databaseElementTag"), //$NON-NLS-1$
									getTypedName((ITypedObject) target)));
						}
					}
					messages.add(getQualifiedName((ITypedObject) target));
				}
			}
		}
		// A few checks to be sure we can display our editor
		if (editorInputs.size() > 0 && editorIds.size() == editorInputs.size()) {
			return new ComparisonEditorInput(editorIds.toArray(new String[editorIds.size()]),
					editorInputs.toArray(new IEditorInput[editorInputs.size()]),
					messages.toArray(new String[messages.size()]));
		} else {
			return null;
		}
	}

	@Override
	public String getEditorId(IComparisonItem compItem) {
		return ComparisonSideToSideEditor.EDITOR_ID;
	}

	/**
	 * Retrieves the editor input for comparing {@link ITypedObject} contained in the comparison
	 * item. The item to compare (source / target) is defined through the {@link ComparedElement}
	 * parameter. It allows implementors to have access to the comparison information while being
	 * able to manipulate items directly.
	 * 
	 * @param compItem {@link IComparisonItem} containing the comparison information
	 * @param comparedElement element to compare (could be the source or target)
	 * @return the editor input
	 */
	protected abstract IEditorInput getEditorInput(IComparisonItem comparisonItem,
			ComparedElement comparedElement);

	/**
	 * Retrieves the editor id to use for comparing one element (source or target) of the given
	 * {@link IComparisonItem}.
	 * 
	 * @param comparisonItem comparison information to display in editor
	 * @param comparedElement element to consider in the comparison (source or target) as this
	 *        editor will only display one item
	 * @return editor id the identifier of the comparison editor that will display the comparison to
	 *         the user
	 */
	protected abstract String getEditorId(IComparisonItem comparisonItem,
			ComparedElement comparedElement);

	/**
	 * Constructs a qualified name which will allow the user to know precisely what this object is.
	 * This message is designed to be shown to the user on top of each pane of the comparison
	 * 
	 * @param o object to build a qualified name for
	 * @return a qualified name
	 */
	private String getQualifiedName(ITypedObject o) {
		StringBuffer buf = new StringBuffer();
		buf.append(getTypedName(o));
		IVersionable<?> v = VersionHelper.getVersionable(o);
		if (v != null && v.getVersion() != null) {
			final IVersionInfo version = v.getVersion();
			final String info = MessageFormat.format(VCSUIMessages.getString("comparison.version"), //$NON-NLS-1$
					version.getLabel());
			buf.append(" " + info); //$NON-NLS-1$
		}
		return buf.toString();
	}

	private String getTypedName(ITypedObject o) {
		StringBuffer buf = new StringBuffer();
		buf.append(o.getType().getName().toLowerCase());
		if (o instanceof INamedObject) {
			buf.append(" " + ((INamedObject) o).getName()); //$NON-NLS-1$
		}
		return buf.toString();
	}
}
