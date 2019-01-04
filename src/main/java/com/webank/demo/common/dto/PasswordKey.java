package com.webank.demo.common.dto;

/**
 * 公私钥对象.
 * 
 * @author v_wbgyang
 *
 */
public class PasswordKey {

    /**
     * 私钥字符串
     */
    private String privateKey;
    
    /**
     * 公钥字符串
     */
    private String publicKey;

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    } 
}
