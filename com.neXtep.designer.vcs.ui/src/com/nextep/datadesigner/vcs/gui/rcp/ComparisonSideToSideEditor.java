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
package com.nextep.datadesigner.vcs.gui.rcp;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.part.AbstractMultiEditor;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.vcs.ui.VCSImages;

public class ComparisonSideToSideEditor extends AbstractMultiEditor {

	public final static String EDITOR_ID = "com.nextep.datadesigner.vsc.gui.rcp.ComparisonSideToSideEditor"; //$NON-NLS-1$
	private SashForm sashForm;
	private Composite leftEditor;
	private Composite rightEditor;
	private final Map<IEditorInput, Composite> containerMaps = new HashMap<IEditorInput, Composite>();

	@Override
	public Composite getInnerEditorContainer(IEditorReference innerEditorReference) {
		// final Composite container = containerMaps.get(innerEditorReference);
		// if (container != null) {
		// return container;
		// } else if (containerMaps.entrySet().size() == 0) {
		// containerMaps.put(innerEditorReference, leftEditor);
		// return leftEditor;
		// } else if (containerMaps.entrySet().size() == 1) {
		// containerMaps.put(innerEditorReference, rightEditor);
		// return rightEditor;
		// }
		return null;
	}

	@Override
	protected void innerEditorsCreated() {
		for (IEditorPart e : getInnerEditors()) {
			Composite container = containerMaps.get(e.getEditorInput());
			if (container == null && containerMaps.entrySet().size() == 0) {
				containerMaps.put(e.getEditorInput(), leftEditor);
				container = leftEditor;
			} else if (container == null && containerMaps.entrySet().size() == 1) {
				containerMaps.put(e.getEditorInput(), rightEditor);
				container = rightEditor;
			}
			e.createPartControl(container);
			final Control c = e.getAdapter(Control.class);
			if (c instanceof StyledText) {
				final StyledText styledText = (StyledText) c;
				styledText.getVerticalBar().addSelectionListener(new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent evt) {
						// Aligning all editors top position
						for (IEditorPart e : getInnerEditors()) {
							Control partControl = e.getAdapter(Control.class);
							if (partControl instanceof StyledText && partControl != styledText) {
								((StyledText) partControl).setTopIndex(styledText.getTopIndex());
							}
						}
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
					}
				});
				styledText.getHorizontalBar().addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent evt) {
						for (IEditorPart e : getInnerEditors()) {
							Control partControl = e.getAdapter(Control.class);
							if (partControl instanceof StyledText && partControl != styledText) {
								((StyledText) partControl)
										.setHorizontalPixel(styledText.getHorizontalPixel());
							}
						}
					}
				});
				styledText.addCaretListener(new CaretListener() {

					@Override
					public void caretMoved(CaretEvent event) {
						for (IEditorPart e : getInnerEditors()) {
							Control partControl = e.getAdapter(Control.class);
							if (partControl instanceof StyledText && partControl != styledText) {
								((StyledText) partControl).setTopIndex(styledText.getTopIndex());
							}
						}
					}
				});

			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		ComparisonEditorInput input = (ComparisonEditorInput) getEditorInput();
		sashForm = new SashForm(parent, SWT.HORIZONTAL);
		// Left sash container for heading label and left editor
		Composite leftContainer = new Composite(sashForm, SWT.BORDER);
		leftContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout gl = new GridLayout(1, false);
		gl.marginBottom = gl.marginHeight = gl.marginLeft = gl.marginRight = gl.marginTop = gl.marginWidth = 0;
		leftContainer.setLayout(gl);
		// Heading label for left pane
		CLabel msgLabel = new CLabel(leftContainer, SWT.BORDER_SOLID);
		msgLabel.setImage(VCSImages.ICON_VERSIONTREE);
		msgLabel.setText(input.getMessages()[0]);
		msgLabel.setBackground(FontFactory.LIGHT_YELLOW);
		msgLabel.setForeground(FontFactory.CHECKIN_COLOR);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		msgLabel.setLayoutData(gridData);
		// Left pane editor
		leftEditor = new Composite(leftContainer, SWT.NONE);
		FillLayout f = new FillLayout();
		f.marginHeight = f.marginWidth = f.spacing = 0;
		leftEditor.setLayout(f);
		leftEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (input.getEditors().length > 1) {
			// Right sash container for heading label and right editor
			Composite rightContainer = new Composite(sashForm, SWT.BORDER);
			rightContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout rightLayout = new GridLayout(1, false);
			rightLayout.marginBottom = rightLayout.marginHeight = rightLayout.marginLeft = rightLayout.marginRight = rightLayout.marginTop = rightLayout.marginWidth = 0;
			rightContainer.setLayout(rightLayout);
			// Heading label for right pane
			msgLabel = new CLabel(rightContainer, SWT.BORDER_SOLID);
			msgLabel.setImage(VCSImages.ICON_VERSIONTREE);
			msgLabel.setText(input.getMessages()[1]);
			msgLabel.setBackground(FontFactory.LIGHT_YELLOW);
			msgLabel.setForeground(FontFactory.CHECKIN_COLOR);
			GridData rightData = new GridData();
			rightData.horizontalAlignment = GridData.FILL;
			rightData.grabExcessHorizontalSpace = true;
			msgLabel.setLayoutData(rightData);
			// Right pane editor
			rightEditor = new Composite(rightContainer, SWT.NONE);
			rightEditor.setLayout(new FillLayout());
			rightEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		}
	}

}
