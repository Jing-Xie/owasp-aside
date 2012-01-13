package edu.uncc.sis.aside.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class AsideLoggingListener implements ILogListener {

	private ILog log; // Eclipse log
	private Logger logger; // log4j log

	public AsideLoggingListener(ILog log, Logger logger) {
		this.log = log;
		this.logger = logger;
		log.addLogListener(this);
	}

	@Override
	public void logging(IStatus status, String plugin) {
		if (null == this.logger || null == status)
			return;

		int severity = status.getSeverity();
		Level level = Level.DEBUG;
		if (severity == Status.ERROR)
			level = Level.ERROR;
		else if (severity == Status.WARNING)
			level = Level.WARN;
		else if (severity == Status.INFO)
			level = Level.INFO;
		else if (severity == Status.CANCEL)
			level = Level.FATAL;

		plugin = formatText(plugin);
		String statusPlugin = formatText(status.getPlugin());
		String statusMessage = formatText(status.getMessage());
		StringBuffer message = new StringBuffer();
		if (plugin != null) {
			message.append(plugin);
			message.append(" - ");
		}
		if (statusPlugin != null
				&& (plugin == null || !statusPlugin.equals(plugin))) {
			message.append(statusPlugin);
			message.append(" - ");
		}
		message.append(status.getCode());
		if (statusMessage != null) {
			message.append(" - ");
			message.append(statusMessage);
		}
		this.logger.log(level, message.toString(), status.getException());
	}

	/**
	 * Removes itself from the plug-in log, reset instance variables.
	 */
	public void dispose() {
		if (this.log != null) {
			this.log.removeLogListener(this);
			this.log = null;
			this.logger = null;
		}

	}

	static private String formatText(String text) {
		if (text != null) {
			text = text.trim();
			if (text.length() == 0)
				return null;
		}
		return text;
	}
}
