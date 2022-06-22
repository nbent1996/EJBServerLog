import EJB.LogBean;
import EJB.LogSender;
import HA.Constants;
import org.jboss.ejb.client.RequestSendFailedException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class RemoteEJBClient extends Thread implements Runnable{

    private String message;
    private int opcion, msInterval;
    private boolean state;
    private static LogSender logger;

    public RemoteEJBClient(int msInterval, String message, boolean state, int opcion){
        this.msInterval = msInterval;
        this.message = message;
        this.state = state;
        this.opcion = opcion;
    }


    public static void main(String[] args) throws Exception {
        //rutinaLogsRecurrentes();
        rutinaLogIndividual();

    }
    /*Logs enviados por el usuario*/
    public static void rutinaLogIndividual() throws Exception{
        logger = lookupLogSender(); /*Creo la conexion al CORE-HA*/
        while(true){
            String message;
            System.out.println("Ingrese el mensaje a enviar a la instancia o -1 para finalizar ejecución:");
            message = new Scanner(System.in).nextLine();
            if(message.equals("-1")){
                return;
            }
            try{
                logger.Log(message);
            }catch(RequestSendFailedException ex){
                System.out.println("ERROR DE CONEXION");
        }
        }
    }
    /*Logs enviados por el usuario*/

    /*Logs Recurrentes*/
    public static void rutinaLogsRecurrentes() throws Exception{
        logger = lookupLogSender(); /*Creo la conexion al CORE-HA*/
        RemoteEJBClient rEJB = tomarDatosRecurrente(); /*Tomo datos*/
        Thread tr = new Thread(rEJB);
        tr.start();
        while(rEJB.state){
            System.out.println("Presione 1 si desea frenar el hilo");
            int opcion = new Scanner(System.in).nextInt();
            if(opcion==1){
                rEJB.state=false;
            }
        }
    }
    @Override
    public void run(){
            while (state) {
                try {
                logger.Log(message);
                sleep(msInterval*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }
    private static RemoteEJBClient tomarDatosRecurrente() throws NamingException {
            int opcion, msInterval;
            String message;
            boolean state;
            System.out.println("Seleccione instancia:\n" +
                    "1 - Master1\n" +
                    "2 - Master2");
            opcion = new Scanner(System.in).nextInt();
            System.out.println("Creación de hilos que loguean de forma recurrente en Wildfly");
            System.out.println("Ingrese intervalo en segundos");
            msInterval = new Scanner(System.in).nextInt();
            System.out.println("Ingrese mensaje");
            message = new Scanner(System.in).nextLine();
            state = true;
            return new RemoteEJBClient(msInterval, message, state, opcion);
    }
    /*Logs Recurrentes*/

    /*Invocacion al EJB*/
    private static LogSender lookupLogSender() throws NamingException, RequestSendFailedException {
        final Hashtable jndiProperties = new Hashtable();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        jndiProperties.put(Context.PROVIDER_URL, "remote+http://127.0.0.1:8080, remote+http://127.0.0.1:9080");
        jndiProperties.put(Context.SECURITY_PRINCIPAL, "appCOREnicolas");
        jndiProperties.put(Context.SECURITY_CREDENTIALS, "48283674");
        final Context context = new InitialContext(jndiProperties);
        final String moduleName = "EJBService";
        final String beanName = LogBean.class.getSimpleName();
        final String viewClassName = LogSender.class.getName();
        String retorno = "ejb:/"+ moduleName + "/" + beanName + "!" + viewClassName;
        System.out.println(retorno);
        return (LogSender) context.lookup(retorno);
    }
    /*Invocacion al EJB*/


}
