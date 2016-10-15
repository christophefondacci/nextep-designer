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
package com.nextep.datadesigner.gui.impl;


///**
// * @deprecated if used, need to be refactored.
// *
// * @author Christophe Fondacci
// *
// */
//public class SashDisplayConnector implements IDisplayConnector {
//
//	private SashForm sashForm = null;
//	private int sashCount = 0;
//	private static final int MAX_CHILDS=2;
//	private String title = null;
//	private Image icon = null;
//	private Collection<IDisplayConnector> children = null;
//	private int sashOrientation;
//	private List<Integer> weightList = null;
//	public SashDisplayConnector(String title, Image icon,int swtOrientation) {
//		sashCount=0;
//		this.title=title;
//		this.icon=icon;
//		this.sashOrientation=swtOrientation;
//		children = new ArrayList<IDisplayConnector>();
//		weightList = new ArrayList<Integer>();
//	}
//	@Override
//	public void addConnector(IDisplayConnector child) {
//		if(sashCount<MAX_CHILDS) {
//			// It might be possible to add a connector BEFORE
//			// creating the SWT sash form
//			if(sashForm != null) {
//				child.create(sashForm);
//				child.refreshConnector();
//				focus(child);
//			}
//			children.add(child);
//			sashCount++;
//		}
//	}
//	public void addConnector(IDisplayConnector child, int weight) {
//		addConnector(child);
//		weightList.add(weight);
//	}
//	@Override
//	public Control createSWTControl(Composite parent) {
//		GridData gridData = new GridData();
//		gridData.horizontalAlignment = GridData.FILL;
//		gridData.grabExcessHorizontalSpace = true;
//		gridData.grabExcessVerticalSpace = true;
//		gridData.verticalAlignment = GridData.FILL;
//		sashForm = new SashForm(parent, SWT.SMOOTH);
//		sashForm.setLayoutData(gridData);
//		sashForm.setOrientation(sashOrientation);
//		for( IDisplayConnector c : children ) {
//			c.create(sashForm);
//		}
//		//If weights defined then we setup weights
//		if(weightList.size()==children.size()) {
//			int[] weights = new int[weightList.size()];
//			int i = 0;
//			for(Integer w : weightList) {
//				weights[i++]=w.intValue();
//			}
//			sashForm.setWeights(weights);
//		}
//		return sashForm;
//	}
//
//	@Override
//	public void focus(IDisplayConnector childFocus) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public Image getConnectorIcon() {
//		return icon;
//	}
//
//	@Override
//	public String getTitle() {
//		return title;
//	}
//
//	@Override
//	public void refreshConnector() {
//		// Dispatching to children
//		for(IDisplayConnector c : children ) {
//			c.refreshConnector();
//		}
//	}
//
//	@Override
//	public void removeConnector(IDisplayConnector child) {
//		// TODO Auto-generated method stub
//
//	}
//	/**
//	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
//	 */
//	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
//		// TODO Auto-generated method stub
//
//	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getSWTConnector()
//	 */
//	public Control getSWTConnector() {
//		return sashForm;
//	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getModel()
//	 */
//	public Object getModel() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	/**
//	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
//	 */
//	public void widgetDisposed(DisposeEvent arg0) {
//		// TODO Auto-generated method stub
//
//	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectors()
//	 */
//	public Collection<IDisplayConnector> getConnectors() {
//		return children;
//	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.IConnector#releaseConnector()
//	 */
//	@Override
//	public void releaseConnector() {
//		// TODO Auto-generated method stub
//
//	}
//	/**
//	 * @see com.nextep.datadesigner.gui.model.IConnector#setModel(java.lang.Object)
//	 */
//	@Override
//	public void setModel(Object model) {
//		// TODO Auto-generated method stub
//
//	}
//
//}
