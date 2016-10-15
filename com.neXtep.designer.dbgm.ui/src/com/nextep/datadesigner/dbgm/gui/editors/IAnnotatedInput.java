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
package com.nextep.datadesigner.dbgm.gui.editors;

import java.util.Collection;
import java.util.Map;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

/**
 * This interface defines editor input which can provide their own annotations. This interface has
 * been designed to simplify additions of annotations. You only have to cope with providing
 * annotations. The base editors will, when supported, process those annotations and display / 
 * maintain them.
 * 
 * @author Christophe Fondacci
 * @since 1.0.3
 */
public interface IAnnotatedInput {

	/**
	 * Computes the map of all annotations which should be displayed with the input.
	 * 
	 * @return a map of annotation positions
	 */
	Map<Annotation,Position> getAnnotationMap(IDocument doc);
	/**
	 * Provides the collection of annotation types provided. This will be used by the annotation
	 * manager to remove all annotations from these types and replace them by annotations
	 * provided by the {@link IAnnotatedInput#getAnnotationMap(IDocument)} method.<br>
	 * 
	 * @return a collection of all annotation types provided
	 */
	Collection<String> getAnnotationTypes();
}
