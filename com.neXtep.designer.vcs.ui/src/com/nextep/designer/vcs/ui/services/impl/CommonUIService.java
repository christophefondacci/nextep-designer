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
package com.nextep.designer.vcs.ui.services.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.forms.TypedListBlockComponent;
import com.nextep.designer.ui.model.IFormActionProvider;
import com.nextep.designer.ui.model.ITypedFormPage;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.impl.TypedFormPage;
import com.nextep.designer.ui.services.IUIService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.services.IWorkspaceService;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.actions.CheckoutAction;
import com.nextep.designer.vcs.ui.actions.CommitAction;
import com.nextep.designer.vcs.ui.actions.UndoCheckoutAction;
import com.nextep.designer.vcs.ui.dialogs.FindElementDialog;
import com.nextep.designer.vcs.ui.editors.MasterDetailsPage;
import com.nextep.designer.vcs.ui.editors.TypedComponentsPage;
import com.nextep.designer.vcs.ui.impl.VersionControlledFormActionProvider;
import com.nextep.designer.vcs.ui.jface.TypedContentProvider;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

public class CommonUIService implements ICommonUIService {

	private static final String EXTENSION_ID_PAGE = "com.neXtep.designer.ui.typeEditorPage"; //$NON-NLS-1$
	private static final String EXTENSION_ID_EDITOR_PAGE = "com.neXtep.designer.ui.editorTypedPageContribution"; //$NON-NLS-1$

	private static final String ATTR_CONTENT_PROVIDER = "contentProvider"; //$NON-NLS-1$
	private static final String ATTR_LABEL_PROVIDER = "labelProvider"; //$NON-NLS-1$
	private static final String ATTR_ACTION_PROVIDER = "actionProvider"; //$NON-NLS-1$
	private static final String ATTR_TITLE = "title"; //$NON-NLS-1$
	private static final String ATTR_ICON = "icon"; //$NON-NLS-1$
	private static final String ATTR_EDITOR_TYPE_ID = "editorTypeId"; //$NON-NLS-1$
	private static final String ATTR_PAGE_TYPE_ID = "pageTypeId"; //$NON-NLS-1$
	private static final String ATTR_VENDOR = "dbVendor"; //$NON-NLS-1$
	private static final String ATTR_SINGLE_EDITION = "singleEdition"; //$NON-NLS-1$
	private static final String ATTR_FORM_PART = "editorPageClass"; //$NON-NLS-1$

	private static final String MSG_KEY_USER_LOCK = "userLock"; //$NON-NLS-1$

	private static final Log LOGGER = LogFactory.getLog(CommonUIService.class);

	private Map<String, Image> pageImagesMap = new HashMap<String, Image>();
	private IUIService coreUIService;
	private IMarkerService markerService;

	@Override
	public ITypedObject findElement(Shell parentShell, String title) {
		return findElement(parentShell, title, VCSPlugin.getViewService().getCurrentWorkspace());
	}

	@Override
	public ITypedObject findElement(Shell parentShell, String title, IVersionContainer module,
			IElementType... type) {
		IContentProvider provider = new TypedContentProvider(type);
		FindElementDialog dialog = new FindElementDialog(parentShell, title, provider, module);
		dialog.setBlockOnOpen(true);
		dialog.open();
		final ITypedObject o = dialog.getSelectedElement();
		return o;
	}

	@Override
	public ITypedObject findElement(Shell parentShell, String title, Object input,
			IContentProvider contentProvider, ILabelProvider labelProvider) {
		FindElementDialog dialog = new FindElementDialog(parentShell, title, contentProvider,
				labelProvider, input);
		dialog.setBlockOnOpen(true);
		dialog.open();
		final ITypedObject o = dialog.getSelectedElement();
		return o;
	}

	@Override
	public ITypedObject findElement(Shell parentShell, String title, IElementType... type) {
		return findElement(parentShell, title, VCSPlugin.getViewService().getCurrentWorkspace(),
				type);
	}

	@Override
	public IFormActionProvider handleLifeCycle(IFormActionProvider provider) {
		return new VersionControlledFormActionProvider(provider);
	}

