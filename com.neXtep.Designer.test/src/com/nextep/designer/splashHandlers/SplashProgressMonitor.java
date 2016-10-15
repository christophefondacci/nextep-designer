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
package com.nextep.designer.splashHandlers;



import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.gui.impl.FontFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class SplashProgressMonitor implements IProgressMonitor {

	private ProgressBar bar;
	private Label task;
	private Label subTask;
	private boolean canceled;
	public SplashProgressMonitor(Composite parent) {
		task = new Label(parent,SWT.NONE);
		task.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		task.setBackground(FontFactory.WHITE);
		subTask = new Label(parent,SWT.NONE);
		subTask.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		subTask.setBackground(FontFactory.WHITE);
		bar = new ProgressBar(parent,SWT.NONE);
		bar.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		new Label(parent,SWT.NONE);
	}
	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
	 */
	public void beginTask(final String name, final int totalWork) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				task.setText(name);
				bar.setMaximum(totalWork);
			}
			
		});


	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				bar.setSelection(bar.getMaximum());
			}
			
		});
		
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
//		bar.setSelection(bar.getSelection()+(int)work);

	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean value) {
		this.canceled=value;
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(final String name) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				if(task!=null && !task.isDisposed()) {
					task.setText("neXtep: " + name);
					worked(1);
				}
			}
			
		});
		
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
	 */
	public void subTask(final String name) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				if(subTask!=null && !subTask.isDisposed()) {
					subTask.setText("Eclipse: " + name);
				}
			}
		});
	}

	/**
	 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
	 */
	public void worked(final int work) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				if(bar!=null && !bar.isDisposed())
					bar.setSelection(bar.getSelection()+work);
				
			}
			
		});
		
	}

}
