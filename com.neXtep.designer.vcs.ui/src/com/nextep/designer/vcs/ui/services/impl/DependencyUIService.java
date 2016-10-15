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
package com.nextep.designer.vcs.ui.services.impl;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.impl.DependencySearchRequest;
import com.nextep.designer.vcs.ui.model.DependencyMode;
import com.nextep.designer.vcs.ui.model.IDependencySearchRequest;
import com.nextep.designer.vcs.ui.model.IDependencyServiceListener;
import com.nextep.designer.vcs.ui.services.IDependencyUIService;

public class DependencyUIService implements IDependencyUIService {

	private Set<IDependencyServiceListener> listeners;
	private DependencyMode defaultMode;

	public DependencyUIService() {
		listeners = new HashSet<IDependencyServiceListener>();
		defaultMode = DependencyMode.OBJECTS_DEPENDENT_OF;
	}

	@Override
	public void addListener(IDependencyServiceListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void computeDependencies(final IReferenceable element, final DependencyMode type) {
		Job j = new Job(VCSUIMessages.getString("service.ui.dependencies.computationJobName")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(
						VCSUIMessages.getString("service.ui.dependencies.computationTaskName"), 2); //$NON-NLS-1$
				MultiValueMap invRefMap = CorePlugin.getService(IReferenceManager.class)
						.getReverseDependenciesMap();
				final IDependencySearchRequest request = new DependencySearchRequest(element, type,
						invRefMap);
				for (final IDependencyServiceListener l : listeners) {
					Display.getDefault().syncExec(new Runnable() {

						public void run() {
							l.newDependencyRequest(request);
						};
					});
				}
				return Status.OK_STATUS;
			}
		};
		j.schedule();
	}

	@Override
	public void computeDependencies(IReferenceable element) {
		computeDependencies(element, getDependencyMode());
	}

	@Override
	public void removeListener(IDependencyServiceListener listener) {
		listeners.remove(listener);
	}

	@Override
	public DependencyMode getDependencyMode() {
		return defaultMode;
	}

	@Override
	public void setDependencyMode(DependencyMode defaultMode) {
		this.defaultMode = defaultMode;
	}

}
