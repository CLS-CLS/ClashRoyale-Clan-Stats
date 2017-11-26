package org.lytsiware.clash.service.job;

public interface RunAtStartupJob extends Job {
	boolean shouldRun();
	
	
}
