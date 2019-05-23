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

        ProcessBuilder pb = new ProcessBuilder("java", "-jar",  "file://C:\\Users\\ricar\\OneDrive\\Área de Trabalho\\CSD\\SecureReplicatedBank\\src\\rest\\Hello.jar");
        Process p = pb.start();
        p.destroy();
        while(p.isAlive()){
            System.out.println("ola");
        }


        // Create a new URLClassLoader
        //URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

        // Load the target class
        //Class<?> helloClass = urlClassLoader.loadClass("rest.Hello");

        // Create a new instance from the loaded class
        //Constructor<?> constructor = helloClass.getConstructor();
        //Object beanObj = constructor.newInstance();

        // Getting a method from the loaded class and invoke it
        //Method method = helloClass.getMethod("sayHello");
        //method.invoke(beanObj);

    }

}
