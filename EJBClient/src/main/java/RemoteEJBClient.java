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
    private static ArrayList<LogSender> logger = new ArrayList();
    private static ArrayList<String> usuariosConexion = new ArrayList();
    private static ArrayList<String> urlsConexion = new ArrayList();

    public static void main(String[] args) throws Exception {

        /*Cargar datos de conexion*/
      usuariosConexion.add("appCOREnicolas");
       usuariosConexion.add("appEXTnicolas");

       urlsConexion.add("remote+http://127.0.0.1:8080");
       urlsConexion.add("remote+http://127.0.0.1:9080");

        //rutinaLogsRecurrentes();
        rutinaLogIndividual();

    }
    public static void rutinaLogsRecurrentes() throws Exception{
        RemoteEJBClient rEJB = tomarDatosRecurrente(); /*Tomo datos*/

        logger = new ArrayList();
        logger.add(lookupLogSender(1)); /*Creo la conexion al CORE*/
        logger.add(lookupLogSender(3)); /*Creo la conexion al CORE2*/

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
        logger.add(lookupLogSender(1)); /*Creo la conexion al CORE*/
        logger.add(lookupLogSender(3)); /*Creo la conexion al CORE2*/

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
    private static void tomarDatosIndividual() throws NamingException {
        String message;
        logger = new ArrayList();
        logger.add(lookupLogSender(1)); /*Creo la conexion al CORE*/
        logger.add(lookupLogSender(3)); /*Creo la conexion al CORE2*/
        System.out.println("Ingrese el mensaje a enviar a la instancia:");
        message = new Scanner(System.in).nextLine();
        try{
            logger.get(0).Log(message);
        }catch(RequestSendFailedException ex){
            System.out.println("ERROR DE CONEXION AL NODO MASTER1");
            try{
                logger.get(1).Log(message);
            }catch(RequestSendFailedException e){
                System.out.println("ERROR DE CONEXION AL NODO MASTER2, SERVICIO CAIDO");
            }
        }
    }
    private static LogSender lookupLogSender(int modo) throws NamingException, RequestSendFailedException {
        String url ="";
        String user ="";
        switch(modo){
            case 1: /*MODO CORE*/
                url = urlsConexion.get(0);
                user = usuariosConexion.get(0);
            break;

            case 2: /*MODO EXT*/
                url = urlsConexion.get(1);
                user = usuariosConexion.get(1);
            break;

            case 3: /*MODO CORE2*/
                url = urlsConexion.get(1);
                user = usuariosConexion.get(0);
            break;
        }
        final Hashtable jndiProperties = new Hashtable();
        jndiProperties.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        jndiProperties.put(Context.PROVIDER_URL, url);
        jndiProperties.put(Context.SECURITY_PRINCIPAL, user);
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
