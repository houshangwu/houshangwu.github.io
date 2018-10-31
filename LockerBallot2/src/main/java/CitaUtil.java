import org.nervos.appchain.abi.FunctionEncoder;
import org.nervos.appchain.abi.TypeEncoder;
import org.nervos.appchain.abi.TypeReference;
import org.nervos.appchain.abi.datatypes.Function;
import org.nervos.appchain.abi.datatypes.Type;
import org.nervos.appchain.abi.datatypes.generated.Uint256;
import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.crypto.ECKeyPair;
import org.nervos.appchain.crypto.Keys;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.DefaultBlockParameter;
import org.nervos.appchain.protocol.core.Request;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.*;
import org.nervos.appchain.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by hsw on 2018/9/4.
 */
public class CitaUtil {

    /*部署合约*/
    public static String sendContract(Nervosj service, String privateKey,String contractCode,List<Type> inputParameters) throws Exception{
        AppBlockNumber appBlockNumber = service.appBlockNumber().send();
        BigInteger blockNumber = appBlockNumber.getBlockNumber();

        DefaultBlockParameter defaultParam = DefaultBlockParameter.valueOf("latest");
        AppMetaData.AppMetaDataResult result = service.appMetaData(defaultParam).send().getAppMetaDataResult();
        int chainId = result.chainId;

        /*向区块链节点发送序列化交易。*/
        Random random = new Random(System.currentTimeMillis());
        BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
        long quota = new Long("999999999");
        BigInteger currentHeight = blockNumber;

        Function changeNameFunc = new Function(
                "",
                inputParameters,
                Arrays.asList(new TypeReference<Type>(){}));

        String funcCallData = FunctionEncoder.encode(changeNameFunc);
        String initParmHexStr = funcCallData.replace("0x","").substring(8); //去除“0x”
        contractCode = contractCode + initParmHexStr;

        // 构建合约交易
        Transaction rtx = Transaction.createFunctionCallTransaction("", nonce, quota, currentHeight.longValue() + 100,
                0, chainId, TypeEncoder.encode(new Uint256(BigInteger.ZERO)), contractCode);
        String txStr = rtx.sign(privateKey, false, false);
        AppSendTransaction sendTransaction = service.appSendRawTransaction(txStr).send();
        String sendTransactionHash = sendTransaction.getSendTransactionResult().getHash();

        return sendTransactionHash;
    }

    /*发送交易合约*/
    public static String sendTransaction(Nervosj service, String privateKey,String toAddress,String data) throws Exception{
        AppBlockNumber appBlockNumber = service.appBlockNumber().send();
        BigInteger blockNumber = appBlockNumber.getBlockNumber();

        DefaultBlockParameter defaultParam = DefaultBlockParameter.valueOf("latest");
        AppMetaData.AppMetaDataResult result = service.appMetaData(defaultParam).send().getAppMetaDataResult();
        int chainId = result.chainId;

        /*向区块链节点发送序列化交易。*/
        Random random = new Random(System.currentTimeMillis());
        BigInteger nonce = BigInteger.valueOf(Math.abs(random.nextLong()));
        long quota = new Long("999999999");
        BigInteger currentHeight = blockNumber;

        //构建合约交易
        Transaction rtx = Transaction.createFunctionCallTransaction(toAddress, nonce, quota, currentHeight.longValue() + 100,
                0, chainId, TypeEncoder.encode(new Uint256(BigInteger.ZERO)), data);
        String txStr = rtx.sign(privateKey, false, false);
        AppSendTransaction sendTransaction = service.appSendRawTransaction(txStr).send();
        String sendTransactionHash = sendTransaction.getSendTransactionResult().getHash();

        return sendTransactionHash;
    }

    public static String getFunHexStr(String funName,List<Type> inputParameters,TypeReference returnReference){
        Function func = new Function(
                funName,
                inputParameters,
                Arrays.asList(returnReference));
        String funcCallData = FunctionEncoder.encode(func);
        System.out.println("funcCallData = " + funcCallData);
        return funcCallData;
    }

    public static AppTransaction appGetTransactionByHash(Nervosj service,String transactionHash) throws Exception{
        AppTransaction responseTx = service.appGetTransactionByHash(transactionHash).send();
        return responseTx;
    }

    public static TransactionReceipt appGetTransactionReceiptByHash(Nervosj service,String transactionHash) throws Exception{
        TransactionReceipt txReceipt = service.appGetTransactionReceipt(transactionHash).send().getTransactionReceipt().get();
        return txReceipt;
    }

    public static String getContractAddressByHash(Nervosj service,String contractHash) throws Exception{
        TransactionReceipt txReceipt = service.appGetTransactionReceipt(contractHash).send().getTransactionReceipt().get();
        txReceipt.getBlockNumber();
        String contractAddress = txReceipt.getContractAddress();
        System.out.println("contractAddress = " + contractAddress);
        return contractAddress;
    }

    public static int  getChainId(Nervosj service) throws Exception{
          /*获取指定块高的元数据。*/
        DefaultBlockParameter defaultParam = DefaultBlockParameter.valueOf("latest");
        AppMetaData.AppMetaDataResult result = service.appMetaData(defaultParam).send().getAppMetaDataResult();
        int chainId = result.chainId;
        return chainId;
    }

    public static BigInteger getBlockNumber(Nervosj service) throws Exception{
        AppBlockNumber result2 = service.appBlockNumber().send();
        BigInteger blockNumber = result2.getBlockNumber();
        return blockNumber;
    }

    public static void createEcKeyPair()throws Exception{
        ECKeyPair keys = Keys.createEcKeyPair();  //生成密钥
        System.out.println("私钥 : " + keys.getPrivateKey());//私钥
        System.out.println("公钥 : " + keys.getPublicKey());//公钥*//*
        System.out.println("地址 : " + Keys.getAddress(keys.getPublicKey()));//钱包地址
    }

    public static String getAddressByPrivateKey(String privateKey){
        Credentials payerCredential = Credentials.create(privateKey);
        return payerCredential.getAddress();
    }

    public static String getPublicKeyByPrivateKey(String privateKey){
        Credentials payerCredential = Credentials.create(privateKey);
        return payerCredential.getEcKeyPair().getPublicKey().toString();
    }

    public static ECKeyPair getECKeyPairByPrivateKey(String privateKey){
        Credentials payerCredential = Credentials.create(privateKey);
        return payerCredential.getEcKeyPair();
    }

    public static String translateHexString(String hexStr){
        if(hexStr.startsWith("0x")){
            hexStr = hexStr.substring(2);
        }
        return new String(hexStringToByte(hexStr));
    }

    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }
}
