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
package com.nextep.datadesigner.dbgm.gui.navigators;

import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Bruno Gautier
 */
public class SynonymNavigator extends TypedNavigator {

    public SynonymNavigator(ISynonym synonym, ITypedObjectUIController controller) {
        super(synonym, controller);
    }

    @Override
    public void handleEvent(ChangeEvent event, IObservable source, Object data) {
        refreshConnector();
    }

}
