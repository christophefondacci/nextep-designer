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
package com.nextep.designer.sqlgen.ui.dbgm;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.AbstractMultiEditor;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * This editor is capable of displaying a mix of an element's default typed editor with a SQL editor
 * in a horizontal non-resizable layout.
 * 
 * @author Christophe Fondacci
 */
public class MixedSQLEditor extends AbstractMultiEditor {

	public static final String EDITOR_ID = "com.neXtep.designer.sqlgen.ui.mixedSqlEditor"; //$NON-NLS-1$
	private Composite sqlEditorContainer;
	private Composite builtinEditorContainer;

	// private ScrolledComposite scrollable;
	// private SashForm sash;

	/**
	 * @see com.nextep.designer.sqlgen.ui.editors.SQLEditor#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// ITrigger trigger = (ITrigger) t.getModel();
		// IDisplayConnector triggerEditor = UIControllerFactory.getController(trigger.getType())
		// .initializeEditor(trigger);
		// sash = new SashForm(parent, SWT.VERTICAL);
		GridLayout pl = new GridLayout();
		pl.marginHeight = pl.marginWidth = 0;
		pl.numColumns = 1;
		parent.setLayout(pl);
		// Creating the custom view editor (name / description)
		// scrollable = new ScrolledComposite(sash, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		builtinEditorContainer = new Composite(parent, SWT.NONE);
		builtinEditorContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// scrollable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// scrollable.setContent(builtinEditorContainer);
		// scrollable.setExpandVertical(true);
		// scrollable.setExpandHorizontal(true);

		FillLayout f = new FillLayout();
		f.marginHeight = f.marginWidth = 0;
		builtinEditorContainer.setLayout(f);

		// Creating the SQL editor section
		final MixedSQLEditorInput input = (MixedSQLEditorInput) getEditorInput();

		Composite labeledSqlEditor = new Composite(parent, SWT.BORDER);
		labeledSqlEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout grid = new GridLayout(1, false);
		grid.marginBottom = grid.marginHeight = grid.marginLeft = grid.marginRight = grid.marginTop = grid.marginWidth = 0;
		labeledSqlEditor.setLayout(grid);
		Label sqlLabel = new Label(labeledSqlEditor, SWT.NONE);
		sqlLabel.setText(input.getSqlIntroText());
		sqlEditorContainer = new Composite(labeledSqlEditor, SWT.NONE);
		sqlEditorContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		FillLayout l = new FillLayout();
		l.marginHeight = l.marginWidth = 0;
		sqlEditorContainer.setLayout(l);
		// sash.setWeights(new int[] { 1, 2 });
	}

	@Override
	public Composite getInnerEditorContainer(IEditorReference innerEditorReference) {
		try {
			if (innerEditorReference.getEditorInput() instanceof ISQLEditorInput<?>) {
				return sqlEditorContainer;
			}
			return builtinEditorContainer;
		} catch (PartInitException e) {
			throw new ErrorException("Problems creating editor", e);
		}
	}

	@Override
	protected void innerEditorsCreated() {
		for (IEditorPart p : getInnerEditors()) {
			p.addPropertyListener(new IPropertyListener() {

				@Override
				public void propertyChanged(Object source, int propId) {
					firePropertyChange(propId);
				}
			});
		}
		IEditorInput input = getEditorInput();
		if (input instanceof ITypedObject) {
			setTitleImage(ImageFactory.getImage(((ITypedObject) input).getType().getIcon()));
		}
		// Adjusting sash, scrollable and ratio
		// builtinEditorContainer.pack();
		// sash.pack();
		// Point sashSize = sash.getSize();
		// Point builtInSize = builtinEditorContainer.computeSize(sashSize.x, SWT.DEFAULT);
		// scrollable.setMinSize(builtInSize);
		// sash.setWeights(new int[] { builtInSize.y, sashSize.y - builtInSize.y });

	}
}
