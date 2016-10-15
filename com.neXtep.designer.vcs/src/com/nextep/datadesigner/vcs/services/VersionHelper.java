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
package com.nextep.datadesigner.vcs.services;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.OutOfDateObjectException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.impl.RepositoryUser;
import com.nextep.datadesigner.vcs.impl.VersionBranch;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.IVersionBranch;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.VersionTree;
import com.nextep.designer.vcs.model.impl.Activity;
import com.nextep.designer.vcs.model.impl.VersionInfo;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * This method provides helper methods to deal with version control common operations.
 * 
 * @author Christophe Fondacci
 */
public final class VersionHelper {

	private static final Log log = LogFactory.getLog(VersionHelper.class);
	public static final int PRECISION = IVersionInfo.PRECISION; // Indicates the modulo for a
																// release number component
	public static final long REVISION = IVersionInfo.REVISION;
	public static final long PATCH = IVersionInfo.PATCH;
	public static final long ITERATION = IVersionInfo.ITERATION;
	public static final long MINOR = IVersionInfo.MINOR;
	public static final long MAJOR = IVersionInfo.MAJOR;
	public static IWorkspace currentView = null;

	private VersionHelper() {
	}

	@SuppressWarnings("unchecked")
	public static List<IVersionBranch> listBranches() {
		final List<IVersionBranch> branches = (List<IVersionBranch>) CorePlugin
				.getIdentifiableDao().loadAll(VersionBranch.class);
		Collections.sort(branches, NameComparator.getInstance());
		return branches;
	}

