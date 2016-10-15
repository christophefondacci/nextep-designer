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
package com.nextep.datadesigner.vcs.gui;



import java.util.ArrayList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.gui.impl.swt.TableColumnSorter;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.vcs.command.AddRuleModuleCommand;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.jface.ContainerInfoLabelProvider;
import com.nextep.designer.vcs.ui.jface.ContainerInfoNotInViewContentProvider;
import com.nextep.designer.vcs.ui.jface.ContainerInfoTable;
import com.nextep.designer.vcs.ui.jface.ContainerInfoViewContentProvider;
import com.nextep.designer.vcs.ui.jface.NameFilter;

public class VersionViewRulesNewEditor extends WizardDisplayConnector implements SelectionListener {

	private SashForm sash;
	private Composite leftComposite; 
	private Text nameFilter;
	private IWorkspace view;
	private Button addButton;
	private Button removeButton;
	private TableViewer containersViewer;
	private TableViewer viewContentsViewer;
	private ContainerInfoNotInViewContentProvider provider;
	private Button filterCheckoutButton;
	public VersionViewRulesNewEditor(IWorkspace view) {
		super("View rules definition wizard","View rules definition wizard...",null);
		setMessage(VCSUIMessages.getString("viewRulesWizardMessage"));
		this.view = view;
		Designer.getListenerService().registerListener(this, view, this);
	}
	@Override
	public Control createSWTControl(Composite parent) {
		sash = new SashForm(parent,SWT.HORIZONTAL);
		GridData sashData = new GridData();
		sashData.widthHint = 400;
		sashData.heightHint = 250;
		sash.setLayoutData(sashData);
		
		leftComposite = new Composite(sash,SWT.NONE);
		GridLayout grid = new GridLayout(2,false);
		grid.marginBottom=grid.marginHeight=grid.marginLeft=grid.marginRight=grid.marginTop=0;
		leftComposite.setLayout(grid);
		GridData leftData = new GridData();
		leftData.widthHint = 250;
		leftData.heightHint = 200;
		leftComposite.setLayoutData(leftData);
		
		Label filterLabel = new Label(leftComposite,SWT.NONE);
		filterLabel.setText(VCSUIMessages.getString("viewRulesWizardFilter"));
		nameFilter = new Text(leftComposite,SWT.BORDER);
		nameFilter.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		filterCheckoutButton = new Button(leftComposite,SWT.CHECK);
		filterCheckoutButton.setText("Show uncommitted modules");
		Table containers = ContainerInfoTable.create(leftComposite);
		GridData gd = new GridData(SWT.FILL,SWT.FILL,true,true,2,1);
		gd.heightHint = 200;
		gd.widthHint = 250;
		containers.setLayoutData(gd);
		containersViewer = new TableViewer(containers);
		provider = new ContainerInfoNotInViewContentProvider();
		filterCheckoutButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				provider.setFilterCheckOut(!filterCheckoutButton.getSelection());
				containersViewer.refresh();
			}
		});
		containersViewer.setContentProvider(provider);
		containersViewer.setLabelProvider(new ContainerInfoLabelProvider());
		containersViewer.addFilter(new NameFilter(nameFilter));
		containersViewer.setComparator(new TableColumnSorter(containers, containersViewer));
		containers.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				addSelectionToView();
			}
			@Override
			public void mouseDown(MouseEvent e) {}
			@Override
			public void mouseUp(MouseEvent e) {}
			
		});
		addButton = new Button(leftComposite,SWT.NONE);
		addButton.setText(VCSUIMessages.getString("viewRulesWizardAdd"));
		addButton.addSelectionListener(this);
		
		nameFilter.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				containersViewer.setFilters(new NameFilter[] { new NameFilter(nameFilter) });
			}
		});
		
		Composite rightComposite = new Composite(sash,SWT.NONE);
		GridLayout grid2 = new GridLayout(1,false);
		grid2.marginBottom=grid2.marginHeight=grid2.marginLeft=grid2.marginRight=grid2.marginTop=0;
		rightComposite.setLayout(grid2);
		rightComposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		GridData contData = new GridData();
		contData.widthHint = 150;
		contData.heightHint = 200;
		rightComposite.setLayoutData(contData);
		
		Label viewContentsLbl = new Label(rightComposite,SWT.NONE); 
		viewContentsLbl.setText(VCSUIMessages.getString("viewRulesWizardViewContents"));
		Table viewContents = ContainerInfoTable.create(rightComposite);
		viewContents.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		
		viewContentsViewer = new TableViewer(viewContents);
		viewContentsViewer.setContentProvider(new ContainerInfoViewContentProvider());
		viewContentsViewer.setLabelProvider(new ContainerInfoLabelProvider());
		
		removeButton = new Button(rightComposite,SWT.PUSH);
		removeButton.setText(VCSUIMessages.getString("viewRulesWizardRemove"));
		removeButton.addSelectionListener(this);
//		sash.setWeights(new int[] {2,1});
		return sash;
	}

	@Override
	public Control getSWTConnector() {
		return sash;
	}

	@Override
	public Object getModel() {
		return view;
	}
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);		
	}
	@Override
	public void widgetSelected(SelectionEvent e) {
		if(e.getSource()==addButton) {
			addSelectionToView();
		} else if(e.getSource()==removeButton) {
			final ISelection s = viewContentsViewer.getSelection();
			if(s!=null && !s.isEmpty()) {
				ContainerInfo info = (ContainerInfo)((IStructuredSelection)s).getFirstElement();
				if(info.getRelease().getStatus()!=IVersionStatus.CHECKED_IN) {
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
							VCSUIMessages.getString("removeCheckedOutContainerNotAllowedTitle"), 
							VCSUIMessages.getString("removeCheckedOutContainerNotAllowed"));
					return;
				}
				CommandProgress.runWithProgress(false,new ICommand() {
					@Override
					public Object execute(Object... parameters) {
						ContainerInfo info = (ContainerInfo)((IStructuredSelection)s).getFirstElement();
						for(IVersionable<?> v: new ArrayList<IVersionable<?>>(view.getContents())) {
							if(v.getVersion().getUID().rawId() == info.getRelease().getUID().rawId()) {
								VersionHelper.removeVersionable(v);
//								view.removeVersionable(v);
								break;
							}
						}
						containersViewer.setInput(view);
						viewContentsViewer.setInput(view);
						return null;
					}
					@Override
					public String getName() {
						return VCSUIMessages.getString("viewRulesWizardRemoveContainer");
					}
				});
			}
		}
		
	}

	@Override
	public void refreshConnector() {
		containersViewer.setInput(view);
		viewContentsViewer.setInput(view);
	}
	/**
	 * Adds the currently selected items from the module pane 
	 * to the current view contents.
	 */
	private void addSelectionToView() {
		final ISelection s = containersViewer.getSelection();
		if(s!=null && !s.isEmpty()) {
			final ICommand addCmd = new AddRuleModuleCommand((ContainerInfo)((IStructuredSelection)s).getFirstElement(),view);
			CommandProgress.runWithProgress(false,addCmd);
			containersViewer.setInput(view);
			viewContentsViewer.setInput(view);
		}
	}
	
	@Override
	public void releaseConnector() {
		Designer.getListenerService().unregisterListener(view, this);
		super.releaseConnector();
	}
}
