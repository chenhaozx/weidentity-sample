package com.webank.demo.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.weid.constant.WeIdConstant;
import com.webank.weid.protocol.base.Credential;

/**
 * command need file util
 *
 */
public class DemoUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(DemoCommand.class);
    
    /**
     * 读取电子凭证
     * @return
     */
    public static Credential getCredentialFromJson() {
        
        BufferedReader br = null;
        Credential credential = null;
        try {
            String filePath = "credential.json";
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String jsonStr = br.readLine();
            ObjectMapper objectMapper = new ObjectMapper();
            credential = objectMapper.readValue(jsonStr, Credential.class);
        } catch (IOException e) {
            logger.error("getCredentialFromJson error", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return credential;
    }

    /**
     * 保存电子凭证
     * @param credentialJson
     */
    public static String saveCredential(Credential credential) {
        
        ObjectMapper mapper = new ObjectMapper();
        String credentialJson = null;
        try {
            credentialJson = mapper.writeValueAsString(credential);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        OutputStreamWriter ow = null;
        try {
            String fileStr = "credential.json";
            File file = new File(fileStr.toString());
            if (file.exists()) {
                if (!file.delete()) {
                    logger.error("delete file fail..");
                }
            }
            ow = new OutputStreamWriter(new FileOutputStream(file), WeIdConstant.UTF_8);
            String content = new StringBuffer().append(credentialJson).toString();
            ow.write(content);
            ow.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            logger.error("writer file exception", e);
        } finally {
            if (null != ow) {
                try {
                    ow.close();
                } catch (IOException e) {
                    logger.error("io close exception", e);
                }
            }
        }
        return "";
    }
    
    /**
     * 保存临时数据
     * @param map
     */
    public static void saveTemData(Map<String, String> map) {
        ObjectMapper mapper = new ObjectMapper();
        String s = "";
        try {
            s = mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("./temp.data")));
            bufferedWriter.write(s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }    
}
