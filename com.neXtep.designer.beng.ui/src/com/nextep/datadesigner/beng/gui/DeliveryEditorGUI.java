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
package com.nextep.datadesigner.beng.gui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.beng.ctrl.DeliveryController;
import com.nextep.datadesigner.beng.gui.service.BengUIService;
import com.nextep.datadesigner.beng.gui.swt.DeliveryListTable;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.editors.CheckBoxEditor;
import com.nextep.datadesigner.gui.impl.swt.TableColumnSorter;
import com.nextep.datadesigner.gui.impl.swt.TableSorter;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.impl.SchedulingRuleVolatile;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.datadesigner.vcs.services.NamingService;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.model.IArtefact;
import com.nextep.designer.beng.model.IDeliveryIncrement;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.services.BENGServices;
import com.nextep.designer.beng.services.IDeliveryService;
import com.nextep.designer.beng.ui.BENGImages;
import com.nextep.designer.beng.ui.BengUIMessages;
import com.nextep.designer.beng.ui.BengUIPlugin;
import com.nextep.designer.beng.ui.jface.ContainerInfoNotInDependenciesContentProvider;
import com.nextep.designer.beng.ui.jface.DeliveryFromToContentProvider;
import com.nextep.designer.beng.ui.jface.DeliveryInfoTableLabelProvider;
import com.nextep.designer.beng.ui.services.IDeliveryUIService;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.testing.model.ICompatibilityTest;
import com.nextep.designer.testing.model.ITestEventHandler;
import com.nextep.designer.testing.model.TestEvent;
import com.nextep.designer.testing.model.TestStatus;
import com.nextep.designer.testing.services.TestingServices;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.jface.ContainerInfoLabelProvider;
import com.nextep.designer.vcs.ui.jface.ContainerInfoTable;

/**
 * The graphical editor for delivery modules.
 * 
 * @author Christophe Fondacci
 */
