package com.webank.demo.controller;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;
import org.apache.catalina.mapper.Mapper;
import org.bcos.web3j.crypto.ECKeyPair;
import org.bcos.web3j.crypto.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JsonLoader;

import com.webank.demo.common.dto.PasswordKey;
import com.webank.demo.common.util.FileUtil;
import com.webank.demo.common.util.PropertiesUtils;
import com.webank.demo.service.DemoService;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.Credential;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;

/**
 * Demo控制器.
 *
 * @author v_wbgyang
 */
@RestController
public class DemoController {

    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);

    @Autowired
    private DemoService demoService;

    private String keyDir = PropertiesUtils.getProperty("weid.keys.dir");

    /**
     * jsonSchema.
     */
    public final static String SCHEMA;


    static {
        SCHEMA = FileUtil.getJsonFromFile("JsonSchema.json");
    }

    /**
     * 无参创建weId,并设置相关属性.
     *
     * @return 返回weId和公私钥信息
     */
    @PostMapping("/createWeId")
    public ResponseData<CreateWeIdDataResult> createWeId() {
        ResponseData<CreateWeIdDataResult> response = demoService.createWeIdWithSetAttr();

        if (response.getErrorCode().intValue() == ErrorCode.SUCCESS.getCode()) {
            FileUtil.savePrivateKey(keyDir, response.getResult().getWeId(),
                response.getResult().getUserWeIdPrivateKey().getPrivateKey());
        }
        //私钥不能通过http传输
        response.getResult().setUserWeIdPrivateKey(null);
        return response;
    }

    /**
     * 创建公私钥.
     * 注意此方法作为演示通过代码创建公私钥,私钥禁止网络传输,请妥善保管好自己的私钥
     * 
     * @return 返回公私钥信息
     */
    public PasswordKey createKeys() {
        PasswordKey passwordKey = new PasswordKey();
        try {
            ECKeyPair keyPair = Keys.createEcKeyPair();
            String publicKey = String.valueOf(keyPair.getPublicKey());
            String privateKey = String.valueOf(keyPair.getPrivateKey());
            passwordKey.setPrivateKey(privateKey);
            passwordKey.setPublicKey(publicKey);
        } catch (InvalidAlgorithmParameterException e) {
            logger.error("createKeys error.", e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("createKeys error.", e);
        } catch (NoSuchProviderException e) {
            logger.error("createKeys error.", e);
        }
        return passwordKey;
    }

    /**
     * 传入自己的公私钥,创建weId,并设置相关属性.
     * 注意此方法为演示根据公私钥创建weId,避免私钥网络传入,此处未添加Mapping,仅仅作为代码演示
     *
     * @param publicKey 数字公钥
     * @param privateKey 数字私钥
     */
    public ResponseData<String> createWeIdByKeys(@RequestBody Map<String, String> paramMap) {
        String publicKey = paramMap.get("publicKey");
        String privateKey = paramMap.get("privateKey");
        logger.info("param,publicKey:{},privateKey:{}", publicKey, privateKey);

        ResponseData<String> response = demoService.createWeIdWithSetAttr(publicKey, privateKey);

        if (response.getErrorCode().intValue() == ErrorCode.SUCCESS.getCode()) {
            FileUtil.savePrivateKey(keyDir, response.getResult(),
                privateKey);
        }
        return response;
    }

    /**
     * 机构注册成功权威机构.
     *
     * @param paramMap issuer机构weId   authorityName权威机构名
     * @return 返回是否成功信息
     */
    @PostMapping("/registerAuthorityIssuer")
    public ResponseData<Boolean> registerAuthorityIssuer(
        @RequestBody Map<String, String> paramMap) {
        String issuer = paramMap.get("issuer");
        String authorityName = paramMap.get("authorityName");

        logger.info("param,issuer:{},authorityName:{}", issuer, authorityName);
        return demoService.registerAuthorityIssuer(issuer, authorityName);
    }

    /**
     * 机构发布CPT.
     *
     * @param publisher cpt发布者
     * @param privateKey cpt发布者私钥
     * @return 返回cpt编号等信息
     */
    @PostMapping("/registCpt")
    public ResponseData<CptBaseInfo> registCpt(@RequestBody String jsonStr)
        throws IOException {
        JsonNode jsonNode = JsonLoader.fromString(jsonStr);
        String publisher = jsonNode.get("publisher").textValue();
        String claim = jsonNode.get("claim").toString();
        String privateKey = FileUtil.getPrivateKeyByWeId(keyDir, publisher);
        logger.info("param,publisher:{},privateKey:{},claim:{}", publisher, privateKey, claim);
        claim = this.getJsonSchema(claim);
        return demoService.registCpt(publisher, privateKey, claim);
    }

    /**
     * 机构发布电子凭证.
     *
     * @param cptId cpt编号
     * @param issuer 机构weId
     * @param privateKey 机构私钥
     * @return 返回json格式的电子凭证
     * @throws IOException 
     */
    @PostMapping("/createCredential")
    public ResponseData<Credential> createCredential(@RequestBody String jsonStr)
        throws IOException {
        
        JsonNode jsonNode = JsonLoader.fromString(jsonStr);
        String cptIdStr = jsonNode.get("cptId").textValue();
        String issuer = jsonNode.get("issuer").textValue();
        String claimData = jsonNode.get("claimData").toString();
        Integer cptId = Integer.parseInt(cptIdStr);

        String privateKey = FileUtil.getPrivateKeyByWeId(keyDir, issuer);
        logger.info("param,cptId:{},issuer:{},privateKey:{},claimData:{}", cptId, issuer,
            privateKey, claimData);
        return demoService.createCredential(cptId, issuer, privateKey, claimData);
    }

    /**
     * 验证电子凭证.
     *
     * @param credentialJson json格式的电子凭证
     * @return 返回是否验证成功的结果
     */
    @PostMapping("/verifyCredential")
    public ResponseData<Boolean> verifyCredential(@RequestBody String credentialJson) {
        logger.info("param,credentialJson:{}", credentialJson);
        return demoService.verifyCredential(credentialJson);
    }

    /**
     * 将claim转换成schema.
     *
     * @param claim cpt
     * @return schema jsonSchema
     */
    @SuppressWarnings("deprecation")
    private String getJsonSchema(String claim) throws IOException {
        JsonNode jsonNode = JsonLoader.fromString(SCHEMA);
        ObjectNode objectNode = (ObjectNode) jsonNode;
        objectNode.put("properties", JsonLoader.fromString(claim));
        return jsonNode.toString();
    }
}
