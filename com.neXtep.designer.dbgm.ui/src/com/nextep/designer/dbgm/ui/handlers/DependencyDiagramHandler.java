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
package com.nextep.designer.dbgm.ui.handlers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.DiagramItem;
import com.nextep.datadesigner.vcs.impl.VersionedDiagram;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.dbgm.ui.layout.DiagramLayoutService;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.ui.model.IDependencySearchRequest;

public class DependencyDiagramHandler extends AbstractHandler {

	private final static Log LOGGER = LogFactory.getLog(DependencyDiagramHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Retrieving info
		final IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActivePart();
		if (part instanceof CommonNavigator) {
			CommonViewer viewer = ((CommonNavigator) part).getCommonViewer();
			if (viewer.getInput() instanceof IDependencySearchRequest) {
				final IDependencySearchRequest request = (IDependencySearchRequest) viewer
						.getInput();
				IDiagram diagram = new VersionedDiagram();
				diagram.setName("Dependencies of "
						+ NameHelper.getQualifiedName(request.getElement()));
				Set<IReference> addedReferences = new HashSet<IReference>();
				// Filling diagram with dependencies depending of the current mode
				switch (request.getRequestType()) {
				case OBJECTS_DEPENDENT_OF:
					fillDiagramReverseDependencyItems(diagram, request.getReverseDependenciesMap(),
							request.getElement(), addedReferences);
					break;
				case DIRECT_DEPENDENCIES:
					fillDiagramDependencyItems(diagram, request.getElement(), addedReferences);
					break;
				}
				// Auto-layouting
				DiagramLayoutService.autoLayout(diagram);
				// Opening
				UIControllerFactory.getController(diagram).defaultOpen(diagram);
			}
		}
		return null;
	}

	private void fillDiagramReverseDependencyItems(IDiagram diagram, MultiValueMap invRefMap,
			IReferenceable element, Set<IReference> addedReferences) {
		addDiagramItem(diagram, element);
		addedReferences.add(element.getReference());
		Collection<IReferenceable> dependencies = invRefMap.getCollection(element.getReference());
		if (dependencies != null) {
			for (IReferenceable r : dependencies) {
				if (!addedReferences.contains(r.getReference())) {
					fillDiagramReverseDependencyItems(diagram, invRefMap, r, addedReferences);
				}
			}
		}
	}

	private void fillDiagramDependencyItems(IDiagram diagram, IReferenceable element,
			Set<IReference> addedReferences) {
		addDiagramItem(diagram, element);
		addedReferences.add(element.getReference());
		if (element instanceof IReferencer) {
			Collection<IReference> references = ((IReferencer) element).getReferenceDependencies();
			for (IReference r : references) {
				if (!addedReferences.contains(r.getReference())) {
					try {
						final IReferenceable dependency = VersionHelper.getReferencedItem(r);
						fillDiagramDependencyItems(diagram, dependency, addedReferences);
					} catch (ErrorException e) {
						LOGGER.error(e);
					}
				}
			}
		}
	}

	private void addDiagramItem(IDiagram diagram, IReferenceable element) {
		if (element instanceof ITypedObject) {
			if (((ITypedObject) element).getType() == IElementType.getInstance(IBasicTable.TYPE_ID)) {
				final IDiagramItem elementItem = new DiagramItem(element, 0, 0);
				diagram.addItem(elementItem);
			}
		}
	}
}
