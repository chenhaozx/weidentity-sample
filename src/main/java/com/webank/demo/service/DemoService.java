package com.webank.demo.service;

import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.Credential;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;

/**
 * demo interface.
 * @author v_wbgyang
 *
 */
public interface DemoService {
    
    /**
     * 创建weId并且set相关属性
     */
    public ResponseData<CreateWeIdDataResult> createWeIdWithSetAttr();
    
    /**
     * 注册为权威机构
     * @param authorityName
     * @return
     */
    public ResponseData<Boolean> registerAuthorityIssuer(String issuer, String authorityName);
    
    /**
     * 注册cpt模版
     */
    public ResponseData<CptBaseInfo> registCpt(String publisher, String privateKey);
    
    /**
     * 创建电子凭证
     */
    public ResponseData<Credential> createCredential(Integer cptId, String issuer, String privateKey);
    
    /**
     * 验证电子凭证
     */
    public ResponseData<Boolean> verifyCredential(String credentialJson);

}
