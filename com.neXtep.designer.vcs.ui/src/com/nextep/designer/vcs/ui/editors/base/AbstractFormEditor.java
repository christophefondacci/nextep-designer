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
package com.nextep.designer.vcs.ui.editors.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.ui.model.IFormComponentContainer;
import com.nextep.designer.ui.model.IUIChildComponent;
import com.nextep.designer.ui.model.base.AbstractUIComponent;
import com.nextep.designer.ui.services.IUIService;

/**
 * Base implementation of the most common form editor component composed of a model, one UI section
 * and data binding support.<br>
 * The component listens explicitly to the model to handle binding automatically and updates
 * enablement states.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractFormEditor<T> extends AbstractUIComponent implements
		IModelOriented<T>, IEventListener, IUIChildComponent, ISelectionChangedListener {

	private DataBindingContext context;
	private T model;
	private ISelectionProvider selectionProvider;
	private Section section;
	private String title, description;
	private boolean detailPart;
	private List<ControlDecorationSupport> fieldDecorators;

	public AbstractFormEditor(String title, String description, boolean detailPart) {
		this.title = title;
		this.description = description;
		this.detailPart = detailPart;
		fieldDecorators = new ArrayList<ControlDecorationSupport>();
	}

	@Override
	public final Control create(Composite parent) {
		final IManagedForm managedForm = ((IFormComponentContainer) getUIComponentContainer())
				.getForm();
		final FormToolkit toolkit = managedForm.getToolkit();
		parent.setLayout(new GridLayout());
		// Creating component's section
		section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR
				| Section.EXPANDED);
		GridData d = new GridData(SWT.FILL, SWT.FILL, true, false);
		d.widthHint = 250;
		section.setLayoutData(d);

		section.setText(title);
		section.setDescription(description);

		// Creating main editor composite
		Composite editor = toolkit.createComposite(section);
		editor.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		editor.setLayoutData(gd);

		// Creating inner controls
		createControls(managedForm, toolkit, editor);

		// Binding model
		bindModel();

		section.setClient(editor);

		// If we got the model we refresh
		if (getModel() != null) {
			refresh();
		}

		return section;
	}

	/**
	 * This method initialize properly the {@link DataBindingContext} and delegates the binding
	 * setup to the implementations.
	 */
	@SuppressWarnings("unchecked")
	protected final void bindModel() {
		if (context == null) {
			context = new DataBindingContext();
		} else {
			if (!detailPart) {
				// Disposing every binding, not sure whether this is needed, but otherwise we would
				// need
				// to dispose the ControlDecorationSupport so it seems safer to dispose the whole
				// binding
				for (Object b : new ArrayList<Object>(context.getBindings())) {
					((Binding) b).dispose();
				}
				for (ControlDecorationSupport s : fieldDecorators) {
					s.dispose();
				}
				// Disposing our data binding context on current element
				context.getValidationRealm().exec(new Runnable() {

					@Override
					public void run() {
						context.dispose();
					}
				});
				context = new DataBindingContext();
			}
		}
		doBindModel(context);
	}

	/**
	 * This method is called when UI fields need to get bound to the model fields.
	 * 
	 * @param context the pre-initialize data binding context
	 */
	protected abstract void doBindModel(DataBindingContext context);

	/**
	 * Creates the component's edition controls
	 * 
	 * @param toolkit the form toolkit to use for control creation / adaptation
	 * @param parent parent composite in which controls need to be created
	 */
	protected abstract void createControls(IManagedForm managedForm, FormToolkit toolkit,
			Composite parent);

	@Override
	public void setSelectionProvider(ISelectionProvider selectionProvider) {
		// For non full-detail parts, we need to handle manually the selection change when used
		// in a master / detail context so we explicitly listen to the selection provider to
		// dispatch the model change
		if (!detailPart && getSelectionProvider() != null) {
			selectionProvider.removeSelectionChangedListener(this);
		}
		this.selectionProvider = selectionProvider;
		if (!detailPart && selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(this);
			processSelection(selectionProvider.getSelection());
		}
	}

	/**
	 * This method processes the specified selection in order to set the component's model based on
	 * the selection contents
	 * 
	 * @param selection the {@link ISelection} from which the model should be extracted.
	 */
	@SuppressWarnings("unchecked")
	private void processSelection(ISelection selection) {
		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) selection;
			setModel((T) sel.getFirstElement());
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		// Processing the selection every time it changes to set this component's model
		processSelection(event.getSelection());
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return this.selectionProvider;
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refresh();
	}

	@Override
	public void setModel(T model) {
		final IListenerService listenerService = Designer.getListenerService();
		listenerService.unregisterListeners(this);
		this.model = model;
		if (section != null && !section.isDisposed()) {
			refresh();
			if (!detailPart) {
				bindModel();
			}
		}
		if (model instanceof IObservable) {
			// This editor is listening to model change events
			listenerService.registerListener(this, (IObservable) model, this);
			// Listening to the lockable object which will tell us when enablement state need to
			// change
			listenToParents(model);
			bindController(model);
		}
	}

	private void listenToParents(Object o) {
		final IListenerService listenerService = Designer.getListenerService();
		if (o instanceof IParentable<?>) {
			final Object parent = ((IParentable<?>) o).getParent();
			if (parent instanceof IObservable) {
				listenerService.registerListener(this, (IObservable) parent, this);
			}
			listenToParents(parent);
		}
	}

	protected void bindController(Object model) {
		CorePlugin.getService(IUIService.class).bindController(model);
	}

	protected final void refresh() {
		UIHelper.handleEnablement(section, getModel());
		if (context != null) {
			// Browing every binding
			for (Iterator<?> it = context.getBindings().iterator(); it.hasNext();) {
				Binding binding = (Binding) it.next();
				// We check whether the binding has the current focus
				if (binding.getTarget() instanceof ISWTObservable) {
					final Widget widget = ((ISWTObservable) binding.getTarget()).getWidget();
					if (widget instanceof Control) {
						if (((Control) widget).isFocusControl()) {
							// If so we continue because the refresh was triggered by this control
							continue;
						}
					}
				}
				// Otherwise we refresh
				binding.updateModelToTarget();
			}
		}
		doRefresh();
	}

	/**
	 * This method gets called whenever the form needs to be refreshed from the model's information
	 */
	protected abstract void doRefresh();

	@Override
	public final T getModel() {
		return model;
	}

	/**
	 * Registers the given control decoration so that it gets properly released when needed.
	 * 
	 * @param decorator the {@link ControlDecorationSupport} to register
	 */
	protected void registerControlDecoration(ControlDecorationSupport decorator) {
		fieldDecorators.add(decorator);
	}

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
		if (selectionProvider != null) {
			selectionProvider.removeSelectionChangedListener(this);
		}
	}

	protected void setTitle(String title) {
		this.title = title;
		if (section != null && !section.isDisposed()) {
			section.setText(title);
		}
	}

	protected void setDescription(String description) {
		this.description = description;
		if (section != null && !section.isDisposed()) {
			section.setDescription(description);
		}
	}

}
