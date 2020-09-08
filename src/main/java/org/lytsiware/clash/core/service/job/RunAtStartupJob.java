package org.lytsiware.clash.core.service.job;

public interface RunAtStartupJob {
	boolean shouldRun();
	void run();
	
	
}
