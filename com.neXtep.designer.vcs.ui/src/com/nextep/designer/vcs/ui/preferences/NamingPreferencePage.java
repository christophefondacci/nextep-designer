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
package com.nextep.designer.vcs.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.vcs.impl.NamingPattern;
import com.nextep.datadesigner.vcs.services.NamingService;
import com.nextep.designer.vcs.model.INamingPattern;
import com.nextep.designer.vcs.model.INamingVariableProvider;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog.<br>
 * <p>
 * This specific preference page is persisted in
 * the neXtep repository because its content should
 * affect all neXtep clients.
 * </p>
 * <p>
 * It allows to define naming patterns for any 
 * element type of the repository.
 * </p>
 */

public class NamingPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	public List<IElementType> sortedTypes = null;
	public NamingPreferencePage() {
		super("Name patterns");
		// Listing, filtering and sorting types
		sortedTypes = new ArrayList<IElementType>();
		for(IElementType t : IElementType.values()) {
			if(t.getInterface()!=null && INamedObject.class.isAssignableFrom(t.getInterface())) {
				sortedTypes.add(t);
			}
		}
		Collections.sort(sortedTypes, NameComparator.getInstance());
		
		setPreferenceStore(VCSUIPlugin.getDefault().getPreferenceStore());
		setDescription(VCSUIMessages.getString("namePatternsPrefPageDesc"));
	}
	
	private static class VarProvider implements IStructuredContentProvider {
		private Collection<INamingVariableProvider> varProviders;
		@Override
		public void dispose() {}

		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			varProviders = new ArrayList<INamingVariableProvider>();
			if(newInput instanceof Collection) {
				// We ensure that one variable name is only added once
				// (a same variable name could be provided by multiple provider 
				// for different element types
				List<String> varNames = new ArrayList<String>();
				for(INamingVariableProvider p : (Collection<INamingVariableProvider>)newInput) {
					if(!varNames.contains(p.getVariableName())) {
						varProviders.add(p);
						varNames.add(p.getVariableName());
					}
				}
			}
		}

		@Override
		public Object[] getElements(Object inputElement) {
			
			return varProviders.toArray();
		}
	}
	private static class VarLabelProvider implements ITableLabelProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			final INamingVariableProvider var = (INamingVariableProvider)element;
			switch(columnIndex) {
			case 0:
				return var.getVariableName();
			case 1:
				return var.getDescription();
			}
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {}
		@Override
		public void dispose() {}
		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		@Override
		public void removeListener(ILabelProviderListener listener) {}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite editor = new Composite(parent,SWT.NONE);
		GridLayout l = new GridLayout(2,false);
		l.marginBottom=l.marginHeight=l.marginLeft=l.marginRight=l.marginTop
			=l.marginWidth=0;
		editor.setLayout(l);
		Label varDesc = new Label(editor,SWT.NONE);
		varDesc.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,2,1));
		varDesc.setText(VCSUIMessages.getString("namePatternsPrefPageVars"));
		Table t = new Table(editor,SWT.BORDER|SWT.FULL_SELECTION);
		t.setLinesVisible(true);
		t.setHeaderVisible(true);
		TableColumn varCol = new TableColumn(t,SWT.NONE);
		varCol.setText("Variable");
		varCol.setWidth(150);
		TableColumn descCol = new TableColumn(t,SWT.NONE);
		descCol.setText("Description");
		descCol.setWidth(300);
		GridData gd1 = new GridData(SWT.FILL,SWT.FILL,true,false,2,1);
		gd1.heightHint=120;
		t.setLayoutData(gd1);
		TableViewer v = new TableViewer(t);
		v.setContentProvider(new VarProvider());
		v.setLabelProvider(new VarLabelProvider());
		v.setInput(NamingService.getInstance().listProviders());
		
		// Naming pattern section
		Label patternLbl = new Label(editor,SWT.NONE);
		patternLbl.setText(VCSUIMessages.getString("namePatternsPrefPagePatterns"));
		patternLbl.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,2,1));
		Table patternTab = new Table(editor,SWT.BORDER | SWT.FULL_SELECTION);
		patternTab.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,1,3));
		patternTab.setLinesVisible(true);
		patternTab.setHeaderVisible(true);
		TableColumn typeCol = new TableColumn(patternTab, SWT.NONE);
		typeCol.setText("Element");
		typeCol.setWidth(150);
		TableColumn patternCol = new TableColumn(patternTab, SWT.NONE);
		patternCol.setText("Naming pattern");
		patternCol.setWidth(300);
		final TableViewer patViewer = new TableViewer(patternTab);
		final NamingPatternContentProvider contentProvider = new NamingPatternContentProvider(patViewer);
		patViewer.setContentProvider(contentProvider);
		patViewer.setLabelProvider(new NamingPatternLabelProvider());
		
		// Building editors
		CellEditor[] cellEditors = new CellEditor[2];
		List<String> types = new ArrayList<String>();
		for(IElementType typ : sortedTypes) {
			types.add(typ.getName());
		}
		ComboBoxCellEditor typeEditor = new ComboBoxCellEditor(patternTab,types.toArray(new String[types.size()]),SWT.READ_ONLY);
		cellEditors[0] = typeEditor;
		TextCellEditor patternEditor = new TextCellEditor(patternTab);
		cellEditors[1] = patternEditor;
		patViewer.setCellEditors(cellEditors);
		patViewer.setColumnProperties(new String [] { "TYPE", "PATTERN" });
		patViewer.setCellModifier(new ICellModifier() {

			@Override
			public boolean canModify(Object element, String property) {
				return "TYPE".equals(property) || "PATTERN".equals(property);
			}

			@Override
			public Object getValue(Object element, String property) {
				final INamingPattern p = (INamingPattern)element;
				if("TYPE".equals(property)) {
					return sortedTypes.indexOf(p.getRelatedType());
				} else if("PATTERN".equals(property)) {
					return notNull(p.getPattern());
				}
				return null;
			}

			@Override
			public void modify(Object element, String property, Object value) {
				final INamingPattern p = (INamingPattern)((TableItem)element).getData();
				if("TYPE".equals(property)) {
					p.setRelatedType(sortedTypes.get((Integer)value));
				} else if("PATTERN".equals(property)) {
					p.setPattern((String)value);
				}

			}
			
		});
		patViewer.setInput(NamingService.getInstance().listPatterns());
		
		//Building add / remove buttons
		final Button addButton = new Button(editor,SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				INamingPattern p = new NamingPattern();
				p.setRelatedType(IElementType.getInstance(IVersionContainer.TYPE_ID));
				contentProvider.add(p);
			}
		});
		final Button remButton = new Button(editor,SWT.PUSH);
		remButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		remButton.setText("Remove");
		remButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection s = patViewer.getSelection();
				if(s instanceof IStructuredSelection) {
					INamingPattern p = (INamingPattern)((IStructuredSelection)s).getFirstElement();
					if(p!=null) {
						contentProvider.remove(p);
					}
				}
			}
		});	
		return editor;
	}
	private String notNull(String s) {
		return s==null?"":s;
	}
}