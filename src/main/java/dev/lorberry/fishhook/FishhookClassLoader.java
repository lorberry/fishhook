package dev.lorberry.fishhook;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

public class FishhookClassLoader extends ClassLoader {
    private static JarFile jarFile;

    public FishhookClassLoader(ClassLoader parent) {
        super(parent);
    }

    public static JarFile getJar() throws URISyntaxException, IOException {
        if (jarFile == null) {
            jarFile = new JarFile(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        }
        return jarFile;
    }

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

    @Override
    protected Class<?> findClass(String name) {
        return loadClass(name);
    }
}
