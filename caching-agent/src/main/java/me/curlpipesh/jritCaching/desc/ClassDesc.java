package me.curlpipesh.jritCaching.desc;

import lombok.Value;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author audrey
 * @since 3/5/16.
 */
@Value
public class ClassDesc {
    private final ClassNode node;
    private final String className;
    private final String classPackage;
    private final String classSignature;
    private final String superClassName;
    private final String[] interfaceNames;
    private final int accessLevel;
    private final int javaVersion;
    private final List<MethodDesc> methods = new ArrayList<>();

    public ClassDesc(final ClassNode node, final String className, final String classPackage,
                     final String classSignature, final String superClassName, final String[] interfaceNames,
                     final int accessLevel, final int javaVersion) {
        this.node = node;
        this.className = className;
        this.classPackage = classPackage;
        this.classSignature = classSignature;
        this.superClassName = superClassName;
        this.interfaceNames = interfaceNames;
        this.accessLevel = accessLevel;
        this.javaVersion = javaVersion;
    }

    @SuppressWarnings({"unchecked", "DynamicRegexReplaceableByCompiledPattern"})
    public void analyse() {
        // TODO: Is used?
        getMethods().addAll(((List<MethodNode>) getNode().methods).stream()
                .map(m -> new MethodDesc(m, getNode(), m.access, m.name, m.desc, m.signature,
                        (String[]) m.exceptions.toArray(new String[m.exceptions.size()]))).collect(Collectors.toList()));
    }
}