public class DeliveryEditorGUI extends ControlledDisplayConnector implements SelectionListener,
		ITitleAreaComponent {

	private static final Log log = LogFactory.getLog(DeliveryEditorGUI.class);

	private Composite editor = null; // @jve:decl-index=0:visual-constraint="10,10"

	private Label initialLabel = null;
	// private Combo initialCombo = null;
	private Label initialCombo;
	private Button changeInitialButton;
	private Button reposRadio = null;
	private Button refDBRadio = null;
	// private Button fromDBCheck = null;
	private Combo fromDBCombo = null;
	private Button initCheck = null;
	private Button universalCheck = null;
	private Label targetLabel = null;
	private Label targetCombo = null;

	// Build set
	private Tree buildSetTree;
	private Button computeBuildSetButton;
	private Button generateBuildButton;
	private Button delBuildItemButton;
	private Button showComparisonButton;

	// External pane
	// private Table externalFilesTable;
	// private Button addExternalButton;
	// private Button delExternalButton;
	// private Button upExternalButton;
	// private Button downExternalButton;

	// private Button adminCheck = null;
	// Dependency pane
	private Table dependencyTable = null;
	// private Button newDependencyButton = null;
	// private Button delDependencyButton = null;
	private Button addDependency;
	private Button removeDependency;
	private TableViewer depViewer;
	private Button refreshButton = null;
	private Button filterObsoleteButton = null;

	private Table dlvPrereqTable = null;
	private TableViewer dlvViewer = null;
	// Compatiblity pane
	private Table compatibilityTable = null;
	private Composite compatibilityPane;
	private Label compatHelp;
	private Label compatRelLbl;
	private Label compatCheckLbl;

	public DeliveryEditorGUI(IDeliveryModule module, DeliveryController controller) {
		super(module, controller);
		// Quick fix to make sure name is always synched with what is edited
		NamingService.getInstance().adjustName(module);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createSWTControl(Composite parent) {
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.END;
		gridData11.verticalAlignment = GridData.CENTER;

		GridLayout gridLayout = new GridLayout(4, false);
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(gridLayout);

		// createIncrementalGroup();

		// Version contents table
		// activitiesLabel = new Label(editor,SWT.NONE);
		// activitiesLabel.setText("Activities to deliver : ");
		// activitiesLabel.setLayoutData(new GridData(GridData.FILL,GridData.FILL,false,false,2,1));
		// activityContentLabel = new Label(editor,SWT.NONE);
		// activityContentLabel.setText("Activity contents : ");
		// activityContentLabel.setLayoutData(new
		// GridData(GridData.FILL,GridData.FILL,false,false,2,1));
		//
		// activitiesTable = new Table(editor,SWT.FULL_SELECTION);
		// activitiesTable.setLayoutData(new GridData(GridData.FILL,GridData.FILL,true,true,2,1));
		// activitiesTable.setLinesVisible(true);
		// activitiesTable.setHeaderVisible(true);
		// activitiesTable.addSelectionListener(this);
		// TableColumn c = new TableColumn(activitiesTable,SWT.NONE);
		// c.setText("Activity name");
		// c.setWidth(250);
		//
		// activityContentTree = new Tree(editor, SWT.FULL_SELECTION);
		// activityContentTree.setLayoutData(new
		// GridData(GridData.FILL,GridData.FILL,true,true,2,1));
		// activityContentTree.setLinesVisible(true);
		// activityContentTree.setHeaderVisible(true);

		// GridData buildData2 = new GridData();
		// buildData2.horizontalAlignment = GridData.FILL;
		// buildData2.grabExcessHorizontalSpace = true;
		// buildData2.verticalAlignment = GridData.BEGINNING;
		// buildData2.horizontalSpan=4;
		// buildButton = new Button(editor,SWT.PUSH);
		// buildButton.setText("Build contents...");
		// buildButton.setLayoutData(buildData2);
		// buildButton.addSelectionListener(this);
		//
		// Creating a folder for build sections
		TabFolder tab = new TabFolder(editor, SWT.NONE);
		tab.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 4, 1));

		// Creating the overview folder
		TabItem overviewTabItem = new TabItem(tab, SWT.NONE);
		overviewTabItem.setText(BengUIMessages.getString("deliveryEditor.overview.title")); //$NON-NLS-1$
		Composite overviewPane = new Composite(tab, SWT.NONE);
		overviewPane.setLayout(new GridLayout(4, false));
		overviewTabItem.setControl(overviewPane);

		// Creating the build folder item
		TabItem buildTabItem = new TabItem(tab, SWT.NONE);
		buildTabItem.setText(BengUIMessages.getString("deliveryEditor.buildSet.title")); //$NON-NLS-1$
		Composite buildPane = new Composite(tab, SWT.NONE);
		buildPane.setLayout(new GridLayout(1, false));
		buildTabItem.setControl(buildPane);

		// Creating the dependencies folder item
		TabItem dependenciesTabItem = new TabItem(tab, SWT.NONE);
		dependenciesTabItem.setText(BengUIMessages.getString("deliveryEditor.dependencies.title")); //$NON-NLS-1$
		Composite dependenciesPane = new Composite(tab, SWT.NONE);
		dependenciesPane.setLayout(new GridLayout(4, false));
		dependenciesTabItem.setControl(dependenciesPane);

		// Creating the compatilibity folder item
		TabItem compatibilityTabItem = new TabItem(tab, SWT.NONE);
		compatibilityTabItem
				.setText(BengUIMessages.getString("deliveryEditor.compatibility.title")); //$NON-NLS-1$
		compatibilityPane = new Composite(tab, SWT.NONE);
		compatibilityPane.setLayout(new GridLayout(3, false));
		compatibilityTabItem.setControl(compatibilityPane);

		// // Creating the external folder item
		// TabItem externalTabItem = new TabItem(tab,SWT.NONE);
		// externalTabItem.setText("External");
		// Composite externalPane = new Composite(tab, SWT.NONE);
		// externalPane.setLayout(new GridLayout(1,false));
		// externalTabItem.setControl(externalPane);

		// Creating the properties folder item
		TabItem propertiesTabItem = new TabItem(tab, SWT.NONE);
		propertiesTabItem.setText(BengUIMessages.getString("deliveryEditor.properties.title")); //$NON-NLS-1$
		Composite propertiesPane = new Composite(tab, SWT.NONE);
		propertiesPane.setLayout(new GridLayout(1, false));
		propertiesTabItem.setControl(propertiesPane);

		// Overview pane
		createOverviewPane(overviewPane);

		// Build set section
		// Build set info
		Label buildHelp = new Label(buildPane, SWT.WRAP);
		buildHelp.setText(BengUIMessages.getString("buildHelp")); //$NON-NLS-1$
		GridData buildData = new GridData(GridData.FILL, GridData.FILL, true, false, 4, 1);
		buildData.widthHint = 500;
		buildHelp.setLayoutData(buildData);

		// Building build set toolbox
		Composite buildToolbox = new Composite(buildPane, SWT.NONE);
		GridLayout toolLayout = new GridLayout();
		toolLayout.marginBottom = toolLayout.marginHeight = toolLayout.marginLeft = toolLayout.marginRight = toolLayout.marginTop = toolLayout.marginWidth = 0;
		toolLayout.numColumns = 4;
		buildToolbox.setLayout(toolLayout);
		// Add build tools here
		computeBuildSetButton = new Button(buildToolbox, SWT.NONE); // PUSH);
		computeBuildSetButton.setImage(BENGImages.ICON_COMPUTEBUILD);
		computeBuildSetButton.setToolTipText(BengUIMessages
				.getString("deliveryEditor.buildSet.compute")); //$NON-NLS-1$
		computeBuildSetButton.addSelectionListener(this);
		// addBuildItemButton = new Button(buildToolbox, SWT.PUSH);
		// addBuildItemButton.setImage(BENGImages.ICON_NEW_BUILD_ITEM);
		// addBuildItemButton.setToolTipText(BengUIMessages
		//				.getString("deliveryEditor.buildSet.addItem")); //$NON-NLS-1$
		// addBuildItemButton.addSelectionListener(this);
		delBuildItemButton = new Button(buildToolbox, SWT.PUSH);
		delBuildItemButton.setImage(BENGImages.ICON_DEL_BUILD_ITEM);
		delBuildItemButton.setToolTipText(BengUIMessages
				.getString("deliveryEditor.buildSet.removeItem")); //$NON-NLS-1$
		delBuildItemButton.addSelectionListener(this);
		generateBuildButton = new Button(buildToolbox, SWT.PUSH);
		generateBuildButton.setImage(BENGImages.ICON_BUILD);
		generateBuildButton.setToolTipText(BengUIMessages
				.getString("deliveryEditor.buildSet.generateBuildScripts")); //$NON-NLS-1$
		generateBuildButton.addSelectionListener(this);

		showComparisonButton = new Button(buildToolbox, SWT.PUSH);
		showComparisonButton.setImage(VCSImages.ICON_DIFF);
		showComparisonButton.setToolTipText("Opens this build set in the comparator"); //$NON-NLS-1$
		showComparisonButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IDeliveryModule module = (IDeliveryModule) getModel();
				BengUIPlugin.getService(IDeliveryUIService.class).showArtefactComparison(module);
			}
		});
		// new Label(buildToolbox,SWT.BORDER).setText("TEST");

		//
		buildSetTree = new Tree(buildPane, SWT.FULL_SELECTION | SWT.BORDER);
		GridData bsData = new GridData(GridData.FILL, GridData.FILL, true, true, 4, 1);
		bsData.heightHint = 300;
		buildSetTree.setLayoutData(bsData);
		buildSetTree.setLinesVisible(true);
		buildSetTree.setHeaderVisible(true);

		TreeColumn buildItemCol = new TreeColumn(buildSetTree, SWT.NONE);
		buildItemCol.setWidth(250);
		buildItemCol.setText(BengUIMessages.getString("deliveryEditor.buildSet.itemsToBuild")); //$NON-NLS-1$

		TreeColumn fromRelCol = new TreeColumn(buildSetTree, SWT.NONE);
		fromRelCol.setWidth(100);
		fromRelCol.setText(BengUIMessages.getString("deliveryEditor.buildSet.initialRelease")); //$NON-NLS-1$

		TreeColumn targetRelCol = new TreeColumn(buildSetTree, SWT.NONE);
		targetRelCol.setWidth(100);
		targetRelCol.setText(BengUIMessages.getString("deliveryEditor.buildSet.buildRelease")); //$NON-NLS-1$

		TreeColumn buildTypeCol = new TreeColumn(buildSetTree, SWT.NONE);
		buildTypeCol.setWidth(100);
		buildTypeCol.setText(BengUIMessages.getString("deliveryEditor.buildSet.buildType")); //$NON-NLS-1$

		TreeColumn buildModeCol = new TreeColumn(buildSetTree, SWT.NONE);
		buildModeCol.setWidth(60);
		buildModeCol.setText(BengUIMessages.getString("deliveryEditor.buildSet.mode")); //$NON-NLS-1$

		// Building dependencies toolbox
		Label depHelp = new Label(dependenciesPane, SWT.WRAP);
		depHelp.setText(BengUIMessages.getString("buildDependencyHelp")); //$NON-NLS-1$
		GridData depHelpData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		depHelpData.widthHint = 500;
		depHelp.setLayoutData(depHelpData);
		Composite toolbox = new Composite(dependenciesPane, SWT.NONE);
		toolbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		GridLayout layout = new GridLayout();
		layout.marginBottom = layout.marginHeight = layout.marginLeft = layout.marginRight = layout.marginTop = layout.marginWidth = 0;
		layout.numColumns = 3;
		toolbox.setLayout(layout);
		//
		// newDependencyButton = new Button(toolbox,SWT.PUSH);
		// newDependencyButton.setImage(BENGImages.ICON_NEW_DEPENDENCY);
		// newDependencyButton.setToolTipText("Add a new module dependency");
		// newDependencyButton.addSelectionListener(this);
		// delDependencyButton = new Button(toolbox,SWT.PUSH);
		// delDependencyButton.setImage(BENGImages.ICON_DEL_DEPENDENCY);
		// delDependencyButton.setToolTipText("Remove selected dependency");
		// delDependencyButton.addSelectionListener(this);
		refreshButton = new Button(toolbox, SWT.PUSH);
		refreshButton.setImage(BENGImages.ICON_REFRESH);
		refreshButton.setToolTipText(BengUIMessages
				.getString("deliveryEditor.dependencies.refresh")); //$NON-NLS-1$
		refreshButton.addSelectionListener(this);
		filterObsoleteButton = new Button(toolbox, SWT.CHECK);
		filterObsoleteButton.setText(BengUIMessages.getString("filterObsoleteDependencies")); //$NON-NLS-1$

		Table containerSelection = ContainerInfoTable.create(dependenciesPane);
		GridData contData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		contData.heightHint = 150;
		containerSelection.setLayoutData(contData);
		depViewer = new TableViewer(containerSelection);
		final ContainerInfoNotInDependenciesContentProvider provider = new ContainerInfoNotInDependenciesContentProvider();
		// Default is auto-filtered
		provider.setFilterObsoleteDeps(true);
		filterObsoleteButton.setSelection(true);

		depViewer.setContentProvider(provider);
		depViewer.setLabelProvider(new ContainerInfoLabelProvider());
		depViewer.setInput(getModel());
		depViewer.setComparator(new TableColumnSorter(containerSelection, depViewer));

		filterObsoleteButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				provider.setFilterObsoleteDeps(filterObsoleteButton.getSelection());
				depViewer.setInput(getModel());
			}
		});

		Button dummy = new Button(dependenciesPane, SWT.NONE);
		dummy.setImage(ImageFactory.ICON_DOWN_TINY);
		dummy.setVisible(false);

		addDependency = new Button(dependenciesPane, SWT.PUSH);
		addDependency.setImage(ImageFactory.ICON_DOWN_TINY);
		addDependency.setToolTipText(BengUIMessages
				.getString("deliveryEditor.dependencies.addDependentModule")); //$NON-NLS-1$
		addDependency.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		addDependency.addSelectionListener(this);

		removeDependency = new Button(dependenciesPane, SWT.PUSH);
		removeDependency.setImage(ImageFactory.ICON_UP_TINY);
		removeDependency.setToolTipText(BengUIMessages
				.getString("deliveryEditor.dependencies.removeDependentModule")); //$NON-NLS-1$
		removeDependency.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		removeDependency.addSelectionListener(this);
		new Label(dependenciesPane, SWT.NONE);
		// new Label(dependenciesPane,SWT.NONE);

		SashForm sash = new SashForm(dependenciesPane, SWT.HORIZONTAL);
		addNoMarginLayout(sash, 1);
		GridData sashData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		sashData.heightHint = 150;
		sash.setLayoutData(sashData);
		Composite c = new Composite(sash, SWT.NONE);
		addNoMarginLayout(c, 2);
		Button depUpButton = new Button(c, SWT.PUSH);
		depUpButton.setImage(ImageFactory.ICON_UP_TINY);
		depUpButton.setLayoutData(new GridData(SWT.FILL, SWT.DOWN, false, true));
		dependencyTable = new Table(c, SWT.BORDER | SWT.FULL_SELECTION);
		Button depDownButton = new Button(c, SWT.PUSH);
		depDownButton.setImage(ImageFactory.ICON_DOWN_TINY);
		depDownButton.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, true));

		depUpButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] sel = dependencyTable.getSelection();
				if (sel.length > 0) {
					IVersionInfo dep = (IVersionInfo) sel[0].getData();
					final IDeliveryModule m = (IDeliveryModule) getModel();
					int ind = m.getDependencies().indexOf(dep);
					if (ind > 0) {
						m.getDependencies().remove(ind);
						m.getDependencies().add(ind - 1, dep);
						refreshConnector();
						dependencyTable.select(ind - 1);
					}
				}
			}
		});
		depDownButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] sel = dependencyTable.getSelection();
				if (sel.length > 0) {
					IVersionInfo dep = (IVersionInfo) sel[0].getData();
					final IDeliveryModule m = (IDeliveryModule) getModel();
					int ind = m.getDependencies().indexOf(dep);
					if (ind < m.getDependencies().size() - 1) {
						m.getDependencies().remove(ind);
						m.getDependencies().add(ind + 1, dep);
						refreshConnector();
						dependencyTable.select(ind + 1);
					}
				}
			}
		});
		GridData depData = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 2);
		// depData.heightHint=300;
		// depData.widthHint=300;
		// depData.minimumWidth=300;
		dependencyTable.setLayoutData(depData);
		dependencyTable.setLinesVisible(true);
		dependencyTable.setHeaderVisible(true);
		TableColumn nameCol = new TableColumn(dependencyTable, SWT.NONE);
		nameCol.setText(BengUIMessages.getString("deliveryEditor.dependencies.dependentModule")); //$NON-NLS-1$
		nameCol.setWidth(160);
		TableColumn releaseCol = new TableColumn(dependencyTable, SWT.NONE);
		releaseCol.setText(BengUIMessages
				.getString("deliveryEditor.dependencies.dependentModuleRequired")); //$NON-NLS-1$
		releaseCol.setWidth(60);
		TableColumn lastRelCol = new TableColumn(dependencyTable, SWT.NONE);
		lastRelCol.setText(BengUIMessages
				.getString("deliveryEditor.dependencies.dependentModuleAvailable")); //$NON-NLS-1$
		lastRelCol.setWidth(60);
		TableColumn statusCol = new TableColumn(dependencyTable, SWT.NONE);
		statusCol.setText(BengUIMessages
				.getString("deliveryEditor.dependencies.dependentModuleStatus")); //$NON-NLS-1$
		statusCol.setWidth(50);

		dlvPrereqTable = DeliveryListTable.create(sash);
		dlvPrereqTable.getColumns()[0].setText(BengUIMessages
				.getString("deliveryEditor.dependencies.embeddedDeliveryRequired")); //$NON-NLS-1$
		// GridData preData = new GridData(GridData.FILL,GridData.FILL,true,true,2,1);
		// preData.minimumWidth=300;
		// dlvPrereqTable.setLayoutData(preData);
		dlvViewer = new TableViewer(dlvPrereqTable);
		dlvViewer.setLabelProvider(new DeliveryInfoTableLabelProvider());
		dlvViewer.setContentProvider(new DeliveryFromToContentProvider());
		dlvViewer.setComparator(new TableColumnSorter(dlvPrereqTable, dlvViewer));
		sash.setWeights(new int[] { 1, 2 });

		dependencyTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] depSel = dependencyTable.getSelection();
				if (depSel.length > 0) {
					IVersionInfo dependencyRelease = (IVersionInfo) depSel[0].getData();
					if (dependencyRelease != null) {
						IDeliveryIncrement increment = BengPlugin
								.getService(IDeliveryService.class).computeIncrement(
										(IDeliveryModule) getModel(), dependencyRelease);
						dlvViewer.setInput(increment);
					}
				}
			}
		});

		// Building compatibility pane
		final IDeliveryModule module = (IDeliveryModule) getModel();
		compatHelp = new Label(compatibilityPane, SWT.WRAP);
		compatHelp.setText(BengUIMessages.getString("buildCompatibilityHelp")); //$NON-NLS-1$
		GridData compatHelpData = new GridData(GridData.FILL, GridData.FILL, true, false, 3, 1);
		compatHelpData.widthHint = 500;
		compatHelp.setLayoutData(compatHelpData);
		compatRelLbl = new Label(compatibilityPane, SWT.NONE);
		compatRelLbl.setText(BengUIMessages.getString("deliveryEditor.compatibility.checkWith")); //$NON-NLS-1$
		final Combo relCombo = new Combo(compatibilityPane, SWT.READ_ONLY);
		relCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		int index = 0;
		IVersionInfo rel = module.getTargetRelease().getPreviousVersion();
		while (rel != null) {
			relCombo.add(rel.getLabel());
			relCombo.setData(rel.getLabel(), rel);
			if (rel == module.getFromRelease()) {
				relCombo.select(index);
			}
			index++;
			rel = rel.getPreviousVersion();
		}

		// relCombo.setText(((IDeliveryModule)getModel()).getFromRelease().getLabel());
		Button execButton = new Button(compatibilityPane, SWT.NONE);
		execButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 2));
		execButton.setText(BengUIMessages.getString("deliveryEditor.compatibility.runTest")); //$NON-NLS-1$

		compatCheckLbl = new Label(compatibilityPane, SWT.NONE);
		compatCheckLbl.setText(BengUIMessages
				.getString("deliveryEditor.compatibility.checkAgainst")); //$NON-NLS-1$
		final Combo connCombo = new Combo(compatibilityPane, SWT.READ_ONLY);
		connCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		for (IConnection conn : DBGMHelper.getTargetSet().getConnections()) {
			connCombo.add(conn.getName());
			connCombo.setData(conn.getName(), conn);
			// if(t==TargetType.DEVELOPMENT) {
			// connCombo.setText(DBGMHelper.getTargetSet().getTarget(t).getName());
			// }
		}
		compatibilityTable = new Table(compatibilityPane, SWT.BORDER | SWT.FULL_SELECTION);
		compatibilityTable.setHeaderVisible(true);
		compatibilityTable.setLinesVisible(true);
		GridData compData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		compData.heightHint = 300;
		compatibilityTable.setLayoutData(compData);

		TableColumn typeCol = new TableColumn(compatibilityTable, SWT.NONE);
		typeCol.setWidth(250);
		typeCol.setText(BengUIMessages.getString("deliveryEditor.compatibility.schemaObject")); //$NON-NLS-1$
		TableColumn moduleCol = new TableColumn(compatibilityTable, SWT.NONE);
		moduleCol.setWidth(150);
		moduleCol.setText(BengUIMessages.getString("deliveryEditor.compatibility.module")); //$NON-NLS-1$
		TableColumn objCol = new TableColumn(compatibilityTable, SWT.NONE);
		objCol.setWidth(100);
		objCol.setText(BengUIMessages.getString("deliveryEditor.compatibility.testType")); //$NON-NLS-1$
		TableColumn passedCol = new TableColumn(compatibilityTable, SWT.NONE);
		passedCol.setWidth(70);
		passedCol.setText(BengUIMessages.getString("deliveryEditor.compatibility.status")); //$NON-NLS-1$
		TableColumn reasonCol = new TableColumn(compatibilityTable, SWT.NONE);
		reasonCol.setWidth(70);
		reasonCol.setText(BengUIMessages.getString("deliveryEditor.compatibility.reason")); //$NON-NLS-1$
		TableSorter.handle(compatibilityTable);

		execButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				compatibilityTable.removeAll();
				setCompatibilityColor(FontFactory.VERSIONTREE_CHECKOUT_COLOR);
				final IConnection conn = (IConnection) connCombo.getData(connCombo.getText());
				final IVersionInfo oldRelease = (IVersionInfo) relCombo.getData(relCombo.getText());
				if (conn != null && oldRelease != null) {
					runCompatibilityTest(oldRelease, (IVersionContainer) VersionHelper
							.getReferencedItem(module.getModuleRef()), conn);
					for (IVersionInfo v : module.getDependencies()) {
						final IVersionInfo previousDepRelease = BENGServices
								.getPreviousDependencyRelease(module, v, oldRelease);
						if (previousDepRelease != null) {
							runCompatibilityTest(previousDepRelease,
									(IVersionContainer) VersionHelper.getReferencedItem(v
											.getReference()), conn);
						}
					}
				}
			}
		});

		// Building properties
		initCheck = new Button(propertiesPane, SWT.CHECK);
		initCheck.setText(BengUIMessages.getString("deliveryEditor.properties.firstDelivery")); //$NON-NLS-1$
		CheckBoxEditor.handle(initCheck, ChangeEvent.CUSTOM_2, this);

		universalCheck = new Button(propertiesPane, SWT.CHECK);
		universalCheck.setText(BengUIMessages
				.getString("deliveryEditor.properties.universalDelivery")); //$NON-NLS-1$
		CheckBoxEditor.handle(universalCheck, ChangeEvent.CUSTOM_13, this);
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(editor, "com.neXtep.designer.beng.ui.DeliveryEditor"); //$NON-NLS-1$
		return editor;
	}

	private void setCompatibilityColor(Color c) {
		compatibilityPane.setBackground(c);
		compatHelp.setBackground(c);
		compatRelLbl.setBackground(c);
		compatCheckLbl.setBackground(c);
	}

	private void runCompatibilityTest(IVersionInfo compatRelease, IVersionContainer container,
			IConnection conn) {
		List<ICompatibilityTest> tests = TestingServices.getCompatibilityTests(DBGMHelper
				.getCurrentVendor());
		for (final ICompatibilityTest t : tests) {
			t.setCompatilibityRelease(compatRelease);
			t.setContainer(container);
			t.setConnection(conn);
			t.setEventHandler(new ITestEventHandler() {

				public void handle(ITypedObject source, TestEvent event, TestStatus status,
						Object eventDetails) {
					TableItem i = new TableItem(compatibilityTable, SWT.NONE);
					i.setImage(ImageFactory.getImage(source.getType().getIcon()));
					i.setText(((INamedObject) source).getName());
					if (source instanceof IVersionable<?>) {
						final IVersionable<?> v = ((IVersionable<?>) source);
						final IReference r = v.getReference();
						IVersionContainer container = v.getContainer();
						if (container == null) {
							r.setVolatile(false);
							try {
								IVersionable<?> inViewRef = (IVersionable<?>) VersionHelper
										.getReferencedItem(r);
								r.setVolatile(true);
								if (inViewRef != null) {
									container = inViewRef.getContainer();
								}
							} catch (UnresolvedItemException e) {
								container = null;
							}
						}
						i.setText(1, container == null ? "-" : container.getName()); //$NON-NLS-1$
					}
					i.setText(2, t.getName());
					i.setText(3, status.name());
					if (status != TestStatus.PASSED) {
						if (eventDetails instanceof Exception) {
							i.setText(4, ((Exception) eventDetails).getMessage());
						} else if (eventDetails instanceof String) {
							i.setText(4, (String) eventDetails);
						}
					}
					if (status == TestStatus.PASSED) {
						i.setBackground(FontFactory.VERSIONTREE_CHECKOUT_COLOR);
					} else {
						i.setBackground(FontFactory.LIGHT_RED_ERROR);
						setCompatibilityColor(FontFactory.LIGHT_RED_ERROR);
					}
				};
			});
			Job j = new Job(BengUIMessages.getString("deliveryEditor.compatibility.executing")) { //$NON-NLS-1$

				protected IStatus run(IProgressMonitor monitor) {
					try {
						t.run(monitor);
					} catch (RuntimeException e) {
						log.error("Error", e); //$NON-NLS-1$
					}
					return Status.OK_STATUS;
				};

			};
			j.setRule(SchedulingRuleVolatile.getInstance());
			j.setUser(true);
			j.schedule();
		}
	}

	/**
	 * This method initializes initialReleaseCombo
	 */
	private void createOverviewPane(Composite overviewPane) {

		Label overHelp = new Label(overviewPane, SWT.WRAP);
		overHelp.setText(BengUIMessages.getString("deliveryEditor.overview.help")); //$NON-NLS-1$
		GridData overData = new GridData(GridData.FILL, GridData.FILL, true, false, 4, 1);
		overData.widthHint = 500;
		overHelp.setLayoutData(overData);

		Label filler = new Label(overviewPane, SWT.NONE);
		filler.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 4, 1));

		Label typeLabel = new Label(overviewPane, SWT.NONE);
		typeLabel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 1, 1));
		typeLabel.setText(BengUIMessages.getString("deliveryType")); //$NON-NLS-1$

		reposRadio = new Button(overviewPane, SWT.RADIO);
		reposRadio.addSelectionListener(this);
		reposRadio.setSelection(true);
		reposRadio.setText(BengUIMessages.getString("deliveryTypeRepository")); //$NON-NLS-1$
		new Label(overviewPane, SWT.NONE);
		refDBRadio = new Button(overviewPane, SWT.RADIO);
		refDBRadio.setEnabled(false);
		refDBRadio.addSelectionListener(this);
		refDBRadio.setText(BengUIMessages.getString("deliveryTypeDatabase")); //$NON-NLS-1$
		// new Label(overviewPane,SWT.NONE);

		initialLabel = new Label(overviewPane, SWT.RIGHT);
		initialLabel.setText(BengUIMessages.getString("deliveryEditor.overview.initialRelease")); //$NON-NLS-1$
		initialLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.verticalAlignment = GridData.CENTER;
		initialCombo = new Label(overviewPane, SWT.RIGHT);
		initialCombo.setLayoutData(gridData3);
		initialCombo.setFont(FontFactory.FONT_BOLD);
		changeInitialButton = new Button(overviewPane, SWT.NONE);
		changeInitialButton.setImage(ImageFactory.ICON_EDIT_TINY);
		changeInitialButton.setToolTipText(BengUIMessages
				.getString("deliveryEditor.overview.changeInitial")); //$NON-NLS-1$
		changeInitialButton.addSelectionListener(this);

		createReferenceDBCombo(overviewPane);
		targetLabel = new Label(overviewPane, SWT.RIGHT);
		targetLabel.setText(BengUIMessages.getString("deliveryEditor.overview.targetRelease")); //$NON-NLS-1$
		targetLabel.setLayoutData(new GridData(GridData.END, GridData.FILL, false, false));
		targetCombo = new Label(overviewPane, SWT.RIGHT);
		targetCombo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
		targetCombo.setFont(FontFactory.FONT_BOLD);
		new Label(overviewPane, SWT.NONE);
		new Label(overviewPane, SWT.NONE);

	}

	/**
	 * This method initializes referenceDBCombo
	 */
	private void createReferenceDBCombo(Composite overviewPane) {
		GridData gridData4 = new GridData();
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.verticalAlignment = GridData.CENTER;
		gridData4.horizontalAlignment = GridData.FILL;
		fromDBCombo = new Combo(overviewPane, SWT.READ_ONLY);
		fromDBCombo.setLayoutData(gridData4);
		fromDBCombo.addSelectionListener(this);
		for (IConnection c : DBGMHelper.getTargetSet().getConnections()) {
			fromDBCombo.add(c.getName());
			fromDBCombo.setData(c.getName(), c);
		}
		fromDBCombo.setEnabled(false);

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#focus(com.nextep.datadesigner.gui.model.IDisplayConnector)
	 */
	public void focus(IDisplayConnector childFocus) {
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getConnectorIcon()
	 */
	public Image getConnectorIcon() {
		return BENGImages.ICON_DEPLOY_UNIT;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	public Control getSWTConnector() {
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	public String getTitle() {
		return (MessageFormat
				.format(BengUIMessages.getString("deliveryEditor.tabTitle"), ((INamedObject) getModel()).getName())); //$NON-NLS-1$
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	public void refreshConnector() {
		IDeliveryModule module = (IDeliveryModule) getModel();
		List<IVersionInfo> dependencies = new ArrayList<IVersionInfo>();
		if (module.getDependencies() != null) {
			dependencies.addAll(module.getDependencies());
		}
		dependencyTable.removeAll();
		dlvViewer.setInput(null);
		depViewer.setInput(getModel());
		for (IVersionInfo vc : dependencies) {
			TableItem i = new TableItem(dependencyTable, SWT.NONE);
			// We consider our dependency must be in the current view, so we retrieve the actual
			// name of the underlying container
			final IVersionContainer dependentContainer = (IVersionContainer) VersionHelper
					.getReferencedItem(vc.getReference());
			i.setText(dependentContainer.getName());
			i.setText(1, vc.getLabel());
			i.setImage(BENGImages.ICON_DEPENDENCY);
			i.setData(vc);
			i.setText(2, vc.getLabel());
		}
		// targetCombo.removeAll();
		targetCombo.setText(module.getTargetRelease().getLabel());
		// targetCombo.select(0);
		if (module.getFromRelease() != null) {
			initialCombo.setText(module.getFromRelease().getLabel());
		} else {
			initialCombo.setText(BengUIMessages
					.getString("deliveryEditor.overview.noInitialRelease")); //$NON-NLS-1$
		}
		// else {
		// initialCombo.select(0);
		// }
		initCheck.setSelection(module.isFirstRelease());
		universalCheck.setSelection(module.isUniversal());
		if (module.getReferenceConnection() == null) {
			// reposRadio.setSelection(true);
			// refDBRadio.setSelection(false);
			fromDBCombo.setText(""); //$NON-NLS-1$
		} else {
			fromDBCombo.setText(module.getReferenceConnection().getName());
			// refDBRadio.setSelection(true);
			// reposRadio.setSelection(false);
		}

		// Refreshing Build set tree
		buildSetTree.removeAll();
		List<IArtefact> sortedArtefacts = new ArrayList<IArtefact>(module.getArtefacts());
		Collections.sort(sortedArtefacts, NameComparator.getInstance());
		for (IArtefact a : sortedArtefacts) {
			TreeItem item = new TreeItem(buildSetTree, SWT.NONE);
			item.setText(a.getName());
			if (module.getReferenceConnection() != null) {
				item.setText(1, "[" + module.getReferenceConnection().getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				item.setText(1, a.getInitialRelease() != null ? a.getInitialRelease().getLabel()
						: "-"); //$NON-NLS-1$
			}
			item.setText(2, a.getTargetRelease() != null ? a.getTargetRelease().getLabel() : "-"); //$NON-NLS-1$

			String typeLabel;
			if (a.getInitialRelease() == null) {
				typeLabel = BengUIMessages.getString("deliveryEditor.buildSet.createType"); //$NON-NLS-1$
			} else if (a.getTargetRelease() == null) {
				typeLabel = BengUIMessages.getString("deliveryEditor.buildSet.dropType"); //$NON-NLS-1$
			} else {
				typeLabel = BengUIMessages.getString("deliveryEditor.buildSet.alterType"); //$NON-NLS-1$
			}
			item.setText(3, typeLabel);
			item.setText(4, a.getType().toString());
			item.setData(a);
			item.setImage(ImageFactory.getImage(a.getUnderlyingReference().getType().getIcon()));
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IDeliveryModule module = (IDeliveryModule) getModel();
		switch (event) {
		case CUSTOM_1:
			module.setAdmin((Boolean) data);
			break;
		case CUSTOM_2:
			module.setFirstRelease((Boolean) data);
			break;
		case CUSTOM_13:
			module.setUniversal(universalCheck.getSelection());
			break;
		case ITEM_ADDED:
		case ITEM_REMOVED:
			return;
		}
		refreshConnector();
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		IDeliveryModule module = (IDeliveryModule) getModel();
		/*
		 * if(e.getSource() == fromDBCheck) { fromDBCombo.setEnabled(fromDBCheck.getSelection());
		 * if(!fromDBCheck.getSelection()) { fromDBCombo.setText(""); } } else
		 */
		if (e.getSource() == changeInitialButton) {
			IVersionable<?> v = VersionHelper.getVersionable(VersionHelper.getReferencedItem(module
					.getModuleRef()));
			IVersionInfo initVersion = VCSUIPlugin.getVersioningUIService().pickPreviousVersion(
					editor.getShell(), v,
					BengUIMessages.getString("deliveryEditor.overview.changeVersionDialogTitle")); //$NON-NLS-1$
			module.setFromRelease(initVersion);
		} else if (e.getSource() == refDBRadio) { // && reposRadio.getSelection()) {
			// fromDBCombo.setEnabled(true);
			// IConnection selectedConn = (IConnection)fromDBCombo.getData(fromDBCombo.getText());
			// if(selectedConn == null) {
			// Map<TargetType,IConnection> targetsMap = DBGMHelper.getTargetSet().getTargets();
			// if(targetsMap.get(TargetType.REFERENCE) != null ) {
			// selectedConn = targetsMap.get(TargetType.REFERENCE);
			// } else if( targetsMap.get(TargetType.ASSEMBLY) != null) {
			// selectedConn = targetsMap.get(TargetType.ASSEMBLY);
			// } else if( targetsMap.get(TargetType.DEVELOPMENT) != null) {
			// selectedConn = targetsMap.get(TargetType.DEVELOPMENT);
			// } else {
			// MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
			// BengUIMessages.getString("NoDatabaseTitle"), BengUIMessages.getString("NoDatabase"));
			// refreshConnector();
			// return;
			// }
			// }
			// module.setReferenceConnection(selectedConn);
			// // changeInitialButton.setEnabled(false);
			// // initialCombo.setText("");
		} else if (e.getSource() == fromDBCombo) {
			IConnection selectedConn = (IConnection) fromDBCombo.getData(fromDBCombo.getText());
			module.setReferenceConnection(selectedConn);
		} else if (e.getSource() == reposRadio) { // && refDBRadio.getSelection()) {
			changeInitialButton.setEnabled(true);
			fromDBCombo.setEnabled(false);
			fromDBCombo.setText(""); //$NON-NLS-1$
			module.setReferenceConnection(null);
		} else if (e.getSource() == computeBuildSetButton) {
			BengUIService.buildArtefacts(module);
		} else if (e.getSource() == generateBuildButton) {
			BengUIPlugin.getService(IDeliveryUIService.class).build(module);
			// } else if (e.getSource() == addBuildItemButton) {
			// BengUIService.addUserArtefact(module);
		} else if (e.getSource() == delBuildItemButton) {
			if (buildSetTree.getSelection().length > 0) {
				if (buildSetTree.getSelection()[0].getData() instanceof IArtefact) {
					module.removeArtefact((IArtefact) buildSetTree.getSelection()[0].getData());
				}
			}
			// } else if( e.getSource() == addExternalButton) {
			// File f = getFile(null);
			// if(f!=null && f.exists() && f.isFile()) {
			// // Creating a new external file entry
			// IExternalFile extFile = new ExternalFile(f);
			// module.addExternalFile(extFile);
			// }
			// } else if( e.getSource() == delExternalButton) {
			// if(externalFilesTable.getSelection().length>0) {
			// if(externalFilesTable.getSelection()[0].getData() instanceof IExternalFile) {
			// module.removeExternalFile((IExternalFile)externalFilesTable.getSelection()[0].getData());
			// }
			// }
			// } else if( e.getSource() == upExternalButton ) {
			// TableItem[] sel = externalFilesTable.getSelection();
			// if(sel.length>0) {
			// IExternalFile f = (IExternalFile)sel[0].getData();
			// int pos = f.getDelivery().getExternalFiles().indexOf(f);
			// if(pos>0) {
			// Collections.swap(f.getDelivery().getExternalFiles(), pos, pos-1);
			// }
			// }
			// } else if( e.getSource() == downExternalButton ) {
			// TableItem[] sel = externalFilesTable.getSelection();
			// if(sel.length>0) {
			// IExternalFile f = (IExternalFile)sel[0].getData();
			// int pos = f.getDelivery().getExternalFiles().indexOf(f);
			// if(pos<f.getDelivery().getExternalFiles().size()-1) {
			// Collections.swap(f.getDelivery().getExternalFiles(), pos, pos+1);
			// }
			// }
		} else if (e.getSource() == addDependency) {
			ISelection s = depViewer.getSelection();
			if (!s.isEmpty() && s instanceof IStructuredSelection) {
				final ContainerInfo info = (ContainerInfo) ((IStructuredSelection) s)
						.getFirstElement();
				module.addDependency(info.getRelease());
			}
		} else if (e.getSource() == removeDependency) {
			TableItem[] sel = dependencyTable.getSelection();
			if (sel != null && sel.length > 0) {
				int lastIndex = -1;
				for (TableItem selItem : sel) {
					lastIndex = dependencyTable.indexOf(selItem);
					if (selItem.getData() instanceof IVersionInfo) {
						module.removeDependency((IVersionInfo) selItem.getData());
						selItem.dispose();
					}
				}
				if (dependencyTable.getItemCount() > 0) {
					if (lastIndex >= dependencyTable.getItemCount()) {
						dependencyTable.select(dependencyTable.getItemCount() - 1);
					} else {
						dependencyTable.select(lastIndex);
					}
				}
			}
			// } else if( e.getSource() == newDependencyButton) {
			// DependencySelector selector = new DependencySelector();
			// new GUIWrapper(selector,"Select the dependency to add",400,170).invoke();
			// if(selector.getSelectedRelease()!=null) {
			// module.addDependency(selector.getSelectedRelease());
			// }
			// } else if( e.getSource() == delDependencyButton) {
			// TableItem[] sel = dependencyTable.getSelection();
			// if(sel!=null && sel.length>0) {
			// int lastIndex = -1;
			// for(TableItem selItem : sel) {
			// lastIndex = dependencyTable.indexOf(selItem);
			// if(selItem.getData() instanceof IVersionInfo) {
			// module.removeDependency((IVersionInfo)selItem.getData());
			// selItem.dispose();
			// }
			// }
			// if(dependencyTable.getItemCount()>0) {
			// if(lastIndex >= dependencyTable.getItemCount()) {
			// dependencyTable.select(dependencyTable.getItemCount()-1);
			// } else {
			// dependencyTable.select(lastIndex);
			// }
			// }
			// }
		} else if (e.getSource() == refreshButton) {
			refreshConnector();
		}
		refreshConnector();
	}

	@Override
	public String getDescription() {
		return BengUIMessages.getString("editor.delivery.description"); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return BENGImages.WIZ_NEW_DELIVERY;
	}

	@Override
	public String getAreaTitle() {
		return getTitle();
	};
}
