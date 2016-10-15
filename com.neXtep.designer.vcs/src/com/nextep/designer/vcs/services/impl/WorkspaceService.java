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
package com.nextep.designer.vcs.services.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.datadesigner.vcs.impl.RepositoryUser;
import com.nextep.datadesigner.vcs.impl.VersionBranch;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.ITargetSet;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.IWorkspaceListener;
import com.nextep.designer.vcs.model.impl.Activity;
import com.nextep.designer.vcs.model.impl.Workspace;
import com.nextep.designer.vcs.services.IDependencyService;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.services.IWorkspaceService;

public class WorkspaceService implements IWorkspaceService {

	/** Max number of dependencies displayed in an error message */
	private final static int MAX_DISPLAYED_DEPENDENCIES = 10;
	private IWorkspace currentView;
	private ITargetSet currentTargetSet;
	private List<IWorkspaceListener> listeners;
	private IRepositoryUser currentUser;
	private IVersioningService versioningService;
	private IDependencyService dependencyService;

	public WorkspaceService() {
		listeners = new ArrayList<IWorkspaceListener>();
	}

	@Override
	public IWorkspace createWorkspace() {
		return new Workspace("", ""); //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public IWorkspace getCurrentWorkspace() {
		return currentView;
	}

	@Override
	public ITargetSet getCurrentViewTargets() {
		if (currentTargetSet == null || currentTargetSet.getView() != currentView) {
			Collection<ITypedObject> targets = CorePlugin.getPersistenceAccessor().load(
					IElementType.getInstance(ITargetSet.TYPE_ID), getCurrentWorkspace());
			if (targets != null && targets.size() > 0) {
				currentTargetSet = (ITargetSet) targets.iterator().next();
			}
		}
		return currentTargetSet;
	}

	@Override
	public void setCurrentWorkspace(IWorkspace currentView) {
		this.currentView = currentView;
	}

	@Override
	public void addWorkspaceListener(IWorkspaceListener listener) {
		listener.setWorkspaceService(this);
		listeners.add(listener);
		Collections.sort(listeners, new Comparator<IWorkspaceListener>() {

			@Override
			public int compare(IWorkspaceListener o1, IWorkspaceListener o2) {
				return o1.getPriority() - o2.getPriority();
			}
		});
	}

	@Override
	public void removeWorkspaceListener(IWorkspaceListener listener) {
		if (listeners.remove(listener)) {
			listener.setWorkspaceService(null);
		}
	}

	@Override
	public void changeWorkspace(UID viewId, IProgressMonitor parentMonitor) {
		SubMonitor monitor = SubMonitor.convert(parentMonitor,
				VCSMessages.getString("loadView"), 5000); //$NON-NLS-1$

		for (IWorkspaceListener listener : listeners) {
			listener.workspaceClosed(currentView);
		}
		// We clear our hibernate session and reload our view
		HibernateUtil.getInstance().clearAllSessions(); // getSession().clear();
		CorePlugin.getService(IReferenceManager.class).flush();
		VersionBranch.reset();
		Activity.reset();
		monitor.worked(200);

		// Loading root branch
		CorePlugin.getIdentifiableDao().load(VersionBranch.class, new UID(1));
		monitor.worked(100);
		IRepositoryUser user = (IRepositoryUser) CorePlugin.getIdentifiableDao().load(
				RepositoryUser.class, getCurrentUser().getUID());
		setCurrentUser(user);
		monitor.worked(100);

		// Reloading user
		HibernateUtil.setMonitor(monitor.newChild(1000));
		IWorkspace refreshedView = null;
		try {
			CorePlugin.getService(IReferenceManager.class).startWorkspaceLoad();
			// Loading view
			refreshedView = (IWorkspace) CorePlugin.getIdentifiableDao().load(Workspace.class,
					viewId);
		} finally {
			HibernateUtil.setMonitor(null);
			CorePlugin.getService(IReferenceManager.class).endWorkspaceLoad();
		}
		monitor.setWorkRemaining(600);
		Designer.getInstance().setContext(refreshedView.getDBVendor().name());
		// Keeping reference to old view
		final IWorkspace oldView = getCurrentWorkspace();
		// Setting new view
		setCurrentWorkspace(refreshedView);
		// Notifying listeners
		int childSize = listeners.isEmpty() ? 600 : 600 / listeners.size();
		for (IWorkspaceListener listener : listeners) {
			listener.workspaceChanged(oldView, currentView,
					monitor.newChild(Math.max(childSize, 1)));
		}
		monitor.done();
	}

	@Override
	public IRepositoryUser getCurrentUser() {
		return currentUser;
	}

	@Override
	public void setCurrentUser(IRepositoryUser user) {
		currentUser = user;
	}

