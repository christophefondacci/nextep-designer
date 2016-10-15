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
package com.nextep.designer.dbgm.gef;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.dnd.TransferDropTargetListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.impl.DiagramItem;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.layout.DiagramLayoutService;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * A drop target listener that is able to handle dragged elements from standard jface selection and
 * to drop them on GEF diagram.<br>
 * We need to extend the deprecated GEF interface {@link TransferDropTargetListener} because
 * otherwise GEF will not install its drop targets properly (even if they deprecated the interface
 * and say we need to use pure jface interface !)
 * 
 * @author Christophe Fondacci
 */
public class DiagramSelectionDropTargetListener implements TransferDropTargetListener {

	private final static Log log = LogFactory.getLog(DiagramSelectionDropTargetListener.class);
	private GraphicalViewer viewer;
	private IDiagram diagram;

	public DiagramSelectionDropTargetListener(GraphicalViewer viewer, IDiagram diagram) {
		this.viewer = viewer;
		this.diagram = diagram;
	}

	@Override
	public void dragEnter(DropTargetEvent e) {
		log.debug("Drop enter"); //$NON-NLS-1$
		for (int i = 0; i < e.dataTypes.length; i++) {
			if (LocalSelectionTransfer.getTransfer().isSupportedType(e.dataTypes[i])) {
				e.currentDataType = e.dataTypes[i];
			}
		}
	}

	@Override
	public void dragLeave(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragOperationChanged(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragOver(DropTargetEvent e) {
		for (int i = 0; i < e.dataTypes.length; i++) {
			final ISelection s = LocalSelectionTransfer.getTransfer().getSelection();
			if (s instanceof IStructuredSelection && !s.isEmpty()) {
				final IStructuredSelection sel = (IStructuredSelection) s;
				// Only accepting tables or containers
				final Iterator<?> selIt = sel.iterator();
				while (selIt.hasNext()) {
					Object elt = selIt.next();
					// If not a table and not a container we deny the drop action
					if (!(elt instanceof IBasicTable) && !(elt instanceof IVersionContainer)) {
						return;
					}
				}
				// We fall here when we got compatible elements (tables and/or containers)
				e.currentDataType = e.dataTypes[i];
				e.detail = DND.DROP_MOVE;
			}
		}
	}

	/**
	 * Dropping a navigator element to the graph canvas. The data of the drop event is the class
	 * name of the object which is pushed in the application "drag object".
	 * 
	 * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void drop(final DropTargetEvent e) {

		log.debug("Drop"); //$NON-NLS-1$
		final Point widgetAbsPosition = viewer.getControl().toDisplay(
				new org.eclipse.swt.graphics.Point(0, 0));
		if (e.data instanceof IStructuredSelection) {
			final IStructuredSelection s = (IStructuredSelection) e.data;
			final Point targetPoint = new Point(e.x - widgetAbsPosition.x, e.y
					- widgetAbsPosition.y);
			Job j = new Job(DBGMUIMessages.getString("editor.diagram.addingTablesJob")) { //$NON-NLS-1$

				protected IStatus run(IProgressMonitor m) {
					SubMonitor monitor = SubMonitor.convert(m, 100);
					doDrop(s.toList(), targetPoint, monitor);
					return Status.OK_STATUS;
				};
			};
			j.setUser(true);
			j.schedule();
		}
	}

	private void doDrop(List<?> objectsToDrop, Point targetPoint, IProgressMonitor monitor) {
		monitor
				.beginTask(
						DBGMUIMessages.getString("editor.diagram.addingTablesTask"), computeWorkCount(objectsToDrop) + 1); //$NON-NLS-1$
		// Processing selection import into diagram
		boolean suggestAutoReorg = objectsToDrop.size() > 1;
		for (Object o : objectsToDrop) {
			if (o instanceof IBasicTable) {
				final IBasicTable t = (IBasicTable) o;
				monitor.subTask(MessageFormat.format(DBGMUIMessages
						.getString("addTableToDiagramCommand"), t.getName(), diagram.getName())); //$NON-NLS-1$
				addDroppedTable(t, targetPoint);
				monitor.worked(1);
			} else if (o instanceof IVersionContainer) {
				final IVersionContainer container = (IVersionContainer) o;
				Collection<IVersionable<?>> versionables = VersionHelper.getAllVersionables(
						container, IElementType.getInstance(IBasicTable.TYPE_ID));
				for (IVersionable<?> v : versionables) {
					monitor.subTask(MessageFormat.format(DBGMUIMessages
							.getString("addTableToDiagramCommand"), v.getName(), diagram //$NON-NLS-1$
							.getName()));
					addDroppedTable((IBasicTable) v, targetPoint);
					monitor.worked(1);
				}
				suggestAutoReorg = true;
			}
		}
		if (suggestAutoReorg) {
			monitor.subTask(MessageFormat.format(DBGMUIMessages
					.getString("autoLayoutDiagramCommand"), diagram.getName())); //$NON-NLS-1$
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					if (MessageDialog.openQuestion(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(), DBGMUIMessages
							.getString("questionAutoLayoutDiagramTitle"), DBGMUIMessages //$NON-NLS-1$
							.getString("questionAutoLayoutDiagram"))) { //$NON-NLS-1$
						DiagramLayoutService.autoLayout(diagram);
					}

				}
			});
		}

		diagram.notifyListeners(ChangeEvent.MODEL_CHANGED, null);

	}

	private int computeWorkCount(Collection<?> objectsToDrop) {
		int count = 0;
		for (Object o : objectsToDrop) {
			if (o instanceof IVersionContainer) {
				count += computeWorkCount(((IVersionContainer) o).getContents());
			} else if (o instanceof IBasicTable) {
				count++;
			}
		}
		return count;
	}

	private void addDroppedTable(IBasicTable t, Point location) {
		// We do not allow to add several times the same table
		for (IDiagramItem i : diagram.getItems()) {
			if (i.getItemReference() == t.getReference()) {
				log.warn(MessageFormat.format(DBGMUIMessages.getString("tableAlreadyAddedWarning"), //$NON-NLS-1$
						t.getName()));
				return;
			}
		}
		IDiagramItem i = new DiagramItem((IVersionable<?>) t, location.x, location.y);
		i.setHeight(30 + 27 * t.getColumns().size());
		diagram.addItem(i);
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
	}

	@Override
	public Transfer getTransfer() {
		return LocalSelectionTransfer.getTransfer();
	}

	@Override
	public boolean isEnabled(DropTargetEvent event) {
		return true;
	}

}
