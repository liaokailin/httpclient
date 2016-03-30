package com.lkl.framework.httpclient;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * 请求request
 * 
 * @author liaokailin
 * @version $Id: Request.java, v 0.1 2016年3月11日 下午4:32:22 liaokailin Exp $
 */
public class Request {
    private String              groupKey;  //组名
    private String              commandKey; //命令名
    private String              method;
    private String              url;
    private Map<String, String> headers;
    private String              body;
    private Map<String, String> params;
    private Setter              setter;

    private Request() {
    }

    /**
     * 请求构造器
     * 
     * @author liaokailin
     * @version $Id: Request.java, v 0.1 2016年3月11日 下午5:16:48 liaokailin Exp $
     */
    public static final class Builder {
        private Request request;

        public Builder() {
            this.request = new Request();
        }

        public Builder groupKey(String groupKey) {
            if (StringUtils.isEmpty(this.request.groupKey)) {
                this.request.groupKey = groupKey;
            }
            return this;
        }

        public Builder commandKey(String commandKey) {
            if (StringUtils.isEmpty(this.request.commandKey)) {
                this.request.commandKey = commandKey;
            }
            return this;
        }

        public Builder param(String key, String value) {
            if (this.request.params == null) {
                this.request.params = new HashMap<String, String>();
            }
            this.request.params.put(key, value);
            return this;
        }

        public Builder params(Map<String, String> params) {
            this.request.params = params;
            return this;
        }

        public Builder header(String key, String value) {
            if (this.request.headers == null) {
                this.request.headers = new HashMap<String, String>();
            }
            this.request.headers.put(key, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.request.headers = headers;
            return this;
        }

        public Builder body(String body) {
            if (StringUtils.isEmpty(this.request.body)) {
                this.request.body = body;
            }
            return this;
        }

        public Builder url(String url) {
            if (StringUtils.isEmpty(this.request.url)) {
                this.request.url = url;
            }
            return this;
        }

        public Builder setter(Setter setter) {
            this.request.setter = setter;
            return this;
        }

        public Request get() {
            Preconditions.checkArgument(this.request.params == null, "params must be null with get method.");
            Preconditions.checkArgument(this.request.body == null, "params must be null with get method.");
            this.request.method = Method.get.name();
            return this.request;
        }

        public Request post() {
            this.request.method = Method.post.name();
            return this.request;
        }

    }

    public HttpRequestCommand command() {
        if (this.setter == null) {
            Preconditions.checkNotNull(this.groupKey, "groupKey must not be null");
            Preconditions.checkNotNull(this.commandKey, "commandKey must not be null");
        }
        Preconditions.checkNotNull(this.url, "url must not be null");
        Preconditions.checkNotNull(this.method, "method must not be null");
        return new HttpRequestCommand(this);
    }

    enum Method {
        post, get
    }

    public Setter getHystrixSetter() {
        if (this.setter != null) {
            return this.setter;
        }
        return Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey(this.groupKey))
            .andCommandKey(HystrixCommandKey.Factory.asKey(this.commandKey))
            .andCommandPropertiesDefaults(
                HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(6 * 1000)) //设置超时
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withRequestCacheEnabled(false)); //不启用缓存  可以不配置。
    }

    public String execute() throws Exception {

        if (Method.get.name().equals(this.method)) {
            return HttpUtils.getMethod(url, headers);
        } else if (Method.post.name().equals(this.method)) {
            if (StringUtils.isNotBlank(this.body)) {
                return HttpUtils.postMethod(url, body, headers);
            } else {
                return HttpUtils.postMethod(url, params, headers);
            }
        } else {
            throw new IllegalArgumentException("未知的请求方式:" + this.method);
        }
    }

    @Override
    public String toString() {

        return "Request [groupKey=" + groupKey + ", commandKey=" + commandKey + ", method=" + method + ", url=" + url
               + ", headers=" + headers + ", body=" + body + ", params=" + params + "]";
    }

}
