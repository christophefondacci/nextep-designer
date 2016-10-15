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
package com.nextep.datadesigner.dbgm.gui;

import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.gui.jface.ViewTriggableContentProvider;
import com.nextep.datadesigner.dbgm.model.ITriggable;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.dbgm.model.TriggerTime;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ExternalReferenceException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.editors.CheckBoxEditor;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.services.IParsingService;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * @author Christophe Fondacci
 */
public class TriggerEditorGUI extends ControlledDisplayConnector {

	private final static Log log = LogFactory.getLog(TriggerEditorGUI.class);
	private Composite editor = null; // @jve:decl-index=0:visual-constraint="10,10"
	private Label nameLabel = null;
	private Text nameText = null;
	private Label descLabel = null;
	private Text descText = null;
	private Label fireLabel = null;
	private Combo timeCombo = null;
	private Button manualCheck = null;
	private Button insertCheck = null;
	private Button deleteCheck = null;
	private Button updateCheck = null;
	private Label onLabel = null;
	private Text triggableLabel = null;
	private Button changeTriggableButton = null;

	// private Table eventsTable = null;
	// private Button addEventButton = null;
	// private Button removeEventButton = null;

	public TriggerEditorGUI(ITrigger trigger, ITypedObjectUIController controller) {
		super(trigger, controller);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		GridData gridData12 = new GridData();
		gridData12.verticalSpan = 3;
		gridData12.verticalAlignment = GridData.FILL;
		gridData12.horizontalAlignment = GridData.FILL;
		GridData gridData11 = new GridData();
		gridData11.horizontalSpan = 3;
		GridData gridData4 = new GridData();
		gridData4.horizontalSpan = 2;
		GridData gridData3 = new GridData();
		gridData3.horizontalSpan = 2;
		GridData gridData2 = new GridData();
		gridData2.horizontalSpan = 2;
		gridData2.verticalSpan = 1;
		GridData gridData1 = new GridData();
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.verticalAlignment = GridData.CENTER;
		gridData1.horizontalSpan = 4;
		gridData1.horizontalAlignment = GridData.FILL;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 4;
		gridData.verticalAlignment = GridData.CENTER;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(gridLayout);
		editor.setSize(new Point(538, 200));
		nameLabel = new Label(editor, SWT.NONE);
		nameLabel.setText(DBGMUIMessages.getString("editor.trigger.name")); //$NON-NLS-1$
		nameLabel.setLayoutData(gridData4);
		nameText = new Text(editor, SWT.BORDER);
		nameText.setLayoutData(gridData1);
		nameText.setTextLimit(100);
		TextEditor.handle(nameText, ChangeEvent.NAME_CHANGED, this);
		ColorFocusListener.handle(nameText);
		descLabel = new Label(editor, SWT.NONE);
		descLabel.setText(DBGMUIMessages.getString("editor.trigger.description")); //$NON-NLS-1$
		descLabel.setLayoutData(gridData3);
		descText = new Text(editor, SWT.BORDER);
		descText.setLayoutData(gridData);
		TextEditor.handle(descText, ChangeEvent.DESCRIPTION_CHANGED, this);
		ColorFocusListener.handle(descText);

		onLabel = new Label(editor, SWT.NONE);
		onLabel.setText(DBGMUIMessages.getString("editor.trigger.triggable")); //$NON-NLS-1$
		onLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		triggableLabel = new Text(editor, SWT.BORDER);
		triggableLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		triggableLabel.setFont(FontFactory.FONT_BOLD);
		triggableLabel.setEditable(false);
		changeTriggableButton = new Button(editor, SWT.PUSH);
		changeTriggableButton.setImage(ImageFactory.ICON_EDIT_TINY);
		changeTriggableButton.setToolTipText(DBGMUIMessages
				.getString("editor.trigger.changeTriggableTooltip")); //$NON-NLS-1$
		changeTriggableButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				changeTriggable();
			}
		});

		fireLabel = new Label(editor, SWT.NONE);
		fireLabel.setText(DBGMUIMessages.getString("editor.trigger.triggerTime")); //$NON-NLS-1$
		fireLabel.setLayoutData(gridData2);
		createTimeCombo();
		deleteCheck = new Button(editor, SWT.CHECK);
		deleteCheck.setText(DBGMUIMessages.getString("editor.trigger.actionDelete")); //$NON-NLS-1$
		CheckBoxEditor.handle(deleteCheck, ChangeEvent.CUSTOM_1, this);

		// new Label(editor, SWT.NONE);

		// eventsTable = new Table(editor, SWT.NONE);
		// eventsTable.setHeaderVisible(true);
		// eventsTable.setLayoutData(gridData12);
		// eventsTable.setLinesVisible(true);
		// TableColumn tableColumn = new TableColumn(eventsTable, SWT.NONE);
		// tableColumn.setWidth(120);
		// tableColumn.setText("Triggered events");
		// TableColumn tableColumn1 = new TableColumn(eventsTable, SWT.NONE);
		// tableColumn1.setWidth(100);
		insertCheck = new Button(editor, SWT.CHECK);
		insertCheck.setText(DBGMUIMessages.getString("editor.trigger.actionInsert")); //$NON-NLS-1$
		CheckBoxEditor.handle(insertCheck, ChangeEvent.CUSTOM_2, this);
		// addEventButton = new Button(editor, SWT.NONE);
		updateCheck = new Button(editor, SWT.CHECK);
		updateCheck.setText(DBGMUIMessages.getString("editor.trigger.actionUpdate")); //$NON-NLS-1$
		CheckBoxEditor.handle(updateCheck, ChangeEvent.CUSTOM_3, this);
		// removeEventButton = new Button(editor, SWT.NONE);
		new Label(editor, SWT.NONE);
		manualCheck = new Button(editor, SWT.CHECK);
		manualCheck.setText(DBGMUIMessages.getString("editor.trigger.customDefinition")); //$NON-NLS-1$
		manualCheck.setLayoutData(gridData11);
		CheckBoxEditor.handle(manualCheck, ChangeEvent.CUSTOM_4, this);

		// addEventButton.setImage(ImageFactory.ICON_RIGHT_TINY);
		// removeEventButton.setImage(ImageFactory.ICON_LEFT_TINY);

		return editor;
	}

	/**
	 * This method initializes timeCombo
	 */
	private void createTimeCombo() {
		GridData gridData5 = new GridData();
		gridData5.verticalSpan = 1;
		timeCombo = new Combo(editor, SWT.READ_ONLY);
		for (TriggerTime t : TriggerTime.values()) {
			timeCombo.add(t.toString());
			timeCombo.setData(t.toString(), t);
		}
		timeCombo.setLayoutData(gridData5);
		ComboEditor.handle(timeCombo, ChangeEvent.TRIGGER_TIME_CHANGED, this);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		ITrigger trigger = (ITrigger) getModel();

		if (nameText.isDisposed())
			return;
		nameText.setText(notNull(trigger.getName()));
		descText.setText(notNull(trigger.getDescription()));
		timeCombo.setText(trigger.getTime() != null ? trigger.getTime().toString() : ""); //$NON-NLS-1$
		Set<TriggerEvent> evts = trigger.getEvents();
		insertCheck.setSelection(evts.contains(TriggerEvent.INSERT));
		deleteCheck.setSelection(evts.contains(TriggerEvent.DELETE));
		updateCheck.setSelection(evts.contains(TriggerEvent.UPDATE));
		if (trigger.isCustom()) {
			insertCheck.setSelection(false);
			deleteCheck.setSelection(false);
			updateCheck.setSelection(false);
		}
		if (trigger.getTriggableRef() != null) {
			try {
				triggableLabel.setText(((ITriggable) VersionHelper.getReferencedItem(trigger
						.getTriggableRef())).getName());
			} catch (ExternalReferenceException e) {
				triggableLabel.setText(DBGMUIMessages.getString("editor.trigger.unresolved")); //$NON-NLS-1$
			}
		} else {
			triggableLabel.setText(""); //$NON-NLS-1$
		}
		manualCheck.setSelection(trigger.isCustom());
		timeCombo.setEnabled(!trigger.updatesLocked() && !trigger.isCustom());
		insertCheck.setEnabled(!trigger.updatesLocked() && !trigger.isCustom());
		deleteCheck.setEnabled(!trigger.updatesLocked() && !trigger.isCustom());
		updateCheck.setEnabled(!trigger.updatesLocked() && !trigger.isCustom());
		manualCheck.setEnabled(!trigger.updatesLocked());
		nameText.setEnabled(!trigger.updatesLocked());
		descText.setEnabled(!trigger.updatesLocked());
		if (DBGMHelper.getCurrentVendor() == DBVendor.ORACLE) {
			manualCheck.setEnabled(false);
		}
		changeTriggableButton.setEnabled(!trigger.updatesLocked() && !trigger.isCustom());
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		super.handleEvent(event, source, data);
		ITrigger trigger = (ITrigger) getModel();
		switch (event) {
		case NAME_CHANGED:
			getParsingService().rename(trigger, (String) data);
			break;
		case TRIGGER_TIME_CHANGED:
			trigger.setTime(TriggerTime.valueOf((String) data));
			break;
		case CUSTOM_1:
			toggleTriggerEvent(trigger, TriggerEvent.DELETE);
			break;
		case CUSTOM_2:
			toggleTriggerEvent(trigger, TriggerEvent.INSERT);
			break;
		case CUSTOM_3:
			toggleTriggerEvent(trigger, TriggerEvent.UPDATE);
			break;
		case CUSTOM_4:
			trigger.setCustom(!trigger.isCustom());
			break;
		}
		refreshConnector();
	}

	/**
	 * Toggles the specified event for this trigger. If the event is already defined for this
	 * trigger it will be removed from it, otherwise it will be added.
	 * 
	 * @param trigger trigger to process
	 * @param event event to toggle
	 */
	private void toggleTriggerEvent(ITrigger trigger, TriggerEvent event) {
		if (trigger.getEvents().contains(event)) {
			trigger.removeEvent(event);
		} else {
			trigger.addEvent(event);
		}
	}

	@Override
	public Image getConnectorIcon() {
		return DBGMImages.WIZARD_TRIGGER;
	}

	private IParsingService getParsingService() {
		return DbgmPlugin.getService(IParsingService.class);
	}

	/**
	 * Changes the triggable parent of this trigger by poping a dialog to the user.
	 */
	private void changeTriggable() {
		ITriggable elt = (ITriggable) Designer.getInstance().invokeSelection(
				"find.element", new ViewTriggableContentProvider(), //$NON-NLS-1$
				VCSPlugin.getService(IWorkspaceService.class).getCurrentWorkspace(),
				DBGMUIMessages.getString("editor.trigger.selectTriggableMsg")); //$NON-NLS-1$
		if (elt != null) {
			final ITrigger t = (ITrigger) getModel();
			try {
				// Removing previous trigger registration
				if (t.getTriggableRef() != null) {
					ITriggable triggable = (ITriggable) VersionHelper.getReferencedItem(t
							.getTriggableRef());
					triggable.removeTrigger(t);
				}
			} catch (UnresolvedItemException ex) {
				log.debug("Previous triggable not found: " + ex.getMessage(), ex);
			}
			((ITrigger) getModel()).setTriggableRef(elt.getReference());
			try {
				// Removing previous trigger registration
				ITriggable triggable = (ITriggable) VersionHelper.getReferencedItem(t
						.getTriggableRef());
				triggable.addTrigger(t);
			} catch (UnresolvedItemException ex) {
				log.debug("Previous triggable not found: " + ex.getMessage(), ex);
			}
		}
	}
}
