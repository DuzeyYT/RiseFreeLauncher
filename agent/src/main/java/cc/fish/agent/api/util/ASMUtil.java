package cc.fish.agent.api.util;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@UtilityClass
public class ASMUtil {

    public byte[] writeClassToArray(ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public ClassNode getNode(byte[] bytes) {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        return node;
    }

    public void deleteInsnBetween(MethodNode methodNode, AbstractInsnNode start, AbstractInsnNode end) {
        AbstractInsnNode current = start;
        while (current != end) {
            AbstractInsnNode next = current.getNext();
            methodNode.instructions.remove(current);
            current = next;
        }
        methodNode.instructions.remove(current);
    }

    public AbstractInsnNode[] findThisCall(MethodNode methodNode, String name) {
        AbstractInsnNode start = null, end = null;
        for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
            if (abstractInsnNode.getOpcode() == Opcodes.ALOAD
                    && abstractInsnNode instanceof VarInsnNode varInsnNode) {
                if (varInsnNode.var == 0) start = abstractInsnNode;
            }

            if (start != null && abstractInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL
                    && abstractInsnNode instanceof MethodInsnNode methodInsnNode) {
                if (name.equals(methodInsnNode.owner + "." + methodInsnNode.name + methodInsnNode.desc)) {
                    end = abstractInsnNode;
                    break;
                }
            }
        }

        if (start == null || end == null) return null;

        return new AbstractInsnNode[] { start, end };
    }

    public MethodNode findMethod(ClassNode classNode, String name, String desc) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals(name) && methodNode.desc.equals(desc)) return methodNode;
        }
        return null;
    }

    public boolean isStringInClass(ClassNode classNode, String string) {
        for (MethodNode methodNode : classNode.methods) {
            for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
                if (abstractInsnNode instanceof LdcInsnNode ldcInsnNode
                        && ldcInsnNode.cst instanceof String
                        && ldcInsnNode.cst.equals(string)) return true;
            }
        }
        return false;
    }
}
