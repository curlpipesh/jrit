package me.curlpipesh.jritCaching;

import lombok.Value;
import me.curlpipesh.jritCaching.desc.ClassDesc;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

/**
 * A representation of a class whose bytecode is cached. Stores:
 * <ul>
 * <li>Name</li>
 * <li>Package</li>
 * <li>Original bytecode</li>
 * </ul>
 */
@Value
public final class CachedClass {
    /**
     * Simple name of the class
     */
    private final String name;

    /**
     * Full package of the class, not including name
     */
    private final String pkg;

    /**
     * Original bytecode for the class
     */
    private final byte[] classBytecode;

    /**
     * "Description" for the class.
     */
    private final ClassDesc desc;

    @SuppressWarnings("unchecked")
    public CachedClass(final String name, final String pkg, final byte[] classBytecode) {
        this.name = name;
        this.pkg = pkg;

        this.classBytecode = classBytecode;

        final ClassReader classReader = new ClassReader(classBytecode);
        final ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        desc = new ClassDesc(classNode, name, pkg, classNode.signature, classNode.superName,
                ((List<String>) classNode.interfaces).stream().toArray(String[]::new), classNode.access, classNode.version);
    }
}
