import org.nervos.appchain.abi.*;
import org.nervos.appchain.abi.datatypes.*;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.methods.request.*;
import org.nervos.appchain.protocol.core.methods.response.*;
import org.nervos.appchain.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by hsw on 2018/9/2.
 */
public class LockerTest {
    private static Properties props;
    private static int version;
    private static String netIpAddr;
    public static String privateKey;
    private static Nervosj service;

    public static String lockerAdminAddress;
    public static String lockerAdminPublicKey;
    public static String lockerAdminPrivateKey;
    public static String multiSigWalletAddress;
    public static String multiSigWalletCode;
    public static String lockerVoteCode;

    static {
        props = Config.load();

        service = Nervosj.build(new HttpService("47.92.205.103:1337"));

        lockerAdminAddress = props.getProperty(Config.LOCKER_ADMIN_ADDR);
        //lockerAdminAddress = "0xcdC984E30e98a670500Abc320b148AF5D4890aB9";
        //lockerAdminAddress = "0x640Fa5AaFF1AfFA5909D152D4FC7bF1935C3824d";

        lockerAdminPublicKey = props.getProperty(Config.LOCKER_ADMIN_PUBLIC_KEY);
        lockerAdminPrivateKey = props.getProperty(Config.LOCKER_ADMIN_PRIVATE_KEY);

        privateKey = lockerAdminPrivateKey;                                         //locker员工私钥，测试用
        //privateKey = "0x900e0c615f9466c1ab00cce89f6043ab7fe7bc2624e0bb6665d76cbbd98f7774";
        //privateKey = "0x66f11a773ab9390c20866d63ab0de511d9cee20171ddd90073e12d0b26da6c47";

        multiSigWalletAddress = props.getProperty(Config.MULTI_SIG_WALLET_ADDR);
        multiSigWalletCode = props.getProperty(Config.MULTI_SIG_WALLET_CODE);
        lockerVoteCode = props.getProperty(Config.LOCKER_VOTE_CODE);
    }

    public static void main(String[] args) throws Exception{

        /*部署多签合约*/
        System.out.println("/*部署多签合约*/");
        List<Type> inputParameters = new ArrayList<>();
        List<Address> addressList = new ArrayList<>();
        Address address1 = new Address("0x640Fa5AaFF1AfFA5909D152D4FC7bF1935C3824d");
        Address address2 = new Address("0xC625BB33F6b6eB904B4E8481fe0BF2d0DcC8a6A3");
        Address address3 = new Address("0xaEa1Bb66eD0c2e8927C8CF66ed726ff11f2f30c3");
        Address address4 = new Address(lockerAdminAddress);
        addressList.add(address1);
        addressList.add(address2);
        addressList.add(address3);
        addressList.add(address4);
        DynamicArray dynamicArray = new DynamicArray(addressList);
        inputParameters.add(dynamicArray);
        String hash = CitaUtil.sendContract(service,privateKey,lockerVoteCode,inputParameters);

        TimeUnit.SECONDS.sleep(10);

        String address = CitaUtil.getContractAddressByHash(service,hash);
        System.out.println();

        /*调用addOwner()添加三个owner*/
        //System.out.println("/*调用addOwner()添加三个owner*/");

        /*owner调用confirmTransaction进行投票*/
        confirmTransaction(Collections.emptyList(), address);

        /*调用 getConfirmationCount 查询已投票数*/
        getConfirmationCount(address);

        /*调用 getOwners() 和 getStaffs()*/
        getOwners(address);
        getStaffs(address);
        System.out.println();

        /*调用 replaceOwner()*/
        System.out.println("/*调用 replaceOwner()*/");
        inputParameters = new ArrayList<>();
        Address willReplaceOldOwnerAddress = new Address("0x0ddC553E3EFf90f5Ab246a426cC3Bd5cC53769fD");
        Address willReplaceNewOwnerAddress = new Address("0x8696c4F18Fd86Daf3Cbf5ACfD2A1597709931085");
        inputParameters.add(willReplaceOldOwnerAddress);
        inputParameters.add(willReplaceNewOwnerAddress);

        String funHexStr2 = CitaUtil.getFunHexStr("replaceOwner",inputParameters, new TypeReference<Type>(){});
        String replaceOwnerHash = CitaUtil.sendTransaction(service,privateKey,address,funHexStr2);
        TimeUnit.SECONDS.sleep(10);
        TransactionReceipt transactionReceipt2 = CitaUtil.appGetTransactionReceiptByHash(service,replaceOwnerHash);
        if(transactionReceipt2.getErrorMessage() != null){
            System.err.println("replaceOwner " + willReplaceOldOwnerAddress + " ➡ " + willReplaceNewOwnerAddress + " error : " + transactionReceipt2.getErrorMessage());
        }
        System.out.println();

        /*调用 getOwners() 和 getStaffs()*/
        getOwners(address);
        getStaffs(address);
        System.out.println();

        /*调用 removeOwner()*/
        System.out.println("/*调用 removeOwner()*/");
        inputParameters = new ArrayList<>();
        Address willRemoveAddress =  new Address("0x8696c4F18Fd86Daf3Cbf5ACfD2A1597709931085");
        inputParameters.add(willRemoveAddress);

        String funHexStr1 = CitaUtil.getFunHexStr("removeOwner",inputParameters, new TypeReference<Type>(){});
        String removeOwnerHash = CitaUtil.sendTransaction(service,privateKey,address,funHexStr1);
        TimeUnit.SECONDS.sleep(10);
        TransactionReceipt transactionReceipt = CitaUtil.appGetTransactionReceiptByHash(service,removeOwnerHash);
        if(transactionReceipt.getErrorMessage() != null){
            System.err.println("removeOwner " + willRemoveAddress + " error : " + transactionReceipt.getErrorMessage());
        }
        System.out.println();

        /*调用 getOwners() 和 getStaffs()*/
        getOwners(address);
        getStaffs(address);
        System.out.println();
    }

