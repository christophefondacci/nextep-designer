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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionBranch;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.controllers.VersionableController;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class VersionInfoEditor extends ControlledDisplayConnector implements SelectionListener {

	// private IVersionInfo initialRelease;
	// GUI variables
	private Composite editor;
	private CLabel relTypeLabel = null;
	private Button relTypeRevision = null;
	private Button relTypePatch = null;
	private Button relTypeIteration = null;
	private Button relTypeMinor = null;
	private Button relTypeMajor = null;
	private CLabel relNumLabel = null;
	private Spinner relNumMajorText = null;
	private Spinner relNumMinorText = null;
	private Spinner relNumIterationText = null;
	private Spinner relNumPatchText = null;
	private Spinner relNumRevisionText = null;
	private CLabel dot1Label = null;
	private CLabel dot2Label = null;
	private CLabel dot3Label = null;
	private CLabel dot4Label = null;
	private CLabel branchLabel;
	private Combo branchCombo;
	private Button newBranchButton;
	// private CLabel activityLabel = null;
	// private Combo activityCombo = null;
	// private VersionBranchEditor branchEditor = null;
	// Save of initial release
	int maj;
	int min;
	int itr;
	int pat;
	int rev;
	IVersionBranch brch;
	IActivity lastActivity;
	IVersionInfo previous;
	private boolean showRevision = false;

	public VersionInfoEditor(IVersionInfo version) {
		this(version, true);
	}

	public VersionInfoEditor(IVersionInfo version, boolean showRevision) {
		super(version, version.getUID() == null ? null : UIControllerFactory
				.getController(IElementType.getInstance("VERSIONABLE"))); //$NON-NLS-1$

		// this.versionable=version;
		// Saving initial release for cancel
		maj = version.getMajorRelease();
		min = version.getMinorRelease();
		itr = version.getIteration();
		pat = version.getPatch();
		rev = version.getRevision();
		brch = version.getBranch();
		lastActivity = (version.getUID() == null ? VCSPlugin.getService(IVersioningService.class)
				.getCurrentActivity() : version.getActivity());
		previous = version.getPreviousVersion();
		this.showRevision = showRevision;
	}

	@Override
	public void setModel(Object model) {
		super.setModel(model);
		// branchEditor.setModel(model);
		if (getSWTConnector() != null) {
			refreshConnector();
		}
	}

	private void createReleaseType() {
		GridData labelData = new GridData();
		labelData.horizontalAlignment = GridData.FILL;
		labelData.verticalAlignment = GridData.CENTER;
		GridData majorData = new GridData();
		majorData.horizontalAlignment = GridData.END;
		majorData.grabExcessHorizontalSpace = true;
		majorData.verticalAlignment = GridData.CENTER;
		majorData.horizontalSpan = 2;
		GridData minorData = new GridData();
		minorData.horizontalAlignment = GridData.END;
		minorData.grabExcessHorizontalSpace = true;
		minorData.horizontalSpan = 2;
		minorData.verticalAlignment = GridData.CENTER;
		GridData iterData = new GridData();
		iterData.horizontalAlignment = GridData.END;
		iterData.grabExcessHorizontalSpace = true;
		iterData.horizontalSpan = 2;
		iterData.verticalAlignment = GridData.CENTER;
		GridData patchData = new GridData();
		patchData.horizontalAlignment = GridData.END;
		patchData.grabExcessHorizontalSpace = true;
		patchData.horizontalSpan = 2;
		patchData.verticalAlignment = GridData.CENTER;
		GridData revData = new GridData();
		revData.horizontalAlignment = GridData.END;
		revData.grabExcessHorizontalSpace = true;
		revData.horizontalSpan = 2;
		revData.verticalAlignment = GridData.CENTER;

		relTypeLabel = new CLabel(editor, SWT.RIGHT);
		relTypeLabel.setText(VCSUIMessages.getString("editor.versionInfo.releaseType")); //$NON-NLS-1$
		relTypeLabel.setLayoutData(labelData);
		relTypeMajor = new Button(editor, SWT.RADIO | SWT.LEFT);
		relTypeMajor.setText(VCSUIMessages.getString("editor.versionInfo.major")); //$NON-NLS-1$
		relTypeMajor.setLayoutData(majorData);
		relTypeMinor = new Button(editor, SWT.RADIO);
		relTypeMinor.setText(VCSUIMessages.getString("editor.versionInfo.minor")); //$NON-NLS-1$
		relTypeMinor.setLayoutData(minorData);
		relTypeIteration = new Button(editor, SWT.RADIO);
		relTypeIteration.setText(VCSUIMessages.getString("editor.versionInfo.iteration")); //$NON-NLS-1$
		relTypeIteration.setLayoutData(iterData);
		relTypePatch = new Button(editor, SWT.RADIO);
		relTypePatch.setText(VCSUIMessages.getString("editor.versionInfo.patch")); //$NON-NLS-1$
		relTypePatch.setLayoutData(patchData);
		if (showRevision) {
			relTypeRevision = new Button(editor, SWT.RADIO);
			relTypeRevision.setText(VCSUIMessages.getString("editor.versionInfo.revision")); //$NON-NLS-1$
			relTypeRevision.setLayoutData(revData);
		} else {
			new Label(editor, SWT.NONE);
			new Label(editor, SWT.NONE);
		}

		new Label(editor, SWT.NONE);
	}

	private void createReleaseSpinners() {
		GridData dot4Data = new GridData();
		dot4Data.horizontalAlignment = GridData.FILL;
		dot4Data.grabExcessHorizontalSpace = true;
		GridData dot3Data = new GridData();
		dot3Data.horizontalAlignment = GridData.FILL;
		dot3Data.grabExcessHorizontalSpace = true;
		dot3Data.verticalAlignment = GridData.CENTER;
		GridData dot2Data = new GridData();
		dot2Data.grabExcessHorizontalSpace = true;
		dot2Data.verticalAlignment = GridData.CENTER;
		dot2Data.horizontalAlignment = GridData.FILL;
		GridData dot1Data = new GridData();
		dot1Data.grabExcessHorizontalSpace = true;
		dot1Data.verticalAlignment = GridData.CENTER;
		dot1Data.horizontalAlignment = GridData.FILL;
		GridData revData = new GridData();
		revData.horizontalAlignment = GridData.BEGINNING;
		revData.verticalAlignment = GridData.CENTER;
		GridData patchData = new GridData();
		patchData.horizontalAlignment = GridData.BEGINNING;
		patchData.verticalAlignment = GridData.CENTER;
		GridData iterData = new GridData();
		iterData.horizontalAlignment = GridData.BEGINNING;
		iterData.verticalAlignment = GridData.CENTER;
		GridData minorData = new GridData();
		minorData.horizontalAlignment = GridData.BEGINNING;
		minorData.verticalAlignment = GridData.CENTER;
		GridData majorData = new GridData();
		majorData.horizontalAlignment = GridData.BEGINNING;
		majorData.verticalAlignment = GridData.CENTER;

		GridData labelData = new GridData();
		labelData.horizontalAlignment = GridData.FILL;
		labelData.verticalAlignment = GridData.CENTER;
		relNumLabel = new CLabel(editor, SWT.RIGHT);
		relNumLabel.setText(VCSUIMessages.getString("editor.versionInfo.release")); //$NON-NLS-1$
		relNumLabel.setLayoutData(labelData);
		relNumMajorText = new Spinner(editor, SWT.BORDER | SWT.READ_ONLY);
		relNumMajorText.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.BOLD)); //$NON-NLS-1$
		relNumMajorText.setEnabled(true);
		relNumMajorText.setMaximum(99);
		relNumMajorText.setIncrement(0);
		relNumMajorText.setLayoutData(majorData);
		dot1Label = new CLabel(editor, SWT.CENTER);
		dot1Label.setText("."); //$NON-NLS-1$
		dot1Label.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.BOLD)); //$NON-NLS-1$
		dot1Label.setLayoutData(dot1Data);
		relNumMinorText = new Spinner(editor, SWT.BORDER | SWT.READ_ONLY);
		relNumMinorText.setEnabled(true);
		relNumMinorText.setMaximum(99);
		relNumMinorText.setLayoutData(minorData);
		relNumMinorText.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.BOLD)); //$NON-NLS-1$
		dot2Label = new CLabel(editor, SWT.CENTER);
		dot2Label.setText("."); //$NON-NLS-1$
		dot2Label.setLayoutData(dot2Data);
		relNumIterationText = new Spinner(editor, SWT.BORDER | SWT.READ_ONLY);
		relNumIterationText.setEnabled(true);
		relNumIterationText.setLayoutData(iterData);
		relNumIterationText.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.NORMAL)); //$NON-NLS-1$
		dot3Label = new CLabel(editor, SWT.CENTER);
		dot3Label.setText("."); //$NON-NLS-1$
		dot3Label.setLayoutData(dot3Data);
		relNumPatchText = new Spinner(editor, SWT.BORDER | SWT.READ_ONLY);
		relNumPatchText.setEnabled(true);
		relNumPatchText.setLayoutData(patchData);
		if (showRevision) {
			dot4Label = new CLabel(editor, SWT.CENTER);
			dot4Label.setText("_"); //$NON-NLS-1$
			dot4Label.setLayoutData(dot4Data);
			relNumRevisionText = new Spinner(editor, SWT.BORDER | SWT.READ_ONLY);
			relNumRevisionText.setEnabled(true);
			relNumRevisionText.setLayoutData(revData);
		} else {
			new Label(editor, SWT.NONE);
			new Label(editor, SWT.NONE);
		}
		new Label(editor, SWT.NONE);
		new Label(editor, SWT.NONE);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		GridLayout l = new GridLayout();
		l.marginBottom = l.marginHeight = l.marginLeft = l.marginRight = l.marginTop = l.marginWidth = 0;
		l.numColumns = 12;
		editor.setLayout(l);

		GridData actTextData = new GridData();
		actTextData.horizontalSpan = 11;
		actTextData.verticalAlignment = GridData.CENTER;
		actTextData.grabExcessHorizontalSpace = true;
		actTextData.horizontalAlignment = GridData.FILL;
		GridData actLabelData = new GridData();
		actLabelData.horizontalAlignment = GridData.FILL;
		actLabelData.verticalAlignment = GridData.CENTER;

		createReleaseType();
		createReleaseSpinners();

		// Adding branch editor
		GridData newBranchData = new GridData();
		newBranchData.horizontalAlignment = GridData.FILL;
		newBranchData.verticalAlignment = GridData.CENTER;
		GridData branchLabelData = new GridData();
		branchLabelData.horizontalAlignment = GridData.FILL;
		branchLabelData.verticalAlignment = GridData.CENTER;

		branchLabel = new CLabel(editor, SWT.RIGHT);
		branchLabel.setText(VCSUIMessages.getString("editor.versionInfo.branch")); //$NON-NLS-1$
		branchLabel.setLayoutData(branchLabelData);
		createBranchCombo();
		newBranchButton = new Button(editor, SWT.NONE);
		newBranchButton.setText(VCSUIMessages.getString("editor.versionInfo.newBranch")); //$NON-NLS-1$
		newBranchButton.setLayoutData(newBranchData);
		newBranchButton.addSelectionListener(this);
		// new Label(editor,SWT.NONE);

		// activityLabel = new CLabel(editor, SWT.RIGHT);
		// activityLabel.setText("Activity : ");
		// activityLabel.setLayoutData(actLabelData);
		//
		// activityCombo = new Combo(editor, SWT.NONE);
		// activityCombo.setLayoutData(actTextData);
		// int i = 0;
		// for (IActivity a : VersionHelper.getCurrentUser().getUserActivities()) {
		// activityCombo.add(a.getName());
		// activityCombo.setData(a.getName(), a);
		// if (a == lastActivity) {
		// activityCombo.select(i);
		// }
		// i++;
		// }

		// Adding listeners
		relTypePatch.addSelectionListener(this);
		relTypeIteration.addSelectionListener(this);
		relTypeMinor.addSelectionListener(this);
		relTypeMajor.addSelectionListener(this);
		relNumPatchText.addSelectionListener(this);
		relNumIterationText.addSelectionListener(this);
		relNumMinorText.addSelectionListener(this);
		relNumMajorText.addSelectionListener(this);
		// Setting bounds
		relNumMajorText.setMaximum((int) VersionHelper.PRECISION - 1);
		relNumMinorText.setMaximum((int) VersionHelper.PRECISION - 1);
		relNumIterationText.setMaximum((int) VersionHelper.PRECISION - 1);
		relNumPatchText.setMaximum((int) VersionHelper.PRECISION - 1);

		IVersionInfo release = (IVersionInfo) getModel();
		int releaseType = (int) VersionHelper.getReleaseType(release);
		switch (releaseType) {
		case (int) VersionHelper.PATCH:
			relTypePatch.setSelection(true);
			break;
		case (int) VersionHelper.ITERATION:
			relTypeIteration.setSelection(true);
			break;
		case (int) VersionHelper.MINOR:
			relTypeMinor.setSelection(true);
			break;
		case (int) VersionHelper.MAJOR:
			relTypeMajor.setSelection(true);
			break;
		}
		editor.addDisposeListener(this);
		return editor;
	}

	/**
	 * This method initializes branchCombo
	 */
	private void createBranchCombo() {
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.verticalAlignment = GridData.CENTER;
		gridData4.horizontalSpan = 10;
		branchCombo = new Combo(editor, SWT.READ_ONLY);
		branchCombo.setLayoutData(gridData4);
		for (IVersionBranch b : VersionHelper.listBranches()) {
			branchCombo.add(b.getName());
			branchCombo.setData(b.getName(), b);
		}
		branchCombo.addSelectionListener(this);
	}

	@Override
	public void focus(IDisplayConnector childFocus) {
		editor.setFocus();
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public void refreshConnector() {
		IVersionInfo release = (IVersionInfo) getModel();
		boolean enabled = true;
		if (release.getStatus() == IVersionStatus.CHECKED_IN) {
			enabled = false;
		}

		relNumMajorText.setEnabled(enabled);
		relNumMinorText.setEnabled(enabled);
		relNumIterationText.setEnabled(enabled);
		relNumPatchText.setEnabled(enabled);
		relTypeMajor.setEnabled(enabled);
		relTypeMinor.setEnabled(enabled);
		relTypeIteration.setEnabled(enabled);
		relTypePatch.setEnabled(enabled);
		branchCombo.setEnabled(enabled);

		relNumMajorText.setSelection(release.getMajorRelease());
		relNumMinorText.setSelection(release.getMinorRelease());
		relNumIterationText.setSelection(release.getIteration());
		relNumPatchText.setSelection(release.getPatch());
		if (showRevision) {
			relNumRevisionText.setSelection(release.getRevision());
			relNumRevisionText.setEnabled(enabled);
			relTypeRevision.setEnabled(enabled);
		}

		// Refreshing branch
		if (branchCombo.getSelectionIndex() == -1) {
			IVersionBranch releaseBranch = release.getBranch();
			if (releaseBranch != null) {
				String name = releaseBranch.getName();
				if (name != null && !"".equals(name.trim())) { //$NON-NLS-1$
					int branchIndex = branchCombo.indexOf(name.trim());
					if (branchIndex > -1) {
						branchCombo.select(branchIndex);
					}
				}
			}
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();

	}

	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	public void widgetSelected(SelectionEvent e) {
		IVersionInfo release = (IVersionInfo) getModel();
		if (e.getSource() == branchCombo) {
			int index = branchCombo.getSelectionIndex();
			if (index != -1) {
				IVersionBranch selectedBranch = (IVersionBranch) branchCombo.getData(branchCombo
						.getItem(index));
				release.setBranch(selectedBranch);
			}
		} else if (e.getSource() == newBranchButton) {
			// FIXME: Remove the VersionableController cast and manage new branches properly
			IVersionBranch branch = ((VersionableController) UIControllerFactory
					.getController(IElementType.getInstance("VERSIONABLE"))).newBranch(); //$NON-NLS-1$

			String name = branch.getName();
			int selectedBranchIndex = -1;
			if (name != null && !"".equals(name.trim())) { //$NON-NLS-1$
				int branchIndex = branchCombo.indexOf(name.trim());
				if (branchIndex > -1) {
					selectedBranchIndex = branchIndex;
				} else {
					branchCombo.add(name);
					branchCombo.setData(name, branch);
					selectedBranchIndex = branchCombo.getItemCount() - 1;
				}
				branchCombo.select(selectedBranchIndex);
				release.setBranch((IVersionBranch) branchCombo.getData(branchCombo
						.getItem(selectedBranchIndex)));
			}
		} else if (e.getSource() instanceof Spinner) {
			release.setPatch(relNumPatchText.getSelection());
			release.setIteration(relNumIterationText.getSelection());
			release.setMinorRelease(relNumMinorText.getSelection());
			release.setMajorRelease(relNumMajorText.getSelection());
			if (showRevision) {
				release.setRevision(relNumRevisionText.getSelection());
			}
			refreshConnector();
		} else {
			// Computing release type
			long releaseType = -1;
			if (relTypePatch.getSelection()) {
				releaseType = VersionHelper.PATCH;
			} else if (relTypeIteration.getSelection()) {
				releaseType = VersionHelper.ITERATION;
			} else if (relTypeMinor.getSelection()) {
				releaseType = VersionHelper.MINOR;
			} else if (relTypeMajor.getSelection()) {
				releaseType = VersionHelper.MAJOR;
			} else if (relTypeRevision.getSelection()) {
				releaseType = VersionHelper.REVISION;
			}
			IVersionInfo initialRelease = release.getPreviousVersion();
			long relNumber = 0;
			if (initialRelease != null) {
				relNumber = VersionHelper.computeVersion(initialRelease);
			}
			if (releaseType != -1) {
				long incrementedRelease = VersionHelper.incrementRelease(relNumber, releaseType);
				if (incrementedRelease != VersionHelper.computeVersion(release)) {
					release.setRelease(incrementedRelease, true);
					relNumMajorText.setMinimum(release.getMajorRelease());
					relNumMinorText.setMinimum(release.getMinorRelease());
					relNumIterationText.setMinimum(release.getIteration());
					relNumPatchText.setMinimum(release.getPatch());
					if (showRevision) {
						relNumRevisionText.setMinimum(release.getRevision());
					}
					refreshConnector();
				}
			} else {
				release.setRelease(relNumber, true);
			}
		}
	}

	@Override
	public void widgetDisposed(DisposeEvent event) {
		Designer.getListenerService()
				.unregisterListener((IVersionInfo) getModel(), getController());
		super.widgetDisposed(event);
	}

}
