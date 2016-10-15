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
package com.nextep.designer.vcs.ui.controllers;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.InvokableController;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.ui.jface.NameFilter;
import com.nextep.designer.vcs.ui.jface.TypedViewerComparator;
import com.nextep.designer.vcs.ui.jface.VersionableLabelProvider;

/**
 * @author Christophe Fondacci
 *
 */
public class FindElementController extends InvokableController {

	private Object element;
	
	/**
	 * @see com.nextep.datadesigner.model.IInvokable#invoke(java.lang.Object[])
	 */
	@Override
	public Object invoke(Object... arg) {
		IContentProvider provider = (IContentProvider)arg[0];
		Object input = null;
		String title = "Open element...";
		if(arg.length>1) {
			input = arg[1];
		}
		if(arg.length>2) {
			title = (String)arg[2];
		}
		
		
		Shell s = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		final Shell dialog = new Shell(s,SWT.TITLE | SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
		dialog.setText(title);
		dialog.setSize(600, 400);
		Rectangle r = s.getBounds();
		dialog.setLocation(r.x+(r.width/2-300), r.y+(r.height/2-200));
		dialog.setLayout(new GridLayout(2,false));
		dialog.setImage(ImageFactory.ICON_DESIGNER_TINY);
		
		Composite group = new Composite(dialog,SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true,2,1));
		
		Label filterLabel = new Label(group, SWT.NONE);
		filterLabel.setText("Enter element name filter:");
		filterLabel.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,false));
		
		final Text filterText = new Text(group,SWT.BORDER);
		filterText.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,false));
		
		Label matchLabel = new Label(group, SWT.NONE);
		matchLabel.setText("Matching items:");
		matchLabel.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,false));
		
		final TableViewer viewer = new TableViewer(group);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true));
		viewer.setContentProvider(provider);
		viewer.setLabelProvider(new VersionableLabelProvider());
		viewer.addFilter(new NameFilter(filterText));
		viewer.setSorter(new TypedViewerComparator());
		if(input != null) {
			viewer.setInput(VersionHelper.getCurrentView());
		}

		Button okButton = new Button(dialog,SWT.NONE);
		okButton.setText("     Ok     ");
		okButton.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,true,false));
		okButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(viewer.getSelection() instanceof IStructuredSelection) {
					Object elt = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
					if(elt instanceof ITypedObject) {
						element = elt;
						// Closing dialog
						dialog.dispose();
					}
				}				
			}
		});
		viewer.getControl().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if(viewer.getSelection() instanceof IStructuredSelection) {
					Object elt = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
					if(elt instanceof ITypedObject) {
						element = elt;
						// Closing dialog
						dialog.dispose();
					}
				}	
			}
		});
		dialog.setDefaultButton(okButton);
		Button cancelButton = new Button(dialog,SWT.NONE);
		cancelButton.setText("   Cancel   ");
		cancelButton.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));
		cancelButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				element = null;
				widgetSelected(e);
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				dialog.dispose();
			}
		});
		filterText.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_DOWN) {
					int index = viewer.getTable().getSelectionIndex();
					if (index != -1 && viewer.getTable().getItemCount() > index + 1) {
						viewer.getTable().setSelection(index + 1);
					}
					e.doit=false;
//					viewer.getTable().setFocus();
				} else if (e.keyCode == SWT.ARROW_UP) {
					int index = viewer.getTable().getSelectionIndex();
					if (index != -1 && index >= 1) {
						viewer.getTable().setSelection(index - 1);
//						viewer.getTable().setFocus();
					}
					e.doit=false;
				}
				 //removeFilter(currentFilter);
//				viewer.addFilter(currentFilter = new NameFilter(filterText));
				
			}

			@Override
			public void keyReleased(KeyEvent e) {
				viewer.setFilters(new NameFilter[] { new NameFilter(filterText) });
				if(viewer.getSelection().isEmpty()) {
					if(viewer.getTable().getItemCount()>0) {
						viewer.getTable().select(0);
					}
				}
			}
			
		});
		if(viewer.getTable().getItemCount()>0) {
			viewer.getTable().select(0);
		}
		dialog.open();

		
		
        while (!dialog.isDisposed()) {
        	try {
        		if (!dialog.getDisplay().readAndDispatch()) dialog.getDisplay().sleep();
        	} catch(ErrorException e ) {
        		// An error might happen
        		dialog.dispose();
        	}
        }
		return element;
	}

}
