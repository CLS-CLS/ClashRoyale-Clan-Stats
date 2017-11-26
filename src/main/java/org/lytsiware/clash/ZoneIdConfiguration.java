package org.lytsiware.clash;

import java.time.ZoneId;

import org.springframework.util.StringUtils;

public final class ZoneIdConfiguration {
	
	
	public static final String zoneId = "";
	
	private static ZoneId defaultZoneId = ZoneId.systemDefault();
	
	public static ZoneId zoneId() {
		return StringUtils.isEmpty(zoneId) ?  defaultZoneId : ZoneId.of(zoneId); 
	}
	
	
}
