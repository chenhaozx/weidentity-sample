package com.webank.demo.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读取配置文件
 * @author v_wbgyang
 *
 */
public class PropertiesUtils {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtils.class);
    private static Properties props;

    static {
        loadProps();
    }

    synchronized static private void loadProps() {
        props = new Properties();
        InputStream in = null;
        try {
            InputStream resourceAsStream = PropertiesUtils.class.getClassLoader()
                .getResourceAsStream("application.properties");
            try {
                props.load(resourceAsStream);
            } catch (IOException e) {
                logger.error("loadProps error", e);
            }
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                logger.error("loadProps error", e);
            }
        }
    }

    /**
     * 根据key读取配置文件中的数据
     * @param key 配置的key
     * @return 返回配置的value值
     */
    public static String getProperty(String key) {
        if (null == props) {
            loadProps();
        }
        return props.getProperty(key);
    }

    /**
     * 根据key读取配置文件中的数据,获取不到返回默认值
     * @param key 配置的key
     * @param defaultValue 默认值
     * @return 返回配置的value值
     */
    public static String getProperty(String key, String defaultValue) {
        if (null == props) {
            loadProps();
        }
        return props.getProperty(key, defaultValue);
    }
}
