import Stateless.LogBean;
import Stateless.LogSender;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

/**
 * A sample program which acts a remote client for a EJB deployed on JBoss EAP server. This program shows how to lookup stateful and
 * stateless beans via JNDI and then invoke on them
 *
 * @author Jaikiran Pai
 */
public class RemoteEJBClient extends Thread implements Runnable{

    private String message;
    private int opcion, msInterval;
    private boolean state;
    private static ArrayList<LogSender> logger = new ArrayList();
    private static RemoteEJBClient rm = null;
    private static final String HTTP = "http";

    public static void main(String[] args) throws Exception {
       //rutinaLogsRecurrentes();
        rutinaLogIndividual();
    }
    public static void rutinaLogsRecurrentes() throws Exception{
        RemoteEJBClient rEJB = tomarDatosRecurrente(); /*Tomo datos*/

        logger = new ArrayList();
        logger.add(lookupLogSenderCORE()); /*Creo la conexion al CORE*/
        logger.add(lookupLogSenderEXT()); /*Creo la conexion al EXT*/

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
    public static void rutinaLogIndividual() throws Exception{
        tomarDatosIndividual(); /*Tomo datos*/

        logger = new ArrayList();
        logger.add(lookupLogSenderCORE()); /*Creo la conexion al CORE*/
        logger.add(lookupLogSenderEXT()); /*Creo la conexion al EXT*/

        boolean bandera = true;
        while(bandera){
            tomarDatosIndividual();
            System.out.println("Desea enviar otro mensaje al server?\n1 - Si\n2 - No");
            int opcion = new Scanner(System.in).nextInt();
            if(opcion==1){
                bandera = true;
            } else if (opcion==2){
                bandera = false;
            }
        }

    }
    public RemoteEJBClient(int msInterval, String message, boolean state, int opcion){
        this.msInterval = msInterval;
        this.message = message;
        this.state = state;
        this.opcion = opcion;
    }

    /*Generar un nuevo hilo*/
    @Override
    public void run(){
            while (state) {
                try {
                logger.get(opcion-1).infiniteLog(message);
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
    private static void tomarDatosIndividual() throws NamingException {
        String message;
        logger = new ArrayList();
        logger.add(lookupLogSenderCORE()); /*Creo la conexion al CORE*/
        logger.add(lookupLogSenderEXT()); /*Creo la conexion al EXT*/
        System.out.println("Ingrese el mensaje a enviar a la instancia:");
        message = new Scanner(System.in).nextLine();
        logger.get(0).infiniteLog(message);
    }
    private static LogSender lookupLogSenderCORE() throws NamingException{
        final Hashtable jndiProperties = new Hashtable();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        jndiProperties.put(Context.PROVIDER_URL, "remote+http://127.0.0.1:8080");
        jndiProperties.put(Context.SECURITY_PRINCIPAL, "appCOREnicolas");
        jndiProperties.put(Context.SECURITY_CREDENTIALS, "48283674");
        final Context context = new InitialContext(jndiProperties);
        // The app name is the application name of the deployed EJBs. This is typically the ear name
        // without the .ear suffix. However, the application name could be overridden in the application.xml of the
        // EJB deployment on the server.
        // Since we haven't deployed the application as a .ear, the app name for us will be an empty string
        final String moduleName = "EJBService";
        // AS7 allows each deployment to have an (optional) distinct name. We haven't specified a distinct name for
        // our EJB deployment, so this is an empty string
        final String beanName = LogBean.class.getSimpleName();
        // the remote view fully qualified class name
        final String viewClassName = LogSender.class.getName();
        // let's do the lookup
        String retorno = "ejb:/"+ moduleName + "/" + beanName + "!" + viewClassName;
        System.out.println(retorno);
        return (LogSender) context.lookup(retorno);
    }
    private static LogSender lookupLogSenderEXT() throws NamingException {
            final Hashtable jndiProperties = new Hashtable();
            jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
            jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
            jndiProperties.put(Context.PROVIDER_URL, "remote+http://127.0.0.1:9080");
            jndiProperties.put(Context.SECURITY_PRINCIPAL, "appEXTnicolas");
            jndiProperties.put(Context.SECURITY_CREDENTIALS, "48283674");
            final Context context = new InitialContext(jndiProperties);
            // The app name is the application name of the deployed EJBs. This is typically the ear name
            // without the .ear suffix. However, the application name could be overridden in the application.xml of the
            // EJB deployment on the server.
            // Since we haven't deployed the application as a .ear, the app name for us will be an empty string
            final String moduleName = "EJBService";
            // AS7 allows each deployment to have an (optional) distinct name. We haven't specified a distinct name for
            // our EJB deployment, so this is an empty string
            final String beanName = LogBean.class.getSimpleName();
            // the remote view fully qualified class name
            final String viewClassName = LogSender.class.getName();
            // let's do the lookup
            String retorno = "ejb:/"+ moduleName + "/" + beanName + "!" + viewClassName;
            System.out.println(retorno);
            return (LogSender) context.lookup(retorno);



    }
}
