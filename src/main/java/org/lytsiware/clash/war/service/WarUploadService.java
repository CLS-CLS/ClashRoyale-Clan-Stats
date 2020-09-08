package org.lytsiware.clash.war.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface WarUploadService {
    String replaceNameWithTag(InputStream inputStream, String filename);

    void upload(MultipartFile[] files) throws IOException;

}
