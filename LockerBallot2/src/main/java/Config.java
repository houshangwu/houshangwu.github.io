import java.io.FileInputStream;
import java.util.Properties;

public class Config {
    private static final String configPath = "C:/Users/hsw/Desktop/locker/LockerBallot2/src/main/resources/config.properties";
    public static Properties props;

    public static final String VERSION = "Version";
    public static final String TEST_NET_ADDR = "TestNetIpAddr";
    public static final String NET_ADDR = "NetIpAddr";
    public static final String DEFAULT_QUOTA = "DefaultQuota";
    public static final String LOCKER_ADMIN_ADDR = "LockerAdminAddress";
    public static final String LOCKER_ADMIN_PUBLIC_KEY = "LockerAdminPublicKey";
    public static final String LOCKER_ADMIN_PRIVATE_KEY = "LockerAdminPrivateKey";
    public static final String MULTI_SIG_WALLET_ADDR = "MultiSigWalletAddress";
    public static final String MULTI_SIG_WALLET_CODE = "MultiSigWalletCode";
    public static final String LOCKER_VOTE_CODE = "LockerVoteCode";


    public static Properties load(String path) {
        props = new Properties();
        try {
            props.load(new FileInputStream(path));
        } catch (Exception e) {
            System.out.println("Failed to read config at path " + path);
            System.exit(1);
        }
        return props;
    }

    public static Properties load() {
        try {
            props = new Properties();
            props.load(new FileInputStream(configPath));
        } catch (Exception e) {
            System.out.println("Failed to read config file. Error: " + e);
            e.printStackTrace();
            System.exit(1);
        }
        return props;
    }

}
