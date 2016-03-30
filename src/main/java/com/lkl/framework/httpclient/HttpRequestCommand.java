package com.lkl.framework.httpclient;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.HystrixCommand;

/**
 * http请求命令
 * 
 * @author liaokailin
 * @version $Id: HttpRequestCommand.java, v 0.1 2016年3月11日 下午4:39:45 liaokailin Exp $
 */
public class HttpRequestCommand extends HystrixCommand<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestCommand.class);

    private Request             request;

    public HttpRequestCommand(Request request) {
        super(request.getHystrixSetter());
        this.request = request;
    }

    @Override
    protected String run() throws Exception {
        try {
            return this.request.execute();
        } catch (Exception e) {
            LOGGER.error("Http request fail ." + request.toString(), e);
            throw e;
        }

    }

    @Override
    protected String getFallback() {
        LOGGER.error("Http request fail,invoke fallback method in hystrix.{}", request.toString());
        return StringUtils.EMPTY;
    }

}
