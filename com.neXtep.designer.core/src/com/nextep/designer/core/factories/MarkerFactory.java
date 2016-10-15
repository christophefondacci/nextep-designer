///*******************************************************************************
// * Copyright (c) 2011 neXtep Software and contributors.
// * All rights reserved.
// *
// * This file is part of neXtep designer.
// *
// * NeXtep designer is free software: you can redistribute it 
// * and/or modify it under the terms of the GNU General Public 
// * License as published by the Free Software Foundation, either 
// * version 3 of the License, or any later version.
// *
// * NeXtep designer is distributed in the hope that it will be 
// * useful, but WITHOUT ANY WARRANTY; without even the implied
// * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
// * See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
// *
// * Contributors:
// *     neXtep Softwares - initial API and implementation
// *******************************************************************************/
//package com.nextep.designer.core.factories;
//
//import com.nextep.designer.core.model.IMarker;
//import com.nextep.designer.core.model.IMarkerHint;
//import com.nextep.designer.core.model.MarkerType;
//import com.nextep.designer.core.model.impl.Marker;
//
///**
// * A factory which provides marker implementations
// * 
// * @author Christophe Fondacci
// */
//public final class MarkerFactory {
//
//	private MarkerFactory() {
//	}
//
//	/**
//	 * Creates a default marker with no hint.
//	 * 
//	 * @param relatedObj object being marked
//	 * @param type a {@link MarkerType}
//	 * @param message the message to display on this marker
//	 * @return the {@link IMarker} implementation
//	 */
//	public static IMarker createMarker(Object relatedObj, MarkerType type, String message) {
//		final Marker marker = new Marker();
//		marker.setRelatedObject(relatedObj);
//		marker.setMarkerType(type);
//		marker.setMessage(message);
//		return marker;
//	}
//
//	/**
//	 * Creates a default marker with a default predefined hint.
//	 * 
//	 * @param relatedObj object being marked
//	 * @param type a {@link MarkerType}
//	 * @param message the message to display on this marker
//	 * @param defaultHint hint of this marker
//	 * @return the {@link IMarker} implementation
//	 */
//
//	public static IMarker createMarker(Object relatedObj, MarkerType type, String message,
//			IMarkerHint defaultHint) {
//		final IMarker marker = createMarker(relatedObj, type, message);
//		marker.addAvailableHint(defaultHint);
//		marker.setSelectedHint(defaultHint);
//		return marker;
//	}
// }
