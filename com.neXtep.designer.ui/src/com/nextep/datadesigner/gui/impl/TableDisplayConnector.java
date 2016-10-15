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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.ColumnEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * This connector is a base for display connectors showing a table of elements. Basically, the
 * connector will be attached to the parent model containing the collection to publish in the
 * editing table. It provides helper functions such as table item caching, table cleanup, and
 * disposal.
 * 
 * @author Christophe Fondacci
 */
public abstract class TableDisplayConnector extends ControlledDisplayConnector {

	private static final Log log = LogFactory.getLog(TableDisplayConnector.class);

	private Map<IObservable, TableItem> tableItems;
	private MultiValueMap persistentEditors;
	private Table swtTable;
	private NextepTableEditor tableEditor;

	private class TableItemModelListener implements IEventListener, IModelOriented<Object> {

		private TableItem item;
		private Object model;
		private ControlledDisplayConnector connector;

		public TableItemModelListener(TableItem item, Object model,
				ControlledDisplayConnector connector) {
			this.item = item;
			this.model = model;
			this.connector = connector;
		}

		@Override
		public void handleEvent(ChangeEvent event, IObservable source, Object data) {
			connector.refreshConnector();
		}

		@Override
		public Object getModel() {
			return model;
		}

		@Override
		public void setModel(Object model) {
			item.setData(model);
			if (model instanceof IObservable) {
				tableItems.remove(this.model);
				tableItems.put((IObservable) model, item);
			}
			this.model = model;
		}

	}

	/**
	 * Default constructor which must be called by all implementors. It initializes caches and
	 * register appropriate model listeners.
	 * 
	 * @param parentModel the versionable object containing the edited model, could be the object
	 *        itself
	 * @param controller controller of the model
	 */
	protected TableDisplayConnector(IObservable parentModel, ITypedObjectUIController controller) {
		super(parentModel, controller);
		Designer.getListenerService().unregisterListener(parentModel, controller);
		tableItems = new HashMap<IObservable, TableItem>();
		persistentEditors = new MultiValueMap();
	}

	protected void initializeTable(Table swtTable, NextepTableEditor editor) {
		this.swtTable = swtTable;
		this.tableEditor = editor;
	}

	/**
	 * Helper method which builds new items or retrieve them from the cache if they have already
	 * been created. Implementors which do create table items manually will have unexpected
	 * behaviour.
	 * 
	 * @param model model which will be represented by the table item
	 * @param index index at which the table item should be created or -1 to append a new item
	 * @return a new table item or the one already associated to the model
	 */
	protected TableItem getOrCreateItem(IObservable model, int index) {
		TableItem item = tableItems.get(model);
		if (item == null) {
			if (index != -1) {
				item = new TableItem(swtTable, SWT.NONE, index);
			} else {
				item = new TableItem(swtTable, SWT.NONE);
			}
			// Listening to model through a custom listener which forwards messages to the
			// connector and handles switch of the underlying model
			Designer.getListenerService().registerListener(item, model,
					new TableItemModelListener(item, model, this));
			// Controller will listen to model too
			if (this.getController() != null) {
				try {
					Designer.getListenerService().registerListener(item, model,
							UIControllerFactory.getController(model)); // this.getController());
				} catch (ErrorException e) {
					log.debug("No controller is listening to table item.");
				}
			}
			// Item data is our IObservable model object
			item.setData(model);
			// Optionnally add any persistent editor
			addPersistentEditors(item);
			tableItems.put(model, item);
		}
		return item;
	}

	/**
	 * Same as {@link TableDisplayConnector#getOrCreateItem(Table, IObservable, int)} but it happens
	 * th item after the last item of the table.
	 * 
	 * @param model model which should be associated with the item
	 * @return the newly created (or retrieved) TableItem
	 */
	protected TableItem getOrCreateItem(IObservable model) {
		return getOrCreateItem(model, -1);
	}

	/**
	 * Retrieves the table item from the child model
	 * 
	 * @param model
	 * @return the table item representing the specified child model
	 */
	protected TableItem getTableItem(IObservable model) {
		return tableItems.get(model);
	}

	/**
	 * @return all table items
	 */
	protected Collection<TableItem> getAllTableItems() {
		return tableItems.values();
	}

	/**
	 * Method called to set any persistent editor on a given table item. The default implementation
	 * is provided by this abstract class and does nothing (no persistent editor). Any sub class
	 * which needs persistent editors should override this method to properly initialize editors.<br>
	 * All persistent editors should be initialized by calling the <code>addPersistentEditors</code>
	 * method to register them correctly.
	 * 
	 * @param i table item being created
	 * @return the
	 */
	protected void addPersistentEditors(TableItem i) {
	}

