package jw.xuite.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.nio.charset.Charset;

/**
 * Use HttpClient send request and get response.
 * Created by jw on 2015/12/16.
 */
public class HttpClientUtils {
    //logging
    private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtils.class);

    //constant
    private static final String NEW_LINE = System.getProperty("line.separator");

    private CloseableHttpClient httpClient = null;
    private CookieStore cookieStore = null;
    private HttpClientContext clientContext = null;

    //timeout time
    private int timeoutInMillis = 60 * 1000;

    public HttpClientUtils() {
        httpClient = HttpClients.createDefault();
        this.clientContext = HttpClientContext.create();

        //make sure cookies is turn on
        CookieHandler.setDefault(new CookieManager());

        this.cookieStore = new BasicCookieStore();
        this.clientContext.setCookieStore(this.cookieStore);

        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectTimeout(timeoutInMillis);
        requestConfigBuilder.setConnectionRequestTimeout(timeoutInMillis);
        requestConfigBuilder.setSocketTimeout(timeoutInMillis);
        requestConfigBuilder.setRedirectsEnabled(true);
        this.clientContext.setRequestConfig(requestConfigBuilder.build());
    }

    /**
     * Get timeout time.
     */
    public int getTimeoutMillis() {
        return timeoutInMillis;
    }

    /**
     * Set timeout time.
     *
     * @param timeoutInMillis
     * @return
     */
    public HttpClientUtils setTimeoutInMillis(int timeoutInMillis) {
        this.timeoutInMillis = timeoutInMillis;
        return this;
    }

    public int crawlPage(File outputFile, String url, int timeoutInMillis) throws Exception {
        setTimeoutInMillis(timeoutInMillis);
        return crawlPage(outputFile, url);
    }

    public int crawlPage(File outputFile, String url) throws Exception {
        //create HTTP GET object
        final HttpGet GET = new HttpGet(url);
        GET.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        GET.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4,ja;q=0.2,zh-CN;q=0.2");
        GET.setHeader("User-Agent", randUA());

        //add referer
        GET.setHeader("Refere", url);

        CloseableHttpResponse responseGet = null;

        LOG.info("Fetching url: " + url);

        try {
            //get response
            responseGet = httpClient.execute(GET, clientContext);
            //get response entity
            final HttpEntity ENTITY = responseGet.getEntity();

            if (ENTITY != null) {
                //extract document charset
                Charset docCharset = extractCharset(ENTITY, Charset.forName("UTF-8"));

                final InputStream IS = ENTITY.getContent();
                String html = readPlainContent(IS, docCharset);

                //extract status code
                final int STATUS_CODE = responseGet.getStatusLine().getStatusCode();

                if (STATUS_CODE == HttpStatus.SC_OK) {
                    FileUtils.writeStringToFile(outputFile, html);
                    return 0;
                }
            }
        } catch (IOException e) {
            LOG.error("Fetch exception: ", e);
        } finally {
            if (responseGet != null) {
                responseGet.close();
            }
        }

        return 1;
    }

    /**
     * Read HTTP document text.
     *
     * @param is
     * @param charset
     * @return
     * @throws IOException
     */
    private String readPlainContent(InputStream is, Charset charset) throws IOException {
        StringBuffer buffer = new StringBuffer();

        BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));

        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                buffer.append(line).append(NEW_LINE);
            }
        } finally {
            if (is != null) {
                is.close();
            }

            if (br != null) {
                br.close();
            }
        }

        return buffer.toString();
    }

    /**
     * Extract document charset.
     *
     * @param entity
     * @param defaultCharset
     * @return
     */
    private Charset extractCharset(HttpEntity entity, Charset defaultCharset) {
        Charset docCharset = null;
        ContentType contentType = ContentType.get(entity);

        if (contentType != null) {
            docCharset = contentType.getCharset();
        }
        if (docCharset == null) {
            docCharset = defaultCharset;
        }

        return docCharset;
    }

    /**
     * Return random user agent.
     *
     * @return
     */
    private String randUA() {
        final String[] UAS = {"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1664.3 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1664.3 Safari/537.36",
                "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 7.0; InfoPath.3; .NET CLR 3.1.40767; Trident/6.0; en-IN)", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
                "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)",
                "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/4.0; InfoPath.2; SV1; .NET CLR 2.0.50727; WOW64)",
                "Mozilla/5.0 (compatible; MSIE 10.0; Macintosh; Intel Mac OS X 10_7_3; Trident/6.0)", "Mozilla/4.0 (Compatible; MSIE 8.0; Windows NT 5.2; Trident/6.0)",
                "Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)", "Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)"};
        final int RAN_NUM = RandomUtils.nextInt(0, UAS.length);

        return UAS[RAN_NUM];
    }

}
