package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.poller.remote.gwt.client.utils.StringUtils;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>ApplicationState class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationState implements Serializable, IsSerializable {
	private Map<String,ApplicationDetails> m_statuses = new HashMap<String,ApplicationDetails>();

	private StatusDetails m_statusDetails;

	/**
	 * <p>Constructor for ApplicationState.</p>
	 */
	public ApplicationState() {}

	/**
	 * <p>Constructor for ApplicationState.</p>
	 *
	 * @param from a {@link java.util.Date} object.
	 * @param to a {@link java.util.Date} object.
	 * @param applications a {@link java.util.Collection} object.
	 * @param monitors a {@link java.util.List} object.
	 * @param statuses a {@link java.util.Map} object.
	 */
	public ApplicationState(final Date from, final Date to, final Collection<ApplicationInfo> applications, final List<GWTLocationMonitor> monitors, final Map<String, List<GWTLocationSpecificStatus>> statuses) {
		for (final ApplicationInfo app : applications) {
			m_statuses.put(app.getName(), new ApplicationDetails(app, from, to, monitors, statuses.get(app.getName())));
		}
	}

	/**
	 * <p>getStatusDetails</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
	 */
	public StatusDetails getStatusDetails() {
		if (m_statusDetails == null) {
			m_statusDetails = getStatusDetailsUncached();
		}
		return m_statusDetails;
	}

	private StatusDetails getStatusDetailsUncached() {
		if (m_statuses.size() == 0) {
			return StatusDetails.unknown("No applications are currently defined.");
		}
		final List<String> m_applicationsUnknown  = new ArrayList<String>();
		final List<String> m_applicationsDown     = new ArrayList<String>();
		final List<String> m_applicationsMarginal = new ArrayList<String>();
		for (final String appName : m_statuses.keySet()) {
			final ApplicationDetails status = m_statuses.get(appName);
			switch(status.getStatusDetails().getStatus()) {
				case UNKNOWN: {
					m_applicationsUnknown.add(appName);
					break;
				}
				case DOWN: {
					m_applicationsDown.add(appName);
					break;
				}
				case MARGINAL: {
					m_applicationsMarginal.add(appName);
					break;
				}
			}
		}
		if (m_applicationsUnknown.size() > 0) {
			return StatusDetails.unknown("The following applications are reporting an unknown status: " + StringUtils.join(m_applicationsUnknown, ", "));
		}
		if (m_applicationsDown.size() > 0) {
			return StatusDetails.down("The following applications are reported as down: " + StringUtils.join(m_applicationsDown, ", "));
		}
		if (m_applicationsMarginal.size() > 0) {
			return StatusDetails.marginal("The following applications are reported as marginal: " + StringUtils.join(m_applicationsMarginal, ", "));
		}
		return StatusDetails.up();
	}
}
