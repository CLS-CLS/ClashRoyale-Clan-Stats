package org.lytsiware.clash.core.service.integration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Marker inteface to denote that the implementors parse data from a resource and transforms it to an object response
 *
 * @param <R>
 */
public interface SiteIntegrationService<R> {

    /**
     * retrieves data from a site
     */
    R retrieveData();

    /**
     * helper method to connect to a resource url, read the data and create a document
     */
    default Document createDocumentFromResource(Resource dataResource) {
        // Document result = Jsoup.parse(new
        // URL("https://clashstat.com/Home/Clan/802LU8UY"), 10000);
        try {
            URL url = dataResource.getURL();
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine).append("\n");
            }
            br.close();
            return Jsoup.parse(sb.toString(), "utf-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
