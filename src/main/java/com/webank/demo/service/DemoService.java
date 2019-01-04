package com.webank.demo.service;

import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.Credential;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;

/**
 * demo 服务接口.
 * 
 * @author v_wbgyang
 *
 */
public interface DemoService {

    /**
     * 通过自己的公私钥去创建weId.
     * 
     * @param publicKey 公钥
     * @param privateKey 私钥
     * @return 返回处理结果，成功包含weId
     */
    public ResponseData<String> createWeIdWithSetAttr(String publicKey, String privateKey);

    /**
     * 创建weId并且set相关属性.
     * 
     * @return 返回weId信息和公私钥信息
     */
    public ResponseData<CreateWeIdDataResult> createWeIdWithSetAttr();

    /**
     * 注册为权威机构.
     * 
     * @param authorityName
     * @return 返回注册结果
     */
    public ResponseData<Boolean> registerAuthorityIssuer(String issuer, String authorityName);

    /**
     * 注册cpt模版.
     * 
     * @param publisher 发布者weId
     * @param privateKey 发布者私钥
     * @param claim cpt
     * @return
     */
    public ResponseData<CptBaseInfo> registCpt(String publisher, String privateKey, String claim);

    /**
     * 创建电子凭证.
     * 
     * @param cptId cpt编号
     * @param issuer 机构weId
     * @param privateKey 机构weId私钥
     * @param claimDate cpt数据
     * @return
     */
    public ResponseData<Credential> createCredential(Integer cptId, String issuer,
            String privateKey, String claimDate);

    /**
     * 验证电子凭证.
     * 
     * @param credentialJson 电子凭证
     * @return
     */
    public ResponseData<Boolean> verifyCredential(String credentialJson);

}
