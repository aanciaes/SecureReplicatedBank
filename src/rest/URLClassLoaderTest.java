package rest;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author ashraf
 *
 */
public class URLClassLoaderTest {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Getting the jar URL which contains target class
        URL[] classLoaderUrls = new URL[]{new URL("file://C:\\Users\\ricar\\OneDrive\\Área de Trabalho\\CSD\\SecureReplicatedBank\\src\\rest\\Hello.jar")};
        // Create a new URLClassLoader
        URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);
        /*ProcessBuilder pb = new ProcessBuilder("java", "-jar",  "file://C:\\Users\\ricar\\OneDrive\\Área de Trabalho\\CSD\\SecureReplicatedBank\\src\\rest\\Hello.jar");
        Process p = pb.start();
        //p.destroy();
        while(p.isAlive()){
            System.out.println("ola");
        }




        // Load the target class
        Class<?> helloClass = urlClassLoader.loadClass("rest.Hello");

        // Create a new instance from the loaded class
        Constructor<?> constructor = helloClass.getConstructor();
        Object beanObj = constructor.newInstance();

        // Getting a method from the loaded class and invoke it
        Method method = helloClass.getMethod("main");
        method.invoke(beanObj);*/

        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar",  "file://C:\\Users\\ricar\\OneDrive\\Área de Trabalho\\CSD\\SecureReplicatedBank\\src\\rest\\Hello.jar");
            Process p = pb.start();
            final Class<?> clazz = Class.forName("rest.Hello");
            final Method method = clazz.getMethod("main", String[].class);

            final Object[] arg = new Object[1];
            arg[0] = new String[] { "1", "2"};
            method.invoke(null, arg);
            p.destroy();
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

}
