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
package com.nextep.designer.vcs.ui.views;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import com.nextep.datadesigner.gui.impl.AbstractSelectionProvider;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IComparisonListener;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.compare.IComparisonEditorProvider;
import com.nextep.designer.vcs.ui.navigators.DiffLineNavigator;

public class DiffPreviewView extends ViewPart implements IComparisonListener, Listener {

	public final static String VIEW_ID = "com.neXtep.designer.vcs.ui.views.DiffPreviewView";
	private Tree tree;
	private static boolean showUnchanged = false;
	private final static Log log = LogFactory.getLog(DiffPreviewView.class);
	private AbstractSelectionProvider selectionProvider;

	@Override
	public void createPartControl(Composite parent) {
		tree = new Tree(parent, SWT.FULL_SELECTION);
		tree.addListener(SWT.MouseDoubleClick, this);
		tree.setHeaderVisible(true);
		final TreeColumn objCol = new TreeColumn(tree, SWT.NONE);
		objCol.setText(VCSUIMessages.getString("comparison.view.colObjectName"));
		objCol.setWidth(250);
		final TreeColumn diffType = new TreeColumn(tree, SWT.NONE);
		diffType.setText(VCSUIMessages.getString("comparison.view.colDiffType"));
		diffType.setWidth(50);
		diffType.setAlignment(SWT.CENTER);
		final TreeColumn diffTarget = new TreeColumn(tree, SWT.NONE);
		diffTarget.setText(VCSUIMessages.getString("comparison.view.colTargetName"));
		diffTarget.setWidth(250);

		initializeTree();
		selectionProvider = new AbstractSelectionProvider() {

			@Override
			public ISelection getSelection() {
				TreeItem[] selItems = tree.getSelection();
				if (selItems.length > 0) {
					final Object o = selItems[0].getData();
					if (o instanceof IModelOriented<?>) {
						return new StructuredSelection(((IModelOriented<?>) o).getModel());
					} else {
						return new StructuredSelection(o);
					}
				}
				return StructuredSelection.EMPTY;
			}
		};
		tree.addSelectionListener(selectionProvider);
		registerContextMenu();
		getSite().setSelectionProvider(selectionProvider);
	}

	private void registerContextMenu() {
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);

		// this is to work around complaints about missing standard groups.
		contextMenu.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				// manager.add(new GroupMarker("editions"));
				// manager.add(new Separator());
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				// manager.add(new Separator());
				// manager.add(new GroupMarker("sql"));
				// manager.add(new Separator());
				// manager.add(new GroupMarker("actions"));
				// manager.add(new Separator());
				// manager.add(new GroupMarker("version"));
			}
		});

		getSite().registerContextMenu(contextMenu, selectionProvider);
		Menu menu = contextMenu.createContextMenu(tree);
		tree.setMenu(menu);
	}

	/**
	 * Initializes the tree by removing all items and creating root nodes
	 */
	private void initializeTree() {
		tree.removeAll();
		// Static root nodes creation
		// additionsItem = new TreeItem(tree, SWT.NONE);
		// additionsItem.setText(VCSUIMessages.getString("comparison.additionNode"));
		// additionsItem.setImage(VCSImages.ICON_DIFF_ADDED);
		//
		// updatesItem = new TreeItem(tree, SWT.NONE);
		// updatesItem.setText(VCSUIMessages.getString("comparison.updateNode"));
		// updatesItem.setImage(VCSImages.ICON_DIFF_CHANGED);
		//
		// deletionsItem = new TreeItem(tree, SWT.NONE);
		// deletionsItem.setText(VCSUIMessages.getString("comparison.removalNode"));
		// deletionsItem.setImage(VCSImages.ICON_DIFF_REMOVED);
		//
		// unchangedItem = new TreeItem(tree, SWT.NONE);
		// unchangedItem.setText(VCSUIMessages.getString("comparison.unchangedNode"));
	}

	@Override
	public void setFocus() {
		tree.setFocus();
	}

	@Override
	public void newComparison(String description, IComparisonItem... comparisonItems) {
		setContentDescription(description == null ? "" : description);
		initializeTree();
		addComparisonItems(null, comparisonItems);
		autoExpand(tree.getItems());
	}

	private void autoExpand(TreeItem[] items) {
		if (items.length == 1) {
			items[0].setExpanded(true);
			autoExpand(items[0].getItems());
		}
	}

	/**
	 * Adds comparison items to the parent item
	 * 
	 * @param parent parent tree item to add items to, maybe null to append to the tree root
	 * @param comparisonItems comparison items to add
	 */
	private void addComparisonItems(TreeItem parent, IComparisonItem... comparisonItems) {
		for (IComparisonItem item : comparisonItems) {
			if (item instanceof ComparisonAttribute) {
				continue;
			}
			if (showUnchanged || item.getDifferenceType() != DifferenceType.EQUALS) {
				IReferenceable r = item.getSource();
				if (r == null) {
					r = item.getTarget();
				}
				if (r instanceof ITypedObject) {
					INavigatorConnector c = new DiffLineNavigator(item);
					c.setTree(tree);
					// TreeItem parentItem = parent;
					// if (parentItem == null) {
					// switch (item.getDifferenceType()) {
					// case DIFFER:
					// parentItem = updatesItem;
					// break;
					// case EQUALS:
					// parentItem = unchangedItem;
					// break;
					// case MISSING_SOURCE:
					// parentItem = deletionsItem;
					// break;
					// case MISSING_TARGET:
					// parentItem = additionsItem;
					// break;
					// }
					// }
					final TreeItem treeItem = c.create(parent, -1);
					c.refreshConnector();
					addComparisonItems(
							treeItem,
							item.getSubItems().toArray(
									new IComparisonItem[item.getSubItems().size()]));
				}
			}
		}
	}

	@Override
	public void dispose() {
		VCSPlugin.getComparisonManager().removeComparisonListener(this);
		super.dispose();
	}

	@Override
	public void handleEvent(Event evt) {
		switch (evt.type) {
		case SWT.MouseDoubleClick:
			TreeItem[] selItems = tree.getSelection();
			if (selItems.length > 0) {
				TreeItem selItem = selItems[0];
				if (selItem.getData() instanceof DiffLineNavigator) {
					final DiffLineNavigator diffNav = (DiffLineNavigator) selItem.getData();
					final IComparisonItem compItem = (IComparisonItem) diffNav.getModel();
					try {
						openComparisonFor(compItem);
					} catch (RuntimeException e) {
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "error",
								"Failed to open comparison: " + e.getMessage());
						log.error("Failed to open comparison: " + e.getMessage(), e);
					}
				}
			}
		}
	}

	/**
	 * Opens the comparison editor for the specified {@link IComparisonItem}. The comparison editor
	 * is able to show a 2-pane comparison showing the source in the left pane and the target in the
	 * right pane.
	 * 
	 * @param compItem
	 */
	private void openComparisonFor(IComparisonItem compItem) {
		final IComparisonEditorProvider editorProvider = VCSUIPlugin.getComparisonUIManager()
				.getComparisonEditorProvider(compItem.getType());
		try {
			final IEditorInput input = editorProvider.getEditorInput(compItem);
			final String editorId = editorProvider.getEditorId(compItem);
			if (input != null && editorId != null) {
				// Opening our multi editor
				CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(input, editorId);
			}
		} catch (PartInitException e) {
			log.error("Unable to open comparison editor: " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IComparisonListener.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

}
