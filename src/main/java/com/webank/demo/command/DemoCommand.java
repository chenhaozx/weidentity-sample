package com.webank.demo.command;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.bcos.contract.tools.ToolConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.webank.demo.common.util.FileUtil;
import com.webank.weid.protocol.base.CptBaseInfo;
import com.webank.weid.protocol.base.Credential;
import com.webank.weid.protocol.base.WeIdPrivateKey;
import com.webank.weid.protocol.response.CreateWeIdDataResult;


/**
 * @author v_wbpenghu
 */
public class DemoCommand {

    public final static String PRIVKEY;
    
    protected static ApplicationContext context;
    
    private static final Logger logger = LoggerFactory.getLogger(DemoCommand.class);

    /**
     * schema.
     */
    public final static String SCHEMA;
    
    /**
     * claimData.
     */
    public final static String CLAIMDATA;

    static {
        context = new ClassPathXmlApplicationContext(new String[]{
            "classpath:applicationContext.xml",
            "classpath:SpringApplicationContext-demo.xml"});

        ToolConf toolConf = context.getBean(ToolConf.class);
        PRIVKEY = new BigInteger(toolConf.getPrivKey(), 16).toString();
        
        SCHEMA = FileUtil.getJsonFromFile("JsonSchema.json");//获取jsonSchema
        CLAIMDATA = FileUtil.getJsonFromFile("ClaimData.json");//获取schemaData
    }



    /**
     * main.
     */
    public static void main(String[] args) {

        if (args == null) {
            args = new String[1];
            args[0] = "issuer";
        }
        
        switch (args[0]) {
            case "issuer":
                issue();
                break;
            case "user":
                user();
                break;
            case "verifier":
                verify();
                break;
            default:
                issue();
                break;
        }
        System.exit(0);
    }

    /**
     * 权威机构的运行流程
     * 1.创建weId
     * 2.set相关属性
     * 3.链上注册为权威机构
     * 4.创建CPT
     */
    private static void issue() {
        
        BaseBean.print("issue() init...");
        // 获取服务实例
        DemoService demo = context.getBean(DemoService.class);
        Map<String, String> map = new HashMap<>();
        
        BaseBean.print("begin createWeId...");
        // 机构注册自己的weId,机构需要保存好自己的weId和私钥
        CreateWeIdDataResult createWeId = demo.createWeId();
        map.put("weId", createWeId.getWeId());
        map.put("privateKey", createWeId.getUserWeIdPrivateKey().getPrivateKey());
        
        BaseBean.print("------------------------------");
        BaseBean.print("begin setPublicKey...");
        // 设置公钥属性, 公钥类型默认使用 "secp256k1"
        demo.setPublicKey(createWeId, "secp256k1");
        
        BaseBean.print("------------------------------");
        BaseBean.print("begin setAuthenticate...");
        // 设置认证者属性,签名类型默认使用 "RsaSignatureAuthentication2018"
        demo.setAuthenticate(createWeId, "RsaSignatureAuthentication2018");
        
        BaseBean.print("------------------------------");
        BaseBean.print("begin registerAuthorityIssuer...");
        // 机构注册为权威机构, "webank"为权威机构名, "0"为默认
        demo.registerAuthorityIssuer(createWeId, "webank", "0");

        BaseBean.print("------------------------------");
        BaseBean.print("begin registCpt...");
        // 注册cpt模版，机构需要保存好自己的模版编号cptId
        CptBaseInfo cptResult = demo.registCpt(createWeId, SCHEMA);
        map.put("cptId", cptResult.getCptId().toString());
        
        DemoUtil.saveTemData(map);
        
        BaseBean.print("------------------------------");
        BaseBean.print("issue() finish...");
    }

    /**
     * 用户的运行流程:
     * 1.用户注册weId
     * 2.提供CPT数据,机构为其创建电子凭证
     * 
     */
    private static void user() {
        
        BaseBean.print("user() init...");
        DemoService demo = context.getBean(DemoService.class);
        
        BaseBean.print("------------------------------");
        BaseBean.print("begin createWeId...");
        // 机构注册自己的weId,机构需要保存好自己的weId和私钥
        CreateWeIdDataResult createWeId = demo.createWeId();
        
        String json = FileUtil.getDataByPath("./temp.data");
        ObjectMapper om = new ObjectMapper();
        Map<String, String> paramMap = null;
        try {
            paramMap = om.readValue(json, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("read temp.data error", e);
        }
        
        // 机构根据用户提供的模版数据为其创建电子凭证(weId和privateKey均所属权威机构)
        BaseBean.print("------------------------------");
        BaseBean.print("begin createCredential...");
        CreateWeIdDataResult weIdResult = new CreateWeIdDataResult();
        weIdResult.setWeId(paramMap.get("weId"));
        weIdResult.setUserWeIdPrivateKey(new WeIdPrivateKey());
        weIdResult.getUserWeIdPrivateKey().setPrivateKey(paramMap.get("privateKey"));

        long expirationDate = System.currentTimeMillis() + (1000L * 60 * 24);
        // cptId为权威机构的模版(CPT)编号
        Integer cptId = new Integer(paramMap.get("cptId"));

        String cData = CLAIMDATA;
        // 将用户weId作为cpt数据的一部分,用于识别电子凭证归属
        cData =  cData.replace("{userWeId}", createWeId.getWeId());
        
        Credential credential =
            demo.createCredential(weIdResult, cptId, cData, expirationDate);

        // 将电子凭证保存为json格式文件，供用户自行保管,办理业务时提供即可
        BaseBean.print("------------------------------");
        BaseBean.print("beign saveCredential...");
        String path = DemoUtil.saveCredential(credential);
        BaseBean.print("saveCredential success, path:" + path);
        
        BaseBean.print("------------------------------");
        BaseBean.print("user() finish...");
    }

    /**
     * 验签机构运行流程：
     * 1.验证用户提供的电子凭证
     */
    private static void verify() {
        
        BaseBean.print("verify() init...");
        DemoService demo = context.getBean(DemoService.class);
        
        // 获取服务实例
        BaseBean.print("------------------------------");
        BaseBean.print("begin getCredentialFromJson...");
        // 获取用户提供的电子凭证
        Credential credential = DemoUtil.getCredentialFromJson();
        BaseBean.print("getCredentialFromJson result:");
        BaseBean.print(credential);
        
        BaseBean.print("------------------------------");
        BaseBean.print("begin verifyCredential...");
        boolean result = demo.verifyCredential(credential);
        if (result) {
            BaseBean.print("verify success");
        } else {
            BaseBean.print("verify fail");
        }
        
        BaseBean.print("------------------------------");
        BaseBean.print("verify() finish...");
    }
}
