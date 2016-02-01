package jw;

import jw.xuite.utils.HttpClientUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;

/**
 * Created by jw on 2016/2/1.
 */
public class Downloader {

    public static void main(String[] args) throws Exception {
        HttpClientUtils utils = new HttpClientUtils();
        int status = utils.crawlPage(new File("xuite.html"), "http://photo.xuite.net/shie7101");

        // parse
        Document doc = Jsoup.parse(new File("xuite.html"), "UTF-8");
        Elements imgEles = doc.body().select("div.album_item.inline-block img");
        String firstImgUrl = imgEles.first().attr("src");
        System.out.println(firstImgUrl);


        //download img
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(firstImgUrl);
        HttpResponse response = client.execute(get);
        HttpEntity entity = response.getEntity();
        InputStream io = new BufferedInputStream(entity.getContent());
        OutputStream os = new BufferedOutputStream(new FileOutputStream(new File("test.jpg")));
        int inByte;
        try {
            while ((inByte = io.read()) != -1) {
                os.write(inByte);
            }
        } finally {
            io.close();
            os.close();
        }

    }
}
