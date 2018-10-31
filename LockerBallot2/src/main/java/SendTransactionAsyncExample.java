import java.math.BigInteger;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.nervos.appchain.crypto.Credentials;
import org.nervos.appchain.protocol.Nervosj;
import org.nervos.appchain.protocol.core.DefaultBlockParameterName;
import org.nervos.appchain.protocol.core.RemoteCall;
import org.nervos.appchain.protocol.core.methods.request.Transaction;
import org.nervos.appchain.protocol.core.methods.response.AppGetBalance;
import org.nervos.appchain.protocol.core.methods.response.AppSendTransaction;
import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.protocol.http.HttpService;
import org.nervos.appchain.tx.response.PollingTransactionReceiptProcessor;
import org.nervos.appchain.utils.Convert;

public class SendTransactionAsyncExample {
    private static Properties props;
    static String testNetAddr;
    static String payerKey;
    static String payeeAddr = "0x558D51dB15BbaF7248c7f6B853b04451aA70B438";
    static int chainId;
    static int version;
    static long quota;

    static Nervosj service;

    static {
        props = Config.load();
        testNetAddr = props.getProperty(Config.TEST_NET_ADDR);
        service = Nervosj.build(new HttpService(testNetAddr));
        quota = Long.parseLong(props.getProperty(Config.DEFAULT_QUOTA));
        version = Integer.parseInt(props.getProperty(Config.VERSION));
        chainId = 1;
        payerKey = props.getProperty(Config.LOCKER_ADMIN_PRIVATE_KEY);

        HttpService.setDebug(false);
        service = Nervosj.build(new HttpService(testNetAddr));
    }

    static BigInteger getBalance(String address) {
        BigInteger balance = null;
        try {
            AppGetBalance response = service.appGetBalance(
                    address, DefaultBlockParameterName.LATEST).send();
            balance = response.getBalance();
        } catch (Exception e) {
            System.out.println("failed to get balance.");
            System.exit(1);
        }
        return balance;
    }

    static TransactionReceipt transferSync(
            String payerKey, String payeeAddr, String value)
            throws Exception {
        PollingTransactionReceiptProcessor txProcessor =
                new PollingTransactionReceiptProcessor(
                        service,
                        15 * 1000,
                        40);

        Transaction tx = new Transaction(payeeAddr,
                TestUtil.getNonce(),
                quota,
                TestUtil.getValidUtilBlock(service).longValue(),
                version, chainId,
                value,
                "");

        String rawTx = tx.sign(payerKey, false, false);
        AppSendTransaction ethSendTrasnction = service
                .appSendRawTransaction(rawTx).send();

        TransactionReceipt txReceipt = txProcessor
                .waitForTransactionReceipt(
                        ethSendTrasnction.getSendTransactionResult().getHash());

        return txReceipt;
    }

    static CompletableFuture<TransactionReceipt> transferAsync(
            String payerKey, String payeeAddr, String value) {
        return new RemoteCall<>(
                () -> transferSync(payerKey, payeeAddr, value)).sendAsync();
    }

    public static void main(String[] args) {
        Credentials payerCredential = Credentials.create(payerKey);
        String payerAddr = payerCredential.getAddress();
        System.out.println(Convert.fromWei(getBalance(payerAddr).toString(), Convert.Unit.ETHER));
        System.out.println(Convert.fromWei(getBalance(payeeAddr).toString(), Convert.Unit.ETHER));

        String value = "1";
        String valueWei = Convert.toWei(value, Convert.Unit.ETHER).toString();

        CompletableFuture<TransactionReceipt> receiptFuture =
                transferAsync(payerKey, payeeAddr, valueWei);
        receiptFuture.whenCompleteAsync((data, exception) -> {
            if (exception == null) {
                if (data.getErrorMessage() == null) {
                    System.out.println(
                            Convert.fromWei(getBalance(payerAddr).toString(), Convert.Unit.ETHER));
                    System.out.println(
                            Convert.fromWei(getBalance(payeeAddr).toString(), Convert.Unit.ETHER));
                } else {
                    System.out.println("Error get receipt: " + data.getErrorMessage());
                }
            } else {
                System.out.println("Exception happens: " + exception);
            }
        });
    }
}
