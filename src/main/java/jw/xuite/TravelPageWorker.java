package jw.xuite;

import jw.xuite.utils.HttpClientUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;

/**
 * Created by jw on 2016/2/1.
 */
public class TravelPageWorker {

    private static final String URL = "http://photo.xuite.net/shie7101";
    private HttpClientUtils client = new HttpClientUtils();

    public static void main(String[] args) {
        TravelPageWorker worker = new TravelPageWorker();
        worker.work();
    }

    public void work() {
        boolean hasNextPage = true;
        String requestUrl = URL;


        try {
            do {
                final String FILE_NAME = FilenameUtils.getBaseName(requestUrl);
                System.out.println(FILE_NAME);
                int status = client.download(new File(FILE_NAME), requestUrl);
                if (status == 0) {
                    //parse
                    Document doc = Jsoup.parse(new File(FILE_NAME), "UTF-8");
                    Elements nextPageEles = doc.body().select("a#nav-next");
                    System.out.println(nextPageEles);
                    if (!nextPageEles.isEmpty()) {
                        String nextPage = nextPageEles.first().attr("href").trim();
                        requestUrl = nextPage;
                        hasNextPage = true;
                    } else {
                        hasNextPage = false;
                    }
                }
            } while (hasNextPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
