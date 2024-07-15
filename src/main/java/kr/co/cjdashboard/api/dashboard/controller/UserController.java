package kr.co.cjdashboard.api.dashboard.controller;

import kr.co.cjdashboard.api.dashboard.model.LoginDto;
import kr.co.cjdashboard.api.dashboard.model.res.CommonResult;
import kr.co.cjdashboard.api.dashboard.service.ResponseService;
import kr.co.cjdashboard.exception.InvalidException;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.UUID;

import static kr.co.cjdashboard.util.MessageUtil.exceptionMessage;

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
 * packageName    : kr.co.cjdashboard.api.dashboard.controller
 * fileName       : DashboardController
 * author         : darren
 * date           : 6/13/24
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 6/13/24        darren       최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
public class UserController {

    @Value("${grafana.host}")
    private String uris;

    private final ResponseService responseService;



    @PostMapping("/login")
    public CommonResult login(@RequestBody LoginDto loginDto, HttpServletResponse res, HttpSession session) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        String auth = loginDto.getUsername() + ":" + loginDto.getPassword();
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uris + "/api/login/ping", HttpMethod.GET, entity, String.class);
            checkSessionLimit(session);
            Cookie cookie = new Cookie("JSESSIONID", session.getId());
            res.addCookie(cookie);
            session.setAttribute("session", UUID.randomUUID().toString());
            return response.getStatusCode() == HttpStatus.OK ? responseService.getSuccessResult("Login successful") : responseService.getSuccessResult("Invalid ID/PW");
        } catch (Exception e) {
            throw new InvalidException(exceptionMessage("login.fail"));
        }
    }


//    @PostMapping("/login")
//    public CommonResult login(@RequestBody LoginDto loginDto, HttpSession session) {
//        try {
//
//            BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
//            credsProv.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(loginDto.getUsername(), loginDto.getPassword()));
//            List<URI> uriList = Stream.of(uris.split(","))
//                    .map(String::trim)
//                    .map(this::createUri)
//                    .collect(Collectors.toList());
//
//            List<HttpHost> hosts = uriList.stream()
//                    .map(this::createHttpHost)
//                    .collect(Collectors.toList());
//            RestClientBuilder restClientBuilder = RestClient.builder(hosts.toArray(new HttpHost[0]))
//                    .setHttpClientConfigCallback(httpClientBuilder -> {
//
//                            SSLContext sslContext = disableSslVerification();
//
//                            // Install the all-trusting host verifier
//                            httpClientBuilder.setSSLContext(sslContext)
//                                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
//
//
//                        return httpClientBuilder.setDefaultCredentialsProvider(credsProv);
//                    });
//            RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClientBuilder);
//            MainResponse response = restHighLevelClient.info(RequestOptions.DEFAULT);
//            // 로그인 성공 시 세션 저장
//            checkSessionLimit(session);
//            session.setAttribute("session", UUID.randomUUID().toString());
//            System.out.println("ID = " + session.getId());
//            return responseService.getSuccessResult("Login successful");
//        } catch (Exception e) {
//            throw new InvalidException(exceptionMessage("login.fail"));
//        }
//    }

    @GetMapping("/logout")
    public CommonResult logout(HttpSession session) {
        session.invalidate();
        return responseService.getSuccessResult("Logout successful");
    }

    private void checkSessionLimit(HttpSession session) {
        // 현재 세션 수를 확인하고 5개가 넘으면 모두 무효화
        // (여기에서는 예제이므로 간단히 currentSessions를 관리하는 것으로 가정)
        int currentSessions = session.getServletContext().getAttribute("currentSessions") == null ? 0 :
                (int) session.getServletContext().getAttribute("currentSessions");
        if (currentSessions >= 5) {
            session.getServletContext().setAttribute("currentSessions", 0); // 모든 세션 초기화
        } else {
            session.getServletContext().setAttribute("currentSessions", currentSessions + 1);
        }
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

    public static SSLContext disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            return sc;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

}
