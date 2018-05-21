package org.lytsiware.clash.service.war;

import java.io.InputStream;

public interface WarUploadService {
    String replaceNameWithTag(InputStream inputStream, String filename);

    void upload(InputStream inputStream, String fileName);
}
