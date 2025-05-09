package dev.lorberry.fishhook.loader;

import com.sun.tools.attach.*;

import java.io.File;
import java.io.IOException;

/**
 * @author lorberry
 */
public class Loader {

    /**
     * Main method. Finds the agent file and attaches it to the minecraft process.
     */
    public static void main(String[] args) {
        File agentFile = new File("agent.jar");
        if (!agentFile.exists()) {
            System.out.println("Agent file not found");
            return;
        }

        System.out.println("Found agent file");

        VirtualMachineDescriptor mcProcess = findMcProcess();
        if (mcProcess == null) {
            System.out.println("No minecraft process found");
            return;
        }

        System.out.println("Found minecraft process");

        try {
            VirtualMachine vm = VirtualMachine.attach(mcProcess.id());
            vm.loadAgent(agentFile.getAbsolutePath());
            vm.detach();
            System.out.println("Successfully attached Fish!");
        } catch (AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds the running Minecraft process.
     *
     * @return VirtualMachineDescriptor of Minecraft, or null if not found.
     */
    private static VirtualMachineDescriptor findMcProcess() {
        for (VirtualMachineDescriptor vm : VirtualMachine.list()) {
            if (vm.displayName().startsWith("net.minecraft")) {
                return vm;
            }
        }
        return null;
    }
}
