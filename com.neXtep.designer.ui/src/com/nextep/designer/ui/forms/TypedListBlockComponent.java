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
package com.nextep.designer.ui.forms;

import java.util.Collection;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerListener;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.IFormActionProvider;
import com.nextep.designer.ui.model.IFormComponentContainer;
import com.nextep.designer.ui.model.IGlobalSelectionProvider;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIComponent;

/**
 * A generic UI component composed of a table and Add / Remove button, plus optionally a order Up
 * and order Down button. Content are provided through JFace's content and label providers, and
 * actions are bound using nextep's action providers.<br>
 * This class also acts as a selection provider by delegating calls to the inner viewer.
 * 
 * @author Christophe Fondacci
 */
public class TypedListBlockComponent extends AbstractUIComponent implements ISelectionProvider,
		IModelOriented<ITypedObject>, IEventListener, IMarkerListener, DisposeListener {

	private IContentProvider contentProvider;
	private ILabelProvider labelProvider;
	private IFormActionProvider actionProvider;
	private ITypedObject input;
	private TableViewer viewer;
	private IWorkbenchPart part;

	public TypedListBlockComponent(ILabelProvider labelProvider, IContentProvider contentProvider,
			IFormActionProvider actionProvider, ITypedObject input) {
		this.labelProvider = labelProvider;
		this.contentProvider = contentProvider;
		this.actionProvider = actionProvider;
		setModel(input);
	}

	@Override
	public Control create(Composite parent) {
		final IManagedForm managedForm = ((IFormComponentContainer) getUIComponentContainer())
				.getForm();
		final FormToolkit toolkit = managedForm.getToolkit();

		Composite editor = toolkit.createComposite(parent);
		editor.setLayout(new GridLayout(2, false));
		viewer = new TableViewer(editor, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(contentProvider);
		// We handle standard and styled label provider differently so that we automatically
		// enable styled decoration when possible
		IBaseLabelProvider viewerLabelProvider = labelProvider;
		if (labelProvider instanceof IStyledLabelProvider) {
			viewerLabelProvider = new DecoratingStyledCellLabelProvider(
					(IStyledLabelProvider) labelProvider, PlatformUI.getWorkbench()
							.getDecoratorManager().getLabelDecorator(), null);
		}
		viewer.setLabelProvider(viewerLabelProvider);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 6));
		viewer.setInput(input);
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					final Object o = ((IStructuredSelection) event.getSelection())
							.getFirstElement();
					final ITypedObjectUIController controller = UIControllerFactory
							.getController(o);
					if (controller != null && o instanceof ITypedObject) {
						controller.defaultOpen((ITypedObject) o);
					}
				}
			}
		});
		if (actionProvider.isAddRemoveEnabled()) {
			// Add button
			Button addButton = toolkit.createButton(editor,
					UIMessages.getString("component.typedListBlock.add"), SWT.PUSH); //$NON-NLS-1$
			addButton.setImage(ImageFactory.ICON_ADD_TINY);
			addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			addButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					final Object newObject = actionProvider.add(input);
					if (newObject != null) {
						viewer.setSelection(new StructuredSelection(newObject));
					}
				}
			});
			// Remove button
			Button removeButton = toolkit.createButton(editor,
					UIMessages.getString("component.typedListBlock.remove"), SWT.PUSH); //$NON-NLS-1$
			removeButton.setImage(ImageFactory.ICON_DELETE);
			removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			removeButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					ITypedObject toRemove = extractSingleSelection(viewer.getSelection());
					if (toRemove != null) {
						actionProvider.remove(input, toRemove);
					}
				}
			});
		}
		// Adding edit control if requested
		if (actionProvider.isEditable()) {
			Button editButton = toolkit.createButton(editor,
					UIMessages.getString("component.typedListBlock.properties"), SWT.PUSH); //$NON-NLS-1$
			editButton.setImage(ImageFactory.ICON_EDIT_TINY);
			editButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					final ITypedObject selection = extractSingleSelection(viewer.getSelection());
					if (selection != null) {
						actionProvider.edit(input, selection);
					}
				}
			});
		}
		// Adding sort controls if requested
		if (actionProvider.isSortable()) {
			toolkit.createLabel(editor, UIMessages.getString("component.typedListBlock.orderLabel")); //$NON-NLS-1$
			// Up button
			Button upButton = toolkit.createButton(editor,
					UIMessages.getString("component.typedListBlock.up"), SWT.PUSH); //$NON-NLS-1$
			upButton.setImage(ImageFactory.ICON_UP_TINY);
			upButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			upButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					final ITypedObject selection = extractSingleSelection(viewer.getSelection());
					if (selection != null) {
						actionProvider.up(input, selection);
					}
				}
			});
			// Down button
			Button downButton = toolkit.createButton(editor,
					UIMessages.getString("component.typedListBlock.down"), SWT.PUSH); //$NON-NLS-1$
			downButton.setImage(ImageFactory.ICON_DOWN_TINY);
			downButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			downButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					final ITypedObject selection = extractSingleSelection(viewer.getSelection());
					if (selection != null) {
						actionProvider.down(input, selection);
					}
				}
			});
			toolkit.createLabel(editor, ""); //$NON-NLS-1$
		}
		if (parent instanceof Section) {
			final Section section = (Section) parent;
			final SectionPart spart = new SectionPart(section) {

				@Override
				public boolean setFormInput(Object input) {
					final ISelection s = new StructuredSelection(input);
					viewer.setSelection(s);
					return false;
				}
			};
			managedForm.addPart(spart);
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					managedForm.fireSelectionChanged(spart, event.getSelection());
				}
			});
		}
		// Registering a context menu
		registerContextMenu(viewer);
		// Registering this instance as a marker listener so that our list will be refreshed when
		// markers change
		final IMarkerService markerService = CorePlugin.getService(IMarkerService.class);
		markerService.addMarkerListener(this);
		// Adding a dispose listener so that we can unregister ourselves from marker listening
		viewer.getTable().addDisposeListener(this);

		return editor;
	}

	private void registerContextMenu(ISelectionProvider provider) {
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);

		// this is to work around complaints about missing standard groups.
		contextMenu.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(new GroupMarker("sql")); //$NON-NLS-1$
				manager.add(new Separator());
				manager.add(new GroupMarker("actions")); //$NON-NLS-1$
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
				menuSite.registerContextMenu("typedListBlock_" + contentProvider.toString(), //$NON-NLS-1$
						contextMenu, provider);
			}
		}
		Menu menu = contextMenu.createContextMenu(viewer.getTable());
		viewer.getTable().setMenu(menu);
	}

	private ITypedObject extractSingleSelection(ISelection s) {
		if (s instanceof IStructuredSelection) {
			return (ITypedObject) ((IStructuredSelection) s).getFirstElement();
		}
		return null;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.addSelectionChangedListener(listener);
	}

	@Override
	public ISelection getSelection() {
		return viewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		viewer.removeSelectionChangedListener(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		viewer.setSelection(selection);
	}

	@Override
	public void setModel(ITypedObject model) {
		Designer.getListenerService().unregisterListeners(this);
		if (viewer != null) {
			final ISelection s = viewer.getSelection();
			IReferenceable newObj = null;
			// We extract currently selected object (before checkout)
			if (s != null && !s.isEmpty() && s instanceof IStructuredSelection) {
				final Object o = ((IStructuredSelection) s).getFirstElement();
				if (o instanceof IReferenceable) {
					// We get the reference of that object
					final IReference objRef = ((IReferenceable) o).getReference();
					// Locating new element with this reference
					if (model instanceof IReferenceContainer) {
						newObj = ((IReferenceContainer) model).getReferenceMap().get(objRef);
					}
				}
			}
			viewer.setInput(model);
			if (newObj != null) {
				viewer.setSelection(new StructuredSelection(newObj));
			}
		}
		this.input = model;
		if (model instanceof IObservable) {
			Designer.getListenerService().registerListener(this, (IObservable) model, this);
		}
	}

	@Override
	public ITypedObject getModel() {
		return input;
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// Doing nothing, we only listen to get updated about the model
	}

	/**
	 * @param part the associated workbench part
	 */
	public void setPart(IWorkbenchPart part) {
		this.part = part;
	}

	@Override
	public void markersChanged(final Object o, Collection<IMarker> oldMarkers,
			Collection<IMarker> newMarkers) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				viewer.refresh(o);
			}
		});
	}

	@Override
	public void markersReset(Collection<IMarker> allMarkers) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				viewer.refresh();
			}
		});
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		final IMarkerService markerService = CorePlugin.getService(IMarkerService.class);
		markerService.removeMarkerListener(this);
	}
}
