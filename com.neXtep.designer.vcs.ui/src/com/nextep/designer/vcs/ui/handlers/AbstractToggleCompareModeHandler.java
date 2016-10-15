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
package com.nextep.designer.vcs.ui.handlers;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.compare.IComparisonEditorProvider;

/**
 * Abstract handler to use as a base implementation when providing toggles for comparison modes.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractToggleCompareModeHandler extends AbstractHandler implements
		IElementUpdater {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		VCSUIPlugin.getComparisonUIManager().setComparisonEditorProvider(
				getComparisonEditorProvider());
		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void updateElement(UIElement element, Map parameters) {
		element.setChecked(VCSUIPlugin.getComparisonUIManager().getComparisonEditorProvider(null) == getComparisonEditorProvider());
	}

	protected abstract IComparisonEditorProvider getComparisonEditorProvider();

}
