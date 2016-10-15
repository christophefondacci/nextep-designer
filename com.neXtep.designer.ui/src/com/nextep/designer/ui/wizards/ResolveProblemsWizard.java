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
package com.nextep.designer.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.ui.UIMessages;

/**
 * This wizards displays the problem resolver page which allows the user to view any problem which
 * blocks the current operation and propose hints to resolve those problems.
 * 
 * @author Christophe Fondacci
 */
public class ResolveProblemsWizard extends Wizard {

	private final static Log LOGGER = LogFactory.getLog(ResolveProblemsWizard.class);
	private List<IMarker> markers;
	private ResolveProblemNewPage page;

	public ResolveProblemsWizard(List<IMarker> markers) {
		this.markers = markers;
		setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performFinish() {
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					monitor.beginTask(
							UIMessages.getString("wizard.problems.applyHints"), markers.size() + 1); //$NON-NLS-1$
					for (IMarker marker : markers) {
						final Object markedObj = marker.getRelatedObject();
						monitor.subTask(MessageFormat.format(
								UIMessages.getString("wizard.problems.processingElemnt"), //$NON-NLS-1$
								NameHelper.getQualifiedName(markedObj)));
						if (marker.getSelectedHint() != null) {
							marker.getSelectedHint().execute(marker.getRelatedObject());
						}
						monitor.worked(1);
						while (Display.getDefault().readAndDispatch()) {
						}
					}
					// Refreshing viewer
					monitor.done();
				}
			});
		} catch (InterruptedException e) {
			LOGGER.error(
					MessageFormat.format(UIMessages
							.getString("wizard.problems.interruptedException"), e.getMessage()), e); //$NON-NLS-1$
			return false;
		} catch (InvocationTargetException e) {
			LOGGER.error(
					MessageFormat.format(UIMessages.getString("wizard.problems.targetException"), e //$NON-NLS-1$
							.getMessage()), e);
			return false;
		}
		return true;
	}

	@Override
	public void addPages() {
		page = new ResolveProblemNewPage(markers);
		addPage(page);
	}

}
