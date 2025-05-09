package dev.lorberry.fishhook.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Java agent entry point for Fish
 *
 * @author lorberry
 */
public class Main {

    /**
     * agentmain method. Loads Fish classes into the Minecraft class loader.
     */
    public static void agentmain(String agentArgs, Instrumentation inst) throws URISyntaxException, IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            System.out.println("Started Fish agent (Loaded classes: " + inst.getAllLoadedClasses().length + ")");

            FishClassLoader fCl = null;
            // minecraft class loader
            ClassLoader mcCl = null;
            Class<?> mcClC = null;

            for (Class<?> c : inst.getAllLoadedClasses()) {
                if (c.getTypeName().startsWith("net.minecraft")) {
                    mcCl = c.getClassLoader();
                    mcClC = mcCl.getClass();
                    fCl = new FishClassLoader(mcCl);
                    System.out.println("Attached Fish agent");
                    break;
                }
            }

            if (fCl == null) {
                throw new RuntimeException("Minecraft class loader could not be found");
            }

            JarFile jarFile = FishClassLoader.getJar();
            ArrayList<JarEntry> entriesToLoad = new ArrayList<>();

            for (JarEntry file : jarFile.stream().toList()) {
                if (file.getName().endsWith(".class") && file.getName().startsWith("dev/lorberry/fishhook")) {
                    entriesToLoad.add(file);
                }
            }

            Method defineClassMethod = mcClC.getMethod("defineClassFwd", String.class, byte[].class, int.class, int.class, CodeSource.class);
            defineClassMethod.setAccessible(true);

            while (!entriesToLoad.isEmpty()) {
                ArrayList<JarEntry> failed = new ArrayList<>();

                for (JarEntry file : entriesToLoad) {
                    byte[] classBytes = jarFile.getInputStream(file).readAllBytes();
                    String className = file.getName().replace("/", ".").replace(".class", "");

                    try {
                        defineClassMethod.invoke(mcCl, className, classBytes, 0, classBytes.length, null);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        if (e.getCause() instanceof LinkageError) {
                            failed.add(file);
                        } else {
                            throw e;
                        }
                    }

                    if (failed.size() == entriesToLoad.size()) {
                        throw new RuntimeException("Failed to load any classes");
                    } else {
                        entriesToLoad = failed;
                    }
                }
            }

            Fish.init();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
