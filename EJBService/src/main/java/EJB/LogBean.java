package EJB;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.Remote;

import javax.ejb.Stateless;


@Stateless
@Remote(LogSender.class)
public class LogBean implements LogSender {
    Logger log = LogManager.getLogger(LogBean.class);
    private Level logInfo = Level.forName("INFO", 550);

    @Override
    public void Log(String message) {
        log.log(logInfo, message);
    }

}
