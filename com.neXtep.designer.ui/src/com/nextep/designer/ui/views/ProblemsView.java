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
package com.nextep.designer.ui.views;

import java.util.Collections;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.gui.impl.swt.TableColumnSorter;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.markers.MarkersContentProvider;
import com.nextep.designer.ui.markers.MarkersLabelProvider;
import com.nextep.designer.ui.markers.MarkersTable;

public class ProblemsView extends ViewPart {

	public static final String VIEW_ID = "com.neXtep.designer.ui.markersView"; //$NON-NLS-1$
	private Table markersTab;
	private TableViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
		markersTab = MarkersTable.create(parent);
		viewer = new TableViewer(markersTab);
		viewer.setContentProvider(new MarkersContentProvider());
		viewer.setLabelProvider(new MarkersLabelProvider());
		viewer.setComparator(new TableColumnSorter(markersTab, viewer));
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection s = event.getSelection();
				if (s instanceof IStructuredSelection) {
					IMarker m = (IMarker) ((IStructuredSelection) s).getFirstElement();
					if (m.getRelatedObject() instanceof ITypedObject) {
						try {
							UIControllerFactory.getController(m.getRelatedObject()).defaultOpen(
									(ITypedObject) m.getRelatedObject());
						} catch (CancelException e) {
							// Doing nothing, this is standard
						}
					}
				}
			}
		});
		viewer.setInput(Collections.emptyList());
	}

	@Override
	public void setFocus() {
		markersTab.setFocus();
		viewer.refresh();
	}

}
