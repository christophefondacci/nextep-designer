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
package com.nextep.datadesigner.gui.impl.rcp;

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * This abstract class is a convenient way of registering some processing upon page activation. This
 * way, plugin can register this listener when they start and they will get notified when the
 * workbench page opens.
 * 
 * @author Christophe Fondacci
 */
public abstract class PageTracker implements IWindowListener, IPageListener {

	@Override
	public final void windowActivated(IWorkbenchWindow window) {
	}

	@Override
	public final void windowClosed(IWorkbenchWindow window) {
		window.removePageListener(this);
	}

	@Override
	public final void windowDeactivated(IWorkbenchWindow window) {
	}

	@Override
	public final void windowOpened(IWorkbenchWindow window) {
		window.addPageListener(this);
		if(window.getActivePage()!=null) {
			this.pageOpened(window.getActivePage());
		}
	}

	@Override
	public void pageActivated(IWorkbenchPage page) {
	}
}
