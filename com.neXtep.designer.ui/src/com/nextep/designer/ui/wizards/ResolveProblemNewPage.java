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
package com.nextep.designer.ui.wizards;

import java.text.MessageFormat;
import java.util.List;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
import org.eclipse.swt.nebula.widgets.compositetable.IRowContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.solver.Header;
import com.nextep.designer.ui.solver.Row;

public class ResolveProblemNewPage extends WizardPage {

	private final static String WIZ_PROBLEMS = "/resource/wizProblems.png"; //$NON-NLS-1$
	private List<IMarker> markers;

	public ResolveProblemNewPage(List<IMarker> markers) {
		super("resolveProblems", UIMessages.getString("wizard.problems.title"), CoreUiPlugin //$NON-NLS-1$ //$NON-NLS-2$
				.getImageDescriptor(WIZ_PROBLEMS));
		setDescription(UIMessages.getString("wizard.problems.description")); //$NON-NLS-1$
		this.markers = markers;
	}

	@Override
	public void createControl(Composite main) {
		Composite parent = new Composite(main, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginTop = layout.marginHeight = layout.marginLeft = layout.marginRight = layout.marginWidth = 0;
		parent.setLayout(layout);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label introLabel = new Label(parent, SWT.NONE);
		introLabel.setText(MessageFormat.format(UIMessages.getString("wizard.problems.introMsg"), //$NON-NLS-1$
				markers.size()));
		introLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		CompositeTable table = new CompositeTable(parent, SWT.BORDER);
		new Header(table, SWT.NONE);
		new Row(table, SWT.NONE);
		table.setNumRowsInCollection(markers.size());
		table.addRowContentProvider(new IRowContentProvider() {

			@Override
			public void refresh(CompositeTable table, int offset, Control rowControl) {
				final Row row = (Row) rowControl;
				IMarker m = markers.get(offset);
				final Object obj = m.getRelatedObject();
				StringBuilder buf = new StringBuilder();
				if (obj instanceof ITypedObject) {
					buf.append(((ITypedObject) obj).getType().getName() + " "); //$NON-NLS-1$
				}
				if (obj instanceof INamedObject) {
					buf.append(((INamedObject) obj).getName());
				}
				row.setTitle(buf.toString());
				row.setText(m.getMessage());
				if (m.getSelectedHint() != null) {
					row.setSuggestion(m.getSelectedHint().getDescription());
				}
				row.setIcon(ImageFactory.getImage(m.getIcon()));
				// final Point p = table.getSelection();
				// final Rectangle bounds = rowControl.getBounds();
				// row.setSelected(p != null && bounds.contains(p));
			}
		});
		table.setBackground(FontFactory.WHITE);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setRunTime(true);
		setControl(table);
	}
}
