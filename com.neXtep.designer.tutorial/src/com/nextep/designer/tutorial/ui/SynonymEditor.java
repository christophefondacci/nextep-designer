package com.nextep.designer.tutorial.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.nextep.datadesigner.ctrl.IGenericController;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.tutorial.model.ISynonym;

public class SynonymEditor extends ControlledDisplayConnector {

	private FieldEditor nameEditor;
	private FieldEditor synonymedEditor;
	private Composite editor;
	
	public SynonymEditor(ISynonym synonym, IGenericController controller) {
		super(synonym, controller);
	}
	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent,SWT.NONE);
		editor.setLayout(new GridLayout(2,false));
		nameEditor = new FieldEditor(editor, "Name : ",1,1,true,this,ChangeEvent.NAME_CHANGED);
		synonymedEditor = new FieldEditor(editor, "Synonym to : ",1,1,true,this,ChangeEvent.CUSTOM_1);
		
		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		ISynonym synonym = (ISynonym)getModel();
		nameEditor.setText(synonym.getName());
		synonymedEditor.setText(notNull(synonym.getSynonymedObject()));

		final boolean enabled = !synonym.updatesLocked();
		synonymedEditor.getText().setEnabled(enabled);
		nameEditor.getText().setEnabled(enabled);
	}
	
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		ISynonym synonym = (ISynonym)getModel();
		switch(event) {
		case CUSTOM_1:
			synonym.setSynonymedObject((String)data);
			break;
		}
		super.handleEvent(event, source, data);
	}

}
