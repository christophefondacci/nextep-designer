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
/**
 *
 */
package com.nextep.designer.dbgm.gef;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.internal.InternalImages;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.AlignmentRetargetAction;
import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightRetargetAction;
import org.eclipse.gef.ui.actions.MatchWidthRetargetAction;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import com.nextep.designer.dbgm.ui.DBGMImages;

/**
 * @author Christophe Fondacci
 *
 */
public class DBGMGraphicalActionBarContributor extends ActionBarContributor {

	/**
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#buildActions()
	 */
	@Override
	protected void buildActions() {
		addRetargetAction(new DeleteRetargetAction());
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		
		addRetargetAction(new MatchWidthRetargetAction());
		addRetargetAction(new MatchHeightRetargetAction());
		
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.LEFT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.CENTER));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.RIGHT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.TOP));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.MIDDLE));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.BOTTOM));
		
//		addRetargetAction(new RetargetAction(
//				GEFActionConstants.TOGGLE_RULER_VISIBILITY, 
//				GEFMessages.ToggleRulerVisibility_Label, IAction.AS_CHECK_BOX));
		
		final RetargetAction geometrySnap = new RetargetAction(
				GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY, 
				GEFMessages.ToggleSnapToGeometry_Label, IAction.AS_CHECK_BOX);
		geometrySnap.setImageDescriptor(InternalImages.DESC_HORZ_ALIGN_RIGHT);
		addRetargetAction(geometrySnap);

		final RetargetAction gridSnap = new RetargetAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY, 
				GEFMessages.ToggleGrid_Label, IAction.AS_CHECK_BOX);
		gridSnap.setImageDescriptor(ImageDescriptor.createFromImage(DBGMImages.ICON_GRID));
		addRetargetAction(gridSnap);

	}

	/**
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#declareGlobalActionKeys()
	 */
	@Override
	protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.PRINT.getId());
		addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
		addGlobalActionKey(ActionFactory.PASTE.getId());
		addGlobalActionKey(ActionFactory.DELETE.getId());
	}
/**
 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(org.eclipse.jface.action.IToolBarManager)
 */
@Override
public void contributeToToolBar(IToolBarManager tbm) {
	super.contributeToToolBar(tbm);
	String[] zoomStrings = new String[] {
			ZoomManager.FIT_ALL, ZoomManager.FIT_HEIGHT,
			ZoomManager.FIT_WIDTH
			};
			tbm.add(new ZoomComboContributionItem(getPage(), zoomStrings));
			tbm.add(new Separator());
			tbm.add(getAction(GEFActionConstants.MATCH_WIDTH));
			tbm.add(getAction(GEFActionConstants.MATCH_HEIGHT));
//			tbm.add(new Separator());
//			tbm.add(getAction(GEFActionConstants.ALIGN_LEFT));
//			tbm.add(getAction(GEFActionConstants.ALIGN_CENTER));
//			tbm.add(getAction(GEFActionConstants.ALIGN_RIGHT));
//			tbm.add(new Separator());
//			tbm.add(getAction(GEFActionConstants.ALIGN_TOP));
//			tbm.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
//			tbm.add(getAction(GEFActionConstants.ALIGN_BOTTOM));
			tbm.add(new Separator());	
//			tbm.add(getAction(GEFActionConstants.TOGGLE_RULER_VISIBILITY));
			tbm.add(getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
			tbm.add(getAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY));
			tbm.add(new Separator(
					IWorkbenchActionConstants.MB_ADDITIONS));
//			toolBarManager.add(new PrintAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart()));


	// TODO Auto-generated method stub

}
}
