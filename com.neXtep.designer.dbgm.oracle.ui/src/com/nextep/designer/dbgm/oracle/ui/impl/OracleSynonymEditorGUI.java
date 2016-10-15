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
package com.nextep.designer.dbgm.oracle.ui.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.dbgm.gui.SynonymEditorGUI;
import com.nextep.datadesigner.gui.impl.swt.CheckboxEditor;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.oracle.impl.OracleSynonym;
import com.nextep.designer.dbgm.oracle.model.IOracleSynonym;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * An editor to display and change the properties of a {@link OracleSynonym}.
 * 
 * @author Bruno Gautier
 */
public class OracleSynonymEditorGUI extends SynonymEditorGUI {

    private CheckboxEditor publicCheck = null;
    private FieldEditor refDbObjDbLinkNameField = null;

    /**
     * Creates a new instance of <code>SynonymEditorGUI</code> for the specified
     * <code>IOracleSynonym</code> object.
     * 
     * @param synonym the <code>IOracleSynonym</code> object to display in this editor.
     * @param controller the controller associated with this typed object.
     */
    public OracleSynonymEditorGUI(IOracleSynonym synonym, ITypedObjectUIController controller) {
        super(synonym, controller);
    }

    @Override
    public IOracleSynonym getModel() {
        return (IOracleSynonym)super.getModel();
    }

    @Override
    protected Control createSWTControl(Composite parent) {
        Composite oraSynEditor = (Composite)super.createSWTControl(parent);

        refDbObjDbLinkNameField = new FieldEditor(oraSynEditor,
                DBOMUIMessages.getString("synonym.editor.refDbObjDbLinkName"), 1, 1, true, this,
                ChangeEvent.CUSTOM_4);

        return oraSynEditor;
    }

    @Override
    protected void setTopControls(Composite editor) {
        /*
         * The public checkbox is put first in the editor since name checking rules are not the same
         * for public and private synonyms.
         */

        // This empty label is used to shift the "Public" checkbox to the right column.
        new Label(editor, SWT.NONE);

        publicCheck = new CheckboxEditor(editor, DBOMUIMessages.getString("synonym.editor.public"),
                1, false, this, ChangeEvent.CUSTOM_3, SWT.CHECK);
    }

    @Override
    public void refreshConnector() {
        super.refreshConnector();

        // Setting fields values according to model values.
        IOracleSynonym synonym = getModel();
        publicCheck.setSelection(synonym.isPublic());
        refDbObjDbLinkNameField.setText(notNull(synonym.getRefDbObjDbLinkName()));

        // Enabling/Disabling fields edition according to the synonym lock status.
        boolean isCheckedOut = !synonym.updatesLocked();
        publicCheck.getControl().setEnabled(isCheckedOut);
        refDbObjDbLinkNameField.getText().setEnabled(isCheckedOut);
    }

    @Override
    public void handleEvent(ChangeEvent event, IObservable source, Object data) {
        IOracleSynonym synonym = getModel();
        switch (event) {
            case CUSTOM_3:
                synonym.setPublic((Boolean)data);
                break;
            case CUSTOM_4:
                synonym.setRefDbObjDbLinkName((String)data);
                break;
        }

        super.handleEvent(event, source, data);
    }

}
