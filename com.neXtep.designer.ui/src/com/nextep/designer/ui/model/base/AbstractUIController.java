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
/**
 *
 */
package com.nextep.designer.ui.model.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.impl.rcp.RCPTypedEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.dialogs.ComponentWizard;
import com.nextep.designer.ui.dialogs.ComponentWizardDialog;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.services.IUIService;

/**
 * Abstract controller which provides root functionalities such as default save and a save listener.
 * Every "view" which is created from such a controller and which wants to have auto-save behaviour
 * should add the controller as a listener to the model object and should release the listener
 * properly when the view is disposed.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractUIController implements ITypedObjectUIController {

	// private static final Log log = LogFactory.getLog(AbstractUIController.class);

	private Collection<ChangeEvent> saveEvents = new ArrayList<ChangeEvent>();
	private IElementType type;

	/**
	 * Adds a new event as a trigger of a save operation.
	 * 
	 * @param event event which should trigger a save
	 */
	protected void addSaveEvent(ChangeEvent event) {
		saveEvents.add(event);
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (saveEvents.contains(event)) {
			this.save((IdentifiedObject) source);
		}
	}

	/**
	 * Default implementation for save
	 * 
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#save(com.nextep.datadesigner.model.IdentifiedObject)
	 */
	public/* final */void save(IdentifiedObject o) {
		ControllerFactory.getController(getType()).save(o);
	}

	/**
	 * Default implementation
	 * 
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#emptyInstance(String,
	 *      java.lang.Object)
	 */
	public Object emptyInstance(String name, Object parent) {
		return newInstance(parent);
	}

	public static void newWizardEdition(String title, IUIComponent... pages) {
		final List<IUIComponent> components = Arrays.asList(pages);
		final IWizard wiz = new ComponentWizard(title, components);
		Shell shell = null;
		if (CoreUiPlugin.getDefault().getWorkbench() != null
				&& CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null) {
			shell = CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		} else {
			shell = Display.getDefault().getActiveShell();
		}
		final WizardDialog d = new ComponentWizardDialog(shell, wiz, true);
		d.setHelpAvailable(true);
		d.setTitle(title);
		d.setBlockOnOpen(true);
		d.open();
		if (d.getReturnCode() == Window.CANCEL) {
			throw new CancelException(UIMessages.getString("controller.creationCancelled")); //$NON-NLS-1$
		}
	}

	public Object load(String className, UID id) {
		return ControllerFactory.getController(getType()).load(className, id);
	}

	@Override
	public boolean isEditable() {
		// editable by default
		return true;
	}

	@Override
	public String getEditorId() {
		return RCPTypedEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new TypedEditorInput(model);
	}

	@Override
	public IElementType getType() {
		return type;
	}

	@Override
	public void setType(IElementType type) {
		this.type = type;
	}

	@Override
	public void defaultOpen(ITypedObject model) {
		try {
			final IEditorPart editor = CoreUiPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.openEditor(getEditorInput(model), getEditorId());
			if (editor instanceof FormEditor) {
				final FormEditor formEditor = (FormEditor) editor;
				formEditor
						.setActivePage(IUIService.PAGE_ID_PREFIX + model.getType().getId(), model);
			}
		} catch (PartInitException e) {
			throw new ErrorException(e);
		}

	}

	/**
	 * Retrieves the first available name of a typed element using the specified prefix. An
	 * available name is a name of an element type which does not conflict with any other element
	 * name of the same type.
	 * 
	 * @param type the {@link IElementType} of element to be named
	 * @return the first available name
	 */
	protected String getAvailableName(IElementType type) {
		return getAvailableName(type, type.getId());
	}

	/**
	 * Retrieves the first available name of a typed element using the specified prefix. An
	 * available name is a name of an element type which does not conflict with any other element
	 * name of the same type.<br>
	 * <br>
	 * Names will be generated using the following pattern : <br>
	 * <code>prefix + index</code><br>
	 * where index is an integer which is incremented until the name is available.
	 * 
	 * @param type the {@link IElementType} of element to be named
	 * @param prefix the prefix to use to name the element
	 * @return the first available name
	 */
	protected String getAvailableName(IElementType type, String prefix) {
		int i = 1;
		boolean isAvailable = false;
		String currentName = prefix;
		while (!isAvailable) {
			// Special case for first pass, we try to look for a name exactly equals to the prefix
			if (i == 1) {
				i++;
			} else {
				currentName = prefix + (i++);
			}
			try {
				// Does this name already exist for this type in our current worksapce ?
				final IReferenceable referenceable = CorePlugin.getService(IReferenceManager.class)
						.findByTypeName(type, currentName);
				// The name is available if no object was found with this type / name
				isAvailable = (referenceable == null);
			} catch (ReferenceNotFoundException e) {
				isAvailable = true;
			} catch (ErrorException e) {
				isAvailable = false;
			}
		}
		return currentName;
	}
}
