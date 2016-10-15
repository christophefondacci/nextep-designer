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
package com.nextep.designer.ui.views;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import com.nextep.datadesigner.gui.impl.navigators.PropertyNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.IProperty;
import com.nextep.datadesigner.model.IPropertyProvider;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.ui.UIMessages;

/**
 * @author Christophe Fondacci
 */
public class PropertiesView extends ViewPart implements ISelectionListener, DisposeListener {

	private Tree propertiesTree;
	private TreeColumn nameCol;
	private TreeColumn valueSourceCol;
	private TreeColumn valueTargetCol;
	private IAdaptable selectedModel;

	private static final Log log = LogFactory.getLog(PropertiesView.class);

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		propertiesTree = new Tree(parent, SWT.FULL_SELECTION);
		propertiesTree.setLinesVisible(true);
		propertiesTree.addDisposeListener(this);
		nameCol = new TreeColumn(propertiesTree, SWT.NONE);
		nameCol.setWidth(200);
		nameCol.setText(UIMessages.getString("properties.view.propertyCol")); //$NON-NLS-1$
		valueSourceCol = new TreeColumn(propertiesTree, SWT.NONE);
		valueSourceCol.setWidth(200);
		valueSourceCol.setText(UIMessages.getString("properties.view.valueCol")); //$NON-NLS-1$
		propertiesTree.setHeaderVisible(true);
		valueTargetCol = new TreeColumn(propertiesTree, SWT.NONE);
		valueTargetCol.setWidth(200);

		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
		refreshSelection(this.getSite().getWorkbenchWindow().getSelectionService().getSelection());
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		propertiesTree.setFocus();
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		refreshSelection(selection);
	}

	private void refreshSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (sel.getFirstElement() instanceof IAdaptable) {
				if (sel.getFirstElement() != selectedModel) {
					selectedModel = (IAdaptable) sel.getFirstElement();
					if (selectedModel instanceof IReferenceable) {
						// To avoid problems because volatiles references may be flushed, we
						// do not refresh properties for those objects
						if (((IReferenceable) selectedModel).getReference().isVolatile()) {
							setProvider(null);
							return;
						}
					}
					IPropertyProvider provider = (IPropertyProvider) selectedModel
							.getAdapter(IPropertyProvider.class);
					setProvider(provider);
					return;
				}
			} else {
				selectedModel = null;
				setProvider(null);
			}
		} else {
			setProvider(null);
		}
	}

	private void setProvider(final IPropertyProvider provider) {

		final Tree propTree = propertiesTree;
		// We refresh in the UI thread
		Job propertyDisplayer = new Job("Fetching element properties...") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				if (propTree == null || propTree.isDisposed()) {
					return Status.OK_STATUS;
				}
				// Clearing tree
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						if (propTree != null && !propTree.isDisposed())
							propTree.removeAll();
					}
				});
				if (provider != null && !propTree.isDisposed()) {
					try {
						final List<IProperty> props = provider.getProperties();
						monitor.beginTask("Refreshing properties view", props.size());
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

							@Override
							public void run() {
								for (IProperty p : props) {
									final INavigatorConnector c = new PropertyNavigator(p, propTree);

									c.create(null, -1);
									c.initialize();
									c.refreshConnector();
								}
								monitor.worked(1);
							}
						});
					} catch (RuntimeException e) {
						log.debug("Exception when fetching object properties : " + e.getMessage(),
								e);
					}
				}
				monitor.done();
				// OK
				return Status.OK_STATUS;
			}
		};
		propertyDisplayer.schedule();
	}

	/**
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	@Override
	public void widgetDisposed(DisposeEvent e) {
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);

	}
}
