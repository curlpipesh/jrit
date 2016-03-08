package me.curlpipesh.jritReinstrumentation;

import com.sun.tools.attach.VirtualMachine;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Base64;

@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection", "TypeMayBeWeakened"})
public final class ReinstrumentationAgent {
    private ReinstrumentationAgent() {
    }

    /**
     * <tt>agentArgs</tt> expected to be input as className:base64Bytecode
     *
     * @param agentArgs       I can't believe we actually pass base64'd
     *                        bytecode in this :(
     * @param instrumentation Self-explanatory
     */
    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        // TODO: Fix blatantly ignoring InternalErrors.........
        System.err.println("Preparing update...");
        final String[] split = agentArgs.split(":");
        final String name = split[0];
        final String b64Bytes = split[1];
        final byte[] classBytes = Base64.getDecoder().decode(b64Bytes);
        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            if(name.trim().equalsIgnoreCase(classBeingRedefined.getName().trim())) {
                try {
                    System.err.println("Updating class: " + classBeingRedefined.getName());
                    final ClassReader classReader = new ClassReader(classBytes);
                    final ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, 0);
                    final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    classNode.accept(classWriter);

                    final byte[] bytes = classWriter.toByteArray();
                    CheckClassAdapter.verify(classReader, false, new PrintWriter(System.err));

                    return bytes;
                } catch(final InternalError ignored) {
                } catch(final Exception e) {
                    System.err.println("Error doing instrumentation:\n" + e.getClass().getSimpleName()
                            + ": " + e.getMessage());
                }
            }
            return classfileBuffer;
        }, true);
        try {
            for(final Class<?> c : instrumentation.getInitiatedClasses(Class.forName(getMainClassName()).getClassLoader())) {
                try {
                    instrumentation.retransformClasses(c);
                } catch(final InternalError ignored) {
                } catch(final Throwable e) {
                    if(e instanceof UnmodifiableClassException) {
                        continue;
                    }
                    System.err.println("Error doing instrumentation:\n" + e.getClass().getSimpleName()
                            + ": " + e.getMessage());
                }
            }
        } catch(final InternalError ignored) {
        } catch(final Throwable e) {
            System.err.println("Error doing instrumentation:\n" + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
        }
    }

    /**
     * Super super super ugly hack that gets the name of the main class of the
     * JVM we're attached to. For some ungodly reason, this is actually stored
     * as a system property (I'd've expected it to be in {@link Runtime} or
     * {@link java.lang.management.RuntimeMXBean} or something...), so we can
     * just read it in, in the form <tt>main_class arg1 arg2...</tt>, split it
     * on whitespaces, and then just take the first element of the array
     * derived from splitting it.
     *
     * @return The name of the main class. It tries.
     */
    private static String getMainClassName() {
        return System.getProperty("sun.java.command").split(" ")[0]; // like "org.x.y.Main arg1 arg2"
    }

    public static void main(final String[] args) {
        System.err.println("Running!");
        try {
            final VirtualMachine virtualMachine = VirtualMachine.attach(args[0]);
            virtualMachine.loadAgent(ReinstrumentationAgent.class.getProtectionDomain().getCodeSource().getLocation().getFile(),
                    args[1] + ':' + args[2]);
            virtualMachine.detach();
            System.err.println("Done!");
        } catch(final Exception e) {
            e.printStackTrace();
        }
    }
}
