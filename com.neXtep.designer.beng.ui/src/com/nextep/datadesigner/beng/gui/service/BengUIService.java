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
package com.nextep.datadesigner.beng.gui.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.model.ArtefactMode;
import com.nextep.designer.beng.model.IArtefact;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.services.IDeliveryService;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 * @deprecated need to be refactored and transferred to {@link IDeliveryService}
 */
@Deprecated
public class BengUIService {

	private static final Log log = LogFactory.getLog(BengUIService.class);

	/**
	 * Computes the list of elements which should be generated for this module. This method will
	 * first trigger a comparison on the underlying container versions and will spawn artefacts from
	 * different objects.
	 * 
	 * @param module module to process.
	 */
	@SuppressWarnings("unchecked")
	public static void buildArtefacts(final IDeliveryModule module) {
		final IVersionContainer sourceContainer = (IVersionContainer) VersionHelper
				.getReferencedItem(module.getModuleRef()); // VersionHelper.getVersionable(module).getContainer();
		// Retrieving version information (for display only)
		final IVersionInfo sourceRelease = module.getTargetRelease();
		final IVersionInfo targetRelease = module.getFromRelease(); // VersionHelper.getVersionable(targetContainer).getVersion();
		// Creating comparison command
		ICommand compareCommand = new ICommand() {

			public Object execute(Object... parameters) {
				Collection<IComparisonItem> items = new ArrayList<IComparisonItem>();
				// If no reference connection specified (default), we perform a repository
				// comparison container vs container
				if (module.getReferenceConnection() == null) {
					IMerger merger = MergerFactory.getMerger(IElementType.getInstance("CONTAINER"),
							ComparisonScope.REPOSITORY);
					IComparisonItem result = merger.compare(
							VersionHelper.getVersionable(sourceContainer).getReference(),
							sourceRelease, targetRelease, true);
					// Only 1 result: our container root node
					items.add(result);
				}
				return items;
			}

			public String getName() {
				if (module.getReferenceConnection() == null) {
					return "Computing differences on " + sourceContainer.getName()
							+ (targetRelease != null ? " between " + targetRelease.getLabel() : "")
							+ " to " + sourceRelease.getLabel();
				} else {
					return "Computing differences from database "
							+ module.getReferenceConnection().getName()
							+ " with repository release " + sourceRelease.getLabel();
				}
			}
		};
		// Comparing intial and current container contents
		final Collection<IComparisonItem> result = (Collection<IComparisonItem>) CommandProgress
				.runWithProgress(compareCommand).iterator().next();
		// Filtering unequal items
		List<IComparisonItem> itemsToGenerate = new ArrayList<IComparisonItem>();
		fillGenerationItems(itemsToGenerate, result);

		// Registering items to module
		final List<IArtefact> artefacts = new ArrayList<IArtefact>();
		List<ICommand> artCmds = new ArrayList<ICommand>();
		for (final IComparisonItem i : itemsToGenerate) {
			artCmds.add(new ICommand() {

				@Override
				public Object execute(Object... parameters) {
					final IDeliveryService deliveryService = BengPlugin
							.getService(IDeliveryService.class);
					// TODO: "if" added to avoid attributes being added.
					// Should handle comparison scope correctly instead
					if ((IDatabaseObject.class.isAssignableFrom(i.getType().getInterface()) || IDataSet.class
							.isAssignableFrom(i.getType().getInterface()))) {
						IArtefact a = deliveryService.createArtefact();
						IReference ref = (IReference) CorePlugin.getIdentifiableDao().load(
								Reference.class, i.getReference().getUID());
						a.setUnderlyingReference(ref);
						// Setting initial reference
						if (i.getTarget() != null && i.getTarget() instanceof IVersionable) {
							a.setInitialRelease(((IVersionable<?>) i.getTarget()).getVersion());
						}
						// Setting target release
						if (i.getSource() != null && i.getSource() instanceof IVersionable) {
							a.setTargetRelease(((IVersionable<?>) i.getSource()).getVersion());
						}
						// Checking validity
						if (a.getInitialRelease() == null && a.getTargetRelease() == null) {
							throw new ErrorException(
									"Invalid artefact: non-versionable artefact or initial and target version are null."); //$NON-NLS-1$
						}
						computeName(a);
						a.setType(ArtefactMode.AUTO);
						artefacts.add(a);
					}
					return null;
				}

				@Override
				public String getName() {
					return "Injecting artefacts into delivery module...";
				}
			});

		}
		CommandProgress.runWithProgress(false, artCmds.toArray(new ICommand[artCmds.size()]));
		module.setArtefacts(artefacts);
	}

