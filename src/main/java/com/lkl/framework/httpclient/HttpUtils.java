package com.lkl.framework.httpclient;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http请求工具类
 * 
 * @author liaokailin
 * @version $Id: HttpRequestCommand.java, v 0.1 2016年3月11日 下午4:39:45 liaokailin Exp $
 */
public class HttpUtils {
    private final static Logger                       LOGGER             = LoggerFactory.getLogger(HttpUtils.class);

    private static PoolingHttpClientConnectionManager connManager        = null;
    private static CloseableHttpClient                httpclient         = null;

    public final static int                           getFromPoolTimeout = 4000;
    public final static int                           connectTimeout     = 5000;
    public final static int                           readTimeout        = 6000;

    static {
        try {
            SSLContext sslContext = SSLContexts.custom().useTLS().build();
            sslContext.init(null, new TrustManager[] { new X509TrustManager() {

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            } }, null);

            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {

                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })).build();

            connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            httpclient = HttpClients.custom().setConnectionManager(connManager).build();
            // Create socket configuration
            SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
            connManager.setDefaultSocketConfig(socketConfig);
            // Create message constraints
            MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200)
                .setMaxLineLength(2000).build();
            // Create connection configuration
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE).setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints).build();
            connManager.setDefaultConnectionConfig(connectionConfig);
            connManager.setMaxTotal(200);
            connManager.setDefaultMaxPerRoute(2);
        } catch (KeyManagementException e) {
            LOGGER.error("KeyManagementException", e.toString());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("NoSuchAlgorithmException", e.toString());
        }
    }

    public static String getMethod(String url, Map<String, String> headers) throws Exception {
        String responseString = null;
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeout)
            .setConnectTimeout(connectTimeout).setConnectionRequestTimeout(getFromPoolTimeout).build();

        HttpGet get = new HttpGet(url);
        get.setConfig(requestConfig);
        if (headers != null) {
            Set<String> set = new HashSet<String>();
            set = headers.keySet();
            for (String key : set) {
                get.setHeader(key, headers.get(key));
            }
        }
        try {
            CloseableHttpResponse response = httpclient.execute(get);
            try {
                HttpEntity entity = response.getEntity();
                try {
                    if (entity != null) {
                        responseString = EntityUtils.toString(entity, Consts.UTF_8);
                    }
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("[HttpUtils Get]get response error, url:%s", url), e);
                return responseString;
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (SocketTimeoutException e) {
            LOGGER.error(String.format("[HttpUtils Get]invoke get timout error, url:%s", url), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(String.format("[HttpUtils Get]invoke get error, url:%s", url), e);
            throw e;
        } finally {
            get.releaseConnection();
        }
        return responseString;
    }

    public static String postMethod(String reqURL, Map<String, String> params, Map<String, String> headers)
                                                                                                           throws Exception {

        String responseContent = null;

        HttpPost httpPost = new HttpPost(reqURL);
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeout)
                .setConnectTimeout(connectTimeout).setConnectionRequestTimeout(getFromPoolTimeout).build();
            if (params != null) {
                List<NameValuePair> formParams = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(formParams, Consts.UTF_8));
            }
            if (headers != null) {
                Set<String> set = headers.keySet();
                for (String key : set) {
                    httpPost.setHeader(key, headers.get(key));
                }
            }
            httpPost.setConfig(requestConfig);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                // 执行POST请求
                HttpEntity entity = response.getEntity(); // 获取响应实体
                try {
                    if (null != entity) {
                        responseContent = EntityUtils.toString(entity, Consts.UTF_8);
                    }
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (ClientProtocolException e) {
            LOGGER.error("ClientProtocolException", e);
            throw e;
        } catch (IOException e) {
            LOGGER.error("IOException", e);
            throw e;
        } finally {
            httpPost.releaseConnection();
        }
        return responseContent;

    }

    public static String postMethod(String reqURL, String body, Map<String, String> headers) throws Exception {

        String responseContent = null;

        HttpPost httpPost = new HttpPost(reqURL);
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(readTimeout)
                .setConnectTimeout(connectTimeout).setConnectionRequestTimeout(getFromPoolTimeout).build();

            if (body != null) {
                httpPost.setEntity(new StringEntity(body, ContentType.create(
                    ContentType.APPLICATION_JSON.getMimeType(), Consts.UTF_8)));
            }
            if (headers != null) {
                Set<String> set = headers.keySet();
                for (String key : set) {
                    httpPost.setHeader(key, headers.get(key));
                }
            }
            httpPost.setConfig(requestConfig);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            try {
                // 执行POST请求
                HttpEntity entity = response.getEntity(); // 获取响应实体
                try {
                    if (null != entity) {
                        responseContent = EntityUtils.toString(entity, Consts.UTF_8);
                    }
                } finally {
                    if (entity != null) {
                        entity.getContent().close();
                    }
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (ClientProtocolException e) {
            LOGGER.error("ClientProtocolException", e);
            throw e;
        } catch (IOException e) {
            LOGGER.error("IOException", e);
            throw e;
        } finally {
            httpPost.releaseConnection();
        }
        return responseContent;

    }

}
