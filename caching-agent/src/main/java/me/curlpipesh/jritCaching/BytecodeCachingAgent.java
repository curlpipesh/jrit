package me.curlpipesh.jritCaching;

import lombok.Getter;
import me.curlpipesh.jritCaching.gui.EditorGui;

import java.awt.*;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;

/**
 * The initial agent attached by the tools. Uses a
 * {@link java.lang.instrument.ClassFileTransformer} to cache the initial
 * bytecode of every loaded class, so that a 'revert to no changes' option is
 * possible. For SOME ungodly reason, we can't actually communicate data back
 * to the "main thread" (jritMain), so
 *
 * @author audrey
 * @since 3/5/16
 */
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection", "TypeMayBeWeakened"})
public class BytecodeCachingAgent {
    /**
     * The list of class bytecode that has been cached by this agent.
     * <p>
     * TODO: Potential problems with this approach (see below)
     * <ul>
     * <li>Can't handle multiple processes at once</li>
     * <li>Cached bytecode becomes useless after the VM dies</li>
     * </ul>
     */
    @Getter
    private static final List<CachedClass> cachedBytecode = new ArrayList<>();

    /**
     * The number of classes that have been cached. Used for debug printing.
     */
    private static int cacheCounter;

    /**
     * Run when something attaches a new instance of this
     * agent to a {@link com.sun.tools.attach.VirtualMachine}.
     *
     * @param agentArgs       Arguments being passed in from
     *                        {@link com.sun.tools.attach.VirtualMachine#loadAgent(String, String)}.
     *                        Expected to be <tt>null</tt>.
     * @param instrumentation The {@link Instrumentation} instance passed in by
     *                        {@link com.sun.tools.attach.VirtualMachine#loadAgent(String, String)}.
     *                        A single {@link java.lang.instrument.ClassFileTransformer}
     *                        is registered with this <tt>Instrumentation</tt>
     *                        instance, that does nothing but cache the
     *                        bytecode passed into it.
     */
    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        instrumentation.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
            // Get class' package
            String pkg;
            try {
                pkg = classBeingRedefined.getPackage().getName();
                if (pkg.isEmpty()) {
                    throw new Exception("Falling down to catch{} to handle because lazy~ Something something DRY~");
                }
            } catch (final Exception e) {
                System.err.println("Found default package: " + classBeingRedefined.getName());
                pkg = "<default package>";
            }
            // Add the bytecode given to us to the cache so that undo functionality can come true
            try {
                cachedBytecode.add(new CachedClass(classBeingRedefined.getSimpleName(), pkg,
                        classfileBuffer));
                ++cacheCounter;
            } catch (final Exception e) {
                System.err.println("Class failed: " + classBeingRedefined.getName());
                e.printStackTrace();
            }
            return classfileBuffer;
        }, true);
        try {
            // Apparently this works to instrument everything.
            for (final Class<?> c : instrumentation.getInitiatedClasses(Class.forName(getMainClassName()).getClassLoader())) {
                try {
                    // #TestedWithMinecraft ;-;
                    instrumentation.retransformClasses(c);
                } catch (final UnmodifiableClassException ignored) {
                    cachedBytecode.stream().filter(e -> e.getName().equals(c.getSimpleName())).forEach(cachedBytecode::remove);
                    System.err.println("Ignored immutable class: " + c.getName());
                }
            }
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Cached " + cacheCounter + " classes.");
        EventQueue.invokeLater(() -> new EditorGui().setVisible(true));
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

    /**
     * Ugly hack to determine whether or not a class is an inored JVM,
     * namely a class from <tt>rt.jar</tt>.
     *
     * @param c The class to check
     * @return <tt>true</tt> if the class was loaded from <tt>rt.jar</tt>,
     * <tt>false</tt> otherwise.
     */
    private static boolean isIgnoredClass(final Class c) {
        try {
            final String pkg = c.getPackage().getName();
            final String l = c.getProtectionDomain().getCodeSource().getLocation().toString();
            return l.contains("rt.jar") || c.getName().contains("/")
                    || pkg.startsWith("java") || pkg.startsWith("sun") || pkg.startsWith("com.sun");
        } catch (final Exception e) {
            // If an exception is thrown, automatically assume that it's an issue.
            return true;
        }
    }

}
