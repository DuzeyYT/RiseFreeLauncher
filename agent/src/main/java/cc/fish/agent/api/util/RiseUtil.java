package cc.fish.agent.api.util;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class RiseUtil implements Opcodes {

    // check if the class is the WebSocketClient,
    // it is the only class that uses the ClientManager.connectToServer method
    public MethodNode isClassWSC(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods)
            for (AbstractInsnNode abstractInsnNode : methodNode.instructions)
                if (abstractInsnNode instanceof MethodInsnNode methodInsnNode
                        && methodInsnNode.owner.equals("org/glassfish/tyrus/client/ClientManager")
                        && methodInsnNode.name.equals("connectToServer")
                        && abstractInsnNode.getPrevious()
                                instanceof MethodInsnNode previousMethodInsnNode
                        && previousMethodInsnNode.owner.equals("java/net/URI")
                        && previousMethodInsnNode.name.equals("create")) return methodNode;

        return null;
    }

    public boolean isLoginScreenClass(ClassNode classNode) {
        return classNode.name.equals("hackclient/rise/xz");
    }

    public boolean patchLoginScreen(ClassNode classNode) {
        MethodNode loginGuiLambda = RiseUtil.isLoginGuiLambda(classNode);
        if (loginGuiLambda != null) {
            AbstractInsnNode[] usernameField = ASMUtil.findThisCall(loginGuiLambda, "hackclient/rise/aac.hS()V");
            if (usernameField!=null && usernameField[0]!=null && usernameField[1]!=null)
                ASMUtil.deleteInsnBetween(loginGuiLambda, usernameField[0], usernameField[1]);
        }

        MethodNode loginGuiDrawScreenMethod = RiseUtil.isLoginGuiDrawScreen(classNode);
        if (loginGuiDrawScreenMethod != null) {
            AbstractInsnNode[] usernameField = ASMUtil.findThisCall(loginGuiDrawScreenMethod, "hackclient/rise/xn.b(IIF)V");
            if (usernameField!=null && usernameField[0]!=null && usernameField[1]!=null)
                ASMUtil.deleteInsnBetween(loginGuiDrawScreenMethod, usernameField[0], usernameField[1]);
        }

        MethodNode loginMethod = RiseUtil.findLoginMethod(classNode);
        if (loginMethod != null)
            RiseUtil.patchLoginMethod(loginMethod);

        return loginGuiLambda != null || loginGuiDrawScreenMethod != null || loginMethod != null;
    }

    public MethodNode findLoginMethod(ClassNode classNode) {
        if (!classNode.name.equals("hackclient/rise/xz")) return null;

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("x") && methodNode.desc.equals("(Ljava/lang/String;)V"))
                return methodNode;
        }
        return null;
    }

    public void patchLoginMethod(MethodNode methodNode) {
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (abstractInsnNode.getOpcode() == Opcodes.ALOAD
                    && abstractInsnNode instanceof VarInsnNode varInsnNode
                    && varInsnNode.var == 1) {
                methodNode.instructions.insertBefore(abstractInsnNode, new LdcInsnNode("anal woods"));
                methodNode.instructions.remove(abstractInsnNode);
                break;
            }
        }
    }

    public MethodNode isLoginGuiLambda(ClassNode classNode) {
        if (!classNode.name.equals("hackclient/rise/xz")) return null;
        for (MethodNode methodNode : classNode.methods)
            if (methodNode.name.equals("c") && methodNode.desc.equals("(Lnet/minecraft/client/gui/cz;)V"))
                return methodNode;
        return null;
    }

    public MethodNode isLoginGuiDrawScreen(ClassNode classNode) {
        if (!classNode.name.equals("hackclient/rise/xz")) return null;
        for (MethodNode methodNode : classNode.methods)
            if (methodNode.name.equals("drawScreen") && methodNode.desc.equals("(IIF)V"))
                return methodNode;
        return null;
    }

    public String extractEncryptionKey(ClassLoader loader, ClassNode wscClass) {
        for (MethodNode methodNode : wscClass.methods) {
            for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
                if (abstractInsnNode instanceof MethodInsnNode methodInsnNode
                        && methodInsnNode.owner.equals("javax/websocket/RemoteEndpoint$Async")
                        && methodInsnNode.name.equals("sendText")
                        && abstractInsnNode.getPrevious()
                                instanceof MethodInsnNode previousMethodInsnNode) {

                    String className = previousMethodInsnNode.owner.replace("/", ".");
                    try {
                        Class<?> clazz = loader.loadClass(className);
                        for (Field field : clazz.getDeclaredFields()) {
                            if (field.getType().equals(String.class)) {
                                field.setAccessible(true);
                                return (String) field.get(null);
                            }
                        }
                    } catch (Exception e) {
                        JOptionPane.showConfirmDialog(
                                null, "Failed to extract encryption key: " + e.getMessage());
                        System.exit(1);
                    }
                }
            }
        }

        return null;
    }

    public boolean replaceURI(MethodNode methodNode) {
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (abstractInsnNode instanceof MethodInsnNode methodInsnNode
                    && methodInsnNode.owner.equals("java/net/URI")
                    && methodInsnNode.name.equals("create")) {
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