    public static void confirmTransaction(List inputParameters,String contractAddress) throws Exception{
        System.out.println("/*调用 confirmTransaction()*/");
        String funHexStr1 = CitaUtil.getFunHexStr("confirmTransaction",inputParameters, new TypeReference<Type>(){});
        String confirmTransactionHash = CitaUtil.sendTransaction(service,privateKey,contractAddress,funHexStr1);
        TimeUnit.SECONDS.sleep(10);
        TransactionReceipt transactionReceipt = CitaUtil.appGetTransactionReceiptByHash(service,confirmTransactionHash);
        if(transactionReceipt.getErrorMessage() != null){
            System.err.println("confirmTransaction error : " + transactionReceipt.getErrorMessage());
        }
    }

    public static void getConfirmationCount(String contractAddress) throws Exception{
        System.out.println("/*调用 getConfirmationCount()*/");
        String funHexStr = CitaUtil.getFunHexStr("getConfirmationCount",Collections.emptyList(), new TypeReference<Uint>(){});
        Call call = new Call(lockerAdminAddress,contractAddress, funHexStr);
        AppCall appCall = service.appCall(call, DefaultBlockParameter.valueOf("latest")).send();
        if(appCall.getError() != null){
            System.out.println("call getConfirmationCount() error = " + appCall.getError().getMessage());
        }
        String callResult = appCall.getValue();
        System.out.println("call getConfirmationCount() result = " + callResult);

        Function func = new Function(
                "getConfirmationCount",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint>(){}));
        Uint u = (Uint) FunctionReturnDecoder.decode(
                callResult, func.getOutputParameters()).get(0).getValue();
        if(u != null){
            System.out.println("getConfirmationCount result : " + u.getValue());
        }
    }

    public static void isVote(String ownerAddress,String contractAddress) throws Exception{
       /* System.out.println("*//*调用 isVote()*//*");
        String funHexStr = CitaUtil.getFunHexStr("isVote",Collections.emptyList(), new TypeReference<Uint>(){});
        Call call = new Call(lockerAdminAddress,contractAddress, funHexStr);
        AppCall appCall = service.appCall(call, DefaultBlockParameter.valueOf("latest")).send();
        if(appCall.getError() != null){
            System.out.println("call getOwners() error = " + appCall.getError().getMessage());
        }
        String callResult = appCall.getValue();
        System.out.println("call getOwners() result = " + callResult);

        Function func = new Function(
                "isVote",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint>(){}));
        Uint u = (Uint) FunctionReturnDecoder.decode(
                callResult, func.getOutputParameters()).get(0).getValue();
        if(u != null){
            System.out.println("getConfirmationCount result : " + u.getValue());
        }*/
    }

    public static void getOwners(String address)throws Exception{
        System.out.println("/*调用 getOwners()*/");
        String funHexStr = CitaUtil.getFunHexStr("getOwners",Collections.emptyList(), new TypeReference<DynamicArray<Address>>(){});
        Call call = new Call(lockerAdminAddress,address, funHexStr);
        AppCall appCall = service.appCall(call, DefaultBlockParameter.valueOf("latest")).send();
        if(appCall.getError() != null){
            System.out.println("call getOwners() error = " + appCall.getError().getMessage());
        }
        String callResult = appCall.getValue();
        System.out.println("call getOwners() result = " + callResult);

        Function func = new Function(
                "getOwners",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<DynamicArray<Address>>(){}));
        List<Address> addressList = (List<Address>)FunctionReturnDecoder.decode(
                callResult, func.getOutputParameters()).get(0).getValue();
        if(addressList != null && addressList.size()>0){
            int i = 0 ;
            for (Type resultAddress : addressList) {
                i++;
                System.out.println("resultAddress "+ i +" : " + resultAddress.getValue());
            }
        }
    }

    public static void getStaffs(String address)throws Exception{
        System.out.println("/*调用 getStaffs()*/");
        String funHexStr = CitaUtil.getFunHexStr("getStaffs",Collections.emptyList(), new TypeReference<DynamicArray<Address>>(){});
        Call call = new Call(lockerAdminAddress,address, funHexStr);
        AppCall appCall = service.appCall(call, DefaultBlockParameter.valueOf("latest")).send();
        if(appCall.getError() != null){
            System.out.println("call getStaffs() error = " + appCall.getError().getMessage());
        }
        String callResult = appCall.getValue();
        System.out.println("call getStaffs() result = " + callResult);

        Function func = new Function(
                "getStaffs",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<DynamicArray<Address>>(){}));
        List<Address> addressList = (List<Address>)FunctionReturnDecoder.decode(
                callResult, func.getOutputParameters()).get(0).getValue();
        if(addressList != null && addressList.size()>0){
            int i = 0 ;
            for (Type resultAddress : addressList) {
                i++;
                System.out.println("resultAddress "+ i +" : " + resultAddress.getValue());
            }
        }
    }
}
