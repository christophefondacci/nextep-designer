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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.ui.editors;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.dbgm.ui.jface.DbgmLabelProvider;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.IGlobalSelectionProvider;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * This form part consists in a simple section containing only one table. Table's content are
 * provided through the passed content provider and title / description are customizable.
 * 
 * @author Christophe Fondacci
 */
public class ContentProvidedListFormPart extends SectionPart {

	private TableViewer viewer;
	private IContentProvider provider;
	private String title, description;
	private int colSpan, lineSpan;
	private IWorkbenchPart part;

	public ContentProvidedListFormPart(String title, String description, Composite parent,
			FormToolkit toolkit, IContentProvider provider, IWorkbenchPart part) {
		this(title, description, parent, toolkit, provider, part, 1, 1);
	}

	public ContentProvidedListFormPart(String title, String description, Composite parent,
			FormToolkit toolkit, IContentProvider provider, IWorkbenchPart part, int colSpan,
			int lineSpan) {
		super(parent, toolkit, Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED);
		this.title = title;
		this.description = description;
		this.provider = provider;
		this.part = part;
		this.colSpan = colSpan;
		this.lineSpan = lineSpan;
		fillFormSection();
	}

	private void fillFormSection() {
		final Section section = getSection();
		TableWrapLayout propsLayout = new TableWrapLayout();
		section.setLayout(propsLayout);
		GridData d = new GridData(SWT.FILL, SWT.FILL, true, false, colSpan, lineSpan);
		d.widthHint = 250;
		d.heightHint = 100;
		section.setLayoutData(d);
		section.setText(title);
		section.setDescription(description);
		viewer = new TableViewer(section);
		viewer.setContentProvider(provider);
		viewer.setLabelProvider(new DecoratingStyledCellLabelProvider(new DbgmLabelProvider(),
				PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator(), null));
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.getTable().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				final ISelection s = viewer.getSelection();
				if (s != null && !s.isEmpty() && s instanceof IStructuredSelection) {
					final IStructuredSelection sel = (IStructuredSelection) s;
					final Object selectedObject = sel.getFirstElement();
					if (selectedObject instanceof ITypedObject) {
						final ITypedObjectUIController controller = UIControllerFactory
								.getController(selectedObject);
						if (controller != null) {
							controller.defaultOpen((ITypedObject) selectedObject);
						}
					}
				}
			}
		});
		section.setClient(viewer.getTable());
		registerContextMenu(viewer);
	}

	private void registerContextMenu(ISelectionProvider provider) {
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);

		// this is to work around complaints about missing standard groups.
		contextMenu.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(new GroupMarker("sql"));
				manager.add(new Separator());
				manager.add(new GroupMarker("actions"));
				manager.add(new Separator());
				manager.add(new GroupMarker("version")); //$NON-NLS-1$
			}
		});

		if (part != null) {
			final IWorkbenchPartSite menuSite = part.getSite();
			if (menuSite != null) {
				ISelectionProvider globalProvider = menuSite.getSelectionProvider();
				if (globalProvider instanceof IGlobalSelectionProvider) {
					((IGlobalSelectionProvider) globalProvider).registerSelectionProvider(part,
							provider);
				}
				menuSite.registerContextMenu("typedListBlock_" + provider.toString(), contextMenu,
						provider);
			}
		}
		Menu menu = contextMenu.createContextMenu(viewer.getTable());
		viewer.getTable().setMenu(menu);
	}

	@Override
	public boolean setFormInput(Object input) {
		viewer.setInput(input);
		return true;
	}

}
