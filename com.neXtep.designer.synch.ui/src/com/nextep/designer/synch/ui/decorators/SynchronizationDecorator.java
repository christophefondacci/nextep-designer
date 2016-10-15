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
package com.nextep.designer.synch.ui.decorators;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import com.nextep.designer.synch.ui.SynchUIImages;
import com.nextep.designer.vcs.model.IComparisonItem;

public class SynchronizationDecorator implements Listener {

	final static Image NOT_IN_TARGET = SynchUIImages.NOT_IN_TARGET;
	final static Image NOT_IN_TARGET_DIS = SynchUIImages.NOT_IN_TARGET_DIS;
	final static Image NOT_IN_SOURCE = SynchUIImages.NOT_IN_SOURCE;
	final static Image NOT_IN_SOURCE_DIS = SynchUIImages.NOT_IN_SOURCE_DIS;
	final static Image DIFF = SynchUIImages.DIFF;
	final static Image DIFF_DIS = SynchUIImages.DIFF_DIS;

	public static void handle(Tree tree) {
		tree.addListener(SWT.PaintItem, new SynchronizationDecorator());
	}

	@Override
	public void handleEvent(Event event) {
		final Object data = event.item.getData();
		if (data instanceof IComparisonItem) {
			final IComparisonItem item = (IComparisonItem) data;
			switch (item.getDifferenceType()) {
			case MISSING_TARGET:
				drawImage(event, NOT_IN_TARGET, NOT_IN_TARGET_DIS, item);
				break;
			case DIFFER:
				drawImage(event, DIFF, DIFF_DIS, item);
				break;
			case MISSING_SOURCE:
				drawImage(event, NOT_IN_SOURCE, NOT_IN_SOURCE_DIS, item);
				break;
			}
		}
	}

	public void drawImage(Event event, Image image, Image disabledImage, IComparisonItem item) {
		final Rectangle rect = image.getBounds();
		final int offset = Math.max(0, (event.height - rect.height) / 2);
		if (item.getMergeInfo().getMergeProposal() == item.getTarget()) {
			event.gc.drawImage(disabledImage, 1, event.y + offset);
		} else {
			event.gc.drawImage(image, 1, event.y + offset);
		}

	}
}
