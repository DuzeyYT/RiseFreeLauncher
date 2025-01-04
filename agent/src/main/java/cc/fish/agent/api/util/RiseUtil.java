package cc.fish.agent.api.util;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@UtilityClass
public class RiseUtil implements Opcodes {

    // check if the class is the WebSocketClient,
    // it is the only class that uses the ClientManager.connectToServer method
    public MethodNode isClassWSC(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods)
            for (AbstractInsnNode abstractInsnNode : methodNode.instructions)
                if (abstractInsnNode instanceof MethodInsnNode methodInsnNode
                        && methodInsnNode.owner.equals("org/glassfish/tyrus/client/ClientManager") && methodInsnNode.name.equals("connectToServer")
                        && abstractInsnNode.getPrevious() instanceof MethodInsnNode previousMethodInsnNode && previousMethodInsnNode.owner.equals("java/net/URI")
                        && previousMethodInsnNode.name.equals("create"))
                    return methodNode;

        return null;
    }

    public String extractEncryptionKey(ClassLoader loader, ClassNode wscClass) {
//        JOptionPane.showMessageDialog(null, "Found WSC class: " + wscClass.name);

        for (MethodNode methodNode : wscClass.methods) {
            for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
                if (abstractInsnNode instanceof MethodInsnNode methodInsnNode
                        && methodInsnNode.owner.equals("javax/websocket/RemoteEndpoint$Async") && methodInsnNode.name.equals("sendText")
                        && abstractInsnNode.getPrevious() instanceof MethodInsnNode previousMethodInsnNode) {

                    String className = previousMethodInsnNode.owner.replace("/", ".");
//                    JOptionPane.showMessageDialog(null, "Found class: " + className);
                    try {
                        Class<?> clazz = loader.loadClass(className);
                        for (Field field : clazz.getDeclaredFields()) {
                            if (field.getType().equals(String.class)) {
                                field.setAccessible(true);
                                return (String) field.get(null);
                            }
                        }
                    } catch (Exception e) {
//                        JOptionPane.showConfirmDialog(null, "Failed to extract encryption key: " + e.getMessage());
                        System.exit(1);
                    }
                }
            }
        }

        return null;
    }

    public boolean replaceURI(MethodNode methodNode) {
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (abstractInsnNode instanceof MethodInsnNode methodInsnNode && methodInsnNode.owner.equals("java/net/URI") && methodInsnNode.name.equals("create")) {
                methodNode.instructions.remove(abstractInsnNode.getPrevious());
                InsnList insnList = new InsnList();
                insnList.add(new LdcInsnNode("ws://localhost:8443"));
                methodNode.instructions.insertBefore(abstractInsnNode, insnList);
                return true;
            }
        }
        return false;
    }
}