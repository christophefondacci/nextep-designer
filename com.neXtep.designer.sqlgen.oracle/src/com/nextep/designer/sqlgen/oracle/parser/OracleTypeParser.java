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
package com.nextep.designer.sqlgen.oracle.parser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.ParseData;
import com.nextep.datadesigner.dbgm.impl.ProcedureParameter;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IProcedureParameter;
import com.nextep.datadesigner.dbgm.model.IVariable;
import com.nextep.datadesigner.dbgm.model.ParameterType;
import com.nextep.datadesigner.dbgm.model.Variable;
import com.nextep.datadesigner.sqlgen.impl.LightProcedure;
import com.nextep.designer.dbgm.model.base.AbstractTypedSqlParser;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.dbgm.sql.TextPosition;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleTypeParser extends AbstractTypedSqlParser {

	private static final Log LOGGER = LogFactory.getLog(OracleProcedureParser.class);

	@Override
	public IParseData parse(IParseable parseable, String sql) {
		try {
			return internaleParse(parseable, sql);
		} catch (BadLocationException e) {
			LOGGER.warn("Errors while parsing procedure");
			return null;
		}
	}

	private IParseData internaleParse(IParseable parseable, String contentsToParse)
			throws BadLocationException {
		final IOracleUserType type = (IOracleUserType) parseable;
		if (type.getTypeBody() == null || "".equals(type.getTypeBody().trim())) { //$NON-NLS-1$
			type.setParsed(true);
			type.clearProcedures();
			return null;
		}
		IDocument doc = createDocument(contentsToParse);
		// Reseting
		type.setParsed(false);
		type.clearProcedures();
		IParseData parseData = new ParseData();
		// Parsing block
		PackageScanner pkgScanner = new PackageScanner();
		pkgScanner.setRange(doc, 0, doc.getLength());
		// Parsing procedures
		IToken token = pkgScanner.nextToken();
		int beginCount = 0;
		/** Procedure declaration start offset */
		int procStartOffset = -1;
		/** A flag indicating a procedure declaration has started */
		boolean procedureDecl = false;
		// Procedure declaration parsing variables
		IProcedureParameter currentParam = null;
		boolean paramDefinition = false;
		boolean returnDefinition = false;
		/** The previous token */
		IToken previousToken = null;
		/** currently parsed procedure */
		IProcedure currentProc = null;
		/** Currently parsed variable */
		IVariable currentVar = null;
		/** Pending END */
		boolean pendingEnd = false;
		boolean started = false;
		while (!token.isEOF()) {
			if (!started) {
				if (token == PackageScanner.PROC_ENDSPEC_TOKEN) {
					started = true;
				}
				token = pkgScanner.nextToken();
				continue;
			}
			if (token == PackageScanner.PROC_ENDSPEC_TOKEN && procStartOffset > -1) {
				procedureDecl = false;
				currentProc.setHeader(doc.get(procStartOffset, pkgScanner.getTokenOffset()
						- procStartOffset));
				returnDefinition = false;
				procStartOffset = -1;
			} else if (procedureDecl) {
				if (token == PackageScanner.DECLSTART_TOKEN) {
					paramDefinition = true;
				} else if (token == PackageScanner.DECLEND_TOKEN) {
					currentProc.addParameter(currentParam);
					currentParam = null;
					paramDefinition = false;
				} else if (paramDefinition) {
					if (token == PackageScanner.NEWPARAM_TOKEN) {
						currentProc.addParameter(currentParam);
						currentParam = null;
					} else if (previousToken == PackageScanner.ASSIGN_TOKEN) {
						currentParam.setDefaultExpr(doc.get(pkgScanner.getTokenOffset(),
								pkgScanner.getTokenLength()));
					} else if (token == PackageScanner.WORD_TOKEN) {
						String word = doc.get(pkgScanner.getTokenOffset(),
								pkgScanner.getTokenLength());
						if (currentParam == null) {
							currentParam = new ProcedureParameter(word, null, null);
						} else {
							currentParam.setDatatype(new Datatype(word));
						}
					} else if (token.getData() instanceof ParameterType && currentParam != null) {
						ParameterType paramType = (ParameterType) token.getData();
						if (previousToken.getData() instanceof ParameterType) {
							ParameterType prevParamType = (ParameterType) previousToken.getData();
							if (prevParamType == ParameterType.IN && paramType == ParameterType.OUT) {
								paramType = ParameterType.INOUT;
							}
						}
						currentParam.setParameterType(paramType);
					}
				} else if (token == PackageScanner.RETURN_TOKEN && !paramDefinition) {
					returnDefinition = true;
				} else if (returnDefinition && token == PackageScanner.WORD_TOKEN) {
					// Here we are defining the return datatype
					if (currentProc.getReturnType() == null) {
						currentProc.setReturnType(new Datatype(doc.get(pkgScanner.getTokenOffset(),
								pkgScanner.getTokenLength())));
					} else {
						IDatatype d = currentProc.getReturnType();
						d.setName(d.getName() + " " //$NON-NLS-1$
								+ doc.get(pkgScanner.getTokenOffset(), pkgScanner.getTokenLength()));
					}
				}
			} else if (token == PackageScanner.WORD_TOKEN) {
				// If we have a pending 'END' tag, we only check the word to
				// know if it is a closure or a 'end if' like tag.
				// END will always be closed by the SEMICOLON rule
				if (pendingEnd) {
					String word = doc.get(pkgScanner.getTokenOffset(), pkgScanner.getTokenLength());
					if (currentProc != null) {
						if (!word.equals(currentProc.getName())) {
							pendingEnd = false;
						}
					}
				}

				// If we have started a proc we initialize it
				if (previousToken == PackageScanner.PROC_TOKEN) {
					// We got the proc / func name
					currentProc = new LightProcedure(doc.get(pkgScanner.getTokenOffset(),
							pkgScanner.getTokenLength()), null);
					type.addProcedure(currentProc);
					// Starting declaration
					procedureDecl = true;
					currentParam = null;
				} else if (currentVar == null && beginCount == 0) {
					currentVar = new Variable(doc.get(pkgScanner.getTokenOffset(),
							pkgScanner.getTokenLength()));
					if (currentProc != null) {
						currentProc.addVariable(currentVar);
					}
				} else if (beginCount == 0 && currentVar.getDatatypeName() == null) {
					currentVar.setDatatypeName(doc.get(pkgScanner.getTokenOffset(),
							pkgScanner.getTokenLength()));
				}
			} else if (currentVar != null && token == PackageScanner.SEMICOLON_TOKEN) {
				if (currentVar != null) {
					parseData.setPosition(currentVar, new TextPosition(pkgScanner.getTokenOffset()
							- currentVar.getName().length(), currentVar.getName().length()));
				}
				currentVar = null;
				if (pendingEnd) {
					beginCount--;
					// if(beginCount <= 0 && currentProc != null) {
					// // Setting parse data
					// parseData.setPosition(currentProc, new
					// Position(procStartOffset,pkgScanner.getTokenOffset() +
					// pkgScanner.getTokenLength() - procStartOffset));
					// // Resetting proc
					// currentProc = null;
					// }
					pendingEnd = false;
				}
			} else if (token == PackageScanner.PROC_TOKEN && procStartOffset == -1) {
				// Nothing, check is made against previous token in a word token
				procStartOffset = pkgScanner.getTokenOffset();
			} else if (token == PackageScanner.BEGIN_TOKEN) {
				beginCount++;
			} else if (token == PackageScanner.END_TOKEN) {
				pendingEnd = true;
			} else if (token == PackageScanner.PROC_MEMBER_TOKEN && procStartOffset == -1) {
				procStartOffset = pkgScanner.getTokenOffset();
			}
			// Always set our previous token and switch to next one
			previousToken = token.isUndefined() || token.isWhitespace() ? previousToken : token;
			token = pkgScanner.nextToken();
		}

		// Updating parse status
		return parseData;
	}

	private IDocument createDocument(String content) {
		IDocument document = new Document();
		if (content != null) {
			document.set(content.toUpperCase());
		} else {
			document.set(""); //$NON-NLS-1$
		}
		return document;
	}

	@Override
	protected String getNameDelimiterTag() {
		return "body"; //$NON-NLS-1$
	}

}
