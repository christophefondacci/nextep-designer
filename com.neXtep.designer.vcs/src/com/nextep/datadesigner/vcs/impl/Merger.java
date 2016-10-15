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
package com.nextep.datadesigner.vcs.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.classic.Session;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.impl.StringAttribute;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMergeStrategy;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.MergeInfo;
import com.nextep.designer.vcs.model.MergeStatus;
import com.nextep.designer.vcs.model.VersionableFactory;
import com.nextep.designer.vcs.model.impl.Activity;

/**
 * The abstract root class of all mergers. All mergers should extend this class
 * and must not implement <code>IMerger</code>
 * 
 * @author Christophe Fondacci
 */
public abstract class Merger<T> extends Observable implements IMerger {

	private static final Log log = LogFactory.getLog(Merger.class);
	public static final String ATTR_NAME = "Name";
	public static final String ATTR_DESC = "Description";
	private static boolean refreshWorkEnabled = true;
	private IMergeStrategy mergeStrategy = new MergeStrategyRepository();

	/**
	 * TODO: Once the
	 * {@link MergerFactory#getMerger(com.nextep.datadesigner.model.IElementType)}
	 * will be removed, simply set the provided strategy
	 * 
	 * @see com.nextep.designer.vcs.model.IMerger#setMergeStrategy(com.nextep.designer.vcs.model.IMergeStrategy)
	 */
	@Override
	public void setMergeStrategy(IMergeStrategy mergeStrategy) {
		this.mergeStrategy = MergeStrategy.create(mergeStrategy.getComparisonScope());
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#getMergeStrategy()
	 */
	@Override
	public IMergeStrategy getMergeStrategy() {
		return mergeStrategy;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#compare(com.nextep.designer.vcs.model.VersionReference,
	 *      com.nextep.designer.vcs.model.IVersionInfo,
	 *      com.nextep.designer.vcs.model.IVersionInfo)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final IComparisonItem compare(IReference ref, IVersionInfo sourceVersion,
			IVersionInfo destinationVersion, boolean sandBoxSession) {
		IReferenceable source = null;
		// A flag to clear the session even if source version is null
		final Session session = (sandBoxSession ? HibernateUtil.getInstance().getSandBoxSession()
				: HibernateUtil.getInstance().getSession());
		if (sourceVersion != null) {
			CorePlugin.getService(IReferenceManager.class).flushVolatiles(session);
			IVersionable<IReferenceable> sourceItem = (IVersionable<IReferenceable>) CorePlugin
					.getIdentifiableDao().load(IVersionable.class, sourceVersion.getUID(), session,
							sandBoxSession); // If sandbox, we clear the session
			if (sourceItem != null) {
				source = sourceItem.getVersionnedObject().getModel();
			} else {
				log.warn("Unable to load source item for merge/compare operations.");
			}
		}
		IReferenceable target = null;
		if (destinationVersion != null) {
			CorePlugin.getService(IReferenceManager.class).flushVolatiles(session);
			IVersionable<IReferenceable> targetItem = (IVersionable<IReferenceable>) CorePlugin
					.getIdentifiableDao().load(IVersionable.class, destinationVersion.getUID(),
							session, sandBoxSession); // If sandboxed we clear
														// the session
			if (targetItem != null) {
				target = targetItem.getVersionnedObject().getModel();
			} else {
				log.warn("Unable to load target item for merge/compare operations.");
			}
		}

		if (source instanceof ITypedObject) {
			VersionHelper.relink((ITypedObject) source);
		}
		if (target instanceof ITypedObject) {
			VersionHelper.relink((ITypedObject) target);
		}
		// Preserving result
		refreshProgressLabel(source, target);
		IComparisonItem item = compare(source, target);
		refreshWork();
		return item;
	}

	@Override
	public final IComparisonItem compare(IReferenceable source, IReferenceable target) {
		// Handling MISSING_SOURCE / MISSING_TARGET without any memory usage
		if (getMergeStrategy().getComparisonScope() != ComparisonScope.DB_TO_REPOSITORY) {
			if ((source == null || target == null || source == target) && !copyWhenUnchanged()) {
				final IComparisonItem result = new ComparisonResult(source, target,
						getMergeStrategy().getComparisonScope());
				return result;
			} else {
				// If we have 2 versionables, we only need to compare differing
				// versions
				if (source instanceof IVersionable<?> && target instanceof IVersionable<?>) {
					final IVersionInfo srcVersion = ((IVersionable<?>) source).getVersion();
					final IVersionInfo tgtVersion = ((IVersionable<?>) target).getVersion();
					if (srcVersion.equals(tgtVersion)) {
						final IComparisonItem result = new ComparisonResult(source, target,
								getMergeStrategy().getComparisonScope());
						return result;
					}
				}
			}
		}

		// Generic comparison
		final IComparisonItem result = doCompare(source, target);

		// Specific comparison, using some abstract / inheritance tweaks which
		// are not good. We know that but that's the way mergers are built at
		// the moment
		// and we could not refactor everything at the moment.
		if (isClass(getSpecificClass(), source, target)) {
			fillSpecificComparison(result, (T) source, (T) target);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IComparisonItem compare(IReferenceable source, IVersionInfo otherVersion) {
		IReferenceable target = null;
		final Session session = HibernateUtil.getInstance().getSandBoxSession();
		if (otherVersion != null) {
			CorePlugin.getService(IReferenceManager.class).flushVolatiles(session);
			IVersionable<IReferenceable> targetItem = (IVersionable<IReferenceable>) CorePlugin
					.getIdentifiableDao().load(IVersionable.class, otherVersion.getUID(), session,
							true);
			VersionHelper.relink(targetItem, CorePlugin.getService(IReferenceManager.class)
					.getReverseDependenciesMapFor(targetItem.getReference()));
			if (targetItem != null) {
				target = targetItem.getVersionnedObject().getModel();
			} else {
				log.warn("Unable to load target item for merge/compare operations.");
			}
		}
		IComparisonItem item = compare(source, target);
		return item;
	}

	/**
	 * Makes the comparison between the 2 specified referenceable. This is an
	 * inner protected method as the comparison is *proxied* by the
	 * {@link Merger#compare(IReferenceable, IReferenceable)} method to only
	 * perform the comparison when there is no other way to identify
	 * differences.
	 * 
	 * @param source
	 *            source element of the comparison
	 * @param target
	 *            target element of the comparison
	 * @return a {@link IComparisonItem} containing the comparison information
	 */
	protected abstract IComparisonItem doCompare(IReferenceable source, IReferenceable target);

	protected void refreshProgressLabel(IReferenceable src, IReferenceable tgt) {
		// IReferenceable r = (src == null ? tgt : src);
		if (src == null && tgt == null)
			return;
		if (Designer.getProgressMonitor() != null) {
			// Designer.getProgressMonitor().subTask("Comparing " +
			// Designer.getInstance().getQualifiedName(r) + "...");
		}
	}

	protected void refreshWork() {
		if (Designer.getProgressMonitor() != null && refreshWorkEnabled) {
			try {
				Designer.getProgressMonitor().worked(1);
			} catch (RuntimeException e) {
				log.debug(e);
			}
		}
	}

	public static void setRefreshWorkEnabled(boolean refreshWorkEnabled) {
		Merger.refreshWorkEnabled = refreshWorkEnabled;
	}

	/**
	 * Compares 2 <code>INamedObject</code> and adds the result to the provided
	 * comparison item.
	 * 
	 * @param result
	 *            the current comparison result
	 * @param source
	 *            source of the comparison
	 * @param target
	 *            target of the comparison
	 */
	protected void compareName(IComparisonItem result, INamedObject source, INamedObject target) {
		result.addSubItem(new ComparisonNameAttribute(ATTR_NAME, source == null ? null
				: notNull(source.getName()), target == null ? null : notNull(target.getName()),
				MergeStrategy.isCaseSensitive()));
		result.addSubItem(new ComparisonAttribute(ATTR_DESC, source == null ? null : notNull(source
				.getDescription()), target == null ? null : notNull(target.getDescription()),
				ComparisonScope.REPOSITORY));
	}

	protected String notNull(String str) {
		return str == null ? "" : str;
	}

	protected void fillName(IComparisonItem result, INamedObject target) {
		target.setName(getStringProposal(ATTR_NAME, result));
		target.setDescription(getStringProposal(ATTR_DESC, result));
	}

	public static String getStringProposalValue(String attributeName, IComparisonItem result) {
		return getStringProposal(attributeName, result, null);
	}

	public String getStringProposal(String attributeName, IComparisonItem result) {
		return getStringProposal(attributeName, result, getMergeStrategy().getComparisonScope());
	}

	public static String getStringProposal(String attributeName, IComparisonItem result,
			ComparisonScope scope) {
		ComparisonAttribute attr = (ComparisonAttribute) result.getAttribute(attributeName);
		if (attr == null) {
			// Special use-case for comparison result, we try to return out of
			// scope information
			// rather than nothing. It allows to preserve descriptions, short
			// names, etc. from the
			// repository
			// while reverse synchronizing
			if (result instanceof ComparisonResult) {
				attr = (ComparisonAttribute) ((ComparisonResult) result)
						.getOutOfScopeAttribute(attributeName);
			}
			if (attr == null) {
				return "";
			}
		}
		if (scope != null && attr.getScope() != scope && scope != ComparisonScope.ALL
				&& attr.getScope() != ComparisonScope.ALL) {
			if (scope == ComparisonScope.DB_TO_REPOSITORY
					&& attr.getScope() == ComparisonScope.REPOSITORY) {
				attr.getMergeInfo().setMergeProposal(attr.getTarget());
			} else {
				return "";
			}
		}
		StringAttribute attrVal = (StringAttribute) attr.getMergeInfo().getMergeProposal();
		if (attrVal == null) {
			return null;
		}
		return attrVal.getValue();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#merge(com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IComparisonItem)
	 */
	@Override
	public MergeInfo merge(IComparisonItem result, IComparisonItem sourceRootDiff,
			IComparisonItem targetRootDiff) {
		// Notifying listeners
		final IProgressMonitor m = Designer.getProgressMonitor();
		if (m != null) {
			try {
				m.subTask("Merging "
						+ result.getType().getName()
						+ " "
						+ (result.getReference() != null ? result.getReference().getArbitraryName()
								: ""));
			} catch (RuntimeException e) {
				log.debug(e);
			}
		}
		// Delegating to our strategy
		final MergeInfo info = mergeStrategy.merge(this, result, sourceRootDiff, targetRootDiff);
		if (m != null) {
			try {
				m.worked(1);
			} catch (RuntimeException e) {
				log.debug(e);
			}
		}
		return info;
	}

	/**
	 * Specifies if the merger should copy a resolved already-existing items.
	 * The default is <code>false</code> so a resolved existing item will be
	 * used in place during merge. If set to <code>true</code> the merger will
	 * be invoked to create and fill a brand new objects by calling
	 * {@link Merger#createTargetObject(IComparisonItem, IActivity)} and
	 * {@link Merger#fillObject(Object, IComparisonItem, IActivity)} methods.
	 * Mergers implementation should override this method if they want to hard
	 * copy a resolved item instead of using it in place.
	 * 
	 * @return a flag indicating if the merger should copy an unchanged item or
	 *         use it in place
	 */
	protected boolean copyWhenUnchanged() {
		return false;
	}

	/**
	 * Indicates if the current merger should use the comparison information
	 * from its parent or if it should generate new comparison information.<br>
	 * Default implementation will return false and generate comparison
	 * information, mergers should override this method (mainly for
	 * unversionable items)
	 * 
	 * @return <code>true</code> to use parent comparison, else
	 *         <code>false</code>
	 */
	@Override
	public boolean isVersionable() {
		return true;
	}

	/**
	 * Computes the merge status of the comparison sub elements.
	 * 
	 * @param result
	 *            comparison information
	 * @return a <code>MergeStatus</code> computed from child comparison items
	 */
	public static MergeStatus getSubStatus(IComparisonItem result) {
		MergeStatus status = result.getMergeInfo().getMergeProposal() == null ? MergeStatus.NOT_MERGED
				: MergeStatus.MERGE_RESOLVED;

		for (IComparisonItem item : result.getSubItems()) {
			if (item.getMergeInfo().getMergeProposal() == null
					&& item.getMergeInfo().getStatus() != MergeStatus.MERGE_RESOLVED) {
				status = status.compute(getSubStatus(item));
			} else {
				status = status.compute(item.getMergeInfo().getStatus());
			}
		}
		// Handling case of no subitem
		if (status == MergeStatus.NOT_MERGED) {
			status = MergeStatus.MERGE_UNRESOLVED;
		}
		// log.debug("END Status: [" + status.name() + "] on item " +
		// result.toString());
		return status;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#buildMergedObject(com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	public final Object buildMergedObject(IComparisonItem result, IActivity mergeActivity) {
		if (result == null) {
			return null;
		}
		MergeInfo mergeInfo = result.getMergeInfo();
		// If merge is resolved by the current target, the merged
		// object is this target and we have nothing to do.
		// ONLY IF THE MERGER DOES NOT WANT A COPY (copyWhenUnchanged check)
		if (mergeInfo.getStatus() == MergeStatus.MERGE_RESOLVED
				&& (mergeInfo.getMergeProposal() == result.getTarget() || mergeInfo
						.getMergeProposal() == result.getSource()) && !copyWhenUnchanged()) {
			// Checking if sub items are all checked for either source or
			// target, but not BOTH
			// otherwise we need to merge
			boolean targetSelection = (mergeInfo.getMergeProposal() == result.getTarget());
			if (checkChildTargetSelection(result, targetSelection)) {
				return mergeInfo.getMergeProposal();
			}
		}
		// Checking merge status and raise if any unresolved item
		if (mergeInfo.getStatus() != MergeStatus.MERGE_RESOLVED) {
			// || (mergeInfo.getStatus() == MergeStatus.MERGE_RESOLVED
			// && mergeInfo.getMergeProposal()==null
			// && getSubStatus(result)!=MergeStatus.MERGE_RESOLVED)) {
			throw new ErrorException("There are unresolved items, aborting merge process.");
		}
		// We are ready to merge
		Object mergedObject = createTargetObject(result, mergeActivity);
		if (mergedObject instanceof IReferenceable) {
			// Reloading reference since the session may have been cleared
			// VersionReference r =
			// (VersionReference)IdentifiableDAO.getInstance().load(VersionReference.class,
			// result.getReference().getReferenceId());
			((IReferenceable) mergedObject).setReference(result.getReference());
		}
		// Returning our merged object
		return fillObject(mergedObject, result, mergeActivity);

	}

	/**
	 * This method checks is all sub items of the specified comparison items
	 * have a same target selection flag as its parent.<br>
	 * We need this to detect if a parent item is selected but a different child
	 * selection is made. In this case we will need to merge whereas we would
	 * otherwise take the parent proposal in place.
	 * 
	 * @param item
	 * @param targetSelection
	 * @return
	 */
	private boolean checkChildTargetSelection(IComparisonItem item, boolean targetSelection) {
		for (IComparisonItem child : item.getSubItems()) {
			if (targetSelection != (child.getMergeInfo().getMergeProposal() == child.getTarget())) {
				return false;
			}
			checkChildTargetSelection(child, targetSelection);
		}
		return true;
	}

	/**
	 * Fills the object with its data obtained from the comparison result item.
	 * Implementors should COMPLETELY fill the object as the given target object
	 * might be empty.<br>
	 * It is the responsibility of implementors to return <code>null</code> to
	 * indicate that the object has been removed.<br>
	 * Note that if a merger need to save the object which is being filled, they
	 * <b>MUST</b> do so by calling the {@link Merger#save(IdentifiedObject)}
	 * method. Saving a merge objects directly will result in unexpected
	 * behaviours.
	 * 
	 * @param target
	 *            target merged object
	 * @param result
	 *            comparison result containing resolved merge information
	 * @param activity
	 *            activity to use for any versioning operation
	 * @return the merged object, or <code>null</code>.
	 */
	protected abstract Object fillObject(Object target, IComparisonItem result, IActivity activity);

	/**
	 * Dedicated for external calls which needs to fill information from a
	 * comparison item to refill an existing object
	 * 
	 * @param target
	 *            object to fill
	 * @param result
	 *            comparison information with fully resolved merge information (
	 *            no check will be made)
	 */
	public void fill(Object target, IComparisonItem result) {
		fillObject(target, result, Activity.getDefaultActivity());
	}

	/**
	 * Creates the merge target object. The default implementation will handle
	 * <code>IVersionable</code> by checking out the target merge item. Non
	 * versionable mergers should extend this method to create the target merge
	 * object.
	 * 
	 * @param result
	 *            comparison result containing merge information
	 * @param mergeActivity
	 *            activity to use for versioning operations
	 * @return the object in which the merge will be applied
	 */
	protected Object createTargetObject(IComparisonItem result, IActivity mergeActivity) {
		IVersionable<?> mergedObject = null;
		// In any other case we checkout our target version
		IVersionable<?> v = VersionHelper.getVersionable(result.getTarget() != null ? result
				.getTarget() : result.getSource());
		if (v != null) {
			mergedObject = VersionableFactory.createVersionable(result.getType().getInterface());
			// If we are building the object in a repository merge, we setup
			// proper version
			// information
			// Otherwise this will be a temporary object
			if (getMergeStrategy().getComparisonScope() == ComparisonScope.REPOSITORY) {
				IVersionInfo newVersion = VersionFactory.buildNextVersionInfo(v.getVersion(),
						mergeActivity);
				VersionHelper.incrementRelease(newVersion, VersionHelper.PATCH);
				while (!VersionHelper.isVersionAvailable(newVersion)) {
					VersionHelper.incrementRelease(newVersion, VersionHelper.PATCH);
				}
				// IdentifiableDAO.getInstance().save(newVersion);
				IVersionable<?> srcVersion = VersionHelper.getVersionable(result.getSource());
				if (srcVersion != null) {
					// IVersionInfo mergeSrcVersion =
					// (IVersionInfo)IdentifiableDAO.getInstance().load(VersionInfo.class,
					// srcVersion.getUID());
					newVersion.setMergedFromVersion(srcVersion.getVersion());
				}
				mergedObject.setVersion(newVersion);
			}
			// mergedObject = VersionHelper.checkOut(v, mergeActivity);
		} else {
			throw new ErrorException("Unable to create the merged target object.");
		}
		return mergedObject;
	}

	/**
	 * A method which should be used by mergers when they build objects through
	 * {@link Merger#fillObject(Object, IComparisonItem, IActivity)} to
	 * temporarily save their intermediate objects.
	 * 
	 * @param o
	 *            object to save
	 */
	public void save(IdentifiedObject o) {
		// We only save the object within a repository scope
		if (getMergeStrategy().getComparisonScope() == ComparisonScope.REPOSITORY) {
			CorePlugin.getIdentifiableDao().save(o, true,
					HibernateUtil.getInstance().getSandBoxSession(), true);
		} else {
			log.debug("Skipping merge of non-REPOSITORY scope: " + this.getClass().getName());
		}
	}

	protected String strVal(Object o) {
		if (o == null) {
			return "";
		} else {
			return o.toString();
		}
	}

	protected boolean isClass(Class<?> clazz, Object src, Object tgt) {
		return ((src == null || (clazz.isInstance(src))) && (tgt == null || (clazz.isInstance(tgt))));
	}

	protected void fillSpecificComparison(IComparisonItem result, T src, T tgt) {
		// Default empty implementation, to override
	}

	protected Class<? extends IReferenceable> getSpecificClass() {
		return IReferenceable.class;
	}
}
