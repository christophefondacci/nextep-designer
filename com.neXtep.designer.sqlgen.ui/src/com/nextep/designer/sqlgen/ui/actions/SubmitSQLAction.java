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
package com.nextep.designer.sqlgen.ui.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.dbgm.gui.editors.ISubmitable;
import com.nextep.datadesigner.exception.DesignerException;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.model.ISQLGenerator;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.dbgm.ui.services.DBGMUIHelper;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.ui.Activator;
import com.nextep.designer.sqlgen.ui.SQLGenImages;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.sqlgen.ui.editors.SQLMultiEditor;
import com.nextep.designer.sqlgen.ui.model.SubmitionManager;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;

public class SubmitSQLAction extends Action {

	public static final String ACTION_ID = "submitSQL";
	private static final Log log = LogFactory.getLog(SubmitSQLAction.class);

	public SubmitSQLAction() {
		setId(ACTION_ID);
		setImageDescriptor(ImageDescriptor.createFromImage(SQLGenImages.ICON_BUILD_SMALL));
		setToolTipText(SQLMessages.getString("submitSQL"));
	}

	@Override
	public void run() {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		if (editor instanceof SQLMultiEditor) {
			editor = ((SQLMultiEditor) editor).getCurrentEditor();
		}
		final IEditorInput input = editor.getEditorInput();
		if (input instanceof ISubmitable && input instanceof IModelOriented<?>) {
			// Assuming this object is typed, we may need to add a check here
			final ITypedObject model = (ITypedObject) ((IModelOriented<?>) input).getModel();
			final String name = NameHelper.getQualifiedName(model);
			final IConnection conn = DBGMUIHelper.getConnection(SQLGenUtil.getDefaultTargetType());
			Job job = new Job("Building '" + name + "'...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						monitor.beginTask("Building '" + name + "'...", 3);
						if (model instanceof ISQLScript) {
							monitor.setTaskName("Submitting to target database");
							try {
								SubmitionManager.submit(conn, (ISQLScript) model, null, monitor);
							} catch (DesignerException e) {
								// A generation problem is not a "save" problem so we
								// convert the error as a warning
								log.warn(e.getMessage());
							}
						} else {
							// Trying to compile
							ISQLGenerator generator = GeneratorFactory.getGenerator(
									model.getType(), conn.getDBVendor());
							IGenerationResult result = GenerationFactory.createGenerationResult();
							result.integrate(generator.doDrop(model));
							result.integrate(generator.generateFullSQL(model));
							monitor.worked(1);
							if (monitor.isCanceled()) {
								monitor.setTaskName("Canceled");
								return Status.CANCEL_STATUS;
							}
							try {
								monitor.setTaskName("Submitting to target database");
								SubmitionManager.submit(conn, result, monitor);
							} catch (DesignerException e) {
								// A generation problem is not a "save" problem so we
								// convert the error as a warning
								log.warn(e.getMessage());
							}
						}
						monitor.worked(1);
						CorePlugin.getService(IMarkerService.class).computeAllMarkers();
						SQLEditorUIServices.getInstance().annotateVisibleEditor();
						return Status.OK_STATUS;
					} catch (RuntimeException e) {
						log.error("Problems while submitting procedure", e);
						return new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage());
					}
				}
			};
			job.schedule();
		}
	}
}
