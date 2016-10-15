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
package com.nextep.designer.vcs.ui.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.services.IVersioningUIService;
import com.nextep.designer.vcs.ui.services.VersionUIHelper;

/**
 * A base class for version-oriented handlers which need to track state of selected versionable for
 * their enablement.<br>
 * This handler tracks opening of the workbench to attach a global selection listener. Whenever the
 * selection changes, it will ask the handler to refresh its enablement state. The abstract methods
 * allow extension to control enablement given a {@link IVersionable} selection list and to process
 * its action.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractVersionHandler extends AbstractHandler implements IWindowListener,
		IPageListener, ISelectionListener, IEventListener {

	private static final Log log = LogFactory.getLog(AbstractVersionHandler.class);
	protected ExecutionEvent event;
	private IVersioningUIService versioningService = null;

	public AbstractVersionHandler() {
		try {
			PlatformUI.getWorkbench().addWindowListener(this);
		} catch (Exception e) {
			log.error(e);
		}
	}

	/**
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public final Object execute(ExecutionEvent event) throws ExecutionException {
		this.event = event;
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		versionActions(VersionUIHelper.getSelectedVersionable(window));
		return null;
	}

	/**
	 * This method performs actions on all selected items. This base implementation may be ok for
	 * most cases although extensions may override to perform the full "batch" action.
	 * 
	 * @param selectedVersions all selected versionables
	 */
	protected void versionActions(List<IVersionable<?>> selectedVersions) {
		if (!checkAndConfirm(selectedVersions)) {
			return;
		}
		final List<IVersionable<?>> newSelection = new ArrayList<IVersionable<?>>();
		for (IVersionable<?> v : selectedVersions) {
			if (v != null) {
				newSelection.add((IVersionable<?>) versionAction(v));
				// return o;
			} else {
				log.debug("Non-IVersionable object selected or empty selection"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Command action on the versionable. The given versionable will never be null. This method will
	 * not be invoked if no IVersionable object is currently selected.
	 * 
	 * @param v a non-null IVersionable object
	 * @return the optional Object which the execute method will return
	 */
	protected abstract Object versionAction(IVersionable<?> v);

	/**
	 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		// log.debug(this.getClass().getSimpleName() + ": isEnabled() called.");
		final ISelectionProvider provider = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActivePart().getSite().getSelectionProvider();
		final ISelection s = provider.getSelection();
		if (s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			if (sel.isEmpty()) {
				return false;
			} else {
				for (Object o : sel.toList()) {
					if (o instanceof IVersionable<?>) {
						// Our selection might not point to the current workspace object if the
						// UI is not yet synched with latest model object so we refetch it from
						// reference
						final IVersionable<?> selectedVersion = (IVersionable<?>) o;
						try {
							final IVersionable<?> workspaceVersion = (IVersionable<?>) VersionHelper
									.getReferencedItem(selectedVersion.getReference());
							if (log.isDebugEnabled()) {
								log.debug("HANDLER ENABLEMENT (" + this.getClass().getName() //$NON-NLS-1$
										+ ") : Checking state of " + workspaceVersion + " version " //$NON-NLS-1$//$NON-NLS-2$
										+ workspaceVersion.getVersion());
							}
							if (!isEnabled(workspaceVersion)) {
								return false;
							}
						} catch (ErrorException e) {
							// this will happen when we switch workspace and the current selection
							// cannot be found, we disable everything in this case
							return false;
						}
					} else {
						return false;
					}
				}
				return true;
			}
		}
		return false;

	}

	/**
	 * Controls the enablement of the command. The provided IVersionable object will never be null.
	 * By default the command will be disabled without calling this method if there is no current
	 * selection or if no IVersionable object is currently being selected.
	 * 
	 * @param v a non null IVersionable object
	 * @return the enablement of the command
	 */
	protected abstract boolean isEnabled(IVersionable<?> v);

	/**
	 * This method is called just before performing the action. It allows implementors to check if
	 * the action can be performed (optionally prompting the user to confirm) and indicates if we
	 * should perform the action.<br>
	 * This default implementation checks nothing.
	 * 
	 * @param selectedVersions list of elements to process
	 * @return <code>true</code> to continue with the action, <code>false</code> to cancel
	 */
	protected boolean checkAndConfirm(List<IVersionable<?>> selectedVersions) {
		return true;
	}

	@Override
	public final void windowActivated(IWorkbenchWindow window) {
	}

	@Override
	public final void windowClosed(IWorkbenchWindow window) {

	}

	@Override
	public final void windowDeactivated(IWorkbenchWindow window) {
	}

	@Override
	public final void windowOpened(IWorkbenchWindow window) {
		window.addPageListener(this);
		window.getWorkbench().removeWindowListener(this);
		window.getActivePage().addPostSelectionListener(this);
	}

	@Override
	public final void pageActivated(IWorkbenchPage page) {
	}

	@Override
	public final void pageClosed(IWorkbenchPage page) {
		page.removePostSelectionListener(this);
	}

	@Override
	public final void pageOpened(IWorkbenchPage page) {
		page.addSelectionListener(this);
	}

	@Override
	public final void selectionChanged(IWorkbenchPart part, ISelection sel) {
		Designer.getListenerService().unregisterListeners(this);
		fireHandlerChanged(new HandlerEvent(this, true, false));
		// Listening to selected objects
		if (sel instanceof IStructuredSelection) {
			final Iterator<?> selIt = ((IStructuredSelection) sel).iterator();
			while (selIt.hasNext()) {
				final Object selObj = selIt.next();
				if (selObj instanceof IObservable) {
					Designer.getListenerService()
							.registerListener(this, (IObservable) selObj, this);
				}
			}
		}
	}

	protected IVersioningUIService getVersioningService() {
		if (versioningService == null) {
			versioningService = CorePlugin.getService(IVersioningUIService.class);
		}
		return versioningService;
	}

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
	}

	@Override
	public final void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (log.isDebugEnabled()) {
			log.debug("Firing handler change on " + this.getClass().getName() //$NON-NLS-1$
					+ ", from selected object " + source); //$NON-NLS-1$
			if (source instanceof IVersionable<?>) {
				log.debug("  -> " + ((IVersionable<?>) source).getVersion()); //$NON-NLS-1$
			}
		}
		fireHandlerChanged(new HandlerEvent(this, true, false));
	}
}
