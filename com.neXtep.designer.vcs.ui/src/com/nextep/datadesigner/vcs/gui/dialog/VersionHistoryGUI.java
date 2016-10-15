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
package com.nextep.datadesigner.vcs.gui.dialog;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.gui.impl.TableDisplayConnector;
import com.nextep.datadesigner.gui.impl.swt.TableColumnSorter;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.jface.VersionHistoryLabelProvider;
import com.nextep.designer.vcs.ui.jface.VersionHistoryTable;
import com.nextep.designer.vcs.ui.jface.VersionInfoContentProvider;

/**
 * @author Christophe Fondacci
 */
public class VersionHistoryGUI extends TableDisplayConnector implements MouseListener {

	private Table historyTable = null;
	private IVersionInfo selection;
	private TableViewer viewer;
	private String title;
	private String description;
	private Image image;

	public VersionHistoryGUI(IVersionable<?> versionable) {
		super(versionable, null);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		GridData gridData4 = new GridData();
		gridData4.horizontalSpan = 1;
		gridData4.verticalAlignment = GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.grabExcessVerticalSpace = true;
		gridData4.horizontalAlignment = GridData.FILL;
		historyTable = VersionHistoryTable.create(parent);
		viewer = new TableViewer(historyTable);
		viewer.setLabelProvider(new VersionHistoryLabelProvider());
		viewer.setContentProvider(new VersionInfoContentProvider());
		viewer.setInput(getModel());
		historyTable.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final ISelection s = viewer.getSelection();
				if (s instanceof IStructuredSelection && !s.isEmpty()) {
					selection = (IVersionInfo) ((IStructuredSelection) s).getFirstElement();
					if (selection.getMajorRelease() == -1) {
						selection = null;
					}
				} else {
					selection = null;
				}
			}

		});
		viewer.setComparator(new TableColumnSorter(historyTable, viewer));
		return historyTable;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return historyTable;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		IVersionable<?> versionable = (IVersionable<?>) getModel();
		viewer.setInput(versionable);
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// Comparing current with selection
		final IVersionable<?> v = (IVersionable<?>) getModel();
		// Selected version
		if (historyTable.getSelection().length > 0) {
			final IVersionInfo fromVersion = (IVersionInfo) historyTable.getSelection()[0]
					.getData();
			IComparisonItem result = (IComparisonItem) CommandProgress
					.runWithProgress(new ICommand() {

						@Override
						public Object execute(Object... parameters) {
							IMerger m = MergerFactory.getMerger(v.getType(),
									ComparisonScope.REPOSITORY);
							return m.compare(v.getReference(), fromVersion, v.getVersion(), true);
						}

						@Override
						public String getName() {
							return "Comparing version information...";
						}
					}).iterator().next();
			Designer.getInstance().invokeSelection("version.compare", result);
		}
	}

	@Override
	public void mouseDown(MouseEvent e) {
	}

	@Override
	public void mouseUp(MouseEvent e) {
	}

	@Override
	public IVersionInfo getSelection() {
		return selection;
	}

	@Override
	public String getAreaTitle() {
		if (title != null) {
			return title;
		}
		return super.getAreaTitle();
	}

	/**
	 * @return the description
	 */
	@Override
	public String getDescription() {
		if (description != null) {
			return description;
		} else {
			return super.getDescription();
		}
	}

	@Override
	public Image getImage() {
		if (image != null) {
			return image;
		} else {
			return super.getImage();
		}
	}

	/**
	 * @param titleKey
	 *            the titleKey to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param descriptionKey
	 *            the descriptionKey to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param image
	 *            the image to set
	 */
	public void setImage(Image image) {
		this.image = image;
	}
}
