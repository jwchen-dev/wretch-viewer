package jw.xuite;

import jw.xuite.utils.HttpClientUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;

/**
 * Created by jw on 2016/2/1.
 */
public class TravelImageWorker {

    private static final String URL = "http://photo.xuite.net/shie7101/19757541";
    private HttpClientUtils client = new HttpClientUtils();

    public static void main(String[] args) {
        TravelImageWorker worker = new TravelImageWorker();
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

                        //download all image
                        Elements coverLinkELes=doc.body().select(".photo_item.inline-block>a");
                        for (Element coverLinkEle:coverLinkELes){
                            String coverLink=coverLinkEle.attr("href");
                            client.download(new File("t.html"),coverLink.trim());
                            // get main phto url
                            Document imgPageDoc=Jsoup.parse(new File("t.html"),"UTF-8");
                            Elements singleImageEles=imgPageDoc.body().select(".single-show-image");
                            String singleImageUrl=singleImageEles.first().attr("src").trim();
                            client.downloadImage(new File(FilenameUtils.getBaseName(singleImageUrl)),singleImageUrl);
                            System.exit(0);
                        }
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
