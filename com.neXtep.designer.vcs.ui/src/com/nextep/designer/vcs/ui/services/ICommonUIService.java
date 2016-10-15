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
package com.nextep.designer.vcs.ui.services;

import java.util.List;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.model.IFormActionProvider;
import com.nextep.designer.ui.model.ITypedFormPage;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.services.IUIService;
import com.nextep.designer.vcs.model.IVersionContainer;

/**
 * Provides common methods for neXtep User Interface.
 * 
 * @author Christophe Fondacci
 */
public interface ICommonUIService extends IUIService {

	/**
	 * Finds an element in the whole workspace.
	 * 
	 * @see ICommonUIService#findElement(Shell, String, IVersionContainer)
	 * @param parentShell the shell to use as the parent for displaying dialogs to user
	 * @param title title of the filter / search dialog
	 * @return the {@link ITypedObject} selected by the user
	 */
	ITypedObject findElement(Shell parentShell, String title);

	/**
	 * Finds an element in the whole workspace.
	 * 
	 * @see ICommonUIService#findElement(Shell, String, IVersionContainer, IElementType...)
	 * @param parentShell the shell to use as the parent for displaying dialogs to user
	 * @param title title of the filter / search dialog
	 * @param types list of types restriction, or nothing for all types
	 * @return the {@link ITypedObject} selected by the user
	 */
	ITypedObject findElement(Shell parentShell, String title, IElementType... type);

	/**
	 * Finds an element in the specified module by displaying a user-friendly filter / search
	 * dialog.
	 * 
	 * @param parentShell the shell to use as the parent for displaying dialogs to user
	 * @param title title of the filter / search dialog
	 * @param module module from which elements are taken
	 * @param types list of types restriction, or nothing for all types
	 * @return the {@link ITypedObject} selected by the user
	 */
	ITypedObject findElement(Shell parentShell, String title, IVersionContainer module,
			IElementType... types);

	/**
	 * Displays the element finder using the given content provider so that anything could be
	 * injected in the dialog.
	 * 
	 * @param parentShell {@link Shell} to use to parent the dialog window
	 * @param title dialog title
	 * @param input input of the content provider
	 * @param contentProvider the {@link IContentProvider} to use to provide source elements of the
	 *        dialog
	 * @param labelProvider the {@link ILabelProvider} to use to provide elements label
	 * @return the element chosen by the user, or <code>null</code> if none
	 * @since 1.0.7
	 */
	ITypedObject findElement(Shell parentShell, String title, Object input,
			IContentProvider contentProvider, ILabelProvider labelProvider);

	/**
	 * Wraps the given action provider into a version-aware action provider able to handle and
	 * maintain the lifecycle of any object. It will handle proper lock detection, propose to unlock
	 * elements and will guarantee to provide modifiable elements to the initial action provider so
	 * that no logic needs to be implemented regarding these aspects.
	 * 
	 * @param provider the {@link IFormActionProvider} to wrap
	 * @return a version-aware {@link IFormActionProvider}
	 */
	IFormActionProvider handleLifeCycle(IFormActionProvider provider);

	/**
	 * Creates a UI component which will use the JFace providers to display elements in a list with
	 * action controls which invokes the action provider methods.
	 * 
	 * @param labelProvider the JFace {@link ILabelProvider} to render elements from the list
	 * @param contentProvider the JFace {@link IContentProvider} to extract listed elements from the
	 *        input
	 * @param actionProvider the {@link IFormActionProvider} that implements the actions that the
	 *        user can invoke (add / remove / sort)
	 * @param input input model
	 * @return the {@link IUIComponent}
	 */
	IUIComponent createTypedListComponent(ILabelProvider labelProvider,
			IContentProvider contentProvider, IFormActionProvider actionProvider, ITypedObject input);

	/**
	 * Adds verison control actions to the toolbar.
	 * 
	 * @param toolbarMgr the {@link IToolBarManager} on which we can add our actions
	 * @param model the model element
	 * @param uiElement the UI element on which we can register ourselves for disposal
	 */
	void createVersionControlToolbarActions(IToolBarManager toolbarMgr, Object model,
			Object uiElement);

	/**
	 * Creates the page for the given element type from contributions. The returned page may be
	 * blank if no extension provides information for the given type.
	 * 
	 * @param type the {@link IElementType} for which the form page allows edition
	 * @param parent the parent model
	 * @param editor an optional editor
	 * @param forceSingleElementEdition a boolean flag indicating whether the page should edit a
	 *        single model element or not, whatever extension is registered
	 * @return the {@link IFormPage}
	 */
	IFormPage createPageFor(IElementType type, ITypedObject parent, FormEditor editor,
			boolean forceSingleElementEdition);

	/**
	 * Creates and returns all form editor pages contributing to the editor whose type is given.
	 * 
	 * @param editorType the {@link IElementType} of the main multi-part editor
	 * @param parent main root object model of the editor
	 * @param editor the main {@link FormEditor}
	 * @return the list of pages which should be added in the multi-page editor
	 */
	List<ITypedFormPage> createContributedPagesFor(IElementType editorType, ITypedObject parent,
			FormEditor editor);

	/**
	 * Computes and updates messages displayed for the specified object model.
	 * 
	 * @param form the form on which messages should be displayed
	 * @param model the model to compute messages for
	 */
	void updateFormMessages(IManagedForm form, ITypedObject model, Object source);
}
