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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;

public class VersionableNavigator extends TypedNavigator implements INavigatorConnector,
		IEventListener {

	// private static final Log log = LogFactory.getLog(VersionableNavigator.class);
	private INavigatorConnector wrappedConnector = null;

	public VersionableNavigator(IVersionable<?> versionable, ITypedObjectUIController controller) {
		super(versionable, controller);
		// Initializing wrapped object GUI
		wrappedConnector = UIControllerFactory.getController(versionable.getType())
				.initializeNavigator(versionable.getVersionnedObject());
	}

	public void initialize() {
		super.initialize();
		wrappedConnector.initialize();
	}

	@Override
	protected TreeItem createSWTConnector(TreeItem parent, int treeIndex) {
		// Delegates the creation to the wrapped connector
		TreeItem subItem = wrappedConnector.create(parent, treeIndex);
		// Simply adds a versionable as the data
		subItem.setData(this);

		return subItem;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	 */
	@Override
	public Image getConnectorIcon() {
		return wrappedConnector.getConnectorIcon();
	}

	@Override
	public void refreshConnector() {
		if (!wrappedConnector.isInitialized())
			return;
		IVersionable<?> versionable = (IVersionable<?>) getModel();
		// Refreshing the wrapped connector
		wrappedConnector.refreshConnector();
		// And adding a suffix on the item label to specify the version
		TreeItem subItem = wrappedConnector.getSWTConnector();
		if (subItem.isDisposed()) {
			return;
		}
		subItem.setText(subItem.getText()); // + getFormattedSuffix());

		// And colorize text according to version status
		if (versionable.getVersion().getStatus() == IVersionStatus.CHECKED_IN) {
			subItem.setForeground(FontFactory.CHECKIN_COLOR);
			// subItem.setBackground(CHECKIN_BACK_COLOR);
			// subItem.setFont(CHECKIN_FONT);
		} else {
			subItem.setForeground(FontFactory.CHECKOUT_COLOR);
			// subItem.setBackground(CHECKOUT_BACK_COLOR);
			// subItem.setFont(CHECKOUT_FONT);

		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object o) {
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				refreshConnector();
			}
		});
	}

	/**
	 * Formats the label of a versioned object. The return pattern is name followed by the version
	 * number.
	 * 
	 * @return the label of this versioned object
	 */
	public String getFormattedSuffix() {
		IVersionable<?> versionable = (IVersionable<?>) getModel();
		if (versionable != null && versionable.getVersion() != null) {
			if (versionable.getVersion().getStatus() == IVersionStatus.NOT_VERSIONED) {
				return "";
			}
			return " - " + versionable.getVersion().getLabel();
		}
		return "";
	}

	@Override
	public TreeItem getSWTConnector() {
		if (wrappedConnector != null) {
			return wrappedConnector.getSWTConnector();
		} else {
			return null;
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.INavigatorConnector#getTitle()
	 */
	public String getTitle() {
		if (wrappedConnector != null) {
			return wrappedConnector.getTitle(); // + getFormattedSuffix();
		}
		return "";
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#defaultAction()
	 */
	@Override
	public void defaultAction() {
		wrappedConnector.defaultAction();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ListeningControlledConnector#addListeners(com.nextep.datadesigner.model.IObservable,
	 *      boolean)
	 */
	@Override
	protected void addListeners(IObservable model) {
		super.addListeners(model);
		Designer.getListenerService().registerListener(this,
				((IVersionable<?>) model).getVersion(), this);
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#releaseConnector()
	 */
	@Override
	public void releaseConnector() {
		super.releaseConnector();
		if (wrappedConnector != null) {
			wrappedConnector.releaseConnector();
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#setTree(org.eclipse.swt.widgets.Tree)
	 */
	@Override
	public void setTree(Tree tree) {
		super.setTree(tree);
		wrappedConnector.setTree(tree);
	}

	/**
	 * @return the original title of the wrapped connector. Initially created for speedup of
	 *         {@link VersionableNavigator} comparison (sort) during startup.
	 */
	protected String getWrappedTitle() {
		return wrappedConnector.getTitle();
	}

	@Override
	public int compareTo(INavigatorConnector o) {
		if (o instanceof VersionableNavigator) {
			return getWrappedTitle().compareTo(((VersionableNavigator) o).getWrappedTitle());
		}
		return super.compareTo(o);
	}
}