	@Override
	public IFormPage createPageFor(IElementType type, ITypedObject parent, FormEditor editor,
			boolean isSingleEdition) {
		final Collection<IConfigurationElement> confs = Designer.getInstance().getExtensions(
				EXTENSION_ID_PAGE, "typeId", type.getId()); //$NON-NLS-1$

		if (!confs.isEmpty() && !isSingleEdition) {
			// Building master / details page structure
			final String sectionTitle = MessageFormat.format(
					UIMessages.getString("service.ui.sectionTitle"), //$NON-NLS-1$
					IFormatter.UPPER_LEADING.format(type.getCategoryTitle()));
			final String sectionDesc = MessageFormat.format(
					UIMessages.getString("service.ui.sectionDesc"), type //$NON-NLS-1$
							.getName().toLowerCase());
			final MasterDetailsPage page = new MasterDetailsPage(PAGE_ID_PREFIX + type.getId(),
					type.getCategoryTitle(), sectionTitle, sectionDesc, editor, parent);
			for (IConfigurationElement elt : confs) {
				try {
					// Retrieving attributes from extension point
					final IContentProvider contentProvider = (IContentProvider) elt
							.createExecutableExtension(ATTR_CONTENT_PROVIDER);
					final ILabelProvider labelProvider = (ILabelProvider) elt
							.createExecutableExtension(ATTR_LABEL_PROVIDER);
					final String title = elt.getAttribute(ATTR_TITLE);
					final String iconLocation = elt.getAttribute(ATTR_ICON);
					final IFormActionProvider actionProvider = (IFormActionProvider) elt
							.createExecutableExtension(ATTR_ACTION_PROVIDER);
					// Trying to use our image cache
					Image img = pageImagesMap.get(iconLocation);
					if (img == null) {
						// Loading image if not available yet
						final ImageDescriptor imageDesc = CoreUiPlugin.imageDescriptorFromPlugin(
								elt.getContributor().getName(), iconLocation);
						img = imageDesc.createImage();
						pageImagesMap.put(iconLocation, img);
					}
					// Setting up the page
					page.setContentProvider(contentProvider);
					page.setLabelProvider(labelProvider);
					page.setIcon(img);
					page.setFormTitle(title);
					page.setActionProvider(handleLifeCycle(actionProvider));
				} catch (CoreException e) {
					throw new ErrorException("Unable to instantiate page editor for type " //$NON-NLS-1$
							+ type.getName() + " : " + e.getMessage(), e); //$NON-NLS-1$
				}

			}
			return page;
		} else {
			// Fallbacking on standalone details edition when no master/detail extension definition
			final IFormPage page = new TypedComponentsPage(PAGE_ID_PREFIX + type.getId(), type,
					parent, editor);
			return page;
		}
	}

	private List<ITypedFormPage> createContributedPagesFor(IElementType editorType,
			ITypedObject parent, FormEditor editor, DBVendor vendor) {
		final Collection<IConfigurationElement> confs = Designer.getInstance().getExtensions(
				EXTENSION_ID_EDITOR_PAGE, ATTR_EDITOR_TYPE_ID, editorType.getId());
		final List<ITypedFormPage> pages = new ArrayList<ITypedFormPage>();
		for (IConfigurationElement elt : confs) {
			final String vendorStr = elt.getAttribute(ATTR_VENDOR);
			final String singleEdition = elt.getAttribute(ATTR_SINGLE_EDITION);
			final boolean isSingleEdition = Boolean.parseBoolean(singleEdition);
			final DBVendor extVendor = isEmpty(vendorStr) ? null : DBVendor.valueOf(vendorStr);
			// Only considering a match when both no vendor or equal vendor
			if (vendor == extVendor) {
				// Extracting contributed page type
				final String pageTypeId = elt.getAttribute(ATTR_PAGE_TYPE_ID);
				final IElementType pageType = IElementType.getInstance(pageTypeId);
				// Extracting optional form page class
				final String formPartClass = elt.getAttribute(ATTR_FORM_PART);
				if (!isEmpty(formPartClass)) {
					try {
						final IFormPage page = (IFormPage) elt
								.createExecutableExtension(ATTR_FORM_PART);
						page.initialize(editor);
						if (page instanceof IModelOriented<?>) {
							((IModelOriented) page).setModel(parent);
						}
						pages.add(new TypedFormPage(pageType, page));
					} catch (CoreException e) {
						LOGGER.error(
								"Unable to instantiate contributed form page '" + formPartClass //$NON-NLS-1$
										+ "' to " + editorType.getId() + " editor : " //$NON-NLS-1$ //$NON-NLS-2$
										+ e.getMessage(), e);
					}
				} else {
					// Creating default Master / Details form page from contribution
					IFormPage page = createPageFor(pageType, parent, editor, isSingleEdition);
					pages.add(new TypedFormPage(pageType, page));
				}
			}
		}
		return pages;
	}

