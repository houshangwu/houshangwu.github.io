import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/**
 * Created by hsw on 2018/9/18.
 */
public class EtcTest {
    public static void main(String[] args) throws Exception {
        //设置需要的矿工费
        BigInteger GAS_PRICE = BigInteger.valueOf(22_000_000_000L);
        BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000);

        //调用的是kovan测试环境，生产环境需要接入主链 https://mainnet.infura.io/mew
        Web3j web3j = Web3j.build(new HttpService("http://127.0.0.1:8545"));
        //转账人账户地址
        String ownAddress = "0x558D51dB15BbaF7248c7f6B853b04451aA70B438";
        //转账人私钥
        String ownPrivateKey = "0xe5ccf3eedfc24b1d005d386823716171b3c2c963dcdff6e09fd4fb6433dbd85b";
        //被转人账户地址
        String toAddress = "0x66F751BA888960C22cE4B675B404ccE16bBbD942";

        Credentials credentials = Credentials.create(ownPrivateKey);
        //        Credentials credentials = WalletUtils.loadCredentials(
        //                "123",
       //                "src/main/resources/UTC--2018-03-01T05-53-37.043Z--d1c82c71cc567d63fd53d5b91dcac6156e5b96b3");

        //getNonce（这里的Nonce我也不是很明白）
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                         ownAddress, DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        //创建交易，这里是转0.5个以太币
        BigInteger value = Convert.toWei("0.5", Convert.Unit.ETHER).toBigInteger();
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                         nonce, GAS_PRICE, GAS_LIMIT, toAddress, value);

        //签名Transaction，这里要对交易做签名
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        //发送交易
        EthSendTransaction ethSendTransaction =
                         web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        String transactionHash = ethSendTransaction.getTransactionHash();

        //获得到transactionHash后就可以到以太坊的网站上查询这笔交易的状态了
        System.out.println(transactionHash);

        //TimeUnit.SECONDS.sleep(20);

        TransactionReceipt receipt = web3j.ethGetTransactionReceipt(transactionHash).send().getTransactionReceipt().get();
        receipt.getBlockNumber();

        /*公链余额*/
        for(int i= 0; i<100; i++){
            getBalance();
        }
    }

    //查询余额
    public static void getBalance() throws Exception{
        //调用的是kovan测试环境，生产环境需要接入 一个主链以太坊节点的ip和端口
        //Web3j web3j = Web3j.build(new HttpService());
        Admin web3j = Admin.build(new HttpService("https://mainnet.infura.io/mew"));

        EthGetBalance ethGetBalance = web3j
                .ethGetBalance("0x31f8c2fa6b995525b22e416a31a5ab05f64a7659", DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get();

        BigInteger wei = ethGetBalance.getBalance();
        System.out.println("address：0x31f8c2fa6b995525b22e416a31a5ab05f64a7659 的 余额为：" + wei + "wei");
    }
}
