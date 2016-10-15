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
package com.nextep.designer.synch.ui.navigators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.synch.ui.SynchUIPlugin;
import com.nextep.designer.synch.ui.decorators.SynchronizationDecorator;
import com.nextep.designer.synch.ui.jface.ComparisonItemFilter;
import com.nextep.designer.synch.ui.services.ISynchronizationUIService;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.ui.jface.TextFilter;

public class ComparisonNavigator extends CommonNavigator {

	public final static String VIEW_ID = "com.neXtep.designer.synch.ui.SynchronizationView"; //$NON-NLS-1$
	// De-activated because buggy look'n feel on Windows platforms
	// private final Cursor cursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
	private final static Log LOGGER = LogFactory.getLog(ComparisonNavigator.class);
	private ISynchronizationUIService synchronizationService;
	private Text filterText;
	/*
	 * This field is used as a flag to register when an Expand or Collapse event happened, meaning
	 * the user has clicked on the arrow to the left of a tree item to expand or collapse its
	 * contents. There seems to be a bug in Eclipse on Windows platform which notifies the
	 * EventListeners about the Expand or Collapse events BEFORE the MouseDown event. As a result,
	 * when the expanding or collapsing action causes a window scroll, the following MouseDown event
	 * happens on whatever item is pointed out by the cursor after the window scroll. When this case
	 * happens, we need to ignore the MouseDown and MouseUp events, because the cursor position is
	 * no longer the position it was when the user clicked.
	 */
	private boolean arrowClicked = false;

	public ComparisonNavigator() {
		synchronizationService = SynchUIPlugin.getService(ISynchronizationUIService.class);
	}

	@Override
	protected Object getInitialInput() {
		return ComparisonNavigatorRoot.getInstance();
	}

	@Override
	protected CommonViewer createCommonViewer(Composite aParent) {
		final CommonViewer viewer = super.createCommonViewer(aParent);
		ColumnViewerToolTipSupport.enableFor(viewer);
		final Tree tree = viewer.getTree();
		tree.setLinesVisible(true);
		SynchronizationDecorator.handle(tree);

		tree.addMouseListener(new MouseListener() {

			private int x, y;

			@Override
			public void mouseUp(MouseEvent e) {
				if (x > 1 && x < 16 && e.getSource() instanceof Tree) {
					final Tree tree = (Tree) e.getSource();
					TreeItem item = tree.getItem(new Point(x, y));
					// Linux workaround (cause the getItem() will return null)
					if (item == null) {
						// On linux, item is always selected first
						if (tree.getSelectionCount() > 0) {
							final TreeItem selItem = tree.getSelection()[tree.getSelectionCount() - 1];
							final Rectangle bounds = selItem.getBounds();
							if (y >= bounds.y && y <= bounds.y + bounds.height) {
								item = selItem;
							}
						}
					}
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("[SynchronizationView][MouseUp] clicked item: " + item); //$NON-NLS-1$
					}

					/*
					 * If the arrowClicked flag is true, the mouseUp event must be ignored because
					 * the user clicked on a tree arrow, not on a comparison icon.
					 */
					if (item != null && !arrowClicked) {
						final Object o = item.getData();
						if (o instanceof IComparisonItem) {
							final IComparisonItem compItem = (IComparisonItem) o;
							if (compItem.getDifferenceType() != DifferenceType.EQUALS) {
								if (compItem.getTarget() == compItem.getMergeInfo()
										.getMergeProposal()) {
									synchronizationService.selectProposal(compItem,
											ComparedElement.SOURCE);
									synchronizationService.adjustParents(compItem.getParent());
									if (compItem.getParent() != null) {
										compItem.getParent().getMergeInfo().setMergeProposal(null);
									}
								} else {
									synchronizationService.selectProposal(compItem,
											ComparedElement.TARGET);
									synchronizationService.adjustParents(compItem.getParent());
								}
							}
							viewer.refresh();
						}
					}
				}
				x = y = -1;

				// The arrowClicked flag is reseted for the next user action.
				arrowClicked = false;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					this.x = e.x;
					this.y = e.y;

					if (LOGGER.isDebugEnabled()) {
						final TreeItem item = tree.getItem(new Point(x, y));
						LOGGER.debug("[SynchronizationView][MouseDown] clicked item: " + item); //$NON-NLS-1$
					}
				}
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		// De-activated because buggy look'n feel on Windows platform
		//
		// tree.addListener(SWT.MouseMove, new Listener() {
		//
		// @Override
		// public void handleEvent(Event event) {
		//
		// if (event.x > 1 && event.x < 16) {
		// final TreeItem item = tree.getItem(new Point(event.x, event.y));
		// if (item != null && item.getData() instanceof IComparisonItem) {
		// if (((IComparisonItem) item.getData()).getDifferenceType() != DifferenceType.EQUALS) {
		// tree.setCursor(cursor);
		// return;
		// }
		// }
		// }
		// tree.setCursor(null);
		// }
		// });
		tree.addListener(SWT.Expand, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (LOGGER.isDebugEnabled()) {
					final TreeItem item = tree.getItem(new Point(event.x, event.y));
					LOGGER.debug("[SynchronizationView][Expand] clicked item: " + item); //$NON-NLS-1$
				}

				// We register the Expand event for the MouseListener.
				arrowClicked = true;
			}
		});
		tree.addListener(SWT.Collapse, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (LOGGER.isDebugEnabled()) {
					final TreeItem item = tree.getItem(new Point(event.x, event.y));
					LOGGER.debug("[SynchronizationView][Collapse] clicked item: " + item); //$NON-NLS-1$
				}

				// We register the Collapse event for the MouseListener.
				arrowClicked = true;
			}
		});
		// tree.addListener(SWT.Dispose, new Listener() {
		//
		// @Override
		// public void handleEvent(Event event) {
		// cursor.dispose();
		// }
		// });
		return viewer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == ISynchronizationResult.class) {
			return ComparisonNavigatorRoot.getInstance().getCurrentSynchronization();
		}
		return super.getAdapter(adapter);
	}

	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		// Tweaking common viewer creation to add a search bar on top
		GridLayout parentLayout = new GridLayout(2, false);
		setNoMarginLayout(parentLayout);
		aParent.setLayout(parentLayout);
		Label lbl = new Label(aParent, SWT.RIGHT);
		lbl.setText(SynchUIMessages.getString("synch.navigator.filter")); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		filterText = new Text(aParent, SWT.SEARCH | SWT.ICON_CANCEL);
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Common viewer creation with specific SWT attributes like FULL_SELECTION
		final CommonViewer viewer = new CommonViewer(getViewSite().getId(), aParent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		// Registering our "static" filter
		TextFilter.handle(viewer, filterText);
		viewer.addFilter(new ComparisonItemFilter());
		return viewer;
	}

	private void setNoMarginLayout(GridLayout l) {
		l.marginBottom = l.marginHeight = l.marginHeight = l.marginLeft = l.marginRight = l.marginWidth = 0;
		l.marginTop = 3;
		l.verticalSpacing = 3;
	}

	public Text getFilterText() {
		return filterText;
	}
}
