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
package com.nextep.datadesigner.dbgm.gui.navigators;

import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyIndexWrapper;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Graphical user interface for table typed objects.
 * 
 * @author Christophe Fondacci
 */
public class TableNavigator extends TypedNavigator implements INavigatorConnector {

	public TableNavigator(IBasicTable table, ITypedObjectUIController controller) {
		super(table, controller);
	}

	@Override
	public void initializeChildConnectors() {
		IBasicTable table = (IBasicTable) getModel();
		// Generating sub controllers :
		for (IBasicColumn c : table.getColumns()) {
			INavigatorConnector n = UIControllerFactory.getController(c).initializeNavigator(c);
			this.addConnector(n);
			// Forcing consistency
			if (c.getParent() != table) {
				try {
					Observable.deactivateListeners();
					c.setParent(table);
				} finally {
					Observable.activateListeners();
				}

			}
		}
		for (IKeyConstraint c : table.getConstraints()) {
			this.addConnector(UIControllerFactory.getController(c).initializeNavigator(c));
			// Forcing consistency
			if (c.getConstrainedTable() != table) {
				try {
					Observable.deactivateListeners();
					c.setConstrainedTable(table);
				} finally {
					Observable.activateListeners();
				}
			}
			// Index wrapping
			// if(c instanceof UniqueKeyConstraint) {
			// IIndex ukIndex = wrapUniqueKey((UniqueKeyConstraint)c);
			// this.addConnector(ControllerFactory.getController(ukIndex).initializeNavigator(ukIndex));
			// }
		}
		final ITypedObjectUIController versionableController = UIControllerFactory
				.getController(IElementType.getInstance(IVersionable.VERSIONABLE_TYPE_ID));
		for (IDataSet s : table.getDataSets()) {
			this.addConnector(versionableController.initializeNavigator(s));
		}
		for (IIndex i : table.getIndexes()) {
			this.addConnector(versionableController.initializeNavigator(i));
		}
		for (ITrigger trg : table.getTriggers()) {
			this.addConnector(versionableController.initializeNavigator(trg));
		}
	}

	protected IIndex wrapUniqueKey(UniqueKeyConstraint uk) {
		return new UniqueKeyIndexWrapper(uk);
	}

	// /**
	// * @see com.nextep.datadesigner.gui.model.IConnector#getConnectorIcon()
	// */
	// @Override
	// public Image getConnectorIcon() {
	// return DBGMImages.ICON_TABLE;
	// }
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object o) {
		switch (event) {
		case COLUMN_ADDED:
			IBasicColumn column = (IBasicColumn) o;
			INavigatorConnector connector = UIControllerFactory.getController(column)
					.initializeNavigator(column);
			this.addConnector(connector);
			break;
		case COLUMN_REMOVED:
			IBasicColumn column1 = (IBasicColumn) o;
			INavigatorConnector conn = getConnector(column1);
			if (conn != null && conn.getSWTConnector() != null) {
				this.removeConnector(conn);
			}
			break;
		case DATASET_ADDED:
		case CONSTRAINT_ADDED:
		case INDEX_ADDED:
		case TRIGGER_ADDED:
		case GENERIC_CHILD_ADDED:
			if (o != null) {
				if (getConnector(o) == null) {
					if (o instanceof IVersionable<?>) {
						this.addConnector(UIControllerFactory.getController(
								IElementType.getInstance(IVersionable.VERSIONABLE_TYPE_ID))
								.initializeNavigator(o));
					} else {
						this.addConnector(UIControllerFactory.getController(o).initializeNavigator(
								o));
					}
				}
			}
			break;
		case DATASET_REMOVED:
		case CONSTRAINT_REMOVED:
		case INDEX_REMOVED:
		case TRIGGER_REMOVED:
		case GENERIC_CHILD_REMOVED:
			this.removeConnector(this.getConnector(o));
			break;
		}
		refreshConnector();
	}

}
