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
package com.nextep.datadesigner.vcs.gui.external;

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.gui.VersionableNavigator;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSImages;

/**
 * A paint listener which can handle the graphical additions
 * on TreeItem's icons depending on their locking status or
 * if they are a reference to another item.
 *
 * @author Christophe Fondacci
 *
 */
public class VersionablePaintItemListener implements Listener {

	private static final Log log = LogFactory.getLog(VersionablePaintItemListener.class);
	private boolean showLock,showLink;
	private Image LOCK_IMAGE;
	private Image LINK_IMAGE;
	private Image USER_LOCK_IMAGE;
	private Image ERROR_DECORATOR;
	private TextStyle versionStyle;
	private VersionablePaintItemListener(boolean tiny, boolean showLock, boolean showLink) {
		this.showLock = showLock;
		this.showLink = showLink;
		if(tiny) {
			LOCK_IMAGE = ImageFactory.ICON_LOCK_TINY;
			LINK_IMAGE = VCSImages.ICON_REFERENCE_TINY;
			USER_LOCK_IMAGE = ImageFactory.ICON_USER_LOCK_TINY;
			ERROR_DECORATOR = ImageFactory.ICON_ERROR_DECO_TINY;
		} else {
			LOCK_IMAGE = ImageFactory.ICON_LOCK;
			LINK_IMAGE = VCSImages.ICON_REFERENCE;
			USER_LOCK_IMAGE = ImageFactory.ICON_USER_LOCK;
			ERROR_DECORATOR = ImageFactory.ICON_ERROR_DECO_TINY;
		}
		this.versionStyle  = new TextStyle(Display.getCurrent().getSystemFont(),FontFactory.VERSIONABLE_DECORATOR_COLOR,null);
	}
	public static void handle(Tree tree, boolean tiny, boolean showLock, boolean showLink) {
		tree.addListener(SWT.PaintItem,new VersionablePaintItemListener(tiny, showLock,showLink));
	}

	public void handleEvent(Event event) {
		try {
			 TreeItem item = (TreeItem)event.item;
			 if(event.index!=0 || item ==null) return;
			 //Handling lock / unlock icons incrust on Versionables
			 if(showLock && item.getData() instanceof VersionableNavigator) {
				 //Displaying the lock icon over the treeitem when checked in
				 Object o = ((VersionableNavigator)item.getData()).getModel();
				 final IVersionable<?> v = ((IVersionable<?>)o);
				 final int imgWidth = item.getImage().getBounds().width;
				 final int imgHeight = item.getImage().getBounds().height;
				 if( o != null && v.getVersionnedObject().updatesLocked()) {
					 Image decorator = LOCK_IMAGE;
					 if(v.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
						decorator = USER_LOCK_IMAGE; 
					 }
					 int x = event.x + imgWidth -decorator.getBounds().width;
					 int y = event.y + imgHeight -decorator.getBounds().height;

					 event.gc.drawImage(decorator, x, y);
				 }
				 // Error decoration
				 if(v!=null) {
					 Collection<IMarker> markers = Designer.getMarkerProvider().getMarkersFor(v);
					 if(markers !=null && !markers.isEmpty()) {
						 Image decorator = ERROR_DECORATOR;
						 final int x = event.x + imgWidth -decorator.getBounds().width;
						 final int y = event.y + imgHeight -decorator.getBounds().height;
						 event.gc.drawImage(decorator, x, y);
					 }
				 }
				 
				 //To make sure the versionable will always print its text on top
				 final String suffix = ((VersionableNavigator)item.getData()).getFormattedSuffix();
				 // Finding seperator
				 FontFactory.versionableLayout.setText(suffix);
				 FontFactory.versionableLayout.setStyle(versionStyle, 0, suffix.length());
				 Rectangle r = item.getTextBounds(0);
				 FontFactory.versionableLayout.draw(event.gc, r.x+r.width, r.y+1);
				 event.doit=false;
			 } else if(showLink && item.getData()!=null && item.getData() instanceof ITypedObject) {
				 if( ((ITypedObject)item.getData()).getType() == IElementType.getInstance("REFERENCE")) {
					 int x = event.x + item.getImage().getBounds().width -LINK_IMAGE.getBounds().width;
					 int y = event.y + item.getImage().getBounds().height -LINK_IMAGE.getBounds().height;

					 event.gc.drawImage(LINK_IMAGE, x, y);
				 }
			 }
		} catch( Exception e ) {
			log.debug("Paint listener exception!");
			e.printStackTrace();
//			log.debug(e);
		}

	 }

}