	@Override
	public void move(Collection<IVersionable<?>> versionToMove, IVersionContainer targetContainer,
			IProgressMonitor monitor) {
		monitor.beginTask(MessageFormat.format(VCSMessages.getString("movingVersionableCmdGlobal"), //$NON-NLS-1$
				targetContainer.getName()), versionToMove.size());
		if (targetContainer == null) {
			targetContainer = getCurrentWorkspace();
		}
		Collection<IVersionable<?>> toUnlock = new ArrayList<IVersionable<?>>(versionToMove);
		// If target container is versionable, we need to unlock as well
		if (targetContainer instanceof IVersionable<?>) {
			targetContainer = versioningService.ensureModifiable(targetContainer);
		}
		// Requesting unlock if needed (cancellation would raise here)
		versioningService.unlock(true, toUnlock.toArray(new IVersionable<?>[toUnlock.size()]));
		// Everything is ok, moving
		for (IVersionable<?> v : versionToMove) {
			monitor.subTask(MessageFormat.format(
					VCSMessages.getString("movingVersionableCmd"), v.getName(), v //$NON-NLS-1$
							.getContainer().getName(), targetContainer.getName()));
			v.getContainer().removeVersionable(v);
			targetContainer.addVersionable(v, new ImportPolicyAddOnly());
			CorePlugin.getPersistenceAccessor().save(v);
			monitor.worked(1);
		}
		monitor.done();
	}

	public void setVersioningService(IVersioningService versioningService) {
		this.versioningService = versioningService;
	}

	public void setDependencyService(IDependencyService dependencyService) {
		this.dependencyService = dependencyService;
	}

	@Override
	public void remove(IProgressMonitor m, IReferenceable... elementsToRemove) {
		IProgressMonitor monitor = SubMonitor.convert(m);
		monitor.beginTask(
				VCSMessages.getString("service.view.removeElementsTask"), elementsToRemove.length + 1); //$NON-NLS-1$
		final MultiValueMap invRefMap = CorePlugin.getService(IReferenceManager.class)
				.getReverseDependenciesMap();
		// We are at the root level of dependency computation, so no dependency is deleted so far
		List<IReferencer> deletedDependencies = Collections.emptyList();
		monitor.subTask(VCSMessages.getString("service.view.computeDependenciesTask")); //$NON-NLS-1$
		// Computing all dependencies which would remain in the chain of deletion
		List<IReferencer> remainingDependencies = getRemainingDependencies(deletedDependencies,
				invRefMap, elementsToRemove);
		if (!remainingDependencies.isEmpty()) {
			StringBuilder buf = new StringBuilder();
			int count = 0;
			for (IReferencer r : remainingDependencies) {
				if (count++ < MAX_DISPLAYED_DEPENDENCIES) {
					buf.append("  - " + NameHelper.getQualifiedName(r) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			throw new ErrorException(MessageFormat.format(
					VCSMessages.getString("service.view.remainindDependenciesError"), //$NON-NLS-1$
					remainingDependencies.size(), buf.toString()));
		}
		monitor.worked(1);
		for (IReferenceable r : elementsToRemove) {
			ControllerFactory.getController(r).modelDeleted(r);
			monitor.worked(1);
		}
		monitor.done();

	}

	@SuppressWarnings("unchecked")
	public List<IReferencer> getRemainingDependencies(List<IReferencer> initialDeletedReferencers,
			MultiValueMap invRefMap, IReferenceable... elementsToRemove) {
		// Building deleted referencers list
		List<IReferencer> deletedReferencers = new ArrayList<IReferencer>(initialDeletedReferencers);
		for (IReferenceable obj : elementsToRemove) {
			if (obj instanceof IReferencer) {
				deletedReferencers.add((IReferencer) obj);
			}
			if (obj instanceof IReferenceContainer) {
				final Map<IReference, IReferenceable> refMap = ((IReferenceContainer) obj)
						.getReferenceMap();
				for (IReferenceable child : refMap.values()) {
					if (child instanceof IReferencer) {
						deletedReferencers.add((IReferencer) child);
					}
				}
			}
		}
		// Checking items removal
		Set<IReferencer> remainingDependencies = new HashSet<IReferencer>();
		for (IReferenceable obj : elementsToRemove) {
			// Retrieving this object's reverse dependencies
			Collection<IReferencer> revDeps = (Collection<IReferencer>) invRefMap.getCollection(obj
					.getReference());
			if (revDeps == null) {
				revDeps = Collections.emptySet();
			}
			// Getting remaining elements which reference the current object, which will not be
			// deleted
			Collection<IReferencer> objRemainingDependencies = dependencyService
					.getReferencersAfterDeletion(obj, revDeps, deletedReferencers);
			// We add them to the global remaining dependencies set
			remainingDependencies.addAll(objRemainingDependencies);
		}
		// Now listing elements, because ordering matters here (resursive dependencies must be
		// placed first)
		List<IReferencer> remainingDependencyList = new ArrayList<IReferencer>(
				remainingDependencies);
		// For remaining dependencies which are themselves referenceable, we recursively compute
		// dependencies which would remain after deletion to build an exhaustive list of elements
		// to remove
		for (IReferencer referencer : remainingDependencies) {
			if (referencer instanceof IReferenceable) {
				remainingDependencyList.addAll(
						0,
						getRemainingDependencies(deletedReferencers, invRefMap,
								(IReferenceable) referencer));
			}
		}
		return remainingDependencyList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UID findWorkspaceId(String name) {
		List<Number> idList = (List<Number>) HibernateUtil
				.getInstance()
				.getSandBoxSession()
				.createSQLQuery(
						"select v.VIEW_ID from REP_VERSION_VIEWS v where lower(replace(v.view_name,' ','_'))=lower(replace(?,' ','_'))") //$NON-NLS-1$
				.setString(0, name).list(); //$NON-NLS-1$ 
		if (idList != null && idList.size() == 1) {
			return new UID(idList.iterator().next().longValue());
		}
		return null;
	}
}
