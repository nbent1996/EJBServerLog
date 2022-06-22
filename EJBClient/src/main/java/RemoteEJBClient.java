import EJB.LogBean;
import EJB.LogSender;
import HA.Constants;
import HA.WildflyInstance;
import org.jboss.ejb.client.RequestSendFailedException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Scanner;

public class RemoteEJBClient extends Thread implements Runnable{

    private String message;
    private int opcion, msInterval;
    private boolean state;
    private static WildflyInstance node1 = new WildflyInstance("appCOREnicolas", "48283674", "remote+http://127.0.0.1:8080");
    private static WildflyInstance node2 = new WildflyInstance("appCOREnicolas", "48283674", "remote+http://127.0.0.1:9080");
    private static List<WildflyInstance> HAInstances = Arrays.asList(node1, node2);
    private static ArrayList<LogSender> logger = new ArrayList();

    public RemoteEJBClient(int msInterval, String message, boolean state, int opcion){
        this.msInterval = msInterval;
        this.message = message;
        this.state = state;
        this.opcion = opcion;
    }

    public static void main(String[] args) throws Exception {
        logger.add(lookupLogSender(HAInstances.get(0))); /*Creo la conexion al NODO1*/
        logger.add(lookupLogSender(HAInstances.get(1))); /*Creo la conexion al NODO2*/

        //rutinaLogsRecurrentes();
        rutinaLogIndividual();

    }
    /*Logs enviados por el usuario*/
    public static void rutinaLogIndividual() throws Exception{
        while(true){
            String message;
            System.out.println("Ingrese el mensaje a enviar a la instancia o -1 para finalizar ejecución:");
            message = new Scanner(System.in).nextLine();
            if(message.equals("-1")){
                return;
            }
            try{
                logger.get(0).Log(message);
            }catch(RequestSendFailedException ex){
                System.out.println("ERROR DE CONEXION AL NODO1");
                try{
                    logger.get(1).Log(message);
                }catch(RequestSendFailedException e){
                    System.out.println("ERROR DE CONEXION AL NODO2, SERVICIO CAIDO");
                }
            }
        }
    }
    /*Logs enviados por el usuario*/


    /*Logs Recurrentes*/
    @Override
    public void run(){
            while (state) {
                try {
                logger.get(opcion-1).Log(message);
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
    public static void rutinaLogsRecurrentes() throws Exception{
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
    /*Logs Recurrentes*/

    /*Invocacion al EJB*/

    private static LogSender lookupLogSender(WildflyInstance nodo) throws NamingException, RequestSendFailedException {
        final Hashtable jndiProperties = new Hashtable();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        jndiProperties.put(Context.PROVIDER_URL, nodo.getUrl());
        jndiProperties.put(Context.SECURITY_PRINCIPAL, nodo.getUser());
        jndiProperties.put(Context.SECURITY_CREDENTIALS, nodo.getPassword());
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
    /*Invocacion al EJB*/
}
