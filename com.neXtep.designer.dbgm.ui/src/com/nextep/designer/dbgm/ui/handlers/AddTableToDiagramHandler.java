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
package com.nextep.designer.dbgm.ui.handlers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.vcs.impl.DiagramItem;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.gef.editors.DiagramEditPart;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.jface.TablesNotInDiagramContentProvider;
import com.nextep.designer.dbgm.ui.layout.DiagramLayoutService;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.jface.VersionableLabelProvider;

/**
 * Handler of the Add tables to diagram command. This handler prompts the user for table selection.
 * All selected tables will be added to the diagram.
 * 
 * @author Christophe
 */
public class AddTableToDiagramHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			if (!sel.isEmpty() && sel.getFirstElement() instanceof DiagramEditPart) {
				final DiagramEditPart diagPart = (DiagramEditPart) sel.getFirstElement();
				// Retrieving our current diagram
				IDiagram diagram = (IDiagram) ((DiagramEditPart) sel.getFirstElement()).getModel();
				diagram = VCSUIPlugin.getVersioningUIService().ensureModifiable(diagram);
				promptForTableAdditions(event, diagram);
			}
		}
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Prompts the user to select tables which will be added to the diagram
	 * 
	 * @param event handler event
	 * @param diagram diagram to add the selection to
	 */
	private void promptForTableAdditions(ExecutionEvent event, final IDiagram diagram) {
		final Shell s = new Shell(HandlerUtil.getActiveShell(event), SWT.TITLE | SWT.RESIZE
				| SWT.CLOSE);
		s.setText(DBGMUIMessages.getString("addTablesToDiagramTitle"));
		s.setSize(500, 400);
		Rectangle pRect = s.getParent().getBounds();
		Rectangle cRect = s.getBounds();
		Point location = new Point(Math.max(0, pRect.x + pRect.width / 2 - cRect.width / 2), Math
				.max(0, pRect.y + pRect.height / 2 - cRect.height / 2));
		s.setLocation(location);
		s.setLayout(new GridLayout(1, false));
		final TableViewer viewer = new TableViewer(s, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		final TablesNotInDiagramContentProvider contentProvider = new TablesNotInDiagramContentProvider(
				diagram);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new VersionableLabelProvider());
		viewer.setInput(VersionHelper.getCurrentView());
		if (contentProvider.isEmpty()) {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event), DBGMUIMessages
					.getString("allTablesAlreadyInDiagramTitle"), DBGMUIMessages
					.getString("allTablesAlreadyInDiagram"));
			return;
		}
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Button okButton = new Button(s, SWT.PUSH);
		okButton.setText("    Ok    ");
		okButton.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
		okButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				addSelectionToDiagram(diagram, viewer.getSelection());
				s.dispose();
			}
		});
		// s.pack();
		s.open();
		while (!s.isDisposed()) {
			try {
				if (!s.getDisplay().readAndDispatch())
					s.getDisplay().sleep();
			} catch (ErrorException e) {
				// An error might happen
				s.dispose();
			}
		}
	}

	/**
	 * Adds the specified selection to the diagram. Selection may contain {@link IBasicTable} or
	 * {@link IVersionable} elements
	 * 
	 * @param diagram diagram to add elements to
	 * @param sel JFace selection
	 */
	private void addSelectionToDiagram(final IDiagram diagram, ISelection sel) {
		if (!sel.isEmpty()) {
			if (sel instanceof IStructuredSelection) {
				List<ICommand> addTablesCmds = new ArrayList<ICommand>();
				final Iterator<?> selIt = ((IStructuredSelection) sel).iterator();
				while (selIt.hasNext()) {
					final Object o = selIt.next();
					IBasicTable t = null;
					if (o instanceof IBasicTable) {
						t = (IBasicTable) o;
					} else if (o instanceof IVersionable) {
						t = (IBasicTable) ((IVersionable<?>) o).getVersionnedObject().getModel();
					}
					if (t != null) {
						final ICommand cmd = addTableToDiagram(diagram, t);
						if (cmd != null) {
							addTablesCmds.add(cmd);
						}
					}
				}
				if (!addTablesCmds.isEmpty()) {
					addTablesCmds.add(new ICommand() {

						@Override
						public Object execute(Object... parameters) {
							if (MessageDialog.openQuestion(PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(), DBGMUIMessages
									.getString("questionAutoLayoutDiagramTitle"), DBGMUIMessages //$NON-NLS-1$
									.getString("questionAutoLayoutDiagram"))) { //$NON-NLS-1$
								DiagramLayoutService.autoLayout(diagram);
							}
							diagram.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
							return null;
						}

						@Override
						public String getName() {
							return MessageFormat.format(DBGMUIMessages
									.getString("autoLayoutDiagramCommand"), diagram.getName());
						}
					});
					CommandProgress.runWithProgress(false, addTablesCmds
							.toArray(new ICommand[addTablesCmds.size()]));
				}

			}
		}
	}

	/**
	 * Adds a table to the specified diagram.
	 * 
	 * @param d diagram to add the table to
	 * @param t table to add to the diagram
	 * @return a {@link ICommand} which adds the table to the diagram
	 */
	private ICommand addTableToDiagram(final IDiagram d, final IBasicTable t) {
		for (IDiagramItem i : d.getItems()) {
			if (i.getItemReference() == t.getReference()) {
				return null;
			}
		}
		return new ICommand() {

			@Override
			public Object execute(Object... parameters) {
				try {
					Observable.deactivateListeners();
					d.addItem(new DiagramItem(t, 1, 1));
				} finally {
					Observable.activateListeners();
				}
				return null;
			}

			@Override
			public String getName() {
				return MessageFormat.format(DBGMUIMessages.getString("addTableToDiagramCommand"), t
						.getName(), d.getName());
			}
		};
	}
}
