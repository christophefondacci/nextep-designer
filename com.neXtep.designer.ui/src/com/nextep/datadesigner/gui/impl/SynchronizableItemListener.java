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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.gui.model.IConnector;
import com.nextep.datadesigner.model.ISynchronizable;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class SynchronizableItemListener implements Listener {

	private static final Log log = LogFactory.getLog(SynchronizableItemListener.class);
	private Image SYNCH_IMAGE;
	private Image NOT_SYNCHED_IMAGE;
	private SynchronizableItemListener(boolean tiny) {
		if(tiny) {
			SYNCH_IMAGE = ImageFactory.ICON_SYNCH_TINY;
			NOT_SYNCHED_IMAGE = ImageFactory.ICON_UNSYNCH_TINY;
		} else {
			SYNCH_IMAGE = ImageFactory.ICON_SYNCH;
			NOT_SYNCHED_IMAGE = ImageFactory.ICON_UNSYNCH;
		}
	}
	public static void handle(Tree tree, boolean tiny) {
		tree.addListener(SWT.PaintItem,new SynchronizableItemListener(tiny));
	}

	public void handleEvent(Event event) {
		try {
			 TreeItem item = (TreeItem)event.item;
			 if(event.index!=0 || item ==null) return;
			 //Handling lock / unlock icons incrust on Versionables
			 if(item.getData() instanceof IConnector) {
				 //Displaying the lock icon over the treeitem when checked in
				 Object o = ((IConnector<?, ?>)item.getData()).getModel();
				 if( o instanceof ISynchronizable) {
					 Image img = null;
					 switch(((ISynchronizable)o).getSynchStatus()) {
					 case SYNCHED:
						 img = SYNCH_IMAGE;
						 break;
					 case UNSYNCHED:
						 img = NOT_SYNCHED_IMAGE;
						 break;
					 default:
						 return;
					 }
					 int x = event.x; // + item.getImage().getBounds().width -img.getBounds().width;
					 int y = event.y; // + item.getImage().getBounds().height -img.getBounds().height;

					 event.gc.drawImage(img, x, y);
				 }
			 }
		} catch( Exception e ) {
			log.debug("Paint listener exception!");
			e.printStackTrace();
//			log.debug(e);
		}

	 }

}
