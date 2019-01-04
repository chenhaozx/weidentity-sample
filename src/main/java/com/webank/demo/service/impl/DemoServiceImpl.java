package com.webank.demo.service.impl;

import java.io.IOException;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.webank.demo.common.util.FileUtil;
import com.webank.demo.common.util.PropertiesUtils;
import com.webank.demo.service.DemoService;
import com.webank.weid.constant.ErrorCode;
import com.webank.weid.protocol.base.AuthorityIssuer;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.Credential;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.base.WeIdPublicKey;
import com.webank.weid.protocol.request.CreateCredentialArgs;
import com.webank.weid.protocol.request.CreateWeIdArgs;
import com.webank.weid.protocol.request.RegisterAuthorityIssuerArgs;
import com.webank.weid.protocol.request.RegisterCptArgs;
import com.webank.weid.protocol.request.SetAuthenticationArgs;
import com.webank.weid.protocol.request.SetPublicKeyArgs;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;
import com.webank.weid.rpc.AuthorityIssuerService;
import com.webank.weid.rpc.CptService;
import com.webank.weid.rpc.CredentialService;
import com.webank.weid.rpc.WeIdService;

/**
 * Demo服务类
 *
 * @author v_wbgyang
 */
@Service
public class DemoServiceImpl implements DemoService {

    private static final Logger logger = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Autowired
    private AuthorityIssuerService authorityIssuerService;

    @Autowired
    private CptService cptService;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private WeIdService weIdService;

    /**
     * 获取sdk私钥存放路径
     */
    private String privKeyPath = PropertiesUtils.getProperty("admin.privKeyPath");

    /**
     * 通过自己的公私钥去创建weId
     */
    public ResponseData<String> createWeIdWithSetAttr(String publicKey, String privateKey) {

        // 1,创建weId,此方法自动创建了公私钥对
        CreateWeIdArgs createWeIdArgs = new CreateWeIdArgs();
        createWeIdArgs.setPublicKey(publicKey);
        createWeIdArgs.setWeIdPrivateKey(new WeIdPrivateKey());
        createWeIdArgs.getWeIdPrivateKey().setPrivateKey(privateKey);
        ResponseData<String> createResult = weIdService.createWeId(createWeIdArgs);
        if (createResult.getErrorCode().intValue() != ErrorCode.SUCCESS.getCode()) {
            return createResult;
        }

        CreateWeIdDataResult weIdData = new CreateWeIdDataResult();
        weIdData.setWeId(createResult.getResult());
        weIdData.setUserWeIdPrivateKey(new WeIdPrivateKey());
        weIdData.getUserWeIdPrivateKey().setPrivateKey(privateKey);
        weIdData.setUserWeIdPublicKey(new WeIdPublicKey());
        weIdData.getUserWeIdPublicKey().setPublicKey(publicKey);

        // 2,设置公钥属性
        ResponseData<Boolean> setPublicKeyRes = this.setPublicKey(weIdData);
        if (!setPublicKeyRes.getResult()) {
            createResult.setErrorCode(setPublicKeyRes.getErrorCode());
            createResult.setErrorMessage(setPublicKeyRes.getErrorMessage());
            return createResult;
        }

        // 3,设置认证者属性
        ResponseData<Boolean> setAuthenticateRes = this.setAuthenticate(weIdData);
        if (!setAuthenticateRes.getResult()) {
            createResult.setErrorCode(setAuthenticateRes.getErrorCode());
            createResult.setErrorMessage(setAuthenticateRes.getErrorMessage());
            return createResult;
        }
        return createResult;
    }

    /**
     * 注册weId
     */
    @Override
    public ResponseData<CreateWeIdDataResult> createWeIdWithSetAttr() {

        // 1,创建weId,此方法自动创建了公私钥对
        ResponseData<CreateWeIdDataResult> createResult = weIdService.createWeId();
        logger.info("weIdService is result,errorCode:{},errorMessage:{}",
            createResult.getErrorCode(), createResult.getErrorMessage());

        if (createResult.getErrorCode().intValue() != ErrorCode.SUCCESS.getCode()) {
            return createResult;
        }

        // 2,设置公钥属性
        ResponseData<Boolean> setPublicKeyRes = this.setPublicKey(createResult.getResult());
        if (!setPublicKeyRes.getResult()) {
            createResult.setErrorCode(setPublicKeyRes.getErrorCode());
            createResult.setErrorMessage(setPublicKeyRes.getErrorMessage());
            return createResult;
        }

        // 3,设置认证者属性
        ResponseData<Boolean> setAuthenticateRes = this.setAuthenticate(createResult.getResult());
        if (!setAuthenticateRes.getResult()) {
            createResult.setErrorCode(setAuthenticateRes.getErrorCode());
            createResult.setErrorMessage(setAuthenticateRes.getErrorMessage());
            return createResult;
        }
        return createResult;
    }

    /**
     * 设置公钥属性
     */
    private ResponseData<Boolean> setPublicKey(CreateWeIdDataResult createWeIdDataResult) {

        SetPublicKeyArgs setPublicKeyArgs = new SetPublicKeyArgs();
        setPublicKeyArgs.setWeId(createWeIdDataResult.getWeId());
        setPublicKeyArgs.setPublicKey(createWeIdDataResult.getUserWeIdPublicKey().getPublicKey());
        setPublicKeyArgs.setType("secp256k1");
        setPublicKeyArgs.setUserWeIdPrivateKey(new WeIdPrivateKey());
        setPublicKeyArgs.getUserWeIdPrivateKey()
            .setPrivateKey(createWeIdDataResult.getUserWeIdPrivateKey().getPrivateKey());

        ResponseData<Boolean> setResponse = weIdService.setPublicKey(setPublicKeyArgs);
        logger.info("setPublicKey is result,errorCode:{},errorMessage:{}",
            setResponse.getErrorCode(), setResponse.getErrorMessage());

        return setResponse;
    }

