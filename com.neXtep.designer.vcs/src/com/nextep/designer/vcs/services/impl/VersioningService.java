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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.LockedElementException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.core.model.IProblemSolver;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.core.model.ResourceConstants;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.util.Assert;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.marker.impl.CheckOutHint;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningListener;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.IVersioningValidator;
import com.nextep.designer.vcs.model.VersioningOperation;
import com.nextep.designer.vcs.model.impl.Activity;
import com.nextep.designer.vcs.model.impl.VersionInfo;
import com.nextep.designer.vcs.model.impl.VersioningOperationContext;
import com.nextep.designer.vcs.preferences.VersioningPreferenceConstants;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * Versioning service default implementation.
 * 
 * @author Christophe Fondacci
 */
public class VersioningService implements IVersioningService {

	private final static Log log = LogFactory.getLog(VersioningService.class);
	private final static int RECENT_ACTIVITIES_COUNT = 10;
	private IProblemSolver problemSolver;
	private IListenerService listenerService;
	private ICoreFactory coreFactory;
	private ICoreService coreService;
	private Collection<IVersioningValidator> validators = new ArrayList<IVersioningValidator>();
	private List<IActivity> activities;
	private List<IVersioningListener> listeners = new ArrayList<IVersioningListener>();

	public VersioningService() {
		activities = loadActivities();
	}

	/**
	 * This check out listener allow to be notified of the checked out object model which may happen
	 * in the problem solver context.
	 * 
	 * @author Christophe Fondacci
	 */
	private class CheckOutListener implements IEventListener, IModelOriented<Object> {

		private Object model;

		public CheckOutListener(Object model) {
			this.model = model;
		}

		@Override
		public Object getModel() {
			return model;
		}

		@Override
		public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		}

