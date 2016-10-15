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
package com.nextep.designer.sqlgen.ui.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.services.IGenerationListener;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * @author Christophe Fondacci
 */
public class ShowFullDDLHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			final Iterator<?> selIt = sel.iterator();
			final List<ITypedObject> objects = new ArrayList<ITypedObject>();
			while (selIt.hasNext()) {
				final Object o = selIt.next();
				if (o instanceof ITypedObject) {
					objects.add((ITypedObject) o);
				}
			}

			Job j = new Job(SQLGenMessages.getString("sqlgen.action.generate")) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					CorePlugin.getService(IGenerationService.class).generate(
							new IGenerationListener() {

								@Override
								public void generationSucceeded(final ISQLScript result) {
									// Opening editor
									PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

										@Override
										public void run() {
											UIControllerFactory.getController(result).defaultOpen(
													result);
										}
									});
								}

								@Override
								public void generationFailed(Throwable t, String message) {

								}
							}, monitor, objects.toArray(new ITypedObject[objects.size()]));

					return Status.OK_STATUS;
				}
			};
			j.setUser(true);
			j.schedule();
		}
		return null;
	}
}
