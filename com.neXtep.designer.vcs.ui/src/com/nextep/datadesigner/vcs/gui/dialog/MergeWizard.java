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
package com.nextep.datadesigner.vcs.gui.dialog;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 *
 */
public class MergeWizard extends Wizard {
	private IVersionable<?> toVersionable;
	private IVersionInfo fromRelease;
	private IReference reference;
	public MergeWizard(WizardPage... pages) {
		super();
		setWindowTitle("Merge wizard...");
		for(WizardPage p : pages) {
			addPage(p);
		}
	}
	/**
	 * @see org.eclipse.jface.wizard.Wizard#createPageControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPageControls(Composite pageContainer) {
		getPages()[0].createControl(pageContainer);
	}
	public void setToVersionable(IVersionable<?> toRelease) {
		this.toVersionable=toRelease;
		setReference(toVersionable.getReference());
	}
	public void setFromRelease(IVersionInfo fromRelease) {
		if(fromRelease != null && !fromRelease.equals(this.fromRelease)) {
			this.fromRelease=fromRelease;
			for(IWizardPage p : getPages()) {
				if(p instanceof IDisplayConnector) {
					//((IDisplayConnector)p).handleEvent(ChangeEvent.SELECTION_CHANGED, null, fromRelease);
					((IDisplayConnector)p).refreshConnector();
				}
			}
		}
	}
	public IVersionInfo getToRelease() {
		return toVersionable.getVersion();
	}
	public IVersionInfo getFromRelease() {
		return fromRelease;
	}
	public IReference getReference() {
		return reference;
	}
	public void setReference(IReference reference) {
		this.reference = reference;
	}
	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}


}
