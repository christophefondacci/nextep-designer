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
package com.nextep.designer.vcs.ui.editors;

import java.util.List;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.ui.forms.FormComponentContainer;
import com.nextep.designer.ui.forms.TypedListBlockComponent;
import com.nextep.designer.ui.model.IFormActionProvider;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.services.IUIService;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class MasterDetailsTypedBlock extends MasterDetailsBlock {

	private String title, description;
	private TypedListBlockComponent typedListBlock;
	private IWorkbenchPart part;

	public MasterDetailsTypedBlock(String title, String description,
			IContentProvider contentProvider, ILabelProvider labelProvider,
			IFormActionProvider actionProvider, ITypedObject input) {
		this.title = title;
		this.description = description;
		typedListBlock = new TypedListBlockComponent(labelProvider, contentProvider,
				actionProvider, input);
	}

	@Override
	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		final FormToolkit toolkit = managedForm.getToolkit();
		final Composite parentComposite = toolkit.createComposite(parent);
		parentComposite.setLayout(new GridLayout());
		final Section section = toolkit.createSection(parentComposite, Section.TITLE_BAR
				| Section.DESCRIPTION | Section.EXPANDED);
		GridData d = new GridData(SWT.FILL, SWT.FILL, true, true);
		d.widthHint = 250;
		d.heightHint = 150;
		section.setLayoutData(d);
		section.setText(title);
		section.setDescription(description);
		// Content is created using a generic typed list block
		typedListBlock.setUIComponentContainer(new FormComponentContainer(managedForm));
		Control editor = typedListBlock.create(section);
		section.setClient(editor);
		editor.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				Designer.getListenerService().unregisterListeners(this);
			}
		});
	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.setPageProvider(new IDetailsPageProvider() {

			@Override
			public Object getPageKey(Object object) {
				if (object instanceof ITypedObject) {
					return ((ITypedObject) object).getType().getId();
				}
				return null;
			}

			@Override
			public IDetailsPage getPage(Object key) {
				final IUIService uiService = CorePlugin.getService(IUIService.class);
				List<IUIComponent> components = uiService.getEditorComponentsFor(
						IElementType.getInstance((String) key),
						DBVendor.valueOf(Designer.getInstance().getContext()));
				return new TypedDetailsPage(components, typedListBlock);
			}
		});
	}

	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		CorePlugin.getService(ICommonUIService.class).createVersionControlToolbarActions(
				managedForm.getForm().getToolBarManager(), typedListBlock.getModel(), this);
	}

	/**
	 * @param part the corresponding workbench part
	 */
	public void setPart(IWorkbenchPart part) {
		this.part = part;
		typedListBlock.setPart(part);
	}
}
