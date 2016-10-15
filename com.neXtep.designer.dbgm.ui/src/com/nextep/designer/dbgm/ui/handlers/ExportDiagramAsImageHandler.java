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

import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.DbgmUIPlugin;

/**
 * This handler can export the diagram of the current active editor to a file (BMP, JPG or ICO) by
 * prompting the user for an export destination.
 * 
 * @author Christophe Fondacci
 */
public class ExportDiagramAsImageHandler extends AbstractHandler {

	private final static Log log = LogFactory.getLog(ExportDiagramAsImageHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart currentEditor = HandlerUtil.getActiveEditor(event);
		if (currentEditor != null) {
			final GraphicalViewer viewer = (GraphicalViewer) currentEditor
					.getAdapter(GraphicalViewer.class);
			if (viewer != null) {
				save(currentEditor, viewer);
			}
		}
		return null;
	}

	public static boolean save(final IEditorPart editorPart, final GraphicalViewer viewer,
			final String saveFilePath, final int format) {
		Assert.isNotNull(editorPart,
				DBGMUIMessages.getString("handler.diagramExport.nullEditorError")); //$NON-NLS-1$
		Assert.isNotNull(viewer, DBGMUIMessages.getString("handler.diagramExport.nullViewerError")); //$NON-NLS-1$
		Assert.isNotNull(saveFilePath,
				DBGMUIMessages.getString("handler.diagramExport.nullFilePathError")); //$NON-NLS-1$

		if (format != SWT.IMAGE_BMP && format != SWT.IMAGE_JPEG && format != SWT.IMAGE_ICO
				&& format != SWT.IMAGE_GIF && format != SWT.IMAGE_PNG)
			throw new IllegalArgumentException(
					DBGMUIMessages.getString("handler.diagramExport.unsupportedFormat")); //$NON-NLS-1$

		UIJob j = new UIJob("Creating image from diagram") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Exporting diagram to " + saveFilePath + "...", 100);
					monitor.worked(50);
					// Ensuring that the job dialog is visible to notfiy the user
					while (getDisplay().readAndDispatch()) {
					}
					saveEditorContentsAsImage(editorPart, viewer, saveFilePath, format);
					monitor.worked(45);
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							MessageDialog.openInformation(editorPart.getSite().getShell(),
									DBGMUIMessages
											.getString("handler.diagramExport.exportSuccessTitle"), //$NON-NLS-1$
									MessageFormat.format(DBGMUIMessages
											.getString("handler.diagramExport.exportSuccessMsg"), //$NON-NLS-1$
											saveFilePath));

						}
					});
					monitor.done();
					return Status.OK_STATUS;
				} catch (Throwable ex) {
					log.error(
							DBGMUIMessages.getString("handler.diagramExport.saveErrorMsg") + ex.getMessage(), ex); //$NON-NLS-1$
					return new Status(IStatus.ERROR, DbgmUIPlugin.PLUGIN_ID,
							DBGMUIMessages.getString("handler.diagramExport.saveErrorMsg") //$NON-NLS-1$
									+ ex.getMessage(), ex);
				}
			}
		};
		j.setUser(true);
		j.schedule();
		return true;
	}

	public static boolean save(IEditorPart editorPart, GraphicalViewer viewer) {
		Assert.isNotNull(editorPart,
				DBGMUIMessages.getString("handler.diagramExport.nullEditorError")); //$NON-NLS-1$
		Assert.isNotNull(viewer, DBGMUIMessages.getString("handler.diagramExport.nullViewerError")); //$NON-NLS-1$

		String saveFilePath = getSaveFilePath(editorPart, viewer, -1);
		if (saveFilePath == null)
			return false;

		int format = SWT.IMAGE_JPEG;
		if (saveFilePath.endsWith(".jpeg") || saveFilePath.endsWith(".jpg")) { //$NON-NLS-1$ //$NON-NLS-2$
			format = SWT.IMAGE_JPEG;
		} else if (saveFilePath.endsWith(".bmp")) { //$NON-NLS-1$
			format = SWT.IMAGE_BMP;
		} else if (saveFilePath.endsWith(".png")) { //$NON-NLS-1$
			format = SWT.IMAGE_PNG;
		} else if (saveFilePath.endsWith(".gif")) { //$NON-NLS-1$
			format = SWT.IMAGE_GIF;
		} else {
			format = SWT.IMAGE_PNG;
			saveFilePath += ".png"; //$NON-NLS-1$
		}

		return save(editorPart, viewer, saveFilePath, format);
	}

	private static String getSaveFilePath(IEditorPart editorPart, GraphicalViewer viewer, int format) {
		FileDialog fileDialog = new FileDialog(editorPart.getEditorSite().getShell(), SWT.SAVE);

		String[] filterExtensions = new String[] { "*.png", "*.jpg", "*.jpeg", "*.bmp" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		};
		if (format == SWT.IMAGE_BMP)
			filterExtensions = new String[] { "*.bmp" }; //$NON-NLS-1$
		else if (format == SWT.IMAGE_JPEG)
			filterExtensions = new String[] { "*.jpeg" }; //$NON-NLS-1$
		else if (format == SWT.IMAGE_ICO)
			filterExtensions = new String[] { "*.ico" }; //$NON-NLS-1$
		fileDialog.setFilterExtensions(filterExtensions);
		fileDialog.setOverwrite(true);
		String filePath = fileDialog.open();

		// Checking if extension is found in the name else we append the selected filter
		boolean extensionFound = false;
		for (String ext : filterExtensions) {
			if (filePath.endsWith(ext.substring(1))) {
				extensionFound = true;
				break;
			}
		}
		if (!extensionFound && fileDialog.getFilterIndex() != -1) {
			filePath += filterExtensions[fileDialog.getFilterIndex()].substring(1);
		}
		return filePath;
	}

	private static void saveEditorContentsAsImage(IEditorPart editorPart, GraphicalViewer viewer,
			String saveFilePath, int format) {
		/*
		 * 1. First get the figure whose visuals we want to save as image. So we would like to save
		 * the rooteditpart which actually hosts all the printable layers. NOTE:
		 * ScalableRootEditPart manages layers and is registered graphicalviewer's editpartregistry
		 * with the key LayerManager.ID ... well that is because ScalableRootEditPart manages all
		 * layers that are hosted on a FigureCanvas. Many layers exist for doing different things
		 */
		LayerManager rootEditPart = (LayerManager) viewer.getEditPartRegistry()
				.get(LayerManager.ID);
		IFigure rootFigure = ((LayerManager) rootEditPart)
				.getLayer(LayerConstants.PRINTABLE_LAYERS);// rootEditPart.getFigure();
		Rectangle rootFigureBounds = rootFigure.getBounds();

		/*
		 * 2. Now we want to get the GC associated with the control on which all figures are painted
		 * by SWTGraphics. For that first get the SWT Control associated with the viewer on which
		 * the rooteditpart is set as contents
		 */
		Control figureCanvas = viewer.getControl();
		GC figureCanvasGC = null;
		GC imageGC = null;
		Image img = null;
		try {
			figureCanvasGC = new GC(figureCanvas);

			/* 3. Create a new Graphics for an Image onto which we want to paint rootFigure */
			img = new Image(null, rootFigureBounds.width, rootFigureBounds.height);
			imageGC = new GC(img);
			imageGC.setBackground(figureCanvasGC.getBackground());
			imageGC.setForeground(figureCanvasGC.getForeground());
			imageGC.setFont(figureCanvasGC.getFont());
			imageGC.setLineStyle(figureCanvasGC.getLineStyle());
			imageGC.setLineWidth(figureCanvasGC.getLineWidth());
			// imageGC.setXORMode(figureCanvasGC.getXORMode());
			Graphics imgGraphics = new SWTGraphics(imageGC);

			/* 4. Draw rootFigure onto image. After that image will be ready for save */
			rootFigure.paint(imgGraphics);

			/* 5. Save image */
			ImageData[] imgData = new ImageData[1];
			imgData[0] = img.getImageData();

			ImageLoader imgLoader = new ImageLoader();
			imgLoader.data = imgData;
			imgLoader.save(saveFilePath, format);
		} finally {
			/* release OS resources */
			if (figureCanvasGC != null) {
				figureCanvasGC.dispose();
			}
			if (imageGC != null) {
				imageGC.dispose();
			}
			if (img != null) {
				img.dispose();
			}
		}
	}
}
