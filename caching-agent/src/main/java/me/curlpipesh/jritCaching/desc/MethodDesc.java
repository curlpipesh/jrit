package me.curlpipesh.jritCaching.desc;

import lombok.Data;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author audrey
 * @since 3/5/16.
 */
@Data
public class MethodDesc {
    /**
     * Access modifier of the method
     */
    private final int accessLevel;

    /**
     * Name of the method
     */
    private final String name;

    /**
     * Type description of the method
     */
    private final String description;

    /**
     * Signature of the method
     */
    private final String signature;

    /**
     * Array of Strings representing the exceptions the method throws. May be
     * empty
     */
    private final String[] thrownExceptions;

    /**
     * {@link MethodNode} for this method
     */
    private final MethodNode node;

    /**
     * {@link ClassNode} to which this method belongs
     */
    private final ClassNode owner;

    /**
     * List of places where this method is invoked
     */
    private final Collection<String> methodCallLocations = new ArrayList<>();

    public MethodDesc(final MethodNode n, final ClassNode owner, final int a, final String b, final String c, final String d, final String[] e) {
        accessLevel = a;
        name = b;
        description = c;
        signature = d;
        thrownExceptions = e;
        node = n;
        this.owner = owner;
    }
}
