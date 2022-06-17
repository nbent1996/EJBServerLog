package HA;

import java.io.IOException;
import java.util.Properties;

public class Constants {
    public static Properties LoadProperties(){
        Properties props = new Properties();

        try {
            java.io.InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("file.properties");
            // Read the Properties file
            props.load(stream);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }
    public static final String USER_APP_MASTER = System.getProperty("USER_APP_MASTER");
    public static final String USER_APP_EXT = System.getProperty("USER_APP_EXT");
    public static final String PASSWORD_APP_MASTER = System.getProperty("PASSWORD_APP_MASTER");
    public static final String PASSWORD_APP_EXT = System.getProperty("PASSWORD_APP_EXT");
    public static final String URL_MASTER = System.getProperty("URL_APP_MASTER");
    public static final String URL_EXT = System.getProperty("URL_APP_EXT");
    public static final String URL_MASTER_INSTANCE2 = System.getProperty("URL_MASTER_INSTANCE2");
    public static final String PORT_WITH_OFFSET_MASTER = System.getProperty("PORT_WITH_OFFSET_MASTER"); /*EJEMPLO :8080 O :9080*/
    public static final String PORT_WITH_OFFSET_EXT = System.getProperty("PORT_WITH_OFFSET_EXT"); /*EJEMPLO :8080 O :9080*/
    public static final String PORT_WITH_OFFSET_MASTER_INSTANCE2 = System.getProperty("PORT_WITH_OFFSET_MASTER_INSTANCE2"); /*EJEMPLO :8080 O :9080*/

}
