package dev.lorberry.fishhook.agent;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

/**
 * @author lorberry
 */
public class FishClassLoader extends ClassLoader {
    private static JarFile jarFile;

    /**
     * Constructor with parent class loader
     *
     * @param parent Parent class loader
     */
    public FishClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Returns the agent jar file
     *
     * @return JarFile of agent
     */
    public static JarFile getJar() throws URISyntaxException, IOException {
        if (jarFile == null) {
            jarFile = new JarFile(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        }
        return jarFile;
    }

    /**
     * Loads a class by name from parent or agent jar
     *
     * @param name Class name
     * @return Loaded class or null
     */
    @Override
    public Class<?> loadClass(String name) {
        Class<?> c = null;
        try {
            c = getParent().loadClass(name);
        } catch (ClassNotFoundException ignored) {
        }
        if (c != null) {
            return c;
        }

        JarFile jar;
        try {
            jar = getJar();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        if (jar.getEntry(name.replace('.', '/') + ".class") != null) {
            byte[] classBytes;
            try {
                classBytes = jar.getInputStream(jar.getEntry(name.replace('.', '/') + ".class")).readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return null;
    }

    /**
     * Finds and loads a class by name
     *
     * @param name Class name
     * @return Loaded class or null
     */
    @Override
    protected Class<?> findClass(String name) {
        return loadClass(name);
    }
}
