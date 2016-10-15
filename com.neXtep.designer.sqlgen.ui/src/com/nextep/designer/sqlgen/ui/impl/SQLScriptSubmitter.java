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
//package com.nextep.designer.sqlgen.ui.impl;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.regex.Matcher;
//
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.ui.PartInitException;
//
//import com.nextep.datadesigner.Designer;
//import com.nextep.datadesigner.ctrl.ControllerFactory;
//import com.nextep.datadesigner.dbgm.model.IConnection;
//import com.nextep.datadesigner.dbgm.services.DBGMHelper;
//import com.nextep.datadesigner.exception.ErrorException;
//import com.nextep.datadesigner.model.DBVendor;
//import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
//import com.nextep.datadesigner.sqlgen.model.IGenerationConsole;
//import com.nextep.datadesigner.sqlgen.model.IGenerationResult;
//import com.nextep.datadesigner.sqlgen.model.IGenerationSubmitter;
//import com.nextep.datadesigner.sqlgen.model.ISQLScript;
//import com.nextep.datadesigner.sqlgen.model.ScriptType;
//import com.nextep.datadesigner.sqlgen.model.Status;
//import com.nextep.datadesigner.sqlgen.services.BuildResult;
//import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
//import com.nextep.designer.sqlgen.SQLGenMessages;
//import com.nextep.designer.sqlgen.preferences.PreferenceConstants;
//import com.nextep.designer.sqlgen.ui.Activator;
//import com.nextep.designer.sqlgen.ui.SQLMessages;
//import com.nextep.designer.sqlgen.ui.views.GenerationConsole;
//
//public class SQLScriptSubmitter implements IGenerationSubmitter {
//
//	private IConnection conn;
//	private IGenerationConsole console;
//	private ISQLScript script;
//	private IGenerationResult genResult;
//	private boolean isRunning = false;
//	
//	public SQLScriptSubmitter(ISQLScript script,IConnection conn, IGenerationResult genResult) {
//		setSQLScript(script);
//		setConnection(conn);
//		setConsole(new GenerationConsole(script == null ? new Date().toString() : script.getName(),true));
//		this.genResult = genResult;
//	}
//	@Override
//	public IConnection getConnection() {
//		return conn;
//	}
//
//	@Override
//	public IGenerationConsole getConsole() {
//		return console;
//	}
//
//	@Override
//	public void setConnection(IConnection conn) {
//		this.conn = conn;
//	}
//
//	@Override
//	public void setConsole(IGenerationConsole console) {
//		this.console=console;
//	}
//
//	@Override
//	public BuildResult submit(final IProgressMonitor monitor) {
//		getConsole().start();
//		
//		// Checking if the generator binary is defined
//		final String binary = SQLGenUtil.getGeneratorBinary();
//		if(binary == null || "".equals(binary)) {
//			throw new ErrorException(SQLMessages.getString("Generator.NotFound"));
//		}
//		// Checking connection 
//		if(conn == null) {
//			throw new ErrorException(SQLGenMessages.getString("noDefaultTarget"));
//		}
//		// Showing generator's console
//		
//			Activator.getDefault().getWorkbench().getDisplay().syncExec(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.eclipse.ui.console.ConsoleView");
//					} catch( PartInitException ex) {
//						throw new ErrorException(ex);
//					}
//				}
//			});
//		// Displaying generator's binary location
//		getConsole().log("");
//		getConsole().log("Using generator binary '" + binary + "'...");
//
//		// generating default name
//		SimpleDateFormat f = new SimpleDateFormat("yyMMddHHmmss");
//		final String defaultName = "internalBuild_" + f.format(new Date()) ;
//		
//		// If our script is not external we make it external and save it
//		persistInternalScript(getSQLScript(), defaultName);				
//		
//		// Setting our (always) external file
//		String fullFileName = getSQLScript().getDirectory();
//		if(fullFileName.charAt(fullFileName.length()-1)!=File.separatorChar && fullFileName.charAt(fullFileName.length()-1)!='/' &&
//				fullFileName.charAt(fullFileName.length()-1)!='\\') {
//			fullFileName = fullFileName + File.separatorChar;
//		}
//		fullFileName = fullFileName +getSQLScript().getFilename(); 
//		
//		getConsole().log("Generating \"" + fullFileName + "\" on " + getConnection().getLogin() + "@" + getConnection().getServerIP() + ":" + getConnection().getSID() + ":" + getConnection().getServerPort() + " database...");
//		// Initializing the build result
//		BuildResult result = new BuildResult(new Date(),getSQLScript(),getSQLScript().getDirectory() + "/" + getSQLScript().getName() + ".log");
//		result.setStatus(Status.OK); // Default is ok, until an error is detected
//		result.buildContents(genResult);
//	    try {
//	        String line;
//			// Retrieving preferred vendor from settings
//			DBVendor vendor = DBGMHelper.getCurrentVendor(); //DBVendor.valueOf(SQLGenUtil.getPreference(PreferenceConstants.GENERATOR_VENDOR));
//			// Building executable string depending on our vendor
//			ProcessBuilder pb = null;
//			switch(vendor) {
//			case ORACLE:
//				pb = new ProcessBuilder(new String[] {binary,conn.getLogin()+ "/" + conn.getPassword() + "@" + (conn.getTnsAlias()!=null && !"".equals(conn.getTnsAlias().trim()) ? conn.getTnsAlias() : conn.getSID()), "@" + fullFileName});
////				sqlExec = binary + " " + conn.getLogin() + "/" + conn.getPassword() + "@" + conn.getSID() + " @" + fullFileName;
//				break;
//			case MYSQL:
//				pb = new ProcessBuilder(new String[] {binary, "-u" + conn.getLogin(),"-h"+conn.getServerIP(),("".equals(conn.getPassword()) || conn.getPassword() == null) ? "" : ("-p" + conn.getPassword()), "-vvv","-f","--unbuffered","-P" + conn.getServerPort()} );
////				sqlExec = binary + " -u " + conn.getLogin() + " -h " + conn.getServerIP();
////				if(conn.getPassword()!=null && !"".equals(conn.getPassword().trim())) {
////					sqlExec+=" -p" + conn.getPassword();
////				}
////				sqlExec+=" -vvv";
//////				sqlExec+=" -f --line-numbers -P " + conn.getServerPort() + " --show-warnings " + conn.getSID(); 
//				break;
//			case POSTGRE:
////				pb = new ProcessBuilder(new String[] {binary, "-U " + conn.getLogin(),"-h " + conn.getServerIP(),"-d " + conn.getSID(), "-p " + conn.getServerPort(),"-W"}  );
//				pb = new ProcessBuilder(new String[] {binary, "\"hostaddr=" + conn.getServerIP() + " port=" + conn.getServerPort() + " dbname="+conn.getSID() + " user=" + conn.getLogin() + (conn.getPassword()==null ? "" : " password=" + conn.getPassword()) + "\""} );
//				break;
//			default:
//				throw new ErrorException("Unsupported database vendor.");
//			}
//			
//			pb.redirectErrorStream(true);
//	        final Process p = pb.start(); //Runtime.getRuntime().exec(sqlExec);
//	        isRunning = true;
//	        // The control thread allows proper cancellation of the child process
//	        // from user action. Since this UI thread would wait on the readLine()
//	        // method.
//			Thread controlThread = new Thread() {
//				@Override
//				public void run() {
//					while(isRunning) {
//						if(monitor.isCanceled()) {
//							p.destroy();
//							getConsole().log(SQLMessages.getString("submitionStopped"));
//							return;
//						}
//						try {
//							Thread.sleep(500);
//						} catch(InterruptedException e) {
//							return;
//						}
//					}
//				}
//			};
//			controlThread.start();
//	        BufferedReader input =
//		          new BufferedReader
//		            (new InputStreamReader(p.getInputStream()));
////	        BufferedReader error =
////		          new BufferedReader
////		            (new InputStreamReader(p.getErrorStream())); 
//	        switch(vendor) {
//	        case MYSQL:
//				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
//				writer.write("use " + conn.getSID());
//				writer.newLine();
//				writer.write("source " + fullFileName);
//				writer.newLine();
//				writer.write("exit");
//				writer.newLine();
//				writer.flush();
//				writer.close();
//				break;
//	        case POSTGRE:
//	        	BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
//	        	if(conn.getSchema()!=null) {
//		        	w.write("set search_path to " + conn.getSchema()+";");
//		        	w.newLine();
//	        	}
//	        	w.write("\\i " + fullFileName.replaceAll(Matcher.quoteReplacement("\\"), "/"));
//	        	w.newLine();
//	        	w.write("\\q");
//	        	w.flush();
//	        	w.close();
//	        	break;
//	        }
//
//	        while ((line = input.readLine()) != null) {
//	          getConsole().log(vendor.toString() + "> " + line);
//	          if(line.indexOf("ORA-")>0 || line.indexOf("PLS-")>0 || line.indexOf("ERROR")>0) {
//	        	  result.setStatus(Status.KO);
//	          }
//	        }
//	        // End of execution
//	        isRunning = false;
//	        getConsole().log("Generator exit value was: " + p.exitValue());
//	        input.close();
//		} catch (Exception err) {
////			err.printStackTrace();
//			getConsole().log("Error during execution: " + err.getMessage());
//			result.setStatus(Status.KO);
//		}
//		// Adding our build to current build list
//		if(genResult!=null) {
//			SQLGenUtil.getInstance().addBuildResult(result);
//		}
//		getConsole().end();
//		// Invalidating errors
//		Designer.getMarkerProvider().invalidate();
//		return result;
//	}
//
//	
//	private static void persistInternalScript(ISQLScript script, String defaultName) {
//		if(!script.isExternal()) {
//			// Saving out script in temp directory
//			script.setExternal(true);
//			script.setDirectory(SQLGenUtil.getPreference(PreferenceConstants.TEMP_FOLDER));
//			if(script.getName()==null || "".equals(script.getName())) {
//				script.setName(defaultName);
//			}
//			// First persisting child scripts of any wrapper so that when the wrapper
//			// will be persisted it will generate the proper script links
//			if(script instanceof SQLWrapperScript) {
//				for(ISQLScript s : ((SQLWrapperScript)script).getChildren()) {
//					if(!s.isExternal()) {
//						if(s.getScriptType()==ScriptType.CUSTOM) {
//							persistInternalScript(s,defaultName+"i");
//						} else {
//							persistInternalScript(s,defaultName);
//						}
//							
//					}
//				}
//			}
//			ControllerFactory.getController(script.getType()).save(script);
//			// Undoing changes (directory is unrelevant for non external scripts)
//			script.setExternal(false);
//		}		
//	}
//	@Override
//	public ISQLScript getSQLScript() {
//		return script;
//	}
//	@Override
//	public void setSQLScript(ISQLScript script) {
//		this.script = script;
//	}
//	protected IGenerationResult getGenerationResult() {
//		return genResult;
//	}
//}
