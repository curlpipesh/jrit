package me.curlpipesh.jritCaching.util;

import me.curlpipesh.jritCaching.desc.MethodDesc;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

/**
 * @author audrey
 * @since 1/8/16.
 */
@SuppressWarnings({"unchecked", "DynamicRegexReplaceableByCompiledPattern", "Duplicates"})
public final class DescHelper {
    /**
     * Used for converting arbitrary {@link AbstractInsnNode}s
     * to String-form.
     */
    private static final Printer printer = new Textifier();

    /**
     * Used for converting arbitrary {@link AbstractInsnNode}s
     * to String-form.
     */
    private static final TraceMethodVisitor mp = new TraceMethodVisitor(printer);

    private DescHelper() {
    }

    /**
     * Returns a String representation of a given JVM instruction
     *
     * @param insn The instruction to textualise
     * @return The String representation of the given instruction
     */
    public static String insnToString(final AbstractInsnNode insn) {
        insn.accept(mp);
        // qwq
        final StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();
        return sw.toString();
    }

    /**
     * Creates a more "readable" form of the {@link MethodDesc}
     * given. Used in the "bytecode viewing" pane of the GUI.
     */
    public static String methodToString(final MethodDesc m) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<AbstractInsnNode> i = m.getNode().instructions.iterator();
        while (i.hasNext()) {
            final AbstractInsnNode insn = i.next();
            if (insn instanceof LabelNode) {
                sb.append(insnToString(insn).trim());
            } else {
                sb.append(' ').append(insnToString(insn).trim());
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
