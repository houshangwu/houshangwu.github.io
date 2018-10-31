import org.nervos.appchain.abi.*;
import org.nervos.appchain.abi.datatypes.*;
import org.nervos.appchain.abi.datatypes.generated.AbiTypes;
import org.nervos.appchain.abi.datatypes.generated.Uint256;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.methods.request.*;
import org.nervos.appchain.protocol.core.methods.request.AppFilter;
import org.nervos.appchain.protocol.core.methods.response.*;
import org.nervos.appchain.protocol.http.HttpService;
import static org.nervos.appchain.tx.Contract.staticExtractEventParameters;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by hsw on 2018/9/2.
 */
public class LockerTest2 {
    private static Properties props;
    private static int version;
    private static String testNetIpAddr;
    private static String netIpAddr;
    public static String privateKey;
    private static Random random;
    private static BigInteger quota;
    private static String value;
    private static Nervosj service;         //  http://47.92.205.103:1337
    private static Nervosj testService;     //  http://39.105.54.203:1337

    public static String lockerAdminAddress;
    public static String lockerAdminPublicKey;
    public static String lockerAdminPrivateKey;
    public static String multiSigWalletAddress;
    public static String multiSigWalletCode;

    static {
        props = Config.load();
        version = Integer.parseInt(props.getProperty(Config.VERSION));
        testNetIpAddr = props.getProperty(Config.TEST_NET_ADDR);
        netIpAddr = props.getProperty(Config.NET_ADDR);

        //HttpService.setDebug(false);
        service = Nervosj.build(new HttpService(netIpAddr));
        testService = Nervosj.build(new HttpService(testNetIpAddr));
        random = new Random(System.currentTimeMillis());
        quota = BigInteger.valueOf(1000000);
        value = "0";

        lockerAdminAddress = props.getProperty(Config.LOCKER_ADMIN_ADDR);
        lockerAdminPublicKey = props.getProperty(Config.LOCKER_ADMIN_PUBLIC_KEY);
        lockerAdminPrivateKey = props.getProperty(Config.LOCKER_ADMIN_PRIVATE_KEY);
        multiSigWalletAddress = props.getProperty(Config.MULTI_SIG_WALLET_ADDR);
        multiSigWalletCode = props.getProperty(Config.MULTI_SIG_WALLET_CODE);
    }

    public static void main(String[] args) throws Exception{

    }

    public static String getBallotAddressTest()throws Exception{
            /*部署合约*/
        //String contractHash = CitaUtil.sendContract(service,CitaConfig.privateKey,CitaConfig.helloWorldContractCode,getHelloWorldInputParameters("test"));

        //String helloWorldContractAddress = CitaUtil.getContractAddressByHash(service,contractHash);

        //String funHexStr = getSayHiHexStr("sayHi");
        //String funHexStr = getChangeNameHexStr("changeName","hsw");
        //String  funHexStr = getFunctionHexStr("create");
        //String funHexStr = "0xf8f738080000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000020000000000000000000000000d4df41d1105082237315af8af3baf39afb5519a0000000000000000000000003e4e3cb6f7b8897b88034f37cc820a957ccebe62";

        //System.out.println("funHexStr = "+funHexStr);
        //System.out.println("funHexStr = " + new String(hexStringToByte(funHexStr)).replace("0x",""));

        String funHexStr = getCreateFunctionHexStr();
        //String sendTransactionHash = CitaUtil.sendTransaction(service,CitaConfig.privateKey,CitaConfig.helloWorldContractAddress,funHexStr);
        //System.out.println("sendTransactionHash = " + sendTransactionHash);
        //String hash = "0x266e8a233d3350f863f5051c17d29f608d33b4535490d968b00122b9c982bb31";
        //0x96f9e3247a88c011c0acfbecc978ba2a35965a6bf1eb83f4edf39cf21a89a9a0

         /*调用合约*/
        //String funcCallData = callContract(CitaConfig.superAdminAddress,CitaConfig.multiSigWalletFactoryAddress,funHexStr);
        //System.out.println(CitaUtil.translateHexString(funcCallData));

        /*String hash = CitaUtil.sendTransaction(service,privateKey,multiSigWalletFactoryAddress,funHexStr);
        TimeUnit.SECONDS.sleep(10);
        String address = callContract(CitaConfig.superAdminAddress,multiSigWalletFactoryAddress,funHexStr);
        TransactionReceipt transactionReceipt = CitaUtil.appGetTransactionReceiptByHash(service,hash);
        System.out.println(transactionReceipt.getErrorMessage());
        System.out.println();*/

        /*String hash2 = CitaUtil.sendTransaction(service,privateKey,multiSigWalletFactoryAddress,funHexStr);
        TimeUnit.SECONDS.sleep(10);
        callContract(CitaConfig.superAdminAddress,multiSigWalletFactoryAddress,funHexStr);
        TransactionReceipt transactionReceipt2 = CitaUtil.appGetTransactionReceiptByHash(service,hash2);
        System.out.println(transactionReceipt2.getErrorMessage());
        System.out.println();

        String hash3 = CitaUtil.sendTransaction(service,privateKey,multiSigWalletFactoryAddress,funHexStr);
        TimeUnit.SECONDS.sleep(10);
        callContract(CitaConfig.superAdminAddress,multiSigWalletFactoryAddress,funHexStr);
        TransactionReceipt transactionReceipt3 = CitaUtil.appGetTransactionReceiptByHash(service,hash3);
        System.out.println(transactionReceipt3.getErrorMessage());
        System.out.println();*/

        return "address";
    }