    /**
     * 设置认证属性
     */
    private ResponseData<Boolean> setAuthenticate(CreateWeIdDataResult createWeIdDataResult) {

        SetAuthenticationArgs setAuthenticationArgs = new SetAuthenticationArgs();
        setAuthenticationArgs.setWeId(createWeIdDataResult.getWeId());
        setAuthenticationArgs.setType("RsaSignatureAuthentication2018");
        setAuthenticationArgs
            .setPublicKey(createWeIdDataResult.getUserWeIdPublicKey().getPublicKey());
        setAuthenticationArgs.setUserWeIdPrivateKey(new WeIdPrivateKey());
        setAuthenticationArgs.getUserWeIdPrivateKey()
            .setPrivateKey(createWeIdDataResult.getUserWeIdPrivateKey().getPrivateKey());

        ResponseData<Boolean> setResponse = weIdService.setAuthentication(setAuthenticationArgs);
        logger.info("setAuthentication is result,errorCode:{},errorMessage:{}",
            setResponse.getErrorCode(), setResponse.getErrorMessage());

        return setResponse;
    }

    /**
     * 注册成为权威机构
     */
    @Override
    public ResponseData<Boolean> registerAuthorityIssuer(String issuer, String authorityName) {

        AuthorityIssuer authorityIssuerResult = new AuthorityIssuer();
        authorityIssuerResult.setWeId(issuer);
        authorityIssuerResult.setName(authorityName);
        authorityIssuerResult.setAccValue("0");

        RegisterAuthorityIssuerArgs registerAuthorityIssuerArgs = new RegisterAuthorityIssuerArgs();
        registerAuthorityIssuerArgs.setAuthorityIssuer(authorityIssuerResult);
        registerAuthorityIssuerArgs.setWeIdPrivateKey(new WeIdPrivateKey());

        String privKey = FileUtil.getDataByPath(privKeyPath);

        String privateKey = new BigInteger(privKey, 16).toString();
        registerAuthorityIssuerArgs.getWeIdPrivateKey().setPrivateKey(privateKey);

        ResponseData<Boolean> registResponse =
            authorityIssuerService.registerAuthorityIssuer(registerAuthorityIssuerArgs);
        logger.info("registerAuthorityIssuer is result,errorCode:{},errorMessage:{}",
            registResponse.getErrorCode(), registResponse.getErrorMessage());
        return registResponse;
    }

    /**
     * 注册cpt
     */
    @Override
    public ResponseData<CptBaseInfo> registCpt(String publisher, String privateKey, String claim) {

        RegisterCptArgs registerCptArgs = new RegisterCptArgs();
        registerCptArgs.setCptPublisher(publisher);
        registerCptArgs.setCptPublisherPrivateKey(new WeIdPrivateKey());
        registerCptArgs.getCptPublisherPrivateKey().setPrivateKey(privateKey);
        registerCptArgs.setCptJsonSchema(claim);

        ResponseData<CptBaseInfo> response = cptService.registerCpt(registerCptArgs);
        logger.info("registerCpt is result,errorCode:{},errorMessage:{}", response.getErrorCode(),
            response.getErrorMessage());
        return response;
    }

    /**
     * 创建电子凭证
     */
    @Override
    public ResponseData<Credential> createCredential(Integer cptId, String issuer,
        String privateKey, String claimDate) {

        CreateCredentialArgs registerCptArgs = new CreateCredentialArgs();
        registerCptArgs.setCptId(cptId);
        registerCptArgs.setIssuer(issuer);
        registerCptArgs.setWeIdPrivateKey(new WeIdPrivateKey());
        registerCptArgs.getWeIdPrivateKey().setPrivateKey(privateKey);
        registerCptArgs.setClaim(claimDate);
        // 设置有效期为360天
        registerCptArgs
            .setExpirationDate(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 360));

        ResponseData<Credential> response = credentialService.createCredential(registerCptArgs);
        logger.info("createCredential is result,errorCode:{},errorMessage:{}",
            response.getErrorCode(), response.getErrorMessage());
        return response;
    }

    /**
     * 验证电子凭证
     */
    @Override
    public ResponseData<Boolean> verifyCredential(String credentialJson) {

        ResponseData<Boolean> verifyResponse = new ResponseData<Boolean>();
        ObjectMapper objectMapper = new ObjectMapper();
        Credential credential = null;

        try {
            credential = objectMapper.readValue(credentialJson, Credential.class);
        } catch (IOException e) {
            logger.error("resolve credentialJson error.", e);
            verifyResponse.setResult(false);
            verifyResponse.setErrorCode(ErrorCode.CREDENTIAL_ERROR.getCode());
            verifyResponse.setErrorMessage(ErrorCode.CREDENTIAL_ERROR.getCodeDesc());
            return verifyResponse;
        }

        verifyResponse = credentialService.verifyCredential(credential);
        logger.info("verifyCredential is result,errorCode:{},errorMessage:{}",
            verifyResponse.getErrorCode(), verifyResponse.getErrorMessage());
        return verifyResponse;
    }
}
