package org.opennms.netmgt.util.spikehunter;

import java.util.Collection;
import java.util.List;

import org.jrobin.core.Sample;

public interface DataAnalyzer {
	public List<Integer> findSamplesInViolation(double[] values);
	public void setParms(List<Double> parms);
	public void setVerbose(boolean v);
}