	/**
	 * Formats a String suffix containing version information.
	 * 
	 * @param v version information
	 * @return the formatted version suffix of this version
	 */
	public static String getVersionSuffix(IVersionInfo v) {
		if (v != null) {
			return " - " + v.getLabel(); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Generates the full version tree of this version.
	 * 
	 * @param version
	 * @return
	 */
	public static VersionTree getVersionTree(IVersionable<?> versionable) {
		// TODO : implement getVersionTree
		return new VersionTree(versionable.getVersion(), null);
	}

	public static boolean ensureModifiable(Object o, boolean raise) {
		if (o instanceof IVersionable<?>) {
			o = ((IVersionable<?>) o).getVersionnedObject();
		}
		// Delegating to the neXtep core feature
		return Designer.checkIsModifiable(o, raise);
	}

	/**
	 * Retrieves all versionables of a specified type and return them in a list ordered by Container
	 * name, versionable name.
	 * 
	 * @param container container in which to look for versioned elements
	 * @param type element type to extract or <code>null</code> for all types
	 * @return a list of all IVersionable elements
	 */
	public static List<IVersionable<?>> getAllVersionables(IVersionContainer container,
			IElementType type) {
		List<IVersionable<?>> list = new ArrayList<IVersionable<?>>();
		for (IVersionable<?> v : container.getContents()) {
			// If we have a matching element we add it to the list
			if (v.getType() == type || type == null) {
				list.add(v);
			}
			// Not having a "else" here because the type could be a CONTAINER or VIEW
			if (v instanceof IVersionContainer) {
				list.addAll(getAllVersionables((IVersionContainer) v, type));
			}
		}
		Collections.sort(list, NameComparator.getInstance());
		return list;
	}

	/**
	 * This method can retrieve a Versionable object from its model. This avoids unsafe casts and
	 * retrieves an object which could be used for version control. This method will return null if
	 * no versionable is associated to the specified model.
	 * 
	 * @param model model for which we want the associated versionable
	 * @return the corresponding versionable
	 */
	@SuppressWarnings("unchecked")
	public static <T> IVersionable<T> getVersionable(T model) {
		// First we check if we have a self versionable model
		if (model instanceof IVersionable) {
			return (IVersionable<T>) model;
		} else if (model instanceof IParentable<?>) {
			final Object parent = ((IParentable<?>) model).getParent();
			// Unsafe cast, but callers want IVersionable to work on the IVersionable, not on model
			// so this should be ok
			return (IVersionable<T>) getVersionable(parent);
		} else {
			// TODO Add a version view browsing to retrieve the model
			return null;
		}
	}

	/**
	 * Retrieves the version info of this object.
	 * 
	 * @param o object to retrieve version information for
	 * @return the {@link IVersionInfo} associated with the object or <code>null</code> if this
	 *         object doesn't have any version information connected
	 */
	public static IVersionInfo getVersionInfo(Object o) {
		if (o instanceof IVersionInfo) {
			return (IVersionInfo) o;
		} else if (o instanceof IVersionable<?>) {
			return ((IVersionable<?>) o).getVersion();
		} else {
			return null;
		}
	}

	/**
	 * A layer on top of the reference manager which loads version references when they are found
	 */
	public static IReferenceable getReferencedItem(IReference ref) {
		return getReferencedItem(ref, false);
	}

	public static IReferenceable getReferencedItem(IReference ref, boolean strict) {
		UnresolvedItemException unresolvedException = null;
		List<IReferenceable> filteredRef = Collections.emptyList();
		try {
			final List<IReferenceable> refs = CorePlugin.getService(IReferenceManager.class)
					.getReferencedItems(ref);
			// Filtering VersionInfo
			filteredRef = filterVersionReferences(refs);
		} catch (UnresolvedItemException e) {
			// Setting exception
			unresolvedException = e;
		}
		// In non strict mode we do a last try by attempting to get a workspace element
		if (!strict && (filteredRef.isEmpty() || unresolvedException != null)) {
			if (ref.isVolatile() && ref.getUID() != null && ref.getUID().rawId() > 0) {
				// Last try by converting to a non-volatile ref
				// TODO We should never do this as it could introduce some hardcore bugs
				// But at the moment, removing that piece will make a lot regression
				// The good way to go would be to load the referenced item from db. But since we
				// don't know what kind of elements we need to load, it is very hard to do
				try {
					ref.setVolatile(false);
					log.debug("WARNING: Out of scope reference resolution for " + ref.toString()); //$NON-NLS-1$
					final List<IReferenceable> refs = CorePlugin
							.getService(IReferenceManager.class).getReferencedItems(ref);
					filteredRef = filterVersionReferences(refs);
				} finally {
					ref.setVolatile(true);
				}
			}
		}
		// If we have more than 1 referenceable, we fail
		if (filteredRef.isEmpty()) {
			throw new UnresolvedItemException(MessageFormat.format(
					VCSMessages.getString("helper.version.itemReferenceNotFound"), //$NON-NLS-1$
					ref.getReferenceId(),
					(ref.getType() == null ? "null" : ref.getType().getName()), //$NON-NLS-1$
					ref.getArbitraryName()));
		} else if (filteredRef.size() > 1) {
			throw new ErrorException(MessageFormat.format(
					VCSMessages.getString("helper.version.tooManyReferences"), //$NON-NLS-1$
					ref.getReferenceId(), ref.getType().getName(), filteredRef.size()));
		}
		return filteredRef.iterator().next();
	}

	/**
	 * Filters any object which is a {@link IVersionInfo} from the specified referenceabl list.
	 * 
	 * @param refs the initial referenceable list
	 * @return the input list cleaned from any {@link IVersionInfo} instance
	 */
	private static List<IReferenceable> filterVersionReferences(List<IReferenceable> refs) {
		List<IReferenceable> filteredRefs = new ArrayList<IReferenceable>();
		for (IReferenceable r : refs) {
			if (!(r instanceof IVersionInfo)) {
				filteredRefs.add(r);
			}
		}
		return filteredRefs;
	}

	/**
	 * @deprecated Use {@link Designer#getReferencedItem(IVersionContainer,IReference)} instead
	 */
	// public static IReferenceable getReferencedItem(IVersionContainer container, IReference ref) {
	// return CorePlugin.getService(IReferenceManager.class).getReferencedItem(container, ref);
	// }
	/**
	 * Incremental release alogrythm: This method will compute the next release to come.
	 * 
	 * @param release version to increment
	 * @param releaseType type of release to increment, choose a constant PATCH,ITERATION,MINOR or
	 *        MAJOR.
	 */
	public static void incrementRelease(IVersionInfo release, long releaseType) {
		long fullVersion = computeVersion(release);
		long incrementedRelease = incrementRelease(fullVersion, releaseType);
		release.setRelease(incrementedRelease, true);
	}

	/**
	 * Incremental release alogrythm: This method will compute the next release to come.
	 * 
	 * @param release version to increment
	 * @param releaseType type of release to increment, choose a constant PATCH,ITERATION,MINOR or
	 *        MAJOR.
	 */
	public static long incrementRelease(long releaseNumber, long releaseType) {
		long fullVersion = releaseNumber;
		fullVersion += releaseType;
		// Resetting any low precision numbers
		fullVersion = (fullVersion / releaseType) * releaseType;
		return fullVersion;
	}

	/**
	 * Computes the release full number from its version information
	 * 
	 * @param release
	 * @return the full integer value of this release
	 */
	public static long computeVersion(IVersionInfo release) {
		if (release == null) {
			return 0l;
		} else {
			return computeVersion(release.getMajorRelease(), release.getMinorRelease(),
					release.getIteration(), release.getPatch(), release.getRevision());
		}
	}

	/**
	 * Computes the release full number from release fragments
	 * 
	 * @param major major release number
	 * @param minor minor release number
	 * @param iteration iteration number
	 * @param patch patch number
	 * @param revision revision number
	 * @return the full integer value of this release
	 */
	public static long computeVersion(int major, int minor, int iteration, int patch, int revision) {
		return Math.max(revision, 0) * REVISION + patch * PATCH + iteration * ITERATION + minor
				* MINOR + major * MAJOR;
	}

	/**
	 * Retrieves the release type from the specified version information by checking the release
	 * increment between the previous version of the version hierarchy and the current one.
	 * 
	 * @param release release to retrieve the type of
	 * @return the release type
	 */
	public static long getReleaseType(IVersionInfo release) {
		IVersionInfo previousRelease = release.getPreviousVersion();
		// If this is our first release, it is a major one
		if (previousRelease == null) {
			return MAJOR;
		} else {
			if (release.getMajorRelease() > previousRelease.getMajorRelease()) {
				return MAJOR;
			} else if (release.getMinorRelease() > previousRelease.getMinorRelease()) {
				return MINOR;
			} else if (release.getIteration() > previousRelease.getIteration()) {
				return ITERATION;
			} else if (release.getPatch() > previousRelease.getPatch()) {
				return PATCH;
			} else {
				return 0;
			}
		}
	}

	/**
	 * Sets the current version view
	 * 
	 * @param view current version view
	 * @deprecated please use {@link IWorkspaceService#setCurrentWorkspace(IWorkspace)} instead
	 */
	@Deprecated
	public static void setCurrentView(IWorkspace view) {
		VCSPlugin.getViewService().setCurrentWorkspace(view);
	}

	/**
	 * Retrieves the current version view.
	 * 
	 * @return the current version view
	 * @deprecated please use {@link IWorkspaceService#getCurrentWorkspace()} instead
	 */
	@Deprecated
	public static IWorkspace getCurrentView() {
		return VCSPlugin.getViewService().getCurrentWorkspace();
	}

	/**
	 * Removes a versionable from its owning container. Dependency lookups should already have been
	 * done before calling this method. This method will perform version checks before allowing the
	 * versionable removal.
	 * 
	 * @param v versionable to remove from owning container
	 */
	public static void removeVersionable(IVersionable<?> v) {
		IVersionContainer c = v.getContainer();
		if (c != null) {
			// Ensuring the owning container is modifiable
			c = getVersioningService().ensureModifiable(c);
			// Removing the versionable
			if (!c.removeVersionable(v)) {
				throw new OutOfDateObjectException(c,
						VCSMessages.getString("helper.version.containerUpdated")); //$NON-NLS-1$
			}
			// Unreferencing
			CorePlugin.getService(IReferenceManager.class).dereference(v);
			// Unreferencing inner references
			for (IReferenceable r : v.getReferenceMap().values()) {
				// Unreferencing replaced instance
				try {
					CorePlugin.getService(IReferenceManager.class).dereference(r);
				} catch (ErrorException e) {
					log.error(
							MessageFormat.format(
									VCSMessages.getString("helper.version.dereferenceError"), r //$NON-NLS-1$
											.getReference().getUID().toString()), e);
				}
			}
			// Updating the parent container
			CorePlugin.getIdentifiableDao().save(c);
			log.info(MessageFormat.format(VCSMessages
					.getString("helper.version.versionableRemoved"), v.getType().getName(), //$NON-NLS-1$
					v.getName()));
		}
	}

	/**
	 * This method returns a collection of the commands needed to properly clear the hibernate
	 * session without conflicting with any opened view.
	 * 
	 * @param flushReferences a boolean indicating if the command should flush neXtep soft
	 *        references during the cleanup
	 * @return a list of commands to execute
	 */
	public static List<ICommand> getClearSessionCommands(final boolean flushReferences) {
		List<ICommand> cmds = new ArrayList<ICommand>();
		cmds.add(new ICommand() {

			@Override
			public Object execute(Object... parameters) {
				// We clear our hibernate session and reload our view
				HibernateUtil.getInstance().clearAllSessions(); // getSession().clear();
				if (flushReferences) {
					CorePlugin.getService(IReferenceManager.class).flush();
				}
				VersionBranch.reset();
				Activity.reset();

				return null;
			}

			@Override
			public String getName() {
				return VCSMessages.getString("helper.version.sessionCleanup"); //$NON-NLS-1$
			}
		});
		cmds.add(new ICommand() {

			@Override
			public Object execute(Object... parameters) {
				logInfo(VCSMessages.getString("loadRootBranch")); //$NON-NLS-1$
				CorePlugin.getIdentifiableDao().load(VersionBranch.class, new UID(1));
				// Reloading user
				IRepositoryUser user = (IRepositoryUser) CorePlugin.getIdentifiableDao().load(
						RepositoryUser.class, getCurrentUser().getUID());
				setCurrentUser(user);
				// try {
				// //Sleeping only to ensure proper progress display
				// Thread.sleep(200);
				// } catch(InterruptedException e) {
				//
				// }
				return null;
			}

			@Override
			public String getName() {
				return VCSMessages.getString("loadRootBranch"); //$NON-NLS-1$
			}
		});
		return cmds;
	}

	/**
	 * Relinks the given object into the current view. This method should be called when adding a
	 * new non-empty object into the view after the view initialization.
	 * 
	 * @param o object to relink to the view
	 */
	public static void relink(ITypedObject o) {
		MultiValueMap invRefMap = null;
		if (o instanceof IReferenceable) {
			// Retrieving dependency map of the element
			invRefMap = CorePlugin.getService(IReferenceManager.class)
					.getReverseDependenciesMapFor(((IReferenceable) o).getReference());
		} else {
			// Backward compatibility
			invRefMap = CorePlugin.getService(IReferenceManager.class).getReverseDependenciesMap();
		}
		relink(o, invRefMap);
	}

	/**
	 * Relinks the given object into the current view. This method should be called when adding a
	 * new non-empty object into the view after the view initialization.<br>
	 * This method allow caller to pre-build the reverse dependencies map to use by linkers.
	 * 
	 * @param o object to relink to the view
	 * @param invRefMap reverse dependencies map
	 */
	public static void relink(ITypedObject o, MultiValueMap invRefMap) {
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(
				IViewLinker.LINKER_EXTENSION_POINT_ID, "name", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		for (IConfigurationElement conf : elts) {
			try {
				IViewLinker linker = (IViewLinker) conf.createExecutableExtension("class"); //$NON-NLS-1$
				try {
					linker.relink(o, invRefMap);
				} catch (Exception e) {
					log.error(MessageFormat.format(VCSMessages.getString("linkException"), //$NON-NLS-1$
							conf.getAttribute("name")), e); //$NON-NLS-1$
				}
			} catch (CoreException e) {
				final String confName = conf.getAttribute("name"); //$NON-NLS-1$
				log.error(MessageFormat.format(
						VCSMessages.getString("helper.version.linkerLoadFail"), confName), e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Logs information messages. Should a progress monitor be currently active (typically during
	 * the splash screen), messages will be sent as the task name of the monitor.
	 * 
	 * @param message message to log
	 */
	private static void logInfo(String message) {
		log.info(message);
		IProgressMonitor m = Designer.getProgressMonitor();
		if (m != null) {
			m.setTaskName(message);
		}
	}

	/**
	 * @return the current repository user
	 * @deprecated please use {@link IWorkspaceService#getCurrentUser()} instead
	 */
	@Deprecated
	public static IRepositoryUser getCurrentUser() {
		return VCSPlugin.getViewService().getCurrentUser();
	}

	/**
	 * Defines the currently connected repository user
	 * 
	 * @param user connected user
	 * @deprecated please use {@link IWorkspaceService#setCurrentUser(IRepositoryUser)} instead
	 */
	@Deprecated
	public static void setCurrentUser(IRepositoryUser user) {
		VCSPlugin.getViewService().setCurrentUser(user);
	}

	/**
	 * This method retrieves the real revision number of the specified versionable by querying the
	 * database's current value and compares it with the previously loaded versionable revision
	 * number.<br>
	 * The revision is used to determine if the current workspace object is up to date or if it has
	 * been modified externally and therefore needs to be synchronized.
	 * 
	 * @param v versionable to retrieve revision
	 * @return <code>true</code> if the versionable is up to date, <code>false</code> if it needs to
	 *         be synchronized.
	 */
	public static boolean isUpToDate(IVersionable<?> v) {
		if (v == null || v.getVersion() == null || v.getVersion().getUID() == null) {
			return true;
		}
		return queryIsUpToDate("select UPDATE_REVISION from REP_VERSIONS where VERSION_ID=?", v //$NON-NLS-1$
				.getVersion().getUID().rawId(), v.getVersion().getUpdateRevision());
	}

	/**
	 * Executes the specified revision-check query for the given element id. The return boolean
	 * indicates whether the revision number fetched from the database through the given query
	 * matches the expected revision number.
	 * 
	 * @param query SQL query which can retrieve the revision number from its ID
	 * @param id unique ID of element to check the revision
	 * @param expectedRevision expected revision number
	 * @return <code>true</code> if the expected revision number matches the repository revision
	 *         number, else <code>false</code>
	 */
	private static boolean queryIsUpToDate(String query, long id, long expectedRevision) {
		final Session session = HibernateUtil.getInstance().getSandBoxSession();
		session.flush();
		session.clear();
		SQLQuery sqlQuery = session.createSQLQuery(query);
		sqlQuery.setLong(0, id);
		Number revision = (Number) sqlQuery.uniqueResult();
		return revision == null || (revision != null && revision.longValue() == expectedRevision);
	}

	/**
	 * Same as {@link VersionHelper#isUpToDate(IVersionable)} except that this method can handle
	 * views (which are not versionables
	 * 
	 * @param c container to check
	 * @return <code>true</code> if synched with repository, else <code>false</code>
	 */
	public static boolean isContainerUpToDate(IVersionContainer c) {
		if (c == null || c.getUID() == null) {
			return true;
		}
		if (c instanceof IVersionable<?>) {
			return isUpToDate((IVersionable<?>) c);
		} else if (c instanceof IWorkspace) {
			return queryIsUpToDate("select UPDATE_REVISION from REP_VERSION_VIEWS where VIEW_ID=?", //$NON-NLS-1$
					c.getUID().rawId(), ((IWorkspace) c).getRevision());
		}
		return true;
	}

	/**
	 * Refreshes the specified versionable from the database current information
	 * 
	 * @param v versionable to refresh
	 */
	public static IVersionable<?> refresh(IVersionable<?> v) {
		CorePlugin.getIdentifiableDao().refresh(v.getVersion());
		CorePlugin.getIdentifiableDao().refresh(v);
		return v;
	}

	public static IVersionContainer refreshContainer(IVersionContainer c) {
		if (c instanceof IVersionable<?>) {
			return (IVersionContainer) refresh((IVersionable<?>) c);
		} else {
			return null;
		}
	}

	public static IVersioningService getVersioningService() {
		return VCSPlugin.getService(IVersioningService.class);
	}

	/**
	 * Checks if the specified version is available. This method will query the database to check
	 * that the specified version number has not already been created (for example on another view)
	 * 
	 * @param version version to check the availability of
	 * @return a flag indicating the version number availability
	 */
	@SuppressWarnings("unchecked")
	public static boolean isVersionAvailable(IVersionInfo version) {
		final long versionNo = computeVersion(version);
		List<IVersionInfo> versions = (List<IVersionInfo>) CorePlugin.getIdentifiableDao()
				.loadForeignKey(VersionInfo.class, version.getReference().getReferenceId(),
						"reference"); //$NON-NLS-1$
		// Checking availability of this version
		for (IVersionInfo info : new ArrayList<IVersionInfo>(versions)) {
			if (info.isDropped()) {
				versions.remove(info);
			} else {
				// Comparing values
				if (versionNo == computeVersion(info)) {
					return false;
				}
			}
		}
		return true;

	}

	/**
	 * Informs whether the specified version has the eligible version in any of its previous
	 * versions
	 * 
	 * @param version version whose tree will be checked for predecessor
	 * @param eligiblePredecessor the version which might be a predecessor
	 * @return <code>true</code> if eligiblePredecessor is a predecessor of version, in other words
	 *         whenever it belongs to the previous / merge version tree of version
	 */
	public static boolean isPredecessor(IVersionInfo version, IVersionInfo eligiblePredecessor) {
		// Recursivity stop check
		if (version == null) {
			return false;
		} else if (version.equals(eligiblePredecessor)) {
			return true;
		} else {
			return isPredecessor(version.getPreviousVersion(), eligiblePredecessor)
					|| isPredecessor(version.getMergedFromVersion(), eligiblePredecessor);
		}
	}
}
