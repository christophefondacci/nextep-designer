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
package com.nextep.designer.synch.ui.jface;

import java.text.MessageFormat;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.sqlgen.model.IDropStrategy;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.synch.ui.model.ICategorizedType;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSImages;

public class ComparisonLabelProvider extends CellLabelProvider implements IStyledLabelProvider,
		ILabelProvider {

	private String getName(IComparisonItem bean) {
		String name = "";
		if (bean.getSource() instanceof INamedObject || bean.getTarget() instanceof INamedObject) {
			name = bean.getSource() != null ? ((INamedObject) bean.getSource()).getName()
					: ((INamedObject) bean.getTarget()).getName();
		} else if (bean.getSource() instanceof IReference || bean.getTarget() instanceof IReference) {
			IReference ref = (IReference) (bean.getSource() != null ? bean.getSource() : bean
					.getTarget());
			IReferenceable r = VersionHelper.getReferencedItem(ref);
			if (r instanceof INamedObject) {
				name = ((INamedObject) r).getName();
			}
		}
		return name;
	}

	private String getVersion(IComparisonItem bean) {
		if (bean.getTarget() instanceof IVersionable<?>) {
			if (!bean.getTarget().getReference().isVolatile()) {
				return " - " + ((IVersionable<?>) bean.getTarget()).getVersion().getLabel();
			}
		}
		return "";
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IComparisonItem) {
			final IComparisonItem bean = (IComparisonItem) element;
			return ImageFactory.getImage(bean.getType().getIcon());
		} else if (element instanceof ICategorizedType) {
			final ICategorizedType type = (ICategorizedType) element;
			return ImageFactory.getImage(type.getType().getIcon());
		} else if (element instanceof ISynchronizationResult) {
			final ISynchronizationResult result = (ISynchronizationResult) element;
			switch (result.getComparisonScope()) {
			case DATABASE:
				return DBGMImages.ICON_DATABASE_TINY;
			case DB_TO_REPOSITORY:
				return VCSImages.ICON_CONTAINER_TINY;
			}
		}
		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof IComparisonItem) {
			final IComparisonItem item = (IComparisonItem) element;
			StyledString text = new StyledString();
			Styler nameStyler = item.getMergeInfo().getMergeProposal() == item.getTarget() ? StyledString.QUALIFIER_STYLER
					: null;
			switch (item.getDifferenceType()) {
			case DIFFER:
				text.append(getName(item), nameStyler);
				text.append(getVersion(item), StyledString.COUNTER_STYLER);
				break;
			case MISSING_SOURCE:
				text.append(getName(item), nameStyler);
				text.append(getVersion(item), StyledString.COUNTER_STYLER);
				if (item.getType() != null && item.getScope() == ComparisonScope.DATABASE) {
					IDropStrategy dropStrategy = SQLGenPlugin.getService(IGenerationService.class)
							.getDropStrategy(item.getType());
					String dropText = " [DROP]";
					if (dropStrategy != null) {
						dropText = " [" + dropStrategy.getName() + "]";
					}
					text.append(dropText, StyledString.DECORATIONS_STYLER);
				}
				break;
			case MISSING_TARGET:
				text.append(getName(item), nameStyler);
				text.append(getVersion(item), StyledString.COUNTER_STYLER);
				break;
			default:
				text.append(getName(item), nameStyler);
				text.append(getVersion(item), StyledString.COUNTER_STYLER);
			}
			return text;
		} else if (element instanceof ICategorizedType) {
			StyledString text = new StyledString();
			final ICategorizedType type = (ICategorizedType) element;
			text.append(type.getType().getCategoryTitle());
			if (type.getChangedItems() > 0) {
				text.append(
						" "
								+ MessageFormat.format(
										SynchUIMessages.getString("synch.navigator.changesCount"),
										type.getChangedItems()), StyledString.COUNTER_STYLER);
			}
			return text;
		} else if (element instanceof ISynchronizationResult) {
			final ISynchronizationResult result = (ISynchronizationResult) element;
			final StyledString text = new StyledString();
			switch (result.getComparisonScope()) {
			case DATABASE:
				if (result.getConnection() != null) {
					text.append(result.getConnection().getName());
					text.append(" " + SynchUIMessages.getString("synch.navigator.targetDatabase"),
							StyledString.COUNTER_STYLER);
				} else {
					text.append("No pending synchronization.", StyledString.QUALIFIER_STYLER);
				}
				break;
			case DB_TO_REPOSITORY:
				text.append(VersionHelper.getCurrentView().getName());
				text.append(" " + SynchUIMessages.getString("synch.navigator.targetRepository"),
						StyledString.COUNTER_STYLER);
				break;
			}
			return text;
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IComparisonItem) {
			final IComparisonItem bean = (IComparisonItem) element;
			return getName(bean);
		} else if (element instanceof ICategorizedType) {
			final ICategorizedType type = (ICategorizedType) element;
			return type.getType().getName();
		}
		return null;
	}

	@Override
	public void update(ViewerCell cell) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof IComparisonItem) {
			final IComparisonItem item = (IComparisonItem) element;
			switch (item.getDifferenceType()) {
			case DIFFER:
				return SynchUIMessages.getString("synch.navigator.tooltip.differDb");
			case MISSING_SOURCE:
				return SynchUIMessages.getString("synch.navigator.tooltip.dropDb");
			case MISSING_TARGET:
				return SynchUIMessages.getString("synch.navigator.tooltip.createDb");
			}
		}
		return super.getToolTipText(element);
	}

	@Override
	public boolean useNativeToolTip(Object object) {
		return true;
	}
}
