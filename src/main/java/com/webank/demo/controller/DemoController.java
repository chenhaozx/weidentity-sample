package com.webank.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.webank.demo.service.DemoService;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.Credential;
import com.webank.weid.protocol.response.CreateWeIdDataResult;
import com.webank.weid.protocol.response.ResponseData;

@RestController
public class DemoController {

    @Autowired
    private DemoService demoService;
    
    @PostMapping("/createWeId")
    public ResponseData<CreateWeIdDataResult> createWeId() {
        return demoService.createWeIdWithSetAttr();
    }
    
    @PostMapping("/registerAuthorityIssuer")
    public ResponseData<Boolean> registerAuthorityIssuer(String issuer, String authorityName) {
        return demoService.registerAuthorityIssuer(issuer, authorityName);
    }
    
    @PostMapping("/registCpt")
    public ResponseData<CptBaseInfo> registCpt(String publisher,String privateKey) {
        return demoService.registCpt(publisher, privateKey);
    }

    @PostMapping("/createCredential")
    public ResponseData<Credential> createCredential(Integer cptId, String issuer,String privateKey) {
        return demoService.createCredential(cptId, issuer, privateKey);
    } 
    
    @PostMapping("/verifyCredential")
    public ResponseData<Boolean> verifyCredential(String credentialJson) {
        return demoService.verifyCredential(credentialJson);
    } 
}
