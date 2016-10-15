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
package com.nextep.designer.vcs.ui.navigators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class ReferenceNavigator extends UntypedNavigator implements INavigatorConnector {
	private static final Log log = LogFactory.getLog(ReferenceNavigator.class);
	private IReference ref;
	private IReferenceable model;
	private INavigatorConnector conn;
	private TreeItem item;
	public ReferenceNavigator(IReference ref) {
		super(null,null);
		this.ref=ref;
		try {
			this.model = VersionHelper.getReferencedItem(ref);
			conn = UIControllerFactory.getController(ref.getType()).initializeNavigator(model);
		} catch( ErrorException e ) {
//			// We try to retrieve the reference from the global repository
//			if(!ref.isVolatile()) {
//				CorePlugin.getService(IReferenceManager.class).findByTypeName(ref.getType(), ref.getArbitraryName());
//				// TRying with non volatile
////				ref.setVolatile(false);
//				try {
//					this.model = VersionHelper.getReferencedItem(ref);
//					conn = ControllerFactory.getController(ref.getType()).initializeNavigator(model);
//				} catch( ErrorException e2) {
//					// We might have an external reference
//					log.error(e.getMessage());
//					this.model=null;
//					conn = null;
//				} finally {
////					ref.setVolatile(true);
//				}
//			} else {
				log.error(e.getMessage(),e);
				this.model=null;
				conn = null;
//			}

		}

	}
//	/**
//	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
//	 */
//	@Override
//	public void setModel(Object model) {
//		// Reloading model
//		this.model = VersionHelper.getReferencedItem(ref);
//		conn.setModel(this.model);
//	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#createSWTConnector(org.eclipse.swt.widgets.TreeItem, int)
	 */
	@Override
	protected TreeItem createSWTConnector(TreeItem parent, int treeIndex) {
		if(conn!=null) {
			return conn.create(parent, treeIndex);
		} else {
			if(treeIndex == -1) {
				item = new TreeItem(parent,SWT.NONE);
			} else {
				item = new TreeItem(parent,SWT.NONE,treeIndex);
			}
			item.setImage(ImageFactory.ICON_ERROR);
			return item;
		}
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return ImageFactory.ICON_ERROR;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#initialize()
	 */
	@Override
	public void initialize() {
		if(conn!=null) {
			try {
				deactivateRegister();
				conn.initialize();
			} finally {
				activateRegister();
			}
		}
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance("REFERENCE");
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getModel()
	 */
	@Override
	public Object getModel() {
		return ref;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public TreeItem getSWTConnector() {
		if(conn!=null) {
			return conn.getSWTConnector();
		} else {
			return item;
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		if(conn!=null) {
			return "[Ref] " + conn.getTitle();
		} else {
			return "[Ref] Unresolved " + ref.getType().getName() + " UID <" + ref.getReferenceId() + ">";
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		if(conn != null) {
			conn.refreshConnector();
			conn.getSWTConnector().setText(getTitle());
		} else {
			item.setText(getTitle());
		}
	}
	/**
	 * We cannot edit a reference so there is no default action
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#defaultAction()
	 */
	@Override
	public void defaultAction() {
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if(conn!=null) {
			conn.handleEvent(event, source, data);
		}

	}


}
