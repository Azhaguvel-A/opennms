package org.opennms.util.ilr;

import java.util.Date;

public interface LogMessage {

	public abstract boolean isEndMessage();

	public abstract boolean isPersistMessage();

	public abstract boolean isPersistBeginMessage();
	
	public abstract boolean isPersistEndMessage();

	public abstract boolean isBeginMessage();

	public abstract boolean isErrorMessage();

	public abstract boolean isCollectorBeginMessage();

	public abstract boolean isCollectorEndMessage();

	public abstract Date getDate();

	public abstract String getServiceID();

	public abstract String getThread();

}