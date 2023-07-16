package com.xqt.util;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.ws.Response;
import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

public abstract class Request<T,D> {

    String url;
    JSONObject headers;
    DefaultHttpClient client;
    Logger logger;
    T t;
    D d;


    public abstract D postRequest();
    public abstract D getRequest();


    D getResult(HttpResponse response) {
        String code = response.getStatusLine().getStatusCode()+"";
        String content;
        String msg;
        try {
            content = readHtmlContentFromEntity(response.getEntity());
            msg = "success";
        }catch (Exception e){
            content = "";
            msg = "exception";
        }
        if(d.getClass().equals(Map.class)){
            Map<String, String> maps = new HashMap<>();
            maps.put("code",code);
            maps.put("data", content);
            maps.put("msg",msg );
            return (D) maps;
        }else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code",code);
            jsonObject.put("data", content);
            jsonObject.put("msg",msg );
            return (D) jsonObject;
        }

    }


    D getResultException(Exception e) {
        if(d.getClass().equals(Map.class)){
            Map<String, String> maps = new HashMap<>();
            maps.put("code","-1");
            maps.put("data", "");
            maps.put("msg",e.getMessage() );
            return (D) maps;
        }else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code","-1");
            jsonObject.put("data", "");
            jsonObject.put("msg",e.getMessage() );
            return (D) jsonObject;
        }
    }



    void setHeader(HttpRequestBase requestBase){
        headers.keySet().stream().forEach(key->{requestBase.setHeader((String) key, (String) headers.get(key));});
    }

    public static DefaultHttpClient getHttpsClient(String proxyIp) {
        DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager());
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");//TLS SSL

            X509TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = client.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", 443, ssf));

            LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();
            client.setRedirectStrategy(redirectStrategy);

            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);

            client.getParams().setParameter("http.protocol.max-redirects", 2);
            client.getParams().setParameter(CoreConnectionPNames.SO_KEEPALIVE, true);
            client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

            if (StringUtils.isNotBlank(proxyIp)) {
                String[] ippool = proxyIp.split(":");
                HttpHost proxy = new HttpHost(ippool[0], Integer.parseInt(ippool[1]), "http");
                client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                AuthScope auth = new AuthScope(ippool[0], Integer.parseInt(ippool[1]));
                Credentials credentials = new org.apache.http.auth.NTCredentials("test", "test123!@#", "", "");
                client.getCredentialsProvider().setCredentials(auth, credentials);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

    public static String readHtmlContentFromEntity(HttpEntity httpEntity) throws Exception {
        String html = "";
        Header header = httpEntity.getContentEncoding();
        if (httpEntity.getContentLength() < 2147483647L) { // EntityUtils无法处理ContentLength超过2147483647L的Entity
            if (header != null && "gzip".equals(header.getValue())) {
                html = EntityUtils.toString(new GzipDecompressingEntity(httpEntity), "UTF-8");
            } else {
                html = EntityUtils.toString(httpEntity, "UTF-8");
            }
        } else {
            InputStream in = httpEntity.getContent();
            if (header != null && "gzip".equals(header.getValue())) {
                html = unZip(in, ContentType.getOrDefault(httpEntity).getCharset().toString());
            } else {
                html = readInStreamToString(in, ContentType.getOrDefault(httpEntity).getCharset().toString());
            }
            if (in != null) {
                in.close();
            }
        }
        EntityUtils.consume(httpEntity);
        return html;
    }

    public static String unZip(InputStream in, String charSet) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(in);
            byte[] _byte = new byte[1024];
            int len = 0;
            while ((len = gis.read(_byte)) != -1) {
                baos.write(_byte, 0, len);
            }
            String unzipString = new String(baos.toByteArray(), charSet);
            return unzipString;
        } finally {
            if (gis != null) {
                gis.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
    }
    public static String readInStreamToString(InputStream in, String charSet) throws IOException {
        StringBuilder str = new StringBuilder();
        String line;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, charSet));
        while ((line = bufferedReader.readLine()) != null) {
            str.append(line);
            str.append("\n");
        }
        if (bufferedReader != null) {
            bufferedReader.close();
        }
        return str.toString();
    }
}