	/**
	 * Computes the name of this artefact from its underlying reference and initial / target
	 * versions.
	 */
	private static void computeName(IArtefact a) {
		// Setting name
		IReferenceable r;
		try {
			r = VersionHelper.getReferencedItem(a.getUnderlyingReference());
		} catch (ErrorException e) {
			r = null;
		}
		if (r instanceof INamedObject) {
			a.setName(((INamedObject) r).getName());
		} else {
			if (a.getInitialRelease() != null) {
				// If we have an initial release, we try to initialize the name from there
				IVersionable<?> v = (IVersionable<?>) CorePlugin.getIdentifiableDao().load(
						IVersionable.class, a.getInitialRelease().getUID(),
						HibernateUtil.getInstance().getSandBoxSession(), false);
				if (v != null) {
					a.setName(v.getName());
				} else {
					a.setName("Unknown"); //$NON-NLS-1$
				}
			} else {
				// Otherwise we try to get the reference last known name
				final String refName = a.getUnderlyingReference().getArbitraryName();
				if (refName != null) {
					a.setName(refName);
				} else {
					// We might fall here... Setting an "unknown" name to avoid later NPE
					a.setName("Unknown"); //$NON-NLS-1$
				}
			}
		}
	}

	private static void fillGenerationItems(List<IComparisonItem> itemsToGenerate,
			Collection<IComparisonItem> items) {
		for (IComparisonItem item : items) {
			if (item.getDifferenceType() != DifferenceType.EQUALS) {
				if (item.getType() == IElementType.getInstance(IVersionContainer.TYPE_ID)) {
					if (item.getDifferenceType() == DifferenceType.MISSING_SOURCE) {
						for (IVersionable<?> v : ((IVersionContainer) item.getTarget())
								.getContents()) {
							IMerger m = MergerFactory.getMerger(v.getType(),
									ComparisonScope.REPOSITORY);
							if (m != null) {
								IComparisonItem subItem = m.compare(null, v);
								List<IComparisonItem> subItems = new ArrayList<IComparisonItem>();
								subItems.add(subItem);
								fillGenerationItems(itemsToGenerate, subItems);
							}
						}
					} else if (item.getDifferenceType() == DifferenceType.MISSING_TARGET) {
						for (IVersionable<?> v : ((IVersionContainer) item.getSource())
								.getContents()) {
							IMerger m = MergerFactory.getMerger(v.getType(),
									ComparisonScope.REPOSITORY);
							if (m != null) {
								IComparisonItem subItem = m.compare(v, null);
								List<IComparisonItem> subItems = new ArrayList<IComparisonItem>();
								subItems.add(subItem);
								fillGenerationItems(itemsToGenerate, subItems);
							}
						}
					} else {
						fillGenerationItems(itemsToGenerate, item.getSubItems());
					}
				} else {
					itemsToGenerate.add(item);
				}
			}
		}
	}

	/**
	 * Shows the difference information on the specified artefact.
	 * 
	 * @param artefact artefact to process
	 */
	public static void showMergeInfo(final IArtefact artefact) {
		VCSUIPlugin.getComparisonUIManager().compare(artefact.getUnderlyingReference(),
				artefact.getInitialRelease(), artefact.getTargetRelease());
	}

	public static void addUserArtefact(IDeliveryModule module) {
		IVersionable<?> elt = (IVersionable<?>) Designer.getInstance().invokeSelection(
				"find.element", new ViewVersionableContentProvider(),
				VersionHelper.getCurrentView(), "Pick an element to add to build set");
		if (elt != null) {
			IVersionInfo initVersion = VCSUIPlugin.getVersioningUIService().pickPreviousVersion(
					elt, "Select the initial release to start generation from");

			final IDeliveryService deliveryService = BengPlugin.getService(IDeliveryService.class);
			IArtefact a = deliveryService.createArtefact();
			a.setUnderlyingReference(elt.getReference());
			a.setInitialRelease(initVersion);
			a.setTargetRelease(elt.getVersion());
			a.setName(elt.getName());

			module.addArtefact(a);
		}
	}

}
