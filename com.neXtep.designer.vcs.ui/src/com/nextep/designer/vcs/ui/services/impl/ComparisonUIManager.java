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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IComparisonListener;
import com.nextep.designer.vcs.model.IComparisonManager;
import com.nextep.designer.vcs.model.IMergeStrategy;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.compare.IComparisonEditorProvider;
import com.nextep.designer.vcs.ui.services.IComparisonUIManager;

/**
 * The UI comparison manager allows UI based comparisons and UI-synchronized listener calls. It also
 * use asynchronous UI jobs to execute the required tasks and thus may not return the results
 * directly.<br>
 * When using the {@link ComparisonUIManager}, callers should only rely on listeners to get access
 * to resulting information because of the asynchronous processes.<br>
 * Callers willing to have synchronous access to comparison results should access the non-UI
 * comparison manager and implement the UI progress themselves.
 * 
 * @author Christophe Fondacci
 */
public class ComparisonUIManager implements IComparisonUIManager {

	private Map<IComparisonListener, IComparisonListener> listenerUIMap;
	private IComparisonEditorProvider currentProvider;
	private final static String EXTENSION_ID = "com.neXtep.designer.vcs.ui.comparisonEditorProvider"; //$NON-NLS-1$
	private final static Log log = LogFactory.getLog(ComparisonUIManager.class);
	private List<IComparisonEditorProvider> cachedProviders;
	private Map<String, IComparisonEditorProvider> defaultProviderTypeMap;
	private IComparisonManager comparisonManager;

	public ComparisonUIManager() {
		listenerUIMap = new HashMap<IComparisonListener, IComparisonListener>();
		defaultProviderTypeMap = new HashMap<String, IComparisonEditorProvider>();
		initializeComparisonEditorProviders();
	}

