package com.webank.demo.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import com.webank.demo.service.impl.DemoServiceImpl;

public class FileUtil {
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
}
