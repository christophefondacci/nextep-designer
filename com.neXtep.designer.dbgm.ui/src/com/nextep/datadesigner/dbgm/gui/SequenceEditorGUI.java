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

import java.math.BigDecimal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.editors.CheckBoxEditor;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 *
 */
public class SequenceEditorGUI extends ControlledDisplayConnector {
	private static final Log log = LogFactory.getLog(SequenceEditorGUI.class);
	private Composite editor = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Label nameLabel = null;
	private Text nameText = null;
	private Label descLabel = null;
	private Text descText = null;
	private Label startLabel = null;
	private Text startText = null;
	private Label incrementLabel = null;
	private Text incrementText = null;
	private Label minLabel = null;
	private Text minText = null;
	private Label maxLabel = null;
	private Text maxText = null;
	private Button cachedCheck = null;
	private Label cacheSizeLabel = null;
	private Text cacheSizeText = null;
	private Button orderedCheck = null;
	private Button cycleCheck = null;
	private Label attributesLabel = null;

	public SequenceEditorGUI(ISequence sequence, ITypedObjectUIController controller) {
		super(sequence,controller);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent,SWT.NONE);
		GridLayout editorLayout = new GridLayout(5,true);
		editor.setLayout(editorLayout);

		nameLabel = new Label(editor, SWT.RIGHT);
		nameLabel.setText("Name : ");
		nameLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		nameText = new Text(editor, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,4,1));
		ColorFocusListener.handle(nameText);
		TextEditor.handle(nameText, ChangeEvent.NAME_CHANGED, this);
		descLabel = new Label(editor, SWT.RIGHT);
		descLabel.setText("Description : ");
		descLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		descText = new Text(editor, SWT.BORDER);
		descText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,4,1));
		TextEditor.handle(descText, ChangeEvent.DESCRIPTION_CHANGED,this);
		ColorFocusListener.handle(descText);
		startLabel = new Label(editor, SWT.RIGHT);
		startLabel.setText("Start with : ");
		startLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		startText = new Text(editor, SWT.BORDER);
		startText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		TextEditor.handle(startText,ChangeEvent.START_CHANGED,this);
		ColorFocusListener.handle(startText);
		incrementLabel = new Label(editor, SWT.RIGHT);
		incrementLabel.setText("Increment by : ");
		incrementLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		incrementText = new Text(editor, SWT.BORDER);
		incrementText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		TextEditor.handle(incrementText,ChangeEvent.INCREMENT_CHANGED,this);
		ColorFocusListener.handle(incrementText);
		new Label(editor, SWT.NONE);
		minLabel = new Label(editor, SWT.RIGHT);
		minLabel.setText("Minimum : ");
		minLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		minText = new Text(editor, SWT.BORDER);
		minText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		TextEditor.handle(minText,ChangeEvent.MIN_CHANGED,this);
		ColorFocusListener.handle(minText);
		maxLabel = new Label(editor, SWT.RIGHT);
		maxLabel.setText("Maximum : ");
		maxLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		maxText = new Text(editor, SWT.BORDER);
		maxText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		TextEditor.handle(maxText,ChangeEvent.MAX_CHANGED,this);
		ColorFocusListener.handle(maxText);
		new Label(editor, SWT.NONE);
		attributesLabel = new Label(editor, SWT.NONE);
		attributesLabel.setText("Sequence attributes : ");
		attributesLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false,5,1));
		new Label(editor,SWT.NONE);
		cachedCheck = new Button(editor, SWT.CHECK);
		cachedCheck.setText("Cached");
		CheckBoxEditor.handle(cachedCheck, ChangeEvent.CACHED_CHANGED, this);

		cacheSizeLabel = new Label(editor,SWT.RIGHT);
		cacheSizeLabel.setText("Cache size : ");
		cacheSizeLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
		cacheSizeText = new Text(editor,SWT.BORDER);
		cacheSizeText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		TextEditor.handle(cacheSizeText, ChangeEvent.CACHE_SIZE_CHANGED, this);
		ColorFocusListener.handle(cacheSizeText);
		new Label(editor, SWT.NONE);
		new Label(editor,SWT.NONE);
		orderedCheck = new Button(editor, SWT.CHECK);
		orderedCheck.setText("Ordered");
		CheckBoxEditor.handle(orderedCheck, ChangeEvent.ORDERED_CHANGED, this);
		new Label(editor, SWT.NONE);
		new Label(editor, SWT.NONE);
		new Label(editor, SWT.NONE);
		new Label(editor,SWT.NONE);
		cycleCheck = new Button(editor, SWT.CHECK);
		cycleCheck.setText("Cycle");
		CheckBoxEditor.handle(cycleCheck, ChangeEvent.CYCLE_CHANGED, this);
		return editor;
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
		ISequence sequence = (ISequence)getModel();
		nameText.setText(notNull(sequence.getName()));
		descText.setText(notNull(sequence.getDescription()));
		startText.setText(strVal(sequence.getStart()));
		incrementText.setText(strVal(sequence.getIncrement()));
		minText.setText(strVal(sequence.getMinValue()));
		maxText.setText(strVal(sequence.getMaxValue()));
		cachedCheck.setSelection( sequence.isCached() == null ? false : sequence.isCached());
		cacheSizeText.setText(strVal(sequence.getCacheSize()));
		if(!sequence.updatesLocked() && cachedCheck.getSelection()) {
			cacheSizeText.setEnabled(true);
		} else {
			cacheSizeText.setEnabled(false);
		}
		orderedCheck.setSelection( sequence.isOrdered());
		cycleCheck.setSelection( sequence.isCycle());

		// Enabling / disabling controls
		boolean l = sequence.updatesLocked();
		nameText.setEnabled(!l);
		descText.setEnabled(!l);
		startText.setEnabled(!l);
		incrementText.setEnabled(!l);
		minText.setEnabled(!l);
		maxText.setEnabled(!l);
		cachedCheck.setEnabled(!l);
		orderedCheck.setEnabled(!l);
		cycleCheck.setEnabled(!l);
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		ISequence seq = (ISequence)getModel();
		switch(event) {
		case NAME_CHANGED:
			seq.setName((String)data);
			break;
		case DESCRIPTION_CHANGED:
			seq.setDescription((String)data);
			break;
		case START_CHANGED:
			seq.setStart(getBigDecimal((String)data));
			break;
		case INCREMENT_CHANGED:
			seq.setIncrement(getLong((String)data));
			break;
		case MIN_CHANGED:
			seq.setMinValue(getBigDecimal((String)data));
			break;
		case MAX_CHANGED:
			seq.setMaxValue(getBigDecimal((String)data));
			break;
		case CACHED_CHANGED:
			seq.setCached((Boolean)data);
			break;
		case CYCLE_CHANGED:
			seq.setCycle((Boolean)data);
			break;
		case ORDERED_CHANGED:
			seq.setOrdered((Boolean)data);
			break;
		case CACHE_SIZE_CHANGED:
			if(data == null || (data!=null && "".equals(((String)data).trim()))) {
				seq.setCacheSize(0);
			} else {
				try {
					seq.setCacheSize(Integer.valueOf((String)data));
				} catch(NumberFormatException e) {
					log.error("Invalid number value");
					seq.setCacheSize(0);
					refreshConnector();
				}
			}


		}
		refreshConnector();

	}

	/**
	 * A safe long parser
	 *
	 * @param s string to convert to a Long value
	 * @return a null or long value
	 * @throws ErrorException if we cannot convert the string to a long
	 */
	private Long getLong(String s) {
		if(s == null || (s!= null && "".equals(s.trim()))) {
			return null;
		} else {
			try {
				Long l = Long.valueOf(s);
				return l;
			} catch( NumberFormatException e) {
				refreshConnector();
				throw new ErrorException("Invalid number value");
			}
		}
	}
	private BigDecimal getBigDecimal(String s) {
		if(s == null || (s!= null && "".equals(s.trim()))) {
			return null;
		} else {
			try {
				BigDecimal bd = new BigDecimal(s);
				return bd;
			} catch( NumberFormatException e) {
				refreshConnector();
				throw new ErrorException("Invalid number value");
			}
		}
	}
}