	/**
	 * Enables or disables the persistent editors of this table. This could be used when the related
	 * model object is locked / unlocked by the version control system.
	 * 
	 * @param flag enable state of the persistent editors
	 */
	protected void setPersistentEditorsEnable(boolean flag) {
		for (Object o : persistentEditors.values()) {
			// Since these are permanent editors, we can retrieve
			// the control by passing (null,null)
			// TODO implement a basic control getter to ColumnEditor
			Control c = ((ColumnEditor) o).getEditor(null, null);
			c.setEnabled(flag);
		}
	}

	/**
	 * Adds a persistent editor to a table item column. Once added, it will be automatically enabled
	 * / disabled on model changes (checkout / checkin) and removed on cleanup. Note that a call to
	 * this method will immediately edit the table item column.
	 * 
	 * @param item edited item of this persistent editor
	 * @param columnIndex edited column of the persistent editor
	 * @param editor column editor that should be shown.
	 */
	public void addPersistentEditor(TableItem item, int columnIndex, ColumnEditor editor) {
		persistentEditors.put(item, editor);
		editor.edit(item.getParent(), item, columnIndex);
	}

	/**
	 * Cleans up the specified SWT Table. The connector will stop listening on items and will
	 * dispose them properly. Once called, the table will be cleared.
	 * 
	 * @param t SWT Table to clean
	 */
	protected void clean(Table t) {
		for (TableItem i : t.getItems()) {
			cleanTableItem(i);
		}
	}

	/**
	 * Removes a table item given the underlying object model to which it correponds
	 * 
	 * @param model object model of the item to remove
	 * @return <code>true</code> if an item has been removed, or <code>false</code> if no matching
	 *         item has been found
	 */
	protected boolean removeTableItem(IObservable model) {
		TableItem i = tableItems.get(model);
		if (i != null) {
			cleanTableItem(i);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Performs a full cleanup of a Table item. This method will dispose any remaining persistent
	 * editor, unregister the listeners, remove the item entry from cache and finally dispose the
	 * SWT control
	 * 
	 * @param i SWT table item to remove
	 */
	@SuppressWarnings("unchecked")
	private void cleanTableItem(TableItem i) {
		// If we have an Observable on this item data
		if (i.getData() instanceof IObservable) {
			// Removing any persistent editor
			Collection<ColumnEditor> editors = (Collection<ColumnEditor>) persistentEditors.get(i);
			if (editors != null) {
				for (ColumnEditor e : editors) {
					e.disposeEditor();
				}
			}
			persistentEditors.remove(i);

			// Then we remove item from cache
			IObservable model = (IObservable) i.getData();
			tableItems.remove(model);
		}
		// Finishing by disposing the item
		i.dispose();
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#createModelListener(com.nextep.datadesigner.model.IObservable)
	 */
	@Override
	protected IEventListener createModelListener(IObservable model) {
		return new ParentModelListener(this);
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(Object model) {
		super.setModel(model);
		log.debug("Switching parent model of a SWT table connector <" + this + ">");
		if (tableEditor != null) {
			tableEditor.setVersionedParent(getTableVersionedParent(model));
		}
		if (swtTable != null) {
			clean(swtTable);
		}
		if (isInitialized()) {
			refreshConnector();
		}
	}

	/**
	 * This method provides the lockable object model which is the parent of the items listed in the
	 * table. This information allows the abstraction to enable / disable edition on the whole table
	 * depending on the state of this element.<br>
	 * TODO: Should switch to a {@link ILockable} signature, would allow non-versionable objects to
	 * fit FIXME: Check this information still used for this behaviour
	 * 
	 * @param model model of this connector
	 * @return the lockable parent
	 */
	protected Object getTableVersionedParent(Object model) {
		return model;
	}

	/**
	 * A listener implementation which inihibates events from parent to the connector. It only
	 * routes setModel and getModel method calls to the connector.
	 * 
	 * @author Christophe Fondacci
	 */
	protected class ParentModelListener implements IEventListener, IModelOriented<Object> {

		TableDisplayConnector connector;

		public ParentModelListener(TableDisplayConnector connector) {
			this.connector = connector;
		}

		/**
		 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
		 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
		 */
		@Override
		public void handleEvent(ChangeEvent event, IObservable source, Object data) {
			// We do not dispatch anything from parent to child
		}

		/**
		 * @see com.nextep.datadesigner.model.IModelOriented#getModel()
		 */
		@Override
		public Object getModel() {
			return connector.getModel();
		}

		/**
		 * @see com.nextep.datadesigner.model.IModelOriented#setModel(java.lang.Object)
		 */
		@Override
		public void setModel(final Object model) {
			// Display.getDefault().syncExec(new Runnable() {
			//
			// @Override
			// public void run() {
			connector.setModel(model);
			// }
			// });
		}
	}

	/**
	 * @return the currently selected model or <code>null</code> if no current selection. Only
	 *         supports single selection.
	 */
	protected IObservable getSelection() {
		if (swtTable == null)
			return null;
		TableItem[] sel = swtTable.getSelection();
		if (sel.length > 0) {
			return (IObservable) sel[0].getData();
		} else {
			return null;
		}
	}
}
