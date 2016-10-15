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
package com.nextep.designer.sqlgen.ui.editors;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.sqlgen.ui.PackageEditorInput;
import com.nextep.designer.sqlgen.ui.SQLEditorInput;
import com.nextep.designer.sqlgen.ui.SpecEditorInput;

/**
 * @author Christophe Fondacci
 */
public class SQLMultiEditor extends MultiPageEditorPart {

	public final static String EDITOR_ID = "com.neXtep.designer.sqlgen.ui.multiSQLEditor";
	SQLEditor mainEditor;
	List<IEditorPart> nestedEditors = new ArrayList<IEditorPart>();

	/**
	 * @see org.eclipse.ui.part.MultiPageEditorPart#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
	}

	/**
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
	 */
	@Override
	protected void createPages() {
		IEditorInput input = getEditorInput();
		try {
			setPartName(getEditorInput().getName());
			if (input instanceof PackageEditorInput) {
				ISQLEditorInput<?> sqlInput = (ISQLEditorInput<?>) input;
				mainEditor = new SQLEditor();
				PackageEditorInput mainInput = new PackageEditorInput((IPackage) sqlInput
						.getModel());
				int index = addPage(mainEditor, mainInput);
				setPageText(index, "Body");
				SQLEditor specEditor = new SQLEditor();
				SpecEditorInput specInput = new SpecEditorInput((IPackage) sqlInput.getModel());
				index = addPage(specEditor, specInput);
				setPageText(index, "Spec");
				// Setting the spec editor for the main input
				// mainInput.setSpecSQLEditor(specEditor);
				// mainInput.setBodyEditor(mainEditor);
				// mainInput.refreshErrors("PACKAGE BODY", mainEditor.getDocumentProvider(),
				// mainInput);
				// specInput.refreshErrors("PACKAGE", specEditor.getDocumentProvider(), specInput);
			} else if (input instanceof ISQLEditorInput<?>) {

				mainEditor = new SQLEditor();
				int index = addPage(mainEditor, input);
				setPageText(index, input.getName()); // script.getFilename());
				// If we have a wrapper then we open children
				if (((ISQLEditorInput<?>) input).getModel() instanceof SQLWrapperScript) {
					SQLWrapperScript script = (SQLWrapperScript) ((ISQLEditorInput<?>) input)
							.getModel();
					// Adding pages for subscripts
					for (ScriptType t : ScriptType.values()) {
						for (ISQLScript s : script.getChildren()) {
							if (s.getScriptType() == t) {
								index = addPage(new SQLEditor(), new SQLEditorInput(s));
								if (s.getName().equals(script.getName())) {
									setPageText(index, s.getScriptType().getCode());
								} else {
									setPageText(index, s.getFilename());
								}
							}
						}
					}
				}
			}
			// Specific listener for editor tab title update
			if (input instanceof IModelOriented<?>) {
				Object model = ((IModelOriented<?>) input).getModel();
				if (model instanceof IObservable) {
					((IObservable) model).addListener(new IEventListener() {

						@Override
						public void handleEvent(ChangeEvent event, IObservable source, Object data) {
							switch (event) {
							case MODEL_CHANGED:
								if (source instanceof INamedObject) {
									String name = ((INamedObject) source).getName();
									if (!getPartName().equals(name)) {
										setPartName(name);
									}
								}
							}
						}
					});
				}
			}
		} catch (PartInitException e) {
			throw new ErrorException("Error while initializing wrapped SQL editor", e);
		}
	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		// Saving all editors, ensuring the main editor is saved last
		for (IEditorPart part : nestedEditors) {
			if (part != mainEditor) {
				part.doSave(monitor);
			}
		}
		// Saving main editor last
		mainEditor.doSave(monitor);
	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		mainEditor.doSaveAs();
	}

	/**
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return mainEditor.isSaveAsAllowed();
	}

	public ITextEditor getCurrentEditor() {
		return (ITextEditor) getActiveEditor();
	}

	/**
	 * @see org.eclipse.ui.part.MultiPageEditorPart#addPage(org.eclipse.ui.IEditorPart,
	 *      org.eclipse.ui.IEditorInput)
	 */
	@Override
	public int addPage(IEditorPart editor, IEditorInput input) throws PartInitException {
		int index = super.addPage(editor, input);
		// Adding the editor to our nested editors list
		nestedEditors.add(editor);
		return index;
	}

}
