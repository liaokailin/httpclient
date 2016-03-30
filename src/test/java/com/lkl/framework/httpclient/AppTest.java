package com.lkl.framework.httpclient;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class AppTest {

    Request.Builder buider = new Request.Builder();
    Logger          LOGGER = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void testGet() {
        LOGGER.info(buider.url("http://www.baidu.com").groupKey("baidu").commandKey("baidu-index").get().command()
            .execute());
    }

    @Test
    public void testGetHeaders() {
        LOGGER.info(buider.url("http://localhost:4040/test?").groupKey("baidu").commandKey("baidu-index")
            .header("name", "liaokailin").header("age", "26").get().command().execute());
    }

    @Test
    public void testPostParams() {
        LOGGER.info(buider.url("http://localhost:4040/test?").groupKey("baidu").commandKey("baidu-index")
            .param("name", "liaokailin").param("age", "26").post().command().execute());
    }

    @Test
    public void testPostHeader() {
        LOGGER.info(buider.url("http://localhost:4040/test?").groupKey("baidu").commandKey("baidu-index")
            .param("name", "liaokailin").header("age", "26").post().command().execute());
    }
}
