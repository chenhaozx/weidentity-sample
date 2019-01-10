package com.webank.demo.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.webank.weid.constant.WeIdConstant;

/**
 * file tool.
 * 
 * @author v_wbgyang
 *
 */
public class FileUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    
    /**
     * this method stores weId private key information by file and stores
     * private key information by itself in actual scene.
     * 
     * @param path save path
     * @param weId the weId
     * @param privateKey the private key
     * @return returns saved results
     */
    public static boolean savePrivateKey(String path, String weId, String privateKey) {
        try {
            if (weId == null) {
                return false;
            }
            String fileName = weId.substring(weId.lastIndexOf(":")+1);
            
            String chckPath = checkDir(path);
            String filePath = chckPath + fileName;
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(privateKey.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            logger.error("savePrivateKey error", e);
        } 
        return false;    
    }
    
    /**
     * get the private key by weId.
     * 
     * @param path the path
     * @param weId the weId
     * @return returns the private key
     */
    public static String getPrivateKeyByWeId(String path, String weId) {
        try {
            if (weId == null) {
                return StringUtils.EMPTY;
            }
            String fileName = weId.substring(weId.lastIndexOf(":")+1);
            String chckPath = checkDir(path);
            String filePath = chckPath + fileName;
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            byte[] buff = new byte[fis.available()];
            fis.read(buff);
            fis.close();
            return new String(buff);
        } catch (IOException e) {
            logger.error("getPrivateKeyByWeId error", e);
        } 
        return StringUtils.EMPTY;
    }
    
    /**
     * check the path is exists, create and return the path if it does not exist.
     * @param path the path
     * @return returns the path
     */
    public static String checkDir(String path) {
        String checkPath = path;
        if (!checkPath.endsWith("/")) {
            checkPath = checkPath + "/" ;
        }
        File checkDir = new File(checkPath);
        if (!checkDir.exists()) {
            checkDir.mkdirs();
        }
        return checkPath;
    }

    /**
     * read data from the path.
     * 
     * @param path the path
     * @return returns the data
     */
    public static String getDataByPath(String path) {
        FileInputStream fis = null;
        String str = null;
        try {
            fis = new FileInputStream(path);
            byte[] buff = new byte[fis.available()];
            fis.read(buff);
            str = new String(buff, WeIdConstant.UTF_8);
        } catch (FileNotFoundException e) {
            logger.error("getDataByPath error", e);
        } catch (IOException e) {
            logger.error("getDataByPath error", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error("getDataByPath error", e);
                }
            }
        }
        return str;
    }
}
