/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.modelrepository;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.osgi.framework.Bundle;

/**
 * Logging Support
 * 
 * @author Phillip Beauvoir
 */
@SuppressWarnings("nls")
class LoggingSupport {

    private static final int MAX_BYTES = 1024 * 1024 * 10; // 10 mb files
    private static final int FILE_COUNT = 3; // Max files to use
    private static final String FILENAME_FORMAT = "log-%g.txt"; // File name format

    // 1 time
    // 2 source
    // 3 logger name
    // 4 level
    // 5 message
    // 6 thrown
    private static final String LOGGER_FORMAT = "[%1$tF %1$tH:%1$tM:%1$tS.%1$tL] %4$-7s [%2$s] %5$s%6$s%n"; // Formatter
    
    private static FileHandler fileHandler;
    
    static void init(Bundle bundle) throws SecurityException, IOException {
        // Root logger - should be "com.archimatetool.modelrepository"
        Logger rootLogger = Logger.getLogger(bundle.getSymbolicName());
        
        // Don't use parent handlers so no logging to console
        rootLogger.setUseParentHandlers(false);
        
        // Set the format for SimpleFormatter
        System.setProperty("java.util.logging.SimpleFormatter.format", LOGGER_FORMAT);
        
        // Set file handler on root logger
        String filePattern = ModelRepositoryPlugin.INSTANCE.getUserModelRepositoryFolder().getPath() + "/" + FILENAME_FORMAT;
        fileHandler = new FileHandler(filePattern, MAX_BYTES, FILE_COUNT, true); // 10mb
        fileHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(fileHandler);
        
        // We could add this, but do we want to add a log entry every time Archi is launched and perhaps coArchi is not used? 
        // String coArchiversion = bundle.getVersion().toString();
        // String archiVersion = ArchiPlugin.INSTANCE.getBundle().getVersion().toString();
        // rootLogger.log(Level.INFO, "Starting coArchi {0} with Archi v{1}", new Object[]{coArchiversion, archiVersion});
    }

    static void close() {
        if(fileHandler != null) {
            fileHandler.close();
        }
    }
    
}
