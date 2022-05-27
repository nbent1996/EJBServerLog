import Stateless.CalculatorBean;
import Stateless.RemoteCalculator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.Hashtable;

/**
 * A sample program which acts a remote client for a EJB deployed on JBoss EAP server. This program shows how to lookup stateful and
 * stateless beans via JNDI and then invoke on them
 *
 * @author Jaikiran Pai
 */
public class RemoteEJBClient {

    private static final String HTTP = "http";

    public static void main(String[] args) throws Exception {
        // Invoke a stateless bean
        invokeStatelessBean();

        // Invoke a stateful bean
        //invokeStatefulBean();
    }

    /**
     * Looks up a stateless bean and invokes on it
     *
     * @throws NamingException
     */
    private static void invokeStatelessBean() throws NamingException {
        // Let's lookup the remote stateless calculator
        final RemoteCalculator statelessRemoteCalculator = lookupRemoteStatelessCalculator();
        System.out.println("Obtained a remote stateless calculator for invocation");
        // invoke on the remote calculator
        int a = 204;
        int b = 340;
        System.out.println("Adding " + a + " and " + b + " via the remote stateless calculator deployed on the server");
        int sum = statelessRemoteCalculator.add(a, b);
        System.out.println("Remote calculator returned sum = " + sum);
        if (sum != a + b) {
            throw new RuntimeException("Remote stateless calculator returned an incorrect sum " + sum + " ,expected sum was "
                + (a + b));
        }
        // try one more invocation, this time for subtraction
        int num1 = 3434;
        int num2 = 2332;
        System.out.println("Subtracting " + num2 + " from " + num1
            + " via the remote stateless calculator deployed on the server");
        int difference = statelessRemoteCalculator.subtract(num1, num2);
        System.out.println("Remote calculator returned difference = " + difference);
        if (difference != num1 - num2) {
            throw new RuntimeException("Remote stateless calculator returned an incorrect difference " + difference
                + " ,expected difference was " + (num1 - num2));
        }
    }
    private static RemoteCalculator lookupRemoteStatelessCalculator() throws NamingException {
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
            final String beanName = CalculatorBean.class.getSimpleName();
            // the remote view fully qualified class name
            final String viewClassName = RemoteCalculator.class.getName();
            // let's do the lookup
            String retorno = "ejb:/"+ moduleName + "/" + beanName + "!" + viewClassName;
            System.out.println(retorno);
            return (RemoteCalculator) context.lookup(retorno);



    }
}