	private boolean isEmpty(String s) {
		return s == null || "".equals(s.trim()); //$NON-NLS-1$
	}

	@Override
	public IUIComponent createTypedListComponent(ILabelProvider labelProvider,
			IContentProvider contentProvider, IFormActionProvider actionProvider, ITypedObject input) {
		return new TypedListBlockComponent(labelProvider, contentProvider,
				handleLifeCycle(actionProvider), input);
	}

	@Override
	public List<IUIComponent> getEditorComponentsFor(IElementType type, DBVendor vendor) {
		return coreUIService.getEditorComponentsFor(type, vendor);
	}

	@Override
	public List<IUIComponent> getEditorComponentsFor(ITypedObject object, DBVendor vendor) {
		return coreUIService.getEditorComponentsFor(object, vendor);
	}

	/**
	 * @param coreUIService the coreUIService to set
	 */
	public void setCoreUIService(IUIService coreUIService) {
		this.coreUIService = coreUIService;
	}

	@Override
	public List<ITypedFormPage> createContributedPagesFor(IElementType editorType,
			ITypedObject parent, FormEditor editor) {

		final List<ITypedFormPage> pages = new ArrayList<ITypedFormPage>();
		pages.addAll(createContributedPagesFor(editorType, parent, editor, null));
		pages.addAll(createContributedPagesFor(editorType, parent, editor,
				DBVendor.valueOf(Designer.getInstance().getContext())));
		return pages;
	}

	@Override
	public void bindController(Object model) {
		coreUIService.bindController(model);
	}

	@Override
	public void createVersionControlToolbarActions(IToolBarManager toolbarMgr, Object model,
			Object uiElement) {
		final IVersionable<?> versionable = VersionHelper.getVersionable(model);
		if (versionable != null) {
			final Action commitAction = new CommitAction(this, versionable);
			final Action checkOutAction = new CheckoutAction(this, versionable);
			final Action undoCheckOutAction = new UndoCheckoutAction(this, versionable);
			toolbarMgr.add(commitAction);
			toolbarMgr.add(checkOutAction);
			toolbarMgr.add(undoCheckOutAction);
		}
	}

	@Override
	public void updateFormMessages(IManagedForm form, ITypedObject model, Object source) {
		form.getMessageManager().removeAllMessages();
		// Checking external user lock
		IVersionable<?> versionable = VersionHelper.getVersionable(model);
		if (versionable != null) {
			final IVersionInfo version = versionable.getVersion();
			// If element is not committed and does not belong to us, we display the lock msg
			if (version.getStatus() != IVersionStatus.CHECKED_IN
					&& version.getUser() != VCSPlugin.getService(IWorkspaceService.class)
							.getCurrentUser()) {
				// Warning message to inform that everything is locked
				form.getMessageManager().addMessage(MSG_KEY_USER_LOCK,
						MessageFormat.format(VCSUIMessages.getString("page.lockedByUserMsg"), //$NON-NLS-1$
								version.getUser().getName()), null, IMessageProvider.WARNING);
			}
		}

		// Fetching markers
		final IMessageManager msgManager = form.getMessageManager();
		fillMessages(model, msgManager);
		if (model instanceof IReferenceContainer) {
			for (IReferenceable r : ((IReferenceContainer) model).getReferenceMap().values()) {
				if (r instanceof ITypedObject) {
					fillMessages((ITypedObject) r, msgManager);
				}
			}
		}
	}

	private void fillMessages(ITypedObject model, IMessageManager msgManager) {
		final Collection<IMarker> markers = markerService.getMarkersFor(model);
		for (IMarker m : markers) {
			int msgType = IMessageProvider.NONE;
			switch (m.getMarkerType()) {
			case ERROR:
				msgType = IMessageProvider.ERROR;
				break;
			case WARNING:
				msgType = IMessageProvider.WARNING;
				break;
			default:
				msgType = IMessageProvider.INFORMATION;
			}
			msgManager.addMessage(m, m.getMessage(), m, msgType);
		}
	}

	/**
	 * @param markerService the markerService to set
	 */
	public void setMarkerService(IMarkerService markerService) {
		this.markerService = markerService;
	}
}
