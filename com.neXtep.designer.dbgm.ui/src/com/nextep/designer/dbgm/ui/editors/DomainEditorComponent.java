/*******************************************************************************
 * Copyright (c) 2013 neXtep Software and contributors.
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
 * along with neXtep.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.ui.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.model.IDomain;
import com.nextep.datadesigner.dbgm.model.IDomainVendorType;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.services.IDatatypeService;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.jface.DomainContentProvider;
import com.nextep.designer.dbgm.ui.jface.DomainLabelProvider;
import com.nextep.designer.dbgm.ui.jface.DomainTable;
import com.nextep.designer.dbgm.ui.jface.DomainTypeContentProvider;
import com.nextep.designer.dbgm.ui.jface.DomainTypeLabelProvider;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.base.AbstractUIComponent;

/**
 * @author Christophe Fondacci
 * 
 */
public class DomainEditorComponent extends AbstractUIComponent implements ITitleAreaComponent {
	private static final String COL_SEL = "SEL"; //$NON-NLS-1$
	private static final String COL_NAME = "NAME"; //$NON-NLS-1$
	private static final String COL_DESC = "DESC"; //$NON-NLS-1$
	private static final String COL_LENGTH = "LENGTH"; //$NON-NLS-1$
	private static final String COL_PRECISION = "PRECISION"; //$NON-NLS-1$

	private static final String COL_VENDOR = "VENDOR"; //$NON-NLS-1$
	private static final String COL_TYPE = "TYPE"; //$NON-NLS-1$

	private final List<String> sortedVendors;
	private TableViewer domainViewer;

	public DomainEditorComponent() {
		sortedVendors = new ArrayList<String>();
		for (DBVendor v : DBVendor.values()) {
			if (v != DBVendor.JDBC) {
				sortedVendors.add(v.toString());
			}
		}
		Collections.sort(sortedVendors);
	}

	@Override
	public Control create(Composite parent) {
		final Composite editor = new Composite(parent, SWT.NONE);
		GridLayout l = new GridLayout(2, false);
		l.marginBottom = l.marginHeight = l.marginLeft = l.marginRight = l.marginTop = l.marginWidth = 0;
		editor.setLayout(l);

		Table t = DomainTable.create(editor);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2);
		gd.heightHint = 250;
		t.setLayoutData(gd);

		domainViewer = new TableViewer(t);
		final DomainContentProvider provider = new DomainContentProvider();
		domainViewer.setContentProvider(provider);
		domainViewer.setLabelProvider(new DomainLabelProvider());

		CellEditor[] editors = new CellEditor[5];
		editors[1] = new TextCellEditor(t);
		editors[2] = new TextCellEditor(t);
		editors[3] = new TextCellEditor(t);
		editors[4] = new TextCellEditor(t);
		domainViewer.setCellEditors(editors);
		domainViewer.setColumnProperties(new String[] { COL_SEL, COL_NAME, COL_DESC, COL_LENGTH,
				COL_PRECISION });
		domainViewer.setSorter(new ViewerSorter());
		domainViewer.setCellModifier(new ICellModifier() {

			@Override
			public boolean canModify(Object element, String property) {
				return !COL_SEL.equals(property);
			}

			@Override
			public Object getValue(Object element, String property) {
				final IDomain d = (IDomain) element;
				if (COL_NAME.equals(property)) {
					return notNull(d.getName());
				}
				if (COL_DESC.equals(property)) {
					return notNull(d.getDescription());
				}
				if (COL_LENGTH.equals(property)) {
					return notNull(d.getLengthExpr());
				}
				if (COL_PRECISION.equals(property)) {
					return notNull(d.getPrecisionExpr());
				}
				return null;
			}

			@Override
			public void modify(Object element, String property, Object value) {
				final IDomain d = (IDomain) ((TableItem) element).getData();
				if (COL_NAME.equals(property)) {
					d.setName((String) value);
				} else if (COL_DESC.equals(property)) {
					d.setDescription((String) value);
				} else if (COL_LENGTH.equals(property)) {
					d.setLengthExpr((String) value);
				} else if (COL_PRECISION.equals(property)) {
					d.setPrecisionExpr((String) value);
				}
			}
		});

