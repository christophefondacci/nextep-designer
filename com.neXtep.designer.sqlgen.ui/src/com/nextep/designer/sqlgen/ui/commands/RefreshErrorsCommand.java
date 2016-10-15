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
///**
// * Copyright (c) 2008 neXtep Softwares.
// * All rights reserved. Terms of the neXtep licence
// * are available at http://www.nextep-softwares.com
// */
//package com.nextep.designer.sqlgen.ui.commands;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.eclipse.jface.text.BadLocationException;
//import org.eclipse.jface.text.IDocument;
//import org.eclipse.jface.text.Position;
//import org.eclipse.jface.text.source.Annotation;
//import org.eclipse.jface.text.source.IAnnotationModel;
//import org.eclipse.jface.text.source.IAnnotationModelExtension;
//import org.eclipse.ui.IEditorInput;
//import org.eclipse.ui.texteditor.IDocumentProvider;
//import com.nextep.datadesigner.Designer;
//import com.nextep.datadesigner.model.DBVendor;
//import com.nextep.datadesigner.model.ICommand;
//import com.nextep.datadesigner.model.IMarker;
//import com.nextep.datadesigner.model.IModelOriented;
//import com.nextep.datadesigner.model.ITypedObject;
//import com.nextep.datadesigner.model.MarkerType;
//import com.nextep.datadesigner.vcs.gui.rcp.IComparisonItemEditorInput;
//import com.nextep.datadesigner.vcs.services.VersionHelper;
//import com.nextep.designer.vcs.model.IComparisonItem;
//
///**
// * A command which refreshed the annotation model for the given 
// * input.
// * 
// * @author Christophe
// *
// */
//public class RefreshErrorsCommand implements ICommand {
//
//	private static final Log log = LogFactory.getLog(RefreshErrorsCommand.class);
//	@Override
//	public Object execute(Object... parameters) {
//		// Only for Oracle so far...
//		if(VersionHelper.getCurrentView().getDBVendor()!=DBVendor.ORACLE) return null;
////		String type = (String)parameters[0];
//		IDocumentProvider provider = (IDocumentProvider)parameters[1];
//		IEditorInput input = (IEditorInput)parameters[2];
//		
////		final Collection<IConnection> connections = DBGMHelper.getTargetSet().getTarget(SQLGenUtil.getDefaultTargetType());
////		IConnection connection = null;
////		if(connections.isEmpty()) {
////			return null;
////		} else {
////			// Taking first connection for error info
////			// TODO: add a setting to select error connection or iterate through all
////			connection = connections.iterator().next();
////		}
////		Collection<ErrorInfo> errors = SQLGenUtil.showErrors(input.getName(), connection);
////		if(provider == null) {
////			return null;
////		}
//		IDocument doc = provider.getDocument(input);
////		log.debug(doc.get());
//		IAnnotationModel ann = provider.getAnnotationModel(input);
//		// Removing previous annotations
//		if(ann==null) {
//			return null;
//		}
////		Iterator<?> it = ann.getAnnotationIterator();
////		while(it.hasNext()) {
////			ann.removeAnnotation((Annotation)it.next());
////		}
//
//		// Initializing errors annotations
//		try {
//			if(input instanceof IComparisonItemEditorInput) {
//				annotateDifferences((IComparisonItemEditorInput)input, ann, doc);
//			}
//			final ITypedObject typedObj = (ITypedObject)((IModelOriented<?>)input).getModel();
//			Collection<IMarker> markers = Designer.getMarkerProvider().getMarkersFor(typedObj);
//			if(markers == null || markers.isEmpty()) return null;
//			for(IMarker marker : markers) {
////				if(type.equals(marker.getAttribute(IMarker.ATTR_EXTERNAL_TYPE))) {
//					int line = 1;
//					int col = 1;
//					if(marker.getAttribute(IMarker.ATTR_LINE)!=null) {
//						line = (Integer)marker.getAttribute(IMarker.ATTR_LINE);
//						if(line==0) line=1;
//					}
//					if(marker.getAttribute(IMarker.ATTR_COL)!=null) {
//						col = (Integer)marker.getAttribute(IMarker.ATTR_COL);
//						if(col==0) col=1;
//					}
//					int offset = doc.getLineOffset(line-1) + col-1;
//					int end = getNextSeparator(doc, offset);
//					
//					String annType = null;
//					if(marker.getMarkerType()==MarkerType.ERROR) {
//						annType= "org.eclipse.ui.workbench.texteditor.error";
//					} else {
//						annType= "org.eclipse.ui.workbench.texteditor.warning";
//					}
//					ann.addAnnotation(new Annotation(annType,true,marker.getMessage()),new Position(offset,end -offset) );
////				}
//			}
//			
//		} catch( BadLocationException e) {
//			log.debug("Bad location while adding annotations.",e);
//		}
//		return null;
//	}
//
//	private void annotateDifferences(IComparisonItemEditorInput input, IAnnotationModel ann, IDocument doc) throws BadLocationException {
//		IComparisonItem item = input.getComparisonItem();
//		Iterator<?> it = ann.getAnnotationIterator();
//		Collection<Annotation> toRemove = new ArrayList<Annotation>();
//		Map<Annotation,Position> toAdd = new HashMap<Annotation,Position>();
//		while(it.hasNext()) {
//			toRemove.add((Annotation)it.next());
//		}
//		if(item!=null) {
//			int line = 0;
//			Annotation a;
//			String annType = null;
//			for(IComparisonItem i : item.getSubItems()) {
//				switch(i.getDifferenceType()) {
//				case DIFFER:
//					annType="com.neXtep.designer.sqlgen.ui.diff.modified";
//					break;
//				case MISSING_SOURCE:
//					annType="com.neXtep.designer.sqlgen.ui.diff.new";
//					break;
//				case MISSING_TARGET:
//					annType="com.neXtep.designer.sqlgen.ui.diff.removed";
//					break;
//				default:
//					annType=null;
//					break;
//				}
//				if(annType!=null) {
//					int offset = doc.getLineOffset(line);
//					int endOffset;
//					try {
//						endOffset = doc.getLineOffset(line+1);
//					} catch( BadLocationException e) {
//						endOffset=doc.getLength();
//					}
//					toAdd.put(new Annotation(annType,true,""), new Position(offset, endOffset-offset));
//				}
//				line++;
//			}
//		}
//		((IAnnotationModelExtension)ann).replaceAnnotations(toRemove.toArray(new Annotation[toRemove.size()]), toAdd);
//	}
//	@Override
//	public String getName() {
//		return "Retrieving compilation information...";
//	}
//
//}
