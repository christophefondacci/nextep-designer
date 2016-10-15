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
package com.nextep.datadesigner.gui.impl.rcp;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class LoggerConsole extends MessageConsole {
	private static LoggerConsole console;
	private static Log log = LogFactory.getLog(LoggerConsole.class);
	private static StringBuffer consoleBuffer= new StringBuffer(20000);
	private static MessageConsoleStream stream = null;
	public LoggerConsole(String name, ImageDescriptor imageDescriptor,
            boolean autoLifecycle) {
        super(name, imageDescriptor, autoLifecycle);
    }

	
	public static class LoggerConsoleFactory implements IConsoleFactory {

		@Override
		public void openConsole() {
			showConsole();
			if(stream==null) {
				stream = console.newMessageStream();
			}
			if(consoleBuffer.length()>0) {
				try {
					stream.write(consoleBuffer.toString());
					consoleBuffer = new StringBuffer();
				} catch(IOException e) {
					log.debug(e);
				}
			}
		}
	}
	public static class LogConsoleAppender extends AppenderSkeleton {
		
		public LogConsoleAppender() {
//			LoggerConsole cons = showConsole();
//			stream = cons.newMessageStream();
		}
		@Override
		protected void append(LoggingEvent arg0) {
			if(console==null) {
				consoleBuffer.append(getLayout().format(arg0));
				return;
			}
			try {
				if(stream==null) {
					stream = console.newMessageStream();
				}
				if(stream!=null && !stream.isClosed()) {
					stream.write(getLayout().format(arg0));
				}
			} catch(IOException e) {
				// Only debugging as this might append when disposing workbench
				log.debug(e);
			}
		}

		@Override
		public void close() {
			try {
				if(stream!=null && !stream.isClosed()) {
					stream.close();
				}
			} catch(IOException e) {
				log.error(e);
			}
		}

		@Override
		public boolean requiresLayout() {
			return true;
		}
		
	}
	public static LoggerConsole showConsole() {
        IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
        boolean exists = false;
        if (console != null) {
            IConsole[] existing = manager.getConsoles();
            for (int i = 0; i < existing.length; i++) {
                if (console == existing[i]) {
                    exists = true;
                }
            }
        } else {
            console = new LoggerConsole("neXtep console", null, true);
        }
        if (!exists) {
            manager.addConsoles(new IConsole[] { console });
        }
        manager.showConsoleView(console);
        return console;
	}
}