		@Override
		public void setModel(Object model) {
			this.model = model;
		}
	}

	/**
	 * This class is able to maintain the proper link with a versionable child element during the
	 * versioning lifecycle. A {@link IVersionableChild} is an object whose lifecycle is controlled
	 * by a parent {@link IVersionable} object which may change depending on versioning operations
	 * on the object.<br>
	 * The getModel() method of this class will always return the current workspace element. Note
	 * that it may return <code>null</code> when the child versionable no longer exists in the
	 * workspace.
	 * 
	 * @author Christophe Fondacci
	 */
	private class VersionableChildCheckoutListener implements IEventListener,
			IModelOriented<IReferenceable> {

		private IReferenceable model;

		public VersionableChildCheckoutListener(IReferenceable model) {
			this.model = model;
		}

		@Override
		public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		}

		@Override
		public IReferenceable getModel() {
			return model;
		}

		@Override
		public void setModel(IReferenceable model) {
			if (model instanceof IVersionable<?>) {
				final IVersionable<?> v = (IVersionable<?>) model;
				final Map<IReference, IReferenceable> childElements = v.getReferenceMap();
				if (this.model != null) {
					this.model = childElements.get(this.model.getReference());
				}
			} else {
				this.model = model;
			}
		}

	}

	private class VersionableCollectionListener extends CheckOutListener {

		private Collection<IVersionable<?>> versionables;

		public VersionableCollectionListener(IVersionable<?> model,
				Collection<IVersionable<?>> versionables) {
			super(model);
			this.versionables = versionables;
		}

		@Override
		public void setModel(Object model) {
			if (versionables.contains(getModel())) {
				versionables.remove(getModel());
				versionables.add((IVersionable<?>) model);
			}
			super.setModel(model);
		}
	}

	@Override
	public List<IVersionable<?>> checkOut(IProgressMonitor monitor,
			IVersionable<?>... checkedInVersions) {
		IVersioningOperationContext context = createVersioningContext(VersioningOperation.CHECKOUT,
				checkedInVersions);
		return checkOut(monitor, context);
	}

	@Override
	public List<IVersionable<?>> checkOut(IProgressMonitor monitor,
			IVersioningOperationContext context) {
		IVersionable<?>[] checkedInVersions = context.getVersionables().toArray(
				new IVersionable[context.getVersionables().size()]);
		// Invoking listeners
		notifyListenersBeforeService(context);
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		Assert.notNull(checkedInVersions, VCSMessages.getString("version.checkout.nullObjectError")); //$NON-NLS-1$
		monitor.beginTask(
				VCSMessages.getString("version.checkout.task"), checkedInVersions.length * 4 + 1); //$NON-NLS-1$
		monitor.subTask(VCSMessages.getString("version.checkout.prerequisite")); //$NON-NLS-1$
		// Unlocking everything or raising
		unlock(true, checkedInVersions);
		monitor.worked(1);
		List<IVersionable<?>> checkedOutVersions = new ArrayList<IVersionable<?>>(
				checkedInVersions.length);
		// If we fall here that means all problems have been resolved
		for (IVersionable<?> checkedInVersion : checkedInVersions) {
			monitor.subTask(MessageFormat.format(VCSMessages.getString("version.checkout.subTask"), //$NON-NLS-1$
					checkedInVersion.getType().getName(), checkedInVersion.getName()));
			// Retagging activity (which may have been changed by a problem solver)
			final IVersionInfo targetInfo = context.getTargetVersionInfo(checkedInVersion);
			targetInfo.setActivity(getCurrentActivity());
			// Checkouting
			final IVersionable<?> checkedOutVersion = doCheckOut(context, checkedInVersion, monitor);
			checkedOutVersions.add(checkedOutVersion);
		}
		// Invoking listeners
		notifyListenersAfterService(context, checkedOutVersions);
		return checkedOutVersions;
	}

	private <T> IVersionable<T> doCheckOut(IVersioningOperationContext context,
			IVersionable<T> checkedInVersion, IProgressMonitor monitor) {
		// Notifying listeners
		notifyListenersBeforeVersionable(context, checkedInVersion);
		final IVersionable<T> checkedOutVersion = checkedInVersion.checkOut(context);
		monitor.worked(1);
		// Replacing checked in version by checked out version in parent container
		replaceVersionable(checkedInVersion, checkedOutVersion);
		monitor.worked(1);
		// Switching listeners
		Designer.getListenerService().switchListeners(checkedInVersion, checkedOutVersion);
		monitor.worked(1);
		// Unlocking versioned element updates
		checkedOutVersion.getVersionnedObject().unlockUpdates();
		VersionHelper.relink(checkedOutVersion);
		// Finally notifying IVersionable listeners of checkout
		checkedOutVersion.notifyListeners(ChangeEvent.CHECKOUT, checkedOutVersion.getVersion());
		monitor.worked(1);
		// Notifying listeners
		notifyListenersAfterVersionable(context, checkedOutVersion);
		return checkedOutVersion;
	}

	@Override
	public void commit(IProgressMonitor monitor, IVersionable<?>... checkedOutVersions) {
		IVersioningOperationContext context = createVersioningContext(VersioningOperation.COMMIT,
				checkedOutVersions);
		commit(monitor, context);
	}

	public void commit(IProgressMonitor parentMonitor, IVersioningOperationContext context) {
		// Notifying listeners
		notifyListenersBeforeService(context);
		Collection<IVersionable<?>> checkedOutVersions = context.getVersionables();
		Assert.notNull(checkedOutVersions,
				VCSMessages.getString("version.checkout.nullObjectError")); //$NON-NLS-1$
		SubMonitor monitor = SubMonitor.convert(parentMonitor,
				VCSMessages.getString("version.commit.mainTask"), //$NON-NLS-1$
				300 * checkedOutVersions.size());
		for (IVersionable<?> checkedOutVersion : checkedOutVersions) {
			doCommit(context, monitor.newChild(300), checkedOutVersion);
		}
		// Notifying listeners
		notifyListenersAfterService(context, context.getVersionables());
	}

	private void doCommit(IVersioningOperationContext context, IProgressMonitor parentMonitor,
			IVersionable<?> checkedOutVersion) {
		SubMonitor monitor = SubMonitor.convert(
				parentMonitor,
				MessageFormat.format(
						VCSMessages.getString("version.commit.msg"), checkedOutVersion.getName(), //$NON-NLS-1$
						checkedOutVersion.getVersion().getLabel()), 300);
		// Notifying listeners
		notifyListenersBeforeVersionable(context, checkedOutVersion);
		// Recursively committing container contents
		if (checkedOutVersion instanceof IVersionContainer) {
			final List<IVersionable<?>> toCheckIn = listCheckouts(
					(IVersionContainer) checkedOutVersion.getVersionnedObject().getModel(), false);
			for (IVersionable<?> v : toCheckIn) {
				doCommit(context, monitor.newChild(200), v);
			}
		}
		monitor.setWorkRemaining(50);
		monitor.subTask(MessageFormat.format(
				VCSMessages.getString("version.commit.subTask"), checkedOutVersion.getType() //$NON-NLS-1$
						.getName(), checkedOutVersion.getName()));
		IVersionable<?> checkedInVersion = checkedOutVersion.checkIn(context);
		monitor.worked(25);
		checkedInVersion.getVersionnedObject().lockUpdates();
		// Saving our version forcing checkin save
		CorePlugin.getIdentifiableDao().save(checkedInVersion, true);
		// Notifying IVersionable listeners of checkin
		checkedInVersion.notifyListeners(ChangeEvent.CHECKIN, checkedInVersion);
		monitor.worked(25);
		// Notifying listeners
		notifyListenersAfterVersionable(context, checkedInVersion);

	}

	@Override
	public List<IVersionable<?>> undoCheckOut(IProgressMonitor monitor,
			IVersionable<?>... checkedOutVersions) {
		IVersioningOperationContext context = createVersioningContext(
				VersioningOperation.UNDO_CHECKOUT, checkedOutVersions);
		return undoCheckOut(monitor, context);
	}

	public List<IVersionable<?>> undoCheckOut(IProgressMonitor parentMonitor,
			IVersioningOperationContext context) {
		final Collection<IVersionable<?>> checkedOutVersions = context.getVersionables();
		Assert.notNull(checkedOutVersions,
				VCSMessages.getString("version.checkout.nullObjectError")); //$NON-NLS-1$
		SubMonitor monitor = SubMonitor.convert(parentMonitor, checkedOutVersions.size() * 600);
		// Notifying listeners
		notifyListenersBeforeService(context);
		List<IVersionable<?>> resultingObjects = new ArrayList<IVersionable<?>>();
		for (IVersionable<?> v : checkedOutVersions) {
			final IVersionable<?> result = doUndoCheckOut(monitor.newChild(600), context, v);
			resultingObjects.add(result);
		}
		// Notifying listeners
		notifyListenersAfterService(context, resultingObjects);
		return resultingObjects;
	}

	@SuppressWarnings("unchecked")
	private <T> IVersionable<T> doUndoCheckOut(IProgressMonitor parentMonitor,
			IVersioningOperationContext context, IVersionable<T> checkedOutVersion) {
		SubMonitor monitor = SubMonitor.convert(parentMonitor,
				MessageFormat.format(VCSMessages.getString("version.undo.task"), //$NON-NLS-1$
						checkedOutVersion.getType().getName(), checkedOutVersion.getName()), 600);
		// Notifying listeners
		notifyListenersBeforeVersionable(context, checkedOutVersion);
		// A version cannot undo check out if it is not checked out
		if (checkedOutVersion.getVersion().getStatus() != IVersionStatus.CHECKED_OUT) {
			throw new ErrorException(MessageFormat.format(
					VCSMessages.getString("version.undoCheckOut.notCheckedOutError"), //$NON-NLS-1$
					checkedOutVersion.getType().getName(), checkedOutVersion.getName()));
		}
		// Retrieving previous release
		final IVersionInfo previousVersion = checkedOutVersion.getVersion().getPreviousVersion();
		if (previousVersion == null) {
			throw new ErrorException(VCSMessages.getString("version.undo.noPrevious")); //$NON-NLS-1$
		}

		// Dropping any sub release
		if (checkedOutVersion instanceof IVersionContainer) {
			doRecursiveUndo(monitor.newChild(500), (IVersionContainer) checkedOutVersion);

		}
		monitor.setWorkRemaining(100);
		monitor.subTask(MessageFormat.format(VCSMessages.getString("version.undo.task"), //$NON-NLS-1$
				checkedOutVersion.getType().getName(), checkedOutVersion.getName()));
		IVersionable<T> previousItem = (IVersionable<T>) CorePlugin.getIdentifiableDao().load(
				IVersionable.class, previousVersion.getUID());
		monitor.worked(20);
		if (previousItem == null) {
			throw new ErrorException(VCSMessages.getString("version.undo.loadPreviousError")); //$NON-NLS-1$
		}
		// Dropping release
		checkedOutVersion.getVersion().setDropped(true);
		monitor.worked(5);
		// Replacing checked out version by last release in parent container
		replaceVersionable(checkedOutVersion, previousItem);
		monitor.worked(20);
		// Copying listeners
		Designer.getListenerService().switchListeners(checkedOutVersion, previousItem);
		monitor.worked(20);
		// Notifying
		previousItem.notifyListeners(ChangeEvent.UPDATES_LOCKED, null);
		monitor.worked(20);
		VersionHelper.relink(previousItem);
		monitor.worked(15);
		// Notifying listeners
		notifyListenersAfterVersionable(context, previousItem);
		monitor.done();
		return previousItem;
	}

	private void doRecursiveUndo(IProgressMonitor parentMonitor, IVersionContainer container) {
		SubMonitor monitor = SubMonitor
				.convert(parentMonitor, container.getContents().size() * 100);

		for (final IVersionable<?> innerVersionable : new ArrayList<IVersionable<?>>(
				container.getContents())) {
			final IVersionStatus status = innerVersionable.getVersion().getStatus();
			switch (status) {
			case CHECKED_OUT:
				undoCheckOut(monitor.newChild(100), innerVersionable);
				break;
			case NOT_VERSIONED:
				innerVersionable.getVersion().setDropped(true);
				monitor.worked(100);
				break;
			}
		}
	}

	/**
	 * Fills the list of locked elements in the parents of the given element, including itself.
	 * 
	 * @param element element to lookup for locks and parent locks
	 * @param locked a list which will be filled with locked elements
	 */
	private void fillLockedVersionablesRecursively(IVersionable<?> element,
			List<IVersionable<?>> locked) {
		final IWorkspaceService workspaceService = VCSPlugin.getService(IWorkspaceService.class);
		if (element != null) {
			// If element is locked
			if (!Designer.checkIsModifiable(element, false)) {
				// We cannot handle lock hold by other user, so is this is the reason, we throw
				if (element.getVersion().getUser() != workspaceService.getCurrentUser()
						&& element.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
					throw new ErrorException(MessageFormat.format(
							VCSMessages.getString("versioning.lockedByOtherUser"), //$NON-NLS-1$
							IFormatter.LOWERCASE.format(element.getType().getName()),
							element.getName(), element.getVersion().getUser().getName()));
				}

				// Default behaviour
				// We first add any locked parent
				if (element.getContainer() instanceof IVersionable<?>) {
					fillLockedVersionablesRecursively((IVersionable<?>) element.getContainer(),
							locked);
				}
				if (!locked.contains(element)) {
					locked.add(element);
				}
			}
		}
	}

	private boolean resolveProblems(List<IVersionable<?>> lockedList) {
		if (lockedList.isEmpty()) {
			return true;
		} else if (problemSolver == null) {
			throw new ErrorException(VCSMessages.getString("version.problems.locked")); //$NON-NLS-1$
		}
		// Converting locked IVersionable list into a list of IMarker
		List<IMarker> markers = getMarkersFromLockedList(lockedList);
		// Problem solver
		return problemSolver.solve(markers.toArray(new IMarker[markers.size()]));
	}

	/**
	 * Converts a collection of locked {@link IVersionable} elements into a collection of
	 * {@link IMarker} which describes and provides hints to fix the problem.
	 * 
	 * @param lockedList collection of locked {@link IVersionable} elements
	 * @return collection of {@link IMarker} which can fix this problem
	 */
	private List<IMarker> getMarkersFromLockedList(List<IVersionable<?>> lockedList) {
		List<IMarker> markers = new ArrayList<IMarker>();
		for (IVersionable<?> locked : lockedList) {
			final IMarker m = coreFactory
					.createMarker(
							locked,
							MarkerType.ERROR,
							MessageFormat.format(
									VCSMessages.getString("version.marker.locked"), locked.getName()), new CheckOutHint()); //$NON-NLS-1$
			m.setIcon(coreService.getResource(ResourceConstants.ICON_LOCK));
			markers.add(m);
		}
		return markers;
	}

	public void setProblemSolver(IProblemSolver solver) {
		this.problemSolver = solver;
	}

	/**
	 * Replaces a versionable by another. The parent container will switch the old versionable
	 * reference by the new one and will be saved properly. This method handles VersionManager
	 * updates to remove the old versionable reference.<br>
	 * This method should be used within a context of versioning to change the version of a
	 * versionable within a container.<br>
	 * This method will <b>NOT</b> check the version status of the parent container.
	 * 
	 * @param oldVersionable versionable which will be replaced
	 * @param newVersionable new versionable which replaces the old one
	 */
	public void replaceVersionable(IVersionable<?> oldVersionable, IVersionable<?> newVersionable) {
		// Replacing checked in version by checked out version in parent container
		IVersionContainer c = oldVersionable.getContainer();
		// Switching transparently to avoid listeners call
		c.getContents().remove(oldVersionable);
		c.getContents().add(newVersionable);
		newVersionable.setContainer(c);
		if (newVersionable instanceof IVersionContainer) {
			for (IVersionable<?> v : ((IVersionContainer) newVersionable).getContents()) {
				v.setContainer((IVersionContainer) newVersionable);
			}
		}
		// Updating reference manager
		CorePlugin.getService(IReferenceManager.class).dereference(oldVersionable);
		for (IReferenceable r : oldVersionable.getReferenceMap().values()) {
			// Unreferencing replaced instance
			try {
				CorePlugin.getService(IReferenceManager.class).dereference(r);
			} catch (ErrorException e) {
				log.error(MessageFormat.format(VCSMessages.getString("version.unreferenceError"), r //$NON-NLS-1$
						.getReference().getUID().toString()), e);
			}
		}
		CorePlugin.getService(IReferenceManager.class).reference(newVersionable.getReference(), newVersionable);
		for (IReferenceable r : newVersionable.getReferenceMap().values()) {
			// Referencing new instances
			CorePlugin.getService(IReferenceManager.class).reference(r.getReference(), r, true);
		}

		// Saving our new checked out version
		CorePlugin.getIdentifiableDao().save(newVersionable);
		// Saving our modified parent container (refreshing associations)
		CorePlugin.getIdentifiableDao().save(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T ensureModifiable(T v) {
		if (!Designer.checkIsModifiable(v, false)) {
			if (v instanceof IVersionable<?>) {
				CheckOutListener modelListener = new CheckOutListener(v);
				try {
					listenerService.registerListener(this, (IVersionable<?>) v, modelListener);
					unlock(false, (IVersionable<?>) v);
				} finally {
					listenerService.unregisterListener((IVersionable<?>) v, modelListener);
				}
				T newModel = (T) modelListener.getModel();
				// Last check
				if (Designer.checkIsModifiable(newModel, false)) {
					return newModel;
				}
			}
			throw new LockedElementException((ILockable<?>) v);
		} else {
			return v;
		}
	}

	@Override
	public void unlock(boolean parentsOnly, IVersionable<?>... lockedVersionables) {
		// We loop until all problems are resolved
		boolean problemsResolved = false;
		final List<IVersionable<?>> lockedList = new ArrayList<IVersionable<?>>();
		// Building an ordered list of all locked versionables which would prevent this
		// check-out
		for (IVersionable<?> v : lockedVersionables) {
			fillLockedVersionablesRecursively(
					parentsOnly ? VersionHelper.getVersionable(v.getContainer()) : v, lockedList);
		}
		try {
			do {
				listenVersionableChanges(lockedList);
				problemsResolved = true;
				for (IVersionable<?> v : lockedList) {
					if (!Designer.checkIsModifiable(v, false)) {
						problemsResolved = false;
						break;
					}
				}
				if (!problemsResolved) {
					resolveProblems(lockedList);
				}
			} while (!problemsResolved);
		} finally {
			listenerService.unregisterListeners(lockedList);
		}
	}

	private void listenVersionableChanges(final Collection<IVersionable<?>> versionables) {
		for (IVersionable<?> v : versionables) {
			listenerService.registerListener(versionables, v, new VersionableCollectionListener(v,
					versionables));
		}
	}

	@Override
	public List<IVersionable<?>> listCheckouts(IVersionContainer c, boolean recurseContainers) {
		List<IVersionable<?>> checkedOutItems = new ArrayList<IVersionable<?>>();
		for (IVersionable<?> content : c.getContents()) {
			if (content.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
				checkedOutItems.add(content);
			}
			if (recurseContainers
					&& content.getVersionnedObject().getModel() instanceof IVersionContainer) {
				checkedOutItems.addAll(listCheckouts((IVersionContainer) content
						.getVersionnedObject().getModel(), recurseContainers));
			}
		}
		return checkedOutItems;
	}

	public void setListenerService(IListenerService listenerService) {
		this.listenerService = listenerService;
	}

	@Override
	public List<IMarker> getUnlockMarkers(boolean parentsOnly,
			Collection<?> potentiallyLockedObjects) {
		final List<IVersionable<?>> lockedList = new ArrayList<IVersionable<?>>();
		// Building an ordered list of all locked versionables which would prevent this
		// check-out
		for (Object typedObj : potentiallyLockedObjects) {
			IVersionable<?> v = null;
			if (typedObj instanceof IVersionable<?>) {
				if (parentsOnly) {
					v = VersionHelper.getVersionable(((IVersionable<?>) typedObj).getContainer());
				} else {
					v = (IVersionable<?>) typedObj;
				}
				;
			} else if (typedObj instanceof IParentable<?>) {
				v = VersionHelper.getVersionable(typedObj);
			} else if (typedObj instanceof ILockable<?>) {
				// Should never happen...
				if (((ILockable<?>) typedObj).updatesLocked()) {
					throw new ErrorException(MessageFormat.format(
							"{0} is locked and no unlocking method could be found",
							NameHelper.getQualifiedName(typedObj)));
				}
			}
			fillLockedVersionablesRecursively(v, lockedList);
		}
		final List<IMarker> markers = getMarkersFromLockedList(lockedList);
		return markers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IModelOriented<T> createVersionAwareObject(T object) {
		IModelOriented<T> proxy = null;
		if (object instanceof IVersionable<?>) {
			// listening to version events on the object
			CheckOutListener listener = new CheckOutListener(object);
			listenerService.registerListener(VersioningService.class, (IVersionable<?>) object,
					listener);
			proxy = (IModelOriented<T>) listener;
		} else if (object instanceof IParentable<?>) {
			final IVersionable<?> parentVersionable = VersionHelper.getVersionable(object);
			final VersionableChildCheckoutListener listener = new VersionableChildCheckoutListener(
					(IReferenceable) object);
			listenerService.registerListener(VersioningService.class, parentVersionable, listener);
			proxy = (IModelOriented<T>) listener;
		} else {
			// No version, simple pass-through proxy
			proxy = (IModelOriented<T>) new CheckOutListener(object);
		}
		return proxy;
	}

	public void addValidator(IVersioningValidator validator) {
		validators.add(validator);
	}

	public void removeValidator(IVersioningValidator validator) {
		validators.remove(validator);
	}

	@Override
	public IStatus validate(IVersioningOperationContext context) {
		IStatus status = Status.OK_STATUS;
		for (IVersioningValidator validator : validators) {
			if (validator.isActiveFor(context)) {
				status = validator.validate(context);
				if (!status.isOK()) {
					return status;
				}
			}
		}
		return status;
	};

	private IVersioningOperationContext createVersioningContext(VersioningOperation operation,
			IVersionable<?>... versionables) {
		return createVersioningContext(operation, Arrays.asList(versionables));
	}

	@Override
	public IVersioningOperationContext createVersioningContext(VersioningOperation operation,
			Collection<IVersionable<?>> versionables) {
		final IVersioningOperationContext context = new VersioningOperationContext(operation,
				versionables);

		// Adjusting activity
		IActivity activity = getCurrentActivity();
		context.setActivity(activity);

		initializeContextVersions(context, context.getVersionables());

		return context;
	}

	private void initializeContextVersions(IVersioningOperationContext context,
			Collection<IVersionable<?>> versionables) {
		// Initializing target versions
		for (IVersionable<?> v : versionables) {
			IVersionInfo targetRelease = null;
			switch (context.getVersioningOperation()) {
			case CHECKOUT:
				targetRelease = VersionFactory.buildNextVersionInfo(v.getVersion(),
						context.getActivity());
				incrementRelease(targetRelease);
				break;
			case COMMIT:
				targetRelease = VersionFactory.copyVersion(v.getVersion());
				if (v.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
					if (v instanceof IVersionContainer) {
						initializeContextVersions(context, ((IVersionContainer) v).getContents());
					}
				}
				break;
			case UNDO_CHECKOUT:
				targetRelease = v.getVersion().getPreviousVersion();
				if (v.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
					if (v instanceof IVersionContainer) {
						initializeContextVersions(context, ((IVersionContainer) v).getContents());
					}
				}
				break;
			}
			context.setTargetVersionInfo(v, targetRelease);
		}
	}

	/**
	 * Increments the specified version by the increment defined by current preferences.
	 * 
	 * @param version version to increment
	 */
	private void incrementRelease(IVersionInfo version) {
		final IEclipsePreferences prefs = new InstanceScope().getNode(VCSPlugin.PLUGIN_ID);
		final String defaultIncrement = prefs.get(
				VersioningPreferenceConstants.DEFAULT_RELEASE_INCREMENT,
				String.valueOf(IVersionInfo.PATCH));
		VersionHelper.incrementRelease(version, Long.valueOf(defaultIncrement));
	}

	@Override
	public IActivity createActivity(String activityText) {
		if (activityText == null) {
			activityText = ""; //$NON-NLS-1$
		}
		// Checking if we don't already have this text in the recent activities
		for (IActivity activity : activities) {
			// If we found a match, we set it as current and return it
			if (activityText.equals(activity.getName())) {
				setCurrentActivity(activity);
				return activity;
			}
		}
		// Creating activity
		final IActivity activity = createRawActivity(activityText);
		setCurrentActivity(activity);
		return activity;
	}

	@Override
	public IActivity getCurrentActivity() {
		if (!activities.isEmpty()) {
			return activities.get(0);
		} else {
			return createActivity(""); //$NON-NLS-1$
		}
	}

	@Override
	public List<IActivity> getRecentActivities() {
		return activities;
	}

	/**
	 * Creates a new activity.
	 * 
	 * @param activity activity text to create
	 * @return a new {@link IActivity} instance
	 */
	private IActivity createRawActivity(String activity) {
		return new Activity(activity);
	}

	private List<IActivity> loadActivities() {
		final IEclipsePreferences prefs = new InstanceScope().getNode(VCSPlugin.PLUGIN_ID);
		final List<IActivity> activities = new ArrayList<IActivity>();
		for (int i = 0; i < RECENT_ACTIVITIES_COUNT; i++) {
			final String activity = prefs.get(
					VersioningPreferenceConstants.RECENT_ACTIVITIES_PREFIX + i, ""); //$NON-NLS-1$
			if (activity != null && !"".equals(activity)) { //$NON-NLS-1$
				activities.add(createRawActivity(activity));
			} else {
				break;
			}
		}
		return activities;
	}

	@Override
	public void setCurrentActivity(IActivity currentActivity) {
		final int position = activities.indexOf(currentActivity);
		if (position > 0) {
			// Placing the current activity on top
			Collections.swap(activities, 0, position);
		} else if (position == -1) {
			// Updating the recent activity list
			activities.add(0, currentActivity);
			if (activities.size() > RECENT_ACTIVITIES_COUNT) {
				activities.remove(activities.size() - 1);
			}
			// Persisting recent activities
			final IEclipsePreferences prefs = new InstanceScope().getNode(VCSPlugin.PLUGIN_ID);
			for (int i = 0; i < Math.min(RECENT_ACTIVITIES_COUNT, activities.size()); i++) {
				prefs.put(VersioningPreferenceConstants.RECENT_ACTIVITIES_PREFIX + i, activities
						.get(i).getName());
			}
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
				log.warn("Unable to save recent activities list: " + e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IVersionInfo> listVersions(IReference ref) {
		return (List<IVersionInfo>) CorePlugin.getIdentifiableDao().loadForeignKey(
				VersionInfo.class, ref.getReferenceId(), "reference"); //$NON-NLS-1$
	}

	@Override
	public void addVersioningListener(IVersioningListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeVersioningListener(IVersioningListener listener) {
		listeners.remove(listener);
	}

	private void notifyListenersBeforeService(IVersioningOperationContext context) {
		for (IVersioningListener l : listeners) {
			l.handleBeforeServiceOperation(context.getVersioningOperation(),
					context.getVersionables());
		}
	}

	private void notifyListenersAfterService(IVersioningOperationContext context,
			Collection<IVersionable<?>> versionables) {
		for (IVersioningListener l : listeners) {
			l.handleAfterServiceOperation(context.getVersioningOperation(), versionables);
		}
	}

	private void notifyListenersBeforeVersionable(IVersioningOperationContext context,
			IVersionable<?> v) {
		for (IVersioningListener l : listeners) {
			l.handleBeforeVersionableOperation(context.getVersioningOperation(), v);
		}
	}

	private void notifyListenersAfterVersionable(IVersioningOperationContext context,
			IVersionable<?> v) {
		for (IVersioningListener l : listeners) {
			l.handleAfterVersionableOperation(context.getVersioningOperation(), v);
		}
	}

	/**
	 * @param coreFactory the coreFactory to set
	 */
	public void setCoreFactory(ICoreFactory coreFactory) {
		this.coreFactory = coreFactory;
	}

	/**
	 * @param coreService the coreService to set
	 */
	public void setCoreService(ICoreService coreService) {
		this.coreService = coreService;
	}
}
