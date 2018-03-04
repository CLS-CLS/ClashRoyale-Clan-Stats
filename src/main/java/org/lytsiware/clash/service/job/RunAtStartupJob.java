package org.lytsiware.clash.service.job;

public interface RunAtStartupJob {
	boolean shouldRun();
	void run();
	
	
}
