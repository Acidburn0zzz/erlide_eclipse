/*******************************************************************************
 * Copyright (c) 2007 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class ErlLogger {

    private static final ErlSimpleFormatter SIMPLE_FORMATTER = new ErlSimpleFormatter();
    private static final ErlLogger INSTANCE = new ErlLogger();

    private Logger logger;
    private String logFile;
    private ConsoleHandler consoleHandler = null;
    private FileHandler fileHandler = null;

    public static ErlLogger getInstance() {
        return INSTANCE;
    }

    public void dispose() {
        logger = null;
    }

    public final void setLogFile(final String file) {
        logger.removeHandler(fileHandler);
        final IPath wroot = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        logFile = file == null ? wroot.append("erlide.log").toPortableString() : file;
        addFileHandler(logFile, SIMPLE_FORMATTER);
    }

    public String getLogFile() {
        return logFile;
    }

    public void log(final Level kind, final String fmt, final Object... o) {
        final String str = o.length == 0 ? fmt : String.format(fmt, o);
        final String msg = getCallerMsg(str);
        if (logger != null) {
            logger.log(kind, msg);
            if (logger.getHandlers().length == 0) {
                System.out.println(msg);
            }
        }
    }

    public void log(final Level kind, final Throwable exception) {
        final String str = exception.getMessage();
        final String msg = getCallerMsg(str);
        if (logger != null) {
            logger.log(kind, msg, exception);
            if (logger.getHandlers().length == 0) {
                System.out.println(msg);
            }
        }
    }

    private String getCallerMsg(final String str) {
        final StackTraceElement el = getCaller();
        final String msg = "(" + el.getFileName() + ":" + el.getLineNumber() + ") : "
                + str;
        return msg;
    }

    public void erlangLog(final String module, final int line, final String skind,
            final String fmt, final Object... o) {
        final Level kind = Level.parse(skind);
        final String str = o.length == 0 ? fmt : String.format(fmt, o);
        final String msg = "(" + module + ":" + line + ") : " + str;
        if (logger != null) {
            logger.log(kind, msg);
            if (logger.getHandlers().length == 0) {
                System.out.println(msg);
            }
        }
    }

    public static void trace(final String tag, final String fmt, final Object... o) {
        info("USAGE: " + tag + ": " + fmt, o);
    }

    public static void debug(final String fmt, final Object... o) {
        getInstance().log(Level.FINEST, fmt, o);
    }

    public static void info(final String fmt, final Object... o) {
        getInstance().log(Level.INFO, fmt, o);
    }

    public static void warn(final String fmt, final Object... o) {
        getInstance().log(Level.WARNING, fmt, o);
    }

    public static void error(final String fmt, final Object... o) {
        getInstance().log(Level.SEVERE, fmt, o);
    }

    public static void debug(final Throwable e) {
        getInstance().log(Level.FINEST, e);
    }

    public static void info(final Throwable e) {
        getInstance().log(Level.INFO, e);
    }

    public static void warn(final Throwable e) {
        getInstance().log(Level.WARNING, e);
    }

    public static void error(final Throwable exception) {
        getInstance().log(Level.SEVERE, exception);
    }

    private ErlLogger() {
        logger = Logger.getLogger("org.erlide");
        logger.setUseParentHandlers(false);
        logger.setLevel(java.util.logging.Level.FINEST);
        addConsoleHandler(SIMPLE_FORMATTER);
    }

    private void addConsoleHandler(final ErlSimpleFormatter formatter) {
        consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        final Level lvl = "true".equals(System.getProperty("erlide.logger.console.debug"))
                ? java.util.logging.Level.FINEST : java.util.logging.Level.WARNING;
        consoleHandler.setLevel(lvl);
        logger.addHandler(consoleHandler);
    }

    private void addFileHandler(final String fileName,
            final ErlSimpleFormatter formatter) {
        try {
            fileHandler = new FileHandler(fileName);
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(java.util.logging.Level.FINEST);
            logger.addHandler(fileHandler);
        } catch (final SecurityException e) {
            ErlLogger.error(e);
        } catch (final IOException e) {
            ErlLogger.error(e);
        }
    }

    private static StackTraceElement getCaller() {
        final StackTraceElement[] st = new Throwable().getStackTrace();
        StackTraceElement el = null;
        int i = 2;
        do {
            el = st[i++];
        } while (el.getClassName().contains("Logger"));
        return el;
    }

    public static class ErlSimpleFormatter extends Formatter {

        Date dat = new Date();
        private static final String FORMAT_STRING = "{0,time,HH:mm:ss,SSS}";
        private MessageFormat formatter;

        private final Object[] args = new Object[1];

        private final String lineSeparator = System.getProperty("line.separator");

        @Override
        public synchronized String format(final LogRecord record) {
            final StringBuffer sb = new StringBuffer();
            // Minimize memory allocations here.
            dat.setTime(record.getMillis());
            args[0] = dat;
            final StringBuffer text = new StringBuffer();
            if (formatter == null) {
                formatter = new MessageFormat(FORMAT_STRING);
            }
            formatter.format(args, text, null);
            sb.append(text);
            sb.append(' ');
            final String message = formatMessage(record);
            sb.append(record.getLevel().toString().charAt(0));
            sb.append(": ");
            sb.append(message);
            sb.append(lineSeparator);
            if (record.getThrown() != null) {
                try (final StringWriter sw = new StringWriter();
                        final PrintWriter pw = new PrintWriter(sw)) {
                    record.getThrown().printStackTrace(pw);
                    sb.append(sw.toString());
                } catch (final Exception ex) {
                    // ignore
                }
            }
            return sb.toString();
        }
    }

}