    public static void test2() throws Exception{

/*        DefaultBlockParameter defaultBlockParameter = DefaultBlockParameter.valueOf("latest");
        AppGetAbi getAbi1 = service.appGetAbi(multiSigWalletAddress, defaultBlockParameter).send();
        AppGetAbi getAbi2 = service.appGetAbi(multiSigWalletAddress2, defaultBlockParameter).send();
        String abi1 = getAbi1.getAbi();
        String abi2 = getAbi2.getAbi();
        System.out.println("通过 truffle  部署的 multiSigWallet abi = " + abi1);
        System.out.println("通过 nervousj 部署的 multiSigWallet abi = " + abi2);*/
    }

    /*helloWorld合约 changeName 参数拼接*/
    public static List<Type> getHelloWorldInputParameters(String name) throws Exception{
        List<Type> inputParameters = new ArrayList<>();
        Utf8String initParm = new Utf8String(name);
        inputParameters.add(initParm);
        return inputParameters;
    }

    /*调用智能合约*/
    public static String callContract(String address, String contractAddress, String data) throws Exception{
        Call call = new Call(address, contractAddress, data);
        AppCall appCall = service.appCall(call, DefaultBlockParameter.valueOf("latest")).send();
        if(appCall.getError() != null){
            System.out.println("error = " + appCall.getError().getMessage());
        }
        String callResult = appCall.getValue();
        System.out.println("callResult = " + callResult);
        return callResult;
    }

    public static String getSayHiHexStr(String functionName) throws Exception{
        Function sayHiFunc = new Function(
                functionName,
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Utf8String>(){}));

        String funcCallData = FunctionEncoder.encode(sayHiFunc);
        return funcCallData;
    }

    public static String getChangeNameHexStr(String functionName,String name) throws Exception{
        List<Type> inputParameters = new ArrayList<>();
        Utf8String parm = new Utf8String(name);
        inputParameters.add(parm);
        Function changeNameFunc = new Function(
                functionName,
                inputParameters,
                Arrays.asList(new TypeReference<Type>(){}));
        //
        String funcCallData = FunctionEncoder.encode(changeNameFunc);
        return funcCallData;
    }

    public static String getCreateFunctionHexStr(){

    /*    a8d72d4744120ac28835ae1fc7fb2dfaf1b609ad
        22889780fc0460a374bcae396cad3a3b9afc6d77
        0d63310c98ff839633833c9ed02cf2b70d278691*/
        List<Type> inputParameters = new ArrayList<>();
        List<Address> addressList = new ArrayList<>();
        Address address1 = new Address("0x640Fa5AaFF1AfFA5909D152D4FC7bF1935C3824d");
        Address address2 = new Address("0xC625BB33F6b6eB904B4E8481fe0BF2d0DcC8a6A3");
        Address address3 = new Address("0xaEa1Bb66eD0c2e8927C8CF66ed726ff11f2f30c3");
        addressList.add(address1);
        addressList.add(address2);
        addressList.add(address3);
        DynamicArray dynamicArray = new DynamicArray(addressList);
        inputParameters.add(dynamicArray);
        Uint256 uint = new Uint256(BigInteger.valueOf(1));
        inputParameters.add(uint);
        Function sayHiFunc = new Function(
                "create",
                inputParameters,
                Arrays.asList(new TypeReference<Address>(){}));
        String funcCallData = FunctionEncoder.encode(sayHiFunc);
        System.out.println("funcCallData = " + funcCallData);
        return funcCallData;
    }

    public static class ContractInstantiationEventResponse {
        public String indexedValue;

        public String nonIndexedValue;
    }

}