		Button addButton = new Button(editor, SWT.PUSH);
		addButton.setText(UIMessages.getString("component.typedListBlock.add")); //$NON-NLS-1$
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IDomain d = (IDomain) UIControllerFactory.getController(
						IElementType.getInstance(IDomain.TYPE_ID)).newInstance(null);
				provider.add(d);
				domainViewer.editElement(d, 1);
			}
		});
		Button removeButton = new Button(editor, SWT.PUSH);
		removeButton.setText(UIMessages.getString("component.typedListBlock.remove")); //$NON-NLS-1$
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false));
		removeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection s = domainViewer.getSelection();
				if (s instanceof IStructuredSelection) {
					for (Object o : ((IStructuredSelection) s).toList()) {
						final IDomain d = (IDomain) o;
						// Removing vendor types
						for (IDomainVendorType t : new ArrayList<IDomainVendorType>(d
								.getVendorTypes())) {
							d.removeVendorType(t);
						}
						CorePlugin.getIdentifiableDao().delete(d);
						// Removing domain
						domainViewer.remove(d);
					}
				}
			}
		});

		final IDatatypeService datatypeService = CorePlugin.getService(IDatatypeService.class);
		datatypeService.reset();
		final Collection<IDomain> domains = datatypeService.getDomains();
		domainViewer.setInput(domains);

		// Domain vendor type section
		final Table typeTab = DomainTable.createDomainType(editor);
		final TableViewer typeViewer = new TableViewer(typeTab);
		GridData typeData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		typeData.heightHint = 200;
		typeTab.setLayoutData(typeData);
		Button addTypeButton = new Button(editor, SWT.PUSH);
		addTypeButton.setText(UIMessages.getString("component.typedListBlock.add")); //$NON-NLS-1$
		addTypeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addTypeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IDomain domain = getSelectedDomain();
				if (domain == null) {
					MessageDialog.openWarning(editor.getShell(),
							DBGMUIMessages.getString("domainPrefNoSelectionTitle"), //$NON-NLS-1$
							DBGMUIMessages.getString("domainPrefNoSelection")); //$NON-NLS-1$
					return;
				}
				final Object o = UIControllerFactory.getController(
						IElementType.getInstance(IDomainVendorType.TYPE_ID)).emptyInstance(null,
						domain);
				typeViewer.editElement(o, 0);
			}
		});
		Button removeTypeButton = new Button(editor, SWT.PUSH);
		removeTypeButton.setText(UIMessages.getString("component.typedListBlock.remove")); //$NON-NLS-1$
		removeTypeButton.setLayoutData(new GridData(SWT.FILL, SWT.UP, false, false));

		typeViewer.setContentProvider(new DomainTypeContentProvider());
		typeViewer.setLabelProvider(new DomainTypeLabelProvider());
		typeViewer.setColumnProperties(new String[] { COL_VENDOR, COL_TYPE, COL_LENGTH,
				COL_PRECISION });
		CellEditor[] typeEditors = new CellEditor[4];

		typeEditors[0] = new ComboBoxCellEditor(typeTab,
				sortedVendors.toArray(new String[sortedVendors.size()]), SWT.READ_ONLY);
		typeEditors[1] = new TextCellEditor(typeTab);
		typeEditors[2] = new TextCellEditor(typeTab);
		typeEditors[3] = new TextCellEditor(typeTab);
		typeViewer.setCellEditors(typeEditors);
		typeViewer.setCellModifier(new ICellModifier() {

			@Override
			public boolean canModify(Object element, String property) {
				return true;
			}

			@Override
			public Object getValue(Object element, String property) {
				IDomainVendorType t = (IDomainVendorType) element;
				if (COL_VENDOR.equals(property)) {
					if (t.getDBVendor() != null) {
						return sortedVendors.indexOf(t.getDBVendor().toString());
					} else {
						return 0;
					}
				} else if (COL_TYPE.equals(property)) {
					if (t.getDatatype() != null) {
						return notNull(t.getDatatype().getName());
					} else {
						return ""; //$NON-NLS-1$
					}
				} else if (COL_LENGTH.equals(property)) {
					if (t.getDatatype() != null && t.getDatatype().getLength() != null) {
						return String.valueOf(t.getDatatype().getLength());
					} else {
						return ""; //$NON-NLS-1$
					}
				} else if (COL_PRECISION.equals(property)) {
					if (t.getDatatype() != null && t.getDatatype().getPrecision() != null) {
						return String.valueOf(t.getDatatype().getPrecision());
					} else {
						return ""; //$NON-NLS-1$
					}
				}
				return null;
			}

			@Override
			public void modify(Object element, String property, Object value) {
				IDomainVendorType t = (IDomainVendorType) ((TableItem) element).getData();
				if (COL_VENDOR.equals(property)) {
					String val = sortedVendors.get((Integer) value);
					for (DBVendor v : DBVendor.values()) {
						if (v.toString().equals(val)) {
							t.setDBVendor(v);
						}
					}
				} else if (COL_TYPE.equals(property)) {
					if (t.getDatatype() == null) {
						t.setDatatype(Datatype.getDefaultDatatype());
					}
					t.getDatatype().setName((String) value);
					t.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				} else if (COL_LENGTH.equals(property)) {
					try {
						if (value != null && !"".equals(((String) value).trim())) { //$NON-NLS-1$
							t.getDatatype().setLength(Integer.valueOf((String) value));
						} else {
							t.getDatatype().setLength(null);
						}
					} catch (NumberFormatException e) {
						t.getDatatype().setLength(null);
					}
					t.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				} else if (COL_PRECISION.equals(property)) {
					try {
						if (value != null && !"".equals(((String) value).trim())) { //$NON-NLS-1$
							t.getDatatype().setPrecision(Integer.valueOf((String) value));
						} else {
							t.getDatatype().setPrecision(null);
						}
					} catch (NumberFormatException e) {
						t.getDatatype().setPrecision(null);
					}
					t.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				}
			}
		});

		domainViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					final IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					if (!sel.isEmpty()) {
						typeViewer.setInput(sel.getFirstElement());
					} else {
						typeViewer.setInput(null);
					}
				}
			}
		});
		removeTypeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection s = typeViewer.getSelection();
				if (s instanceof IStructuredSelection) {
					final IStructuredSelection sel = (IStructuredSelection) s;
					if (!sel.isEmpty()) {
						for (Object o : sel.toList()) {
							if (o instanceof IDomainVendorType) {
								final IDomainVendorType vt = (IDomainVendorType) o;
								vt.getDomain().removeVendorType(vt);
							}
						}
					}
				}
			}
		});
		return editor;
	}

	@Override
	public String getAreaTitle() {
		return DBGMUIMessages.getString("domainPrefTitle"); //$NON-NLS-1$
	}

	@Override
	public String getDescription() {
		return DBGMUIMessages.getString("domainPrefDesc"); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return DBGMImages.WIZARD_TYPE;
	}

	/**
	 * @return teh currently selected domain, or <code>null</code> if no
	 *         selection
	 */
	private IDomain getSelectedDomain() {
		if (domainViewer.getSelection() instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) domainViewer.getSelection();
			if (sel.isEmpty()) {
				return null;
			} else {
				return (IDomain) sel.getFirstElement();
			}
		}
		return null;
	}

	private String notNull(String s) {
		return s == null ? "" : s.trim(); //$NON-NLS-1$
	}
}
