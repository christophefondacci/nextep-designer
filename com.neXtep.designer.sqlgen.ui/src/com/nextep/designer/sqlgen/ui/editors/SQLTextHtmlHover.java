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
package com.nextep.designer.sqlgen.ui.editors;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.service.ImageService;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.sqlgen.ui.model.ITypedObjectTextProvider;
import com.nextep.designer.sqlgen.ui.services.SQLEditorUIServices;
import com.nextep.designer.ui.factories.ImageFactory;

public class SQLTextHtmlHover implements ITextHover, ITextHoverExtension {

	private MultiValueMap invRefMap;
	private IInformationControlCreator controlCreator;
	private final static Log log = LogFactory.getLog(SQLTextHtmlHover.class);

	public SQLTextHtmlHover() {
		Job j = new Job(SQLMessages.getString("sqlHover.dependenciesJob")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					invRefMap = CorePlugin.getService(IReferenceManager.class).getReverseDependenciesMap();
				} catch (RuntimeException e) {
					log.error(SQLMessages.getString("sqlHover.dependenciesJobError") //$NON-NLS-1$
							+ e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}
		};
		j.schedule();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		final int offset = hoverRegion.getOffset();
		final IDocument document = textViewer.getDocument();
		if (document == null)
			return null;

		IRegion lineInfo;
		String line;
		try {
			lineInfo = document.getLineInformationOfOffset(offset);
			line = document.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			return null;
		}

		// Retrieving proposals
		ITypedObjectTextProvider provider = SQLEditorUIServices.getInstance()
				.getTypedObjectTextProvider();
		List<String> allProposals = provider.listProvidedElements();
		for (String s : allProposals) {
			if (line.toUpperCase().contains(s.toUpperCase())) {
				// More accurate search
				Pattern p = Pattern.compile("(\\W|\\s|^)" + s.toUpperCase() + "(\\W|\\s|$)"); //$NON-NLS-1$ //$NON-NLS-2$
				Matcher m = p.matcher(line.toUpperCase());
				while (m.find()) {
					if (offset >= (m.start() + lineInfo.getOffset())
							&& offset <= (m.end() + lineInfo.getOffset())) {

						final ITypedObject obj = provider.getElement(s);
						final StringBuffer buf = new StringBuffer();
						final INamedObject named = (INamedObject) obj;
						buf.append("<html>"); //$NON-NLS-1$
						addStyleSheet(buf);
						appendColors(buf, FontFactory.BLACK.getRGB(),
								FontFactory.LIGHT_YELLOW.getRGB());
						// buf.append("\n<table BORDER=0 BORDERCOLOR=\"#000000\" CELLPADDING=0 cellspacing=0 >\n");
						// buf.append("<tr><td>\n");
						URL u = ImageService.getInstance().getImageURL(
								ImageFactory.getImageDescriptor(obj.getType().getIcon()));
						buf.append("<table border=0><tr valign=\"CENTER\"><td><img src=\"" //$NON-NLS-1$
								+ u.toExternalForm() + "\"/>&nbsp;"); //$NON-NLS-1$
						buf.append("</td><td><b>" + obj.getType().getName() + " " + named.getName() //$NON-NLS-1$ //$NON-NLS-2$
								+ "</b></td></tr></table>"); //$NON-NLS-1$
						if (named.getDescription() != null
								&& !"".equals(named.getDescription().trim())) { //$NON-NLS-1$
							buf.append("<i>" + named.getDescription() + "</i><br><br>"); //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							buf.append("<i>"); //$NON-NLS-1$
							buf.append(SQLMessages.getString("sqlHover.noDesc")); //$NON-NLS-1$
							buf.append("</i><br><br>"); //$NON-NLS-1$
						}
						// Temporarily adding table definition here
						if (obj instanceof IBasicTable) {
							final IBasicTable t = (IBasicTable) obj;
							buf.append("<table BORDER=1 BORDERCOLOR=\"#000000\" CELLPADDING=4 cellspacing=0 >\n"); // + //$NON-NLS-1$
							buf.append("<tr bgcolor=\""); //$NON-NLS-1$
							appendColor(buf, new RGB(220, 250, 220));
							buf.append("\"><td><b>"); //$NON-NLS-1$
							buf.append(SQLMessages.getString("sqlHover.columnNameCol")); //$NON-NLS-1$
							buf.append("</b></td><td><b>"); //$NON-NLS-1$
							buf.append(SQLMessages.getString("sqlHover.datatypeCol")); //$NON-NLS-1$
							buf.append("</b></td><td><b>"); //$NON-NLS-1$
							buf.append(SQLMessages.getString("sqlHover.descriptionCol")); //$NON-NLS-1$
							buf.append("</b></td></tr>\n"); //$NON-NLS-1$
							// /*cellspacing=\"0\" callpadding=\"0\" */  "border=\"1\" align=\"left\" width=\"350\">\n");
							for (IBasicColumn c : t.getColumns()) {
								buf.append("<tr>\n"); //$NON-NLS-1$
								buf.append("<td>" + c.getName() + "</td>\n"); //$NON-NLS-1$ //$NON-NLS-2$
								buf.append("<td>" + c.getDatatype() + "</td>\n"); //$NON-NLS-1$ //$NON-NLS-2$
								final String desc = c.getDescription();
								buf.append("<td>" + (desc == null ? "" : desc) + "</td>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								buf.append("</tr>"); //$NON-NLS-1$
							}
							buf.append("</table><br>"); //$NON-NLS-1$
						} else {
							buf.append("<br>"); //$NON-NLS-1$
						}
						if (obj instanceof IReferencer) {
							Collection<IReference> refs = ((IReferencer) obj)
									.getReferenceDependencies();
							if (refs != null && !refs.isEmpty()) {
								buf.append("<b>"); //$NON-NLS-1$
								buf.append(SQLMessages.getString("sqlHover.dependentOf")); //$NON-NLS-1$
								buf.append("</b><br><span>"); //$NON-NLS-1$
								for (IReference r : refs) {
									IReferenceable ref = VersionHelper.getReferencedItem(r);
									buf.append("<li>" + ((ITypedObject) ref).getType().getName() //$NON-NLS-1$
											+ "&nbsp;<u>" + ((INamedObject) ref).getName() //$NON-NLS-1$
											+ "</u></li>"); //$NON-NLS-1$
									// buf.append(Designer.getInstance().getQualifiedName(ref)
									// + "<br>");
								}
								buf.append("</span><br>"); //$NON-NLS-1$
							}
						}
						if (obj instanceof IReferenceable && invRefMap != null) {
							Collection<IReferencer> referencers = (Collection<IReferencer>) invRefMap
									.get(((IReferenceable) obj).getReference());
							if (referencers != null && !referencers.isEmpty()) {
								buf.append("<b>"); //$NON-NLS-1$
								buf.append(SQLMessages.getString("sqlHover.dependencies")); //$NON-NLS-1$
								buf.append("</b><br><span>"); //$NON-NLS-1$
								for (IReferencer r : referencers) {
									buf.append("<li>" + ((ITypedObject) r).getType().getName() //$NON-NLS-1$
											+ "&nbsp;<u>" + ((INamedObject) r).getName() //$NON-NLS-1$
											+ "</u></li>"); //$NON-NLS-1$
								}
							}
							buf.append("</span>"); //$NON-NLS-1$
						}
						// buf.append("</td></tr></table>");
						buf.append("</body></html>"); //$NON-NLS-1$
						return buf.toString();
					}
				}
			}
		}
		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return new Region(offset, 0);
	}

	public IInformationControlCreator getHoverControlCreator() {
		controlCreator = new IInformationControlCreator() {

			@SuppressWarnings("restriction")
			public IInformationControl createInformationControl(Shell parent) {
				if (BrowserInformationControl.isAvailable(parent)) {
					ToolBarManager tbm = new ToolBarManager(SWT.FLAT);
					final BrowserInformationControl infoControl = new BrowserInformationControl(
							parent, "system", true) { //$NON-NLS-1$

						@Override
						public IInformationControlCreator getInformationPresenterControlCreator() {
							return controlCreator;
						}
					};
					infoControl.setBackgroundColor(FontFactory.LIGHT_YELLOW);
					return infoControl;
				} else {
					return new DefaultInformationControl(parent,
							EditorsUI.getTooltipAffordanceString());
				}
			}
		};
		return controlCreator;
	}

	private void addStyleSheet(StringBuffer buf) {
		buf.append("<head><style CHARSET=\"ISO-8859-1\" TYPE=\"text/css\">/* Font definitions */" //$NON-NLS-1$
				+ "html         { font-family: sans-serif; font-size: 9pt; font-style: normal; font-weight: normal; }" //$NON-NLS-1$
				+ "body, h1, h2, h3, h4, h5, h6, p, table, td, caption, th, ul, ol, dl, li, dd, dt { font-size: 1em; }" //$NON-NLS-1$
				+ "pre          { font-family: monospace; }" //$NON-NLS-1$
				+ "/* Margins */body	     { overflow: auto; margin-top: 0px; margin-bottom: 0.5em; margin-left: 0.3em; margin-right: 0px; }" //$NON-NLS-1$
				+ "h1           { margin-top: 0.3em; margin-bottom: 0.04em; }" //$NON-NLS-1$
				+ "h2           { margin-top: 2em; margin-bottom: 0.25em; }" //$NON-NLS-1$
				+ "h3           { margin-top: 1.7em; margin-bottom: 0.25em; }" //$NON-NLS-1$
				+ "h4           { margin-top: 2em; margin-bottom: 0.3em; }" //$NON-NLS-1$
				+ "h5           { margin-top: 0px; margin-bottom: 0px; }" //$NON-NLS-1$
				+ "p            { margin-top: 1em; margin-bottom: 1em; }" //$NON-NLS-1$
				+ "pre          { margin-left: 0.6em; }" //$NON-NLS-1$
				+ "ul	         { margin-top: 0px; margin-bottom: 1em; }" //$NON-NLS-1$
				+ "li	         { margin-top: 0px; margin-bottom: 0px; }" //$NON-NLS-1$
				+ "li p	     { margin-top: 0px; margin-bottom: 0px; }" //$NON-NLS-1$
				+ "ol	         { margin-top: 0px; margin-bottom: 1em; }" //$NON-NLS-1$
				+ "dl	         { margin-top: 0px; margin-bottom: 1em; }" //$NON-NLS-1$
				+ "dt	         { margin-top: 0px; margin-bottom: 0px; font-weight: bold; }" //$NON-NLS-1$
				+ "dd	         { margin-top: 0px; margin-bottom: 0px; }" //$NON-NLS-1$
				+ "/* Styles and colors */" //$NON-NLS-1$
				+ "a:link	     { color: #0000FF; }" //$NON-NLS-1$
				+ "a:hover	     { color: #000080; }" //$NON-NLS-1$
				+ "a:visited    { text-decoration: underline; }" //$NON-NLS-1$
				+ "a.header:link    { text-decoration: none; color: InfoText }" //$NON-NLS-1$
				+ "a.header:visited { text-decoration: none; color: InfoText }" //$NON-NLS-1$
				+ "a.header:hover   { text-decoration: underline; color: #000080; }" //$NON-NLS-1$
				+ "h4           { font-style: italic; }" //$NON-NLS-1$
				+ "strong	     { font-weight: bold; }" //$NON-NLS-1$
				+ "em	         { font-style: italic; }" //$NON-NLS-1$
				+ "var	         { font-style: italic; }" //$NON-NLS-1$
				+ "th	         { font-weight: bold; }</style></head>"); //$NON-NLS-1$
	}

	private static void appendColors(StringBuffer pageProlog, RGB fgRGB, RGB bgRGB) {
		pageProlog.append("<body text=\""); //$NON-NLS-1$
		appendColor(pageProlog, fgRGB);
		pageProlog.append("\" bgcolor=\""); //$NON-NLS-1$
		appendColor(pageProlog, bgRGB);
		pageProlog.append("\">"); //$NON-NLS-1$
	}

	private static void appendColor(StringBuffer buffer, RGB rgb) {
		buffer.append('#');
		appendAsHexString(buffer, rgb.red);
		appendAsHexString(buffer, rgb.green);
		appendAsHexString(buffer, rgb.blue);
	}

	private static void appendAsHexString(StringBuffer buffer, int intValue) {
		String hexValue = Integer.toHexString(intValue);
		if (hexValue.length() == 1)
			buffer.append('0');
		buffer.append(hexValue);
	}
}
