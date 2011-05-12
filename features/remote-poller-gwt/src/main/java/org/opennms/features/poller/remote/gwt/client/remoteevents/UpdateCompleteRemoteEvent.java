package org.opennms.features.poller.remote.gwt.client.remoteevents;


/**
 * <p>UpdateCompleteRemoteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class UpdateCompleteRemoteEvent implements MapRemoteEvent {
	/**
	 * <p>Constructor for UpdateCompleteRemoteEvent.</p>
	 */
	public UpdateCompleteRemoteEvent() {
	}

	/** {@inheritDoc} */
	public void dispatch(final MapRemoteEventHandler locationManager) {
		locationManager.updateComplete();
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return "UpdateCompleteRemoteEvent[]";
	}
}
