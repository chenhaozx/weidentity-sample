package com.webank.demo.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;
import com.webank.demo.service.impl.DemoServiceImpl;

/**
 * 读文件工具.
 * 
 * @author v_wbgyang
 *
 */
public class FileUtil {

    /**
     * 从文件中读取字符串数据.
     * 
     * @param fileName 文件名 默认在CLASSPATH
     * @return
     */
    public static String getJsonFromFile(String fileName) {
        BufferedReader br = null;
        try {
            URL fileUrl = DemoServiceImpl.class.getClassLoader().getResource(fileName);
            String filePath = fileUrl.getFile();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            StringBuffer jsonStr = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                jsonStr.append(line);
            }
            return jsonStr.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 此方法为通过文件存储weId私钥信息，实际场景机构自行存储私钥信息
     * 
     * @param path 存储路径
     * @param weId  weId
     * @param privateKey 私钥
     * @return
     */
    public static boolean savePrivateKey(String path, String weId, String privateKey){
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
            e.printStackTrace();
        } 
        return false;    
    }
    
    /**
     * 根据weId获取私钥信息
     * 
     * @param path
     * @param weId
     * @param privateKey
     * @return
     */
    public static String getPrivateKeyByWeId(String path, String weId){
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
            e.printStackTrace();
        } 
        return StringUtils.EMPTY;
    }
    
    /**
     * 检查路径是否存在
     * @param path
     * @return
     */
    private static String checkDir(String path){
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

    public static String getDataByPath(String path){
        BufferedReader bufferedReader = null;
        String privKey = null;
        try {
            bufferedReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path)));
            privKey = bufferedReader.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return privKey;
    }
}
