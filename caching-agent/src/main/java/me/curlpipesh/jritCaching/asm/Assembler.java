package me.curlpipesh.jritCaching.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Okay. This class is possibly one of the scariest/stupidest things that I've
 * ever written. It takes a set of textual representations of Java bytecode -
 * effectively one instruction per line - and uses the ASM library to convert
 * it into the actual bytecode instructions that the JVM can understand. This
 * is basically done by looking at the first 'word' of each line - the insn
 * name - and using a <tt>switch</tt> block to get the appropriate insn type
 * for that insn. After getting the general type (<tt>ICONST_*</tt>,
 * <tt>INVOKE*</tt>, ...), it gets the exact insn type (<tt>ICONST_0</tt>,
 * <tt>INVOKEVIRTUAL</tt>, ...) and generates the correct instructions,
 * returning an {@link InsnList} of the code it just
 * generated.
 *
 * @author audrey
 * @since 3/5/16
 */
@SuppressWarnings("unused")
public class Assembler implements Opcodes {
    public static InsnList compile(final String[] insns) {
        final InsnList list = new InsnList();
        final List<LabelNode> labels = new ArrayList<>();

        for (final String e : insns) {
            // TODO: Make labels work right :(
            if (e.startsWith("L") && e.length() < 3) {
                labels.add(new LabelNode());
                continue;
            }
            final String[] split = e.trim().split(" ");
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].trim();
            }
            // ~500 lines of "Doing each instruction individually." ;-;
            // Any better method would either be longer or Reflective, neither
            // of which I want
            switch (split[0]) {
                case "LINENUMBER":
                    // TODO: Index verification
                    // TODO: Proper labels...
                    list.add(new LineNumberNode(Integer.parseInt(split[1]), labels.get(labels.size() - 1)));
                    break;
                // visitInsn
                case "NOP":
                    list.add(new InsnNode(NOP));
                    break;
                case "ACONST_NULL":
                    list.add(new InsnNode(ACONST_NULL));
                    break;
                case "ICONST_M1":
                    list.add(new InsnNode(ICONST_M1));
                    break;
                case "ICONST_0":
                    list.add(new InsnNode(ICONST_0));
                    break;
                case "ICONST_1":
                    list.add(new InsnNode(ICONST_1));
                    break;
                case "ICONST_2":
                    list.add(new InsnNode(ICONST_2));
                    break;
                case "ICONST_3":
                    list.add(new InsnNode(ICONST_3));
                    break;
                case "ICONST_4":
                    list.add(new InsnNode(ICONST_4));
                    break;
                case "ICONST_5":
                    list.add(new InsnNode(ICONST_5));
                    break;
                case "LCONST_0":
                    list.add(new InsnNode(LCONST_0));
                    break;
                case "LCONST_1":
                    list.add(new InsnNode(LCONST_1));
                    break;
                case "FCONST_0":
                    list.add(new InsnNode(FCONST_0));
                    break;
                case "FCONST_1":
                    list.add(new InsnNode(FCONST_1));
                    break;
                case "FCONST_2":
                    list.add(new InsnNode(FCONST_2));
                    break;
                case "DCONST_0":
                    list.add(new InsnNode(DCONST_0));
                    break;
                case "DCONST_1":
                    list.add(new InsnNode(DCONST_1));
                    break;
                // visitIntInsn
                case "BIPUSH":
                    // TODO: Verify correctness
                    // TODO: Verify array size
                    list.add(new IntInsnNode(BIPUSH, Integer.parseInt(split[1])));
                    break;
                case "SIPUSH":
                    // TODO: Verify correctness
                    // TODO: Verify array size
                    list.add(new IntInsnNode(SIPUSH, Integer.parseInt(split[1])));
                    break;
                // visitLdcInsn
                case "LDC":
                    // TODO: Verify array size
                    // TODO: Class constants
                    list.add(new LdcInsnNode(e.replaceFirst(split[0], "").trim()));
                    break;
                // visitVarInsn
                case "ILOAD":
                    list.add(new VarInsnNode(ILOAD, Integer.parseInt(split[1])));
                    break;
                case "LLOAD":
                    list.add(new VarInsnNode(LLOAD, Integer.parseInt(split[1])));
                    break;
                case "FLOAD":
                    list.add(new VarInsnNode(FLOAD, Integer.parseInt(split[1])));
                    break;
                case "DLOAD":
                    list.add(new VarInsnNode(DLOAD, Integer.parseInt(split[1])));
                    break;
                case "ALOAD":
                    list.add(new VarInsnNode(ALOAD, Integer.parseInt(split[1])));
                    break;
                // visitInsn
                // TODO: These seem wrong...
                case "IALOAD":
                    list.add(new InsnNode(IALOAD));
                    break;
                case "LALOAD":
                    list.add(new InsnNode(LALOAD));
                    break;
                case "FALOAD":
                    list.add(new InsnNode(FALOAD));
                    break;
                case "DALOAD":
                    list.add(new InsnNode(DALOAD));
                    break;
                case "AALOAD":
                    list.add(new InsnNode(AALOAD));
                    break;
                case "BALOAD":
                    list.add(new InsnNode(BALOAD));
                    break;
                case "CALOAD":
                    list.add(new InsnNode(CALOAD));
                    break;
                case "SALOAD":
                    list.add(new InsnNode(SALOAD));
                    break;
                // visitVarInsn
                case "ISTORE":
                    list.add(new VarInsnNode(ISTORE, Integer.parseInt(split[1])));
                    break;
                case "LSTORE":
                    list.add(new VarInsnNode(LSTORE, Integer.parseInt(split[1])));
                    break;
                case "FSTORE":
                    list.add(new VarInsnNode(FSTORE, Integer.parseInt(split[1])));
                    break;
                case "DSTORE":
                    list.add(new VarInsnNode(DSTORE, Integer.parseInt(split[1])));
                    break;
                case "ASTORE":
                    list.add(new VarInsnNode(ASTORE, Integer.parseInt(split[1])));
                    break;
                // visitInsn
                case "IASTORE":
                    list.add(new InsnNode(IASTORE));
                    break;
                case "LASTORE":
                    list.add(new InsnNode(LASTORE));
                    break;
                case "FASTORE":
                    list.add(new InsnNode(FASTORE));
                    break;
                case "DASTORE":
                    list.add(new InsnNode(DASTORE));
                    break;
                case "AASTORE":
                    list.add(new InsnNode(AASTORE));
                    break;
                case "BASTORE":
                    list.add(new InsnNode(BASTORE));
                    break;
                case "CASTORE":
                    list.add(new InsnNode(CASTORE));
                    break;
                case "SASTORE":
                    list.add(new InsnNode(SASTORE));
                    break;
                case "POP":
                    list.add(new InsnNode(POP));
                    break;
                case "POP2":
                    list.add(new InsnNode(POP2));
                    break;
                case "DUP":
                    list.add(new InsnNode(DUP));
                    break;
                case "DUP_X1":
                    list.add(new InsnNode(DUP_X1));
                    break;
                case "DUP_X2":
                    list.add(new InsnNode(DUP_X2));
                    break;
                case "SWAP":
                    list.add(new InsnNode(SWAP));
                    break;
                case "IADD":
                    list.add(new InsnNode(IADD));
                    break;
                case "LADD":
                    list.add(new InsnNode(LADD));
                    break;
                case "FADD":
                    list.add(new InsnNode(FADD));
                    break;
                case "DADD":
                    list.add(new InsnNode(DADD));
                    break;
                case "ISUB":
                    list.add(new InsnNode(ISUB));
                    break;
                case "LSUB":
                    list.add(new InsnNode(LSUB));
                    break;
                case "FSUB":
                    list.add(new InsnNode(FSUB));
                    break;
                case "DSUB":
                    list.add(new InsnNode(DSUB));
                    break;
                case "IMUL":
                    list.add(new InsnNode(IMUL));
                    break;
                case "LMUL":
                    list.add(new InsnNode(LMUL));
                    break;
                case "FMUL":
                    list.add(new InsnNode(FMUL));
                    break;
                case "DMUL":
                    list.add(new InsnNode(DMUL));
                    break;
                case "IDIV":
                    list.add(new InsnNode(IDIV));
                    break;
                case "LDIV":
                    list.add(new InsnNode(LDIV));
                    break;
                case "FDIV":
                    list.add(new InsnNode(FDIV));
                    break;
                case "DDIV":
                    list.add(new InsnNode(DDIV));
                    break;
                case "IREM":
                    list.add(new InsnNode(IREM));
                    break;
                case "LREM":
                    list.add(new InsnNode(LREM));
                    break;
                case "FREM":
                    list.add(new InsnNode(FREM));
                    break;
                case "DREM":
                    list.add(new InsnNode(DREM));
                    break;
                case "INEG":
                    list.add(new InsnNode(INEG));
                    break;
                case "LNEG":
                    list.add(new InsnNode(LNEG));
                    break;
                case "FNEG":
                    list.add(new InsnNode(FNEG));
                    break;
                case "DNEG":
                    list.add(new InsnNode(DNEG));
                    break;
                case "ISHL":
                    list.add(new InsnNode(ISHL));
                    break;
                case "LSHL":
                    list.add(new InsnNode(LSHL));
                    break;
                case "ISHR":
                    list.add(new InsnNode(ISHR));
                    break;
                case "LSHR":
                    list.add(new InsnNode(LSHR));
                    break;
                case "IUSHR":
                    list.add(new InsnNode(IUSHR));
                    break;
                case "LUSHR":
                    list.add(new InsnNode(LUSHR));
                    break;
                case "IAND":
                    list.add(new InsnNode(IAND));
                    break;
                case "LAND":
                    list.add(new InsnNode(LAND));
                    break;
                case "IOR":
                    list.add(new InsnNode(IOR));
                    break;
                case "LOR":
                    list.add(new InsnNode(LOR));
                    break;
                case "IXOR":
                    list.add(new InsnNode(IXOR));
                    break;
                case "LXOR":
                    list.add(new InsnNode(LXOR));
                    break;
                // visitIincInsn
                case "IINC":
                    // TODO: Verify correctness
                    // TODO: Verify array size
                    list.add(new IincInsnNode(IINC, Integer.parseInt(split[1])));
                    break;
                // visitInsn
                case "I2L":
                    list.add(new InsnNode(I2L));
                    break;
                case "I2F":
                    list.add(new InsnNode(I2F));
                    break;
                case "I2D":
                    list.add(new InsnNode(I2D));
                    break;
                case "L2I":
                    list.add(new InsnNode(L2I));
                    break;
                case "L2F":
                    list.add(new InsnNode(L2F));
                    break;
                case "L2D":
                    list.add(new InsnNode(L2D));
                    break;
                case "F2I":
                    list.add(new InsnNode(F2I));
                    break;
                case "F2L":
                    list.add(new InsnNode(F2L));
                    break;
                case "F2D":
                    list.add(new InsnNode(F2D));
                    break;
                case "D2I":
                    list.add(new InsnNode(D2I));
                    break;
                case "D2L":
                    list.add(new InsnNode(D2L));
                    break;
                case "D2F":
                    list.add(new InsnNode(D2F));
                    break;
                case "I2B":
                    list.add(new InsnNode(I2B));
                    break;
                case "I2C":
                    list.add(new InsnNode(I2C));
                    break;
                case "I2S":
                    list.add(new InsnNode(I2S));
                    break;
                case "LCMP":
                    list.add(new InsnNode(LCMP));
                    break;
                case "FCMPL":
                    list.add(new InsnNode(FCMPL));
                    break;
                case "FCMPG":
                    list.add(new InsnNode(FCMPG));
                    break;
                case "DCMPL":
                    list.add(new InsnNode(DCMPL));
                    break;
                case "DCMPG":
                    list.add(new InsnNode(DCMPG));
                    break;
                // visitJumpInsn
                case "IFEQ":
                    list.add(new JumpInsnNode(IFEQ, labels.get(labels.size() - 1)));
                    break;
                case "IFNE":
                    list.add(new JumpInsnNode(IFNE, labels.get(labels.size() - 1)));
                    break;
                case "IFLT":
                    list.add(new JumpInsnNode(IFLT, labels.get(labels.size() - 1)));
                    break;
                case "IFGT":
                    list.add(new JumpInsnNode(IFGT, labels.get(labels.size() - 1)));
                    break;
                case "IFLE":
                    list.add(new JumpInsnNode(IFLE, labels.get(labels.size() - 1)));
                    break;
                case "IF_ICMPEQ":
                    list.add(new JumpInsnNode(IF_ICMPEQ, labels.get(labels.size() - 1)));
                    break;
                case "IF_ICMPNE":
                    list.add(new JumpInsnNode(IF_ICMPNE, labels.get(labels.size() - 1)));
                    break;
                case "IF_ICMPLT":
                    list.add(new JumpInsnNode(IF_ICMPLT, labels.get(labels.size() - 1)));
                    break;
                case "IF_ICMPGT":
                    list.add(new JumpInsnNode(IF_ICMPGT, labels.get(labels.size() - 1)));
                    break;
                case "IF_ICMPLE":
                    list.add(new JumpInsnNode(IF_ICMPLE, labels.get(labels.size() - 1)));
                    break;
                case "IF_ACMPEQ":
                    list.add(new JumpInsnNode(IF_ICMPEQ, labels.get(labels.size() - 1)));
                    break;
                case "IF_ACMPNE":
                    list.add(new JumpInsnNode(IF_ACMPNE, labels.get(labels.size() - 1)));
                    break;
                case "GOTO":
                    list.add(new JumpInsnNode(GOTO, labels.get(labels.size() - 1)));
                    break;
                case "JSR":
                    list.add(new JumpInsnNode(JSR, labels.get(labels.size() - 1)));
                    break;
                // visitVarInsn
                case "RET":
                    list.add(new TypeInsnNode(RET, split[1]));
                    break;
                // visitTableSwitchInsn
                case "TABLESWITCH":
                    throw new IllegalStateException("TABLESWITCH not implemented!");
                    // visitLookupSwitch(Insn?)
                case "LOOKUPSWITCH":
                    throw new IllegalStateException("LOOKUPSWITCH not implemented!");
                    // visitInsn
                case "IRETURN":
                    list.add(new InsnNode(IRETURN));
                    break;
                case "LRETURN":
                    list.add(new InsnNode(LRETURN));
                    break;
                case "FRETURN":
                    list.add(new InsnNode(FRETURN));
                    break;
                case "DRETURN":
                    list.add(new InsnNode(DRETURN));
                    break;
                case "ARETURN":
                    list.add(new InsnNode(ARETURN));
                    break;
                case "RETURN":
                    list.add(new InsnNode(RETURN));
                    break;
                // visitFieldInsn
                // TODO: Verify array size etc.
                case "GETSTATIC":
                    list.add(new FieldInsnNode(GETSTATIC, split[1], split[2], split[3]));
                    break;
                case "PUTSTATIC":
                    list.add(new FieldInsnNode(PUTSTATIC, split[1], split[2], split[3]));
                    break;
                case "GETFIELD":
                    list.add(new FieldInsnNode(GETFIELD, split[1], split[2], split[3]));
                    break;
                case "PUTFIELD":
                    list.add(new FieldInsnNode(PUTFIELD, split[1], split[2], split[3]));
                    break;
                // visitMethodInsn
                case "INVOKEVIRTUAL":
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, split[1], split[2], split[3], false));
                    break;
                case "INVOKESPECIAL":
                    list.add(new MethodInsnNode(INVOKESPECIAL, split[1], split[2], split[3], false));
                    break;
                case "INVOKESTATIC":
                    list.add(new MethodInsnNode(INVOKESTATIC, split[1], split[2], split[3], false));
                    break;
                case "INVOKEINTERFACE":
                    list.add(new MethodInsnNode(INVOKEINTERFACE, split[1], split[2], split[3], false));
                    break;
                // visitInvokeDynamicInsn
                case "INVOKEDYNAMIC":
                    throw new IllegalStateException("INVOKEDYNAMIC not implemented yet!");
                    // visitTypeInsn
                case "NEW":
                    list.add(new TypeInsnNode(NEW, split[1]));
                    break;
                // visitIntInsn
                case "NEWARRAY":
                    break;
                // visitTypeInsn
                case "ANEWARRAY":
                    list.add(new TypeInsnNode(ANEWARRAY, split[1]));
                    break;
                // visitInsn
                case "ARRAYLENGTH":
                    list.add(new InsnNode(ARRAYLENGTH));
                    break;
                case "ATHROW":
                    list.add(new InsnNode(ATHROW));
                    break;
                // visitTypeInsn
                case "CHECKCAST":
                    list.add(new TypeInsnNode(CHECKCAST, split[1]));
                    break;
                case "INSTANCEOF":
                    list.add(new TypeInsnNode(INSTANCEOF, split[1]));
                    break;
                // visitInsn
                case "MONITORENTER":
                    list.add(new InsnNode(MONITORENTER));
                    break;
                case "MONITOREXIT":
                    list.add(new InsnNode(MONITOREXIT));
                    break;
                // visitMultiANewArrayInsn
                case "MULTIANEWARRAY":
                    // TODO: Verify correctness
                    // TODO: Verify array indices
                    list.add(new MultiANewArrayInsnNode(split[1], Integer.parseInt(split[2])));
                    break;
                // visitJumpInsn
                // TODO: Right labels for jumps
                case "IFNULL":
                    list.add(new JumpInsnNode(IFNULL, labels.get(labels.size() - 1)));
                    break;
                case "IFNONNULL":
                    list.add(new JumpInsnNode(IFNONNULL, labels.get(labels.size() - 1)));
                    break;
                default:
                    System.err.println("[Warning] Instruction " + split[0] + " is unknown.");
            }
        }
        return list;
    }
}
