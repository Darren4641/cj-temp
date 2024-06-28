package kr.co.cjdashboard.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * -------------------------------------------------------------------------------------
 * ::::::'OO::::'OOO::::'OO:::'OO:'OO::::'OO:'OOOOOOOO:::'OOOOOOO::'OO::::'OO:'OO....OO:
 * :::::: OO:::'OO OO:::. OO:'OO:: OO::::.OO: OO.....OO:'OO.....OO: OO:::: OO: OOO...OO:
 * :::::: OO::'OO:..OO:::. OOOO::: OO::::.OO: OO::::.OO: OO::::.OO: OO:::: OO: OOOO..OO:
 * :::::: OO:'OO:::..OO:::. OO:::: OO::::.OO: OOOOOOOO:: OO::::.OO: OO:::: OO: OO.OO.OO:
 * OO:::: OO: OOOOOOOOO:::: OO:::: OO::::.OO: OO.. OO::: OO::::.OO: OO:::: OO: OO..OOOO:
 * :OO::::OO: OO.....OO:::: OO:::: OO::::.OO: OO::. OO:: OO::::.OO: OO:::: OO: OO:..OOO:
 * ::OOOOOO:: OO:::..OO:::: OO::::. OOOOOOO:: OO:::. OO:. OOOOOOO::. OOOOOOO:: OO::..OO:
 * :......:::..:::::..:::::..::::::.......:::..:::::..:::.......::::.......:::..::::..::
 * <p>
 * packageName    : kr.co.cjdashboard.config
 * fileName       : ElasticSearchConfig
 * author         : darren
 * date           : 6/13/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 6/13/24        darren       최초 생성
 */

@Slf4j
@Configuration
public class ElasticSearchConfig {
    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    @Value("${elasticsearch.uris}")
    private String uris;

    @Value("${elasticsearch.api.key}")
    private String apiKey;

    @Value("${elasticsearch.ssl.enabled}")
    private boolean sslEnabled;

    @Value("${elasticsearch.ssl.keystore.location}")
    private String keystoreLocation;

    @Value("${elasticsearch.ssl.keystore.password}")
    private String keystorePassword;

    @Value("${elasticsearch.ssl.truststore.location}")
    private String truststoreLocation;

    @Value("${elasticsearch.ssl.truststore.password}")
    private String truststorePassword;

    @Bean
    public RestHighLevelClient elasticsearchClient() {
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        List<URI> uriList = Stream.of(uris.split(","))
                .map(String::trim)
                .map(this::createUri)
                .collect(Collectors.toList());

        List<HttpHost> hosts = uriList.stream()
                .map(this::createHttpHost)
                .collect(Collectors.toList());

//        List<HttpHost> hosts = Stream.of(uris.split(","))
//                .map(String::trim)
//                .map(this::createHttpHost)
//                .collect(Collectors.toList());
        try {
            RestClientBuilder restClientBuilder = RestClient.builder(hosts.toArray(new HttpHost[0]));


            if(sslEnabled) {
                System.out.println("SSL enable");
                SSLContext sslContext = createSSLContext(keystoreLocation, keystorePassword, truststoreLocation, truststorePassword);
                restClientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setSSLContext(sslContext)
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .setDefaultCredentialsProvider(credsProv));

            } else {
                restClientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credsProv));
            }
            uriList.stream()
                    .map(URI::getPath)
                    .filter(path -> path != null && !path.isEmpty())
                    .findFirst()
                    .ifPresent(restClientBuilder::setPathPrefix);

            return new RestHighLevelClient(restClientBuilder);
        } catch (Exception e) {
            log.error("Elasticsearch rest client error", e);
            e.printStackTrace();
            throw new IllegalArgumentException();
        }

    }

    @Bean
    public ElasticsearchRestTemplate elasticsearchRestTemplate() throws Exception {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }

    private URI createUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI: " + uri, e);
        }
    }

    private HttpHost createHttpHost(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        return new HttpHost(host, port, scheme);
    }

    private HttpHost createHttpHost(String uri) {
        String[] parts = uri.replace("https://", "").replace("http://", "").split(":");
        System.out.println(parts[0]);
        System.out.println(parts[1]);
        String scheme = uri.startsWith("https") ? "https" : "http";
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        return new HttpHost(host, port, scheme);
    }


    private SSLContext createSSLContext(String keystorePath, String keystorePassword, String truststorePath, String truststorePassword) throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        System.out.println("keystorePath >> " + keystorePath);
        System.out.println("truststorePath >> " + truststorePath);
        try (InputStream keystoreStream = new FileInputStream(keystorePath)) {
            keystore.load(keystoreStream, !keystorePassword.isEmpty() ? keystorePassword.toCharArray() : null);
        }

        KeyStore truststore = KeyStore.getInstance("PKCS12");
        try (InputStream truststoreStream = new FileInputStream(truststorePath)) {
            truststore.load(truststoreStream, !truststorePassword.isEmpty() ? truststorePassword.toCharArray() : null);
        }

        return SSLContexts.custom()
                .loadKeyMaterial(keystore, keystorePassword.toCharArray())
                .loadTrustMaterial(truststore, null)
                .build();
    }
}



