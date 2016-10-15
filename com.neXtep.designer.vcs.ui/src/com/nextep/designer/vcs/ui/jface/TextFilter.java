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
package com.nextep.designer.vcs.ui.jface;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.navigator.CommonViewer;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.model.ITypedNode;

public class TextFilter extends ViewerFilter implements ModifyListener {

	private Text text;
	private Viewer viewer;

	private TextFilter(Text text, Viewer viewer) {
		this.text = text;
		this.viewer = viewer;
	}

	public static void handle(StructuredViewer v, Text t) {
		TextFilter filter = new TextFilter(t, v);
		t.addModifyListener(filter);
		v.addFilter(filter);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IComparisonItem) {
			final IComparisonItem item = (IComparisonItem) element;
			return isRecursivelyMatchedText(item, text.getText());
		} else if (element instanceof IVersionContainer) {
			final String pattern = getPattern();
			// Trying to match container name
			if (match(((IVersionContainer) element).getName(), pattern)) {
				return true;
			}
			// If not matched, we recursively match contents
			for (IVersionable<?> v : ((IVersionContainer) element).getContents()) {
				if (match(v.getName(), pattern)) {
					return true;
				}
			}
		} else if (element instanceof ITypedNode) {
			String pattern = getPattern();
			for (ITypedObject o : ((ITypedNode) element).getChildren()) {
				if (o instanceof INamedObject) {
					if (match(((INamedObject) o).getName(), pattern)) {
						return true;
					}
				}
			}
			return false;
		} else if (element instanceof INamedObject) {
			INamedObject named = (INamedObject) element;
			if (named != null && !text.isDisposed() && !"".equals(text.getText().trim())) { //$NON-NLS-1$
				return named.getName().toUpperCase().contains(text.getText().toUpperCase());
			}
		}

		return true;
	}

	private String getPattern() {
		if (!text.isDisposed()) {
			return text.getText().toUpperCase();
		}
		return "";
	}

	private boolean match(String text, String pattern) {
		return text.toUpperCase().contains(pattern.toUpperCase());
	}

	private boolean isRecursivelyMatchedText(IComparisonItem item, String text) {
		INamedObject named = null;
		if (item.getSource() instanceof INamedObject) {
			named = (INamedObject) item.getSource();
		} else if (item.getTarget() instanceof INamedObject) {
			named = (INamedObject) item.getTarget();
		}
		if (named != null && named.getName().toUpperCase().contains(text.toUpperCase())) {
			return true;
		} else {
			for (IComparisonItem childItem : item.getSubItems()) {
				if (isRecursivelyMatchedText(childItem, text)) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public void modifyText(ModifyEvent e) {
		if (viewer instanceof CommonViewer) {
			if (!text.isDisposed() && !"".equals(text.getText().trim())) {
				((CommonViewer) viewer).expandToLevel(4);
			}
		}
		viewer.refresh();
	}
}
