package dev.lorberry.fishhook.loader;

import com.sun.tools.attach.*;

import java.io.File;
import java.io.IOException;

public class Loader {

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

    private static VirtualMachineDescriptor findMcProcess() {
        for (VirtualMachineDescriptor vm : VirtualMachine.list()) {
            if (vm.displayName().startsWith("net.minecraft")) {
                return vm;
            }
        }
        return null;
    }
}
