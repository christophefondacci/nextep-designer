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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.ui.VCSImages;

/**
 * @author Christophe Fondacci
 */
public class DiffLineNavigator extends UntypedNavigator implements INavigatorConnector {

	private INavigatorConnector sourceConn;
	private INavigatorConnector targetConn;
	private TreeItem item;

	public DiffLineNavigator(IComparisonItem item) {
		super(item, null);
		// Initializing internal connectors
		if (item.getSource() != null) {
			this.sourceConn = UIControllerFactory.getController(item.getSource())
					.initializeNavigator(item.getSource());
		}
		if (item.getTarget() != null) {
			this.targetConn = UIControllerFactory.getController(item.getTarget())
					.initializeNavigator(item.getTarget());
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#createSWTConnector(org.eclipse.swt.widgets.TreeItem,
	 *      int)
	 */
	@Override
	protected TreeItem createSWTConnector(TreeItem parent, int treeIndex) {
		if (sourceConn != null) {
			item = sourceConn.create(parent, treeIndex);
		} else if (targetConn != null) {
			item = targetConn.create(parent, treeIndex);
		} else {
			item = new TreeItem(parent, SWT.NONE);
		}
		if (getConnectorIcon() != null) {
			item.setImage(getConnectorIcon());
		}

		item.setData(this);
		item.addDisposeListener(this);
		//
		return item;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		switch (((IComparisonItem) getModel()).getDifferenceType()) {
		case MISSING_SOURCE:
			return targetConn.getConnectorIcon();
		default:
			return sourceConn.getConnectorIcon();
		}
	}

	private Image getDifferenceIcon() {
		switch (((IComparisonItem) getModel()).getDifferenceType()) {
		case MISSING_SOURCE:
			return VCSImages.ICON_DIFF_REMOVED;
		case MISSING_TARGET:
			return VCSImages.ICON_DIFF_ADDED;
		case DIFFER:
			return VCSImages.ICON_DIFF_CHANGED;
		}
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public TreeItem getSWTConnector() {
		return item;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		// IComparisonItem diffItem = (IComparisonItem)getModel();
		item.setText(new String[] { sourceConn != null ? sourceConn.getTitle() : "", "",
				targetConn != null ? targetConn.getTitle() : "" });
		item.setImage(new Image[] {
				sourceConn == null ? null : ImageFactory.getImage(((IComparisonItem) getModel())
						.getType().getIcon()),
				getDifferenceIcon(),
				targetConn == null ? null : ImageFactory.getImage(((IComparisonItem) getModel())
						.getType().getIcon()) });

	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#releaseConnector()
	 */
	@Override
	public void releaseConnector() {
		super.releaseConnector();
		if (sourceConn != null) {
			sourceConn.releaseConnector();
		}
		if (targetConn != null) {
			targetConn.releaseConnector();
		}
	}

	@Override
	public void setTree(Tree tree) {
		super.setTree(tree);
		if (sourceConn != null) {
			sourceConn.setTree(tree);
		}
		if (targetConn != null) {
			targetConn.setTree(tree);
		}
	}
}
