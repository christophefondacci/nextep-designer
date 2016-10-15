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
package com.nextep.designer.vcs.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.jface.NameFilter;
import com.nextep.designer.vcs.ui.jface.TypedViewerComparator;
import com.nextep.designer.vcs.ui.jface.VersionableNewLabelProvider;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * This dialog shows a list of elements provided by a {@link IContentProvider} in a filter dialog
 * allowing user to quickly filter and select an element.<br>
 * This dialog should generally not be used directly. Please use the {@link ICommonUIService}
 * methods instead.
 * 
 * @author Christophe Fondacci
 */
public class FindElementDialog extends Dialog {

	private String title;
	private IContentProvider contentProvider;
	private ILabelProvider labelProvider;
	private TableViewer viewer;
	private ITypedObject selectedElement = null;
	private Object input;

	public FindElementDialog(Shell parent, String title, IContentProvider contentProvider,
			Object input) {
		this(parent, title, contentProvider, new VersionableNewLabelProvider(), input);
	}

	public FindElementDialog(Shell parent, String title, IContentProvider contentProvider,
			ILabelProvider labelProvider, Object input) {
		super(parent);
		this.contentProvider = contentProvider;
		this.labelProvider = labelProvider;
		if (title == null) {
			this.title = VCSUIMessages.getString("dialog.findElement.defaultTitle"); //$NON-NLS-1$
		} else {
			this.title = title;
		}
		this.input = input;
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText(title);
		super.configureShell(newShell);
	}

	@Override
	protected int getShellStyle() {
		return SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 400);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite group = (Composite) super.createDialogArea(parent);

		// Initializing filters
		Label filterLabel = new Label(group, SWT.NONE);
		filterLabel.setText(VCSUIMessages.getString("dialog.findElement.filterLabel")); //$NON-NLS-1$
		filterLabel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		final Text filterText = new Text(group, SWT.BORDER);
		filterText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		Label matchLabel = new Label(group, SWT.NONE);
		matchLabel.setText(VCSUIMessages.getString("dialog.findElement.matchingLabel")); //$NON-NLS-1$
		matchLabel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));

		// Initializing viewer
		viewer = new TableViewer(group);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		viewer.setContentProvider(contentProvider);
		// Handling auto-decoration on styled label provider
		IBaseLabelProvider viewerLabelProvider = labelProvider;
		if (labelProvider instanceof IStyledLabelProvider) {
			viewerLabelProvider = new DecoratingStyledCellLabelProvider(
					(IStyledLabelProvider) labelProvider, PlatformUI.getWorkbench()
							.getDecoratorManager().getLabelDecorator(), null);
		}
		viewer.setLabelProvider(viewerLabelProvider);
		viewer.addFilter(new NameFilter(filterText));
		viewer.setSorter(new TypedViewerComparator());
		viewer.setInput(input == null ? VCSPlugin.getViewService().getCurrentWorkspace() : input);

		// Registering filter actions
		filterText.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					int index = viewer.getTable().getSelectionIndex();
					if (index != -1 && viewer.getTable().getItemCount() > index + 1) {
						viewer.getTable().setSelection(index + 1);
					}
					e.doit = false;
				} else if (e.keyCode == SWT.ARROW_UP) {
					int index = viewer.getTable().getSelectionIndex();
					if (index != -1 && index >= 1) {
						viewer.getTable().setSelection(index - 1);
					}
					e.doit = false;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				viewer.setFilters(new NameFilter[] { new NameFilter(filterText) });
				if (viewer.getSelection().isEmpty()) {
					if (viewer.getTable().getItemCount() > 0) {
						viewer.getTable().select(0);
					}
				}
			}

		});

		// Registering double click action
		viewer.getControl().addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				okPressed();
			}
		});
		if (viewer.getTable().getItemCount() > 0) {
			viewer.getTable().select(0);
		}
		return group;
	}

	@Override
	protected void okPressed() {
		if (viewer.getSelection() instanceof IStructuredSelection) {
			Object elt = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			if (elt instanceof ITypedObject) {
				selectedElement = (ITypedObject) elt;
			}
		}
		super.okPressed();
	}

	public ITypedObject getSelectedElement() {
		return selectedElement;
	}
}
