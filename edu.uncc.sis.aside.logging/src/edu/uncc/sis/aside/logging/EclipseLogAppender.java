package edu.uncc.sis.aside.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

public class EclipseLogAppender extends AppenderSkeleton {

	private ILog eclipseLog; // Eclipse native log

	public EclipseLogAppender() {
		super();
	}

	@Override
	public void close() {
		this.closed = true;
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	@Override
	protected void append(LoggingEvent event) {
		if (this.layout == null) {
			this.errorHandler.error("Missing layout for appender " + this.name,
					null, ErrorCode.MISSING_LAYOUT);
			return;
		}

		String text = this.layout.format(event);

		Throwable thrown = null;
		if (this.layout.ignoresThrowable()) {
			ThrowableInformation info = event.getThrowableInformation();
			if (info != null)
				thrown = info.getThrowable();
		}

		Level level = event.getLevel();
		int severity = Status.OK;

		if (level.toInt() >= Level.ERROR_INT)
			severity = Status.ERROR;
		else if (level.toInt() >= Level.WARN_INT)
			severity = Status.WARNING;
		else if (level.toInt() >= Level.DEBUG_INT)
			severity = Status.INFO;

		this.eclipseLog.log(new Status(severity, this.eclipseLog.getBundle()
				.getSymbolicName(), level.toInt(), text, thrown));
	}

	/**
	 * Sets Eclipse native log instance
	 * 
	 * @param log
	 *            eclipse provided log for the plugin
	 */
	public void setLog(ILog log) {
		this.eclipseLog = log;
	}

}