	@Override
	public void addComparisonListener(final IComparisonListener listener) {
		// Wrapping into a thread-safe UI listener which will execute all
		// listener call on the UI thread
		final IComparisonListener uiListener = new IComparisonListener() {

			@Override
			public void newComparison(final String description,
					final IComparisonItem... comparisonItems) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						listener.newComparison(description, comparisonItems);
					}
				});
			}
		};
		listenerUIMap.put(listener, uiListener);
		comparisonManager.addComparisonListener(uiListener);
	}

	@Override
	public void compare(final IReference refElementToCompare, final IVersionInfo sourceVersion,
			final IVersionInfo targetVersion) {
		Job j = new Job(VCSUIMessages.getString("comparison.jobTitle")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				String name;
				try {
					IReferenceable r = VersionHelper.getReferencedItem(refElementToCompare);
					if (r instanceof INamedObject) {
						name = ((INamedObject) r).getName();
					} else {
						name = NameHelper.getQualifiedName(r);
					}
				} catch (ErrorException e) {
					name = "Unknown"; //$NON-NLS-1$
				}
				monitor.beginTask(MessageFormat.format(VCSUIMessages
						.getString("comparison.jobTask"), name, sourceVersion == null ? "-" //$NON-NLS-1$ //$NON-NLS-2$
						: sourceVersion.getLabel(),
						targetVersion == null ? "-" : targetVersion.getLabel()), 1); //$NON-NLS-1$
				VCSPlugin.getComparisonManager().compare(refElementToCompare, sourceVersion,
						targetVersion);
				monitor.done();
				return Status.OK_STATUS;
			}

		};
		j.setUser(true);
		j.schedule();
	}

	@Override
	public <V extends IReferenceable> List<IComparisonItem> compare(final Collection<V> sources,
			final Collection<V> targets, final IMergeStrategy strategy, final boolean noSwap) {
		Job j = new Job(VCSUIMessages.getString("comparison.jobTitle")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(VCSUIMessages.getString("comparison.jobTitle"), 1); //$NON-NLS-1$
				VCSPlugin.getComparisonManager().compare(sources, targets, strategy, noSwap);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.schedule();
		return Collections.emptyList();
	}

	@Override
	public void removeComparisonListener(IComparisonListener listener) {
		VCSPlugin.getComparisonManager().removeComparisonListener(listenerUIMap.get(listener));
		listenerUIMap.remove(listener);
	}

	@Override
	public void setComparisonEditorProvider(IComparisonEditorProvider editorProvider) {
		if (editorProvider != null) {
			this.currentProvider = editorProvider;
		}
	}

	@Override
	public IComparisonEditorProvider getComparisonEditorProvider(IElementType type) {
		final IComparisonEditorProvider specificProvider = type == null ? null
				: defaultProviderTypeMap.get(type.getId());
		return specificProvider != null ? specificProvider : currentProvider;
	}

	@Override
	public List<IComparisonEditorProvider> getAvailableComparisonEditorProviders(IElementType type) {
		final IComparisonEditorProvider specificProvider = type == null ? null
				: defaultProviderTypeMap.get(type.getId());
		List<IComparisonEditorProvider> providers = cachedProviders;
		if (specificProvider != null) {
			providers = new ArrayList<IComparisonEditorProvider>(cachedProviders);
			providers.add(specificProvider);
		}

		return providers;
	}

	private void initializeComparisonEditorProviders() {
		// Retrieving contributions for comparison editor providers
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(EXTENSION_ID,
				"class", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		cachedProviders = new ArrayList<IComparisonEditorProvider>();
		for (IConfigurationElement elt : elts) {
			try {
				IComparisonEditorProvider provider = (IComparisonEditorProvider) elt
						.createExecutableExtension("class"); //$NON-NLS-1$
				final Boolean isDefault = Boolean.valueOf(elt.getAttribute("default")); //$NON-NLS-1$
				if (isDefault != null && isDefault.booleanValue()) {
					currentProvider = provider;
				}
				final String defaultTypeId = elt.getAttribute("typeIdRestriction"); //$NON-NLS-1$
				if (defaultTypeId != null && !"".equals(defaultTypeId.trim())) { //$NON-NLS-1$
					defaultProviderTypeMap.put(defaultTypeId.trim(), provider);
				} else {
					cachedProviders.add(provider);
				}
			} catch (CoreException e) {
				log.error("Contribution error: Unable to instantiate a comparison editor provider from " //$NON-NLS-1$
						+ elt.getContributor().getName());
			}
		}
		// Sorting providers by label
		Collections.sort(cachedProviders, new Comparator<IComparisonEditorProvider>() {

			public int compare(IComparisonEditorProvider o1, IComparisonEditorProvider o2) {
				if (o1 != null && o2 != null) {
					final String l1 = o1.getLabel();
					final String l2 = o2.getLabel();
					return (l1 == null ? "" : l1).compareTo(l2 == null ? "" : l2); //$NON-NLS-1$ //$NON-NLS-2$
				} else if (o2 == null) {
					return o1 == null ? 0 : 1;
				} else {
					return -1;
				}
			};
		});
	}

	@Override
	public void openComparisonEditor(IComparisonItem compItem, IComparisonEditorProvider provider) {
		try {
			final IEditorInput input = provider.getEditorInput(compItem);
			final String editorId = provider.getEditorId(compItem);
			if (input != null && editorId != null) {
				// Opening our multi editor
				if (PlatformUI.getWorkbench() != null
						&& PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
						&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.openEditor(input, editorId);
				}
			}
		} catch (PartInitException e) {
			log.error(VCSUIMessages.getString("comparison.openComparisonError") + ": " //$NON-NLS-1$ //$NON-NLS-2$
					+ e.getMessage(), e);
		}
	}

	@Override
	public void showComparison(String description, IComparisonItem... items) {
		notifyNewComparison(description, items);
	}

	@Override
	public void notifyNewComparison(String description, IComparisonItem... comparisonItems) {
		getComparisonManager().notifyNewComparison(description, comparisonItems);
	}

	private IComparisonManager getComparisonManager() {
		return comparisonManager;
	}

	public void setComparisonManager(IComparisonManager comparisonManager) {
		this.comparisonManager = comparisonManager;
	}
}
