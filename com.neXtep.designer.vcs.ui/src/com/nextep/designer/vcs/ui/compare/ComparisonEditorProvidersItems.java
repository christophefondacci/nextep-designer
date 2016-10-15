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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * This class implements the contributed control displaying a drop down button proposing the various
 * comparison edition modes for comparing 2 elements together.<br>
 * This class is able to fill toolbar and menus.
 * 
 * @author Christophe Fondacci
 */
public class ComparisonEditorProvidersItems extends ContributionItem {

	private ToolItem dropDownItem;
	private static ChangeComparisonEditorProviderListener changeComparisonListener = new ChangeComparisonEditorProviderListener();
	private SelectionListener openComparisonListener = new OpenComparisonListener();
	private final static String PROVIDER_KEY = "comparisonProvider";

	public ComparisonEditorProvidersItems() {
	}

	public ComparisonEditorProvidersItems(String id) {
		super(id);
	}

	/**
	 * This selection listener handles the menu display when the user clicks on the arrow of this
	 * dropdown contribution.
	 */
	class DropdownSelectionListener extends SelectionAdapter {

		private Menu menu;

		public DropdownSelectionListener(ToolItem dropdown) {
			menu = new Menu(dropdown.getParent().getShell());
			dropdown.addSelectionListener(this);
		}

		public Menu getMenu() {
			return menu;
		}

		public void widgetSelected(SelectionEvent event) {
			if (event.detail == SWT.ARROW) {
				ToolItem item = (ToolItem) event.widget;
				Rectangle rect = item.getBounds();
				Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
				menu.setLocation(pt.x, pt.y + rect.height);
				menu.setVisible(true);
			}
		}
	}

	/**
	 * A menu item listener which changes the current comparison mode (used by toolbar action)
	 */
	static class ChangeComparisonEditorProviderListener extends SelectionAdapter {

		private Set<ToolItem> toolItems = new HashSet<ToolItem>();

		@Override
		public void widgetSelected(SelectionEvent e) {
			MenuItem selected = (MenuItem) e.widget;
			for (ToolItem toolItem : toolItems) {
				toolItem.setToolTipText(selected.getText());
				toolItem.setImage(selected.getImage());
			}
			VCSUIPlugin.getComparisonUIManager().setComparisonEditorProvider(
					(IComparisonEditorProvider) selected.getData(PROVIDER_KEY));
		}

		public void registerToolItem(ToolItem item) {
			toolItems.add(item);
		}

		public void unregisterToolItem(ToolItem item) {
			toolItems.remove(item);
		}
	}

	/**
	 * A menu item listener which opens the comparison with the selected provider (used by menu
	 * contribution)
	 */
	class OpenComparisonListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			MenuItem selected = (MenuItem) e.widget;
			// Retrieving current selection

			final ISelection s = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getSelectionService().getSelection();
			if (!s.isEmpty() && s instanceof IStructuredSelection) {
				Object selObject = ((IStructuredSelection) s).getFirstElement();
				if (selObject instanceof IComparisonItem) {
					VCSUIPlugin.getComparisonUIManager().openComparisonEditor(
							(IComparisonItem) selObject,
							(IComparisonEditorProvider) selected.getData(PROVIDER_KEY));
				}
			}

		}
	}

	@Override
	public void fill(ToolBar parent, int index) {
		dropDownItem = new ToolItem(parent, SWT.DROP_DOWN);
		final IComparisonEditorProvider provider = VCSUIPlugin.getComparisonUIManager()
				.getComparisonEditorProvider(null);
		dropDownItem.setImage(provider.getIcon());
		dropDownItem.setToolTipText(provider.getLabel());
		DropdownSelectionListener listener = new DropdownSelectionListener(dropDownItem);
		changeComparisonListener.registerToolItem(dropDownItem);
		fillComparisonEditorProviderMenuItems(listener.getMenu(), SWT.RADIO,
				changeComparisonListener);
	}

	@Override
	public void fill(Menu menu, int index) {
		fillComparisonEditorProviderMenuItems(menu, SWT.NONE, openComparisonListener);
	}

	/**
	 * Fills the given menu with items representing available comparison editor providers. This
	 * method is used for both the menu contribution and the drop down button menu.
	 * 
	 * @param menu a {@link Menu} to fill with comparison editor providers proposals
	 */
	protected void fillComparisonEditorProviderMenuItems(Menu menu, int style,
			SelectionListener listener) {
		ISelection s = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
				.getSelection();
		IElementType selectedType = null;
		if (s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			if (sel.size() == 1) {
				Object o = sel.getFirstElement();
				if (o instanceof ITypedObject) {
					selectedType = ((ITypedObject) o).getType();
				}
			}
		}
		final List<IComparisonEditorProvider> providers = VCSUIPlugin.getComparisonUIManager()
				.getAvailableComparisonEditorProviders(selectedType);
		for (final IComparisonEditorProvider provider : providers) {
			MenuItem menuItem = new MenuItem(menu, style);
			menuItem.setText(provider.getLabel());
			menuItem.setImage(provider.getIcon());
			menuItem.setData(PROVIDER_KEY, provider);
			menuItem.setSelection(provider == VCSUIPlugin.getComparisonUIManager()
					.getComparisonEditorProvider(selectedType));
			menuItem.addSelectionListener(listener);
		}
	}

	@Override
	public void dispose() {
		changeComparisonListener.unregisterToolItem(dropDownItem);
		super.dispose();
	}
}
