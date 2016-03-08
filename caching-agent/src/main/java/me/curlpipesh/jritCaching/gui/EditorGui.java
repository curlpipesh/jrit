package me.curlpipesh.jritCaching.gui;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import me.curlpipesh.jritCaching.BytecodeCachingAgent;
import me.curlpipesh.jritCaching.CachedClass;
import me.curlpipesh.jritCaching.asm.Assembler;
import me.curlpipesh.jritCaching.desc.MethodDesc;
import me.curlpipesh.jritCaching.util.DescHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

@Data
@EqualsAndHashCode(callSuper = false)
public class EditorGui extends JFrame {
    private JButton jButton1;
    private JButton jButton2;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JSplitPane jSplitPane1;
    private JTabbedPane jTabbedPane1;
    private JTextArea jTextArea1;
    private JToolBar jToolBar1;
    private JTree jTree1;

    // Maps packages to lists of classes
    private final Map<String, List<CachedClass>> cachedPkgClassMap = new HashMap<>();

    // TODO: Unused?
    private final Map<Integer, String> paneIndexTextMap = new HashMap<>();
    private final Map<Integer, Node<MethodDesc>> paneIndexNodeMap = new HashMap<>();

    private final String name;

    public EditorGui() {
        super("JRIT: " + ManagementFactory.getRuntimeMXBean().getName());
        name = ManagementFactory.getRuntimeMXBean().getName();
        initComponents();
        setLocationRelativeTo(null);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        // Set up the package/class map
        for (final CachedClass cachedClass : BytecodeCachingAgent.getCachedBytecode()) {
            if (!cachedPkgClassMap.containsKey(cachedClass.getPkg())) {
                cachedPkgClassMap.put(cachedClass.getPkg(), new ArrayList<>());
            }
            cachedPkgClassMap.get(cachedClass.getPkg()).add(cachedClass);
        }
        for (final Entry<String, List<CachedClass>> entry : cachedPkgClassMap.entrySet()) {
            for (final CachedClass cachedClass : entry.getValue()) {
                // "Analysis." Currently just getting method bodies
                cachedClass.getDesc().analyse();
            }
        }

        jSplitPane1 = new JSplitPane();
        jScrollPane1 = new JScrollPane();
        jTree1 = new JTree();
        jTabbedPane1 = new JTabbedPane();
        jScrollPane2 = new JScrollPane();
        jTextArea1 = new JTextArea();
        jToolBar1 = new JToolBar();
        jButton1 = new JButton();
        jButton2 = new JButton();
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        jTree1.setModel(new JARTreeModel());

        // If it's a node, render the name. If it's a method, render its name. Otherwise, default fallback etc.
        jTree1.setCellRenderer((jTree, o, b, b1, b2, i, b3) -> new JLabel(
                o instanceof Node<?> ? ((Node<?>) o).getName()
                        : o instanceof MethodDesc ? ((MethodDesc) o).getName() + ((MethodDesc) o).getDescription()
                        : "unknown D:"));

        jScrollPane1.setViewportView(jTree1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jSplitPane1.setRightComponent(jTabbedPane1);

        jToolBar1.setRollover(true);

        // Apply changes. The only way that this can actually work is by launching ANOTHER Java process
        // to start up another instrumentation because tools.jar doesn't like loading into an agent
        jButton1.setText("Apply changes");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(SwingConstants.BOTTOM);
        jButton1.addActionListener(evt -> {
            try {
                final Node<MethodDesc> node = paneIndexNodeMap.get(jTabbedPane1.getSelectedIndex());
                final CachedClass cc = (CachedClass) node.getParent().getValue();
                final byte[] input = cc.getClassBytecode();
                final ClassReader classReader = new ClassReader(input);
                final ClassNode classNode = new ClassNode();
                classReader.accept(classNode, 0);
                final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                // Apply changes to method
                ((List<MethodNode>) classNode.methods).stream().filter(m -> m.name.equals(node.getValue().getName())
                        && m.desc.equals(node.getValue().getDescription())).forEach(m -> {
                    m.instructions.clear();
                    m.instructions.insert(Assembler.compile(((JTextPane) ((JScrollPane) jTabbedPane1.getSelectedComponent())
                            .getViewport().getView()).getText().split("\n")));
                    System.err.println("Method edited: " + m.name);
                });
                classNode.accept(classWriter);
                final byte[] bytes = classWriter.toByteArray();
                // Verify correctness. Exceptions are thrown if incorrect
                CheckClassAdapter.verify(classReader, false, new PrintWriter(System.err));

                // Launch the ugly reinstrumentation agent process. Actually passes the changed bytecode as base64.
                final Process process = Runtime.getRuntime().exec("java -cp " + System.getProperty("java.home")
                        + "/../lib/tools.jar:" + EditorGui.class.getProtectionDomain().getCodeSource().getLocation()
                        .toString().replaceAll("file:", "").replaceAll("caching", "reinstrumentation")
                        + " me.curlpipesh.jritReinstrumentation.ReinstrumentationAgent "
                        + ManagementFactory.getRuntimeMXBean().getName().split("@")[0] + ' ' + cc.getPkg() + '.'
                        + cc.getName() + ' ' + Base64.getEncoder().encodeToString(bytes));
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                bufferedReader.lines().forEach(System.out::println);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setText("Revert all changes");
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(SwingConstants.BOTTOM);

        jButton2.addActionListener(evt -> {
            // TODO: Undo == broken
            ((JTextPane) ((JScrollPane) jTabbedPane1.getSelectedComponent()).getComponent(0))
                    .setText(paneIndexTextMap.get(jTabbedPane1.getSelectedIndex()));
        });
        jToolBar1.add(jButton2);

        jTree1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent mouseEvent) {
                final Node<MethodDesc> node;
                try {
                    // Try to get leaf of the tree so that we can actually get information from it
                    node = (Node<MethodDesc>) jTree1.getPathForRow(jTree1.getRowForLocation(mouseEvent.getX(),
                            mouseEvent.getY())).getLastPathComponent();
                } catch (final NullPointerException ignored) {
                    return;
                }

                if (node != null) {
                    // Create new tab as needed.
                    if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                        boolean tabExists = false;
                        for (int i = 0; i < jTabbedPane1.getTabCount(); i++) {
                            final String title = jTabbedPane1.getTitleAt(i);
                            if (title.equalsIgnoreCase(node.getParent().getName() + '#' + node.getValue().getName())) {
                                tabExists = true;
                                break;
                            }
                        }
                        if (!tabExists) {
                            final JTextPane pane = new JTextPane();
                            pane.setEditable(true);
                            pane.setFont(Font.getFont("Monospaced"));
                            pane.setText(DescHelper.methodToString(node.getValue()));

                            final JScrollPane pane2 = new JScrollPane();
                            pane2.setViewportView(pane);

                            jTabbedPane1.addTab(node.getParent().getName() + '#' + node.getValue().getName(), pane2);
                            for (int i = 0; i < jTabbedPane1.getTabCount(); i++) {
                                final String title = jTabbedPane1.getTitleAt(i);
                                if (title.equalsIgnoreCase(node.getParent().getName() + '#' + node.getValue().getName())) {
                                    jTabbedPane1.setTabComponentAt(i, getClosePanel(title, jTabbedPane1));
                                    jTabbedPane1.setSelectedIndex(i);
                                    break;
                                }
                            }
                            paneIndexTextMap.put(jTabbedPane1.getSelectedIndex(), pane.getText());
                            paneIndexNodeMap.put(jTabbedPane1.getSelectedIndex(), node);
                        }
                    }
                }
            }
        });


        final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(jSplitPane1, GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                        .addComponent(jToolBar1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jToolBar1, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(jSplitPane1, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
        );

        pack();
    }

    /**
     * http://stackoverflow.com/questions/11553112/how-to-add-close-button-to-a-jtabbedpane-tab
     *
     * @param title Title of the given tab
     * @return JTabbedPane to replace things and stuff in
     */
    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private Component getClosePanel(final String title, final JTabbedPane pane) {
        final JPanel pnlTab = new JPanel(new GridBagLayout());
        pnlTab.setOpaque(false);
        final JLabel lblTitle = new JLabel(title);
        final JButton btnClose = new JButton("x");
        btnClose.setMaximumSize(new Dimension(16, 16));

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        pnlTab.add(lblTitle, gbc);

        gbc.gridx++;
        gbc.weightx = 0;
        pnlTab.add(btnClose, gbc);
        btnClose.addActionListener(new TabCloseActionHandler(title, pane));
        // TODO: Closing a tab needs to make every map index shift down ;-;

        return pnlTab;
    }

    /**
     * Source: Somewhere on StackOverflow
     */
    private final class TabCloseActionHandler implements ActionListener {
        private final String tabName;
        private final JTabbedPane pane;

        private TabCloseActionHandler(final String tabName, final JTabbedPane pane) {
            this.tabName = tabName;
            this.pane = pane;
        }

        public String getTabName() {
            return tabName;
        }

        public void actionPerformed(final ActionEvent evt) {
            final int index = pane.indexOfTab(getTabName());
            if (index >= 0) {
                pane.removeTabAt(index);
            }
        }
    }

    private final class Node<T> {
        @Getter
        private final Node<?> parent;
        @Getter
        private final String name;
        @Getter
        private final T value;
        @Getter
        private final List<Node<?>> children = new ArrayList<>();

        private Node(final Node<?> parent, final String name, final T value) {
            this.parent = parent;
            this.name = name;
            this.value = value;
        }
    }

    private final class JARTreeModel implements TreeModel {
        private final Collection<TreeModelListener> listeners = new ArrayList<>();
        private final Node<String> root = new Node<>(null, name, name);

        private JARTreeModel() {
            for (final Entry<String, List<CachedClass>> entry : cachedPkgClassMap.entrySet()) {
                final Node<String> child = new Node<>(root, entry.getKey(), entry.getKey());
                for (final CachedClass cachedClass : entry.getValue()) {
                    final Node<CachedClass> c = new Node<>(child, cachedClass.getName() + ".class", cachedClass);
                    for (final MethodDesc desc : cachedClass.getDesc().getMethods()) {
                        final Node<MethodDesc> m = new Node<>(c, desc.getName(), desc);
                        c.getChildren().add(m);
                    }
                    child.getChildren().add(c);
                }
                root.getChildren().add(child);
            }
        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object getChild(final Object parent, final int index) {
            if (!(parent instanceof Node<?>)) {
                throw new IllegalArgumentException();
            }
            if (parent.equals(getRoot())) {
                return root.getChildren().get(index);
            }
            if (((Node<?>) parent).getChildren().isEmpty()
                    && ((Node<?>) parent).getValue() instanceof MethodDesc) {
                return ((Node<MethodDesc>) parent).getValue();
            }
            if (((Node<?>) parent).name.endsWith(".class")) {
                return ((Node<CachedClass>) parent).getChildren().get(index);
            }
            if (!((Node<?>) parent).getChildren().isEmpty()
                    && ((Node<?>) parent).getChildren().get(index).getValue() instanceof CachedClass) {
                return ((Node<CachedClass>) parent).getChildren().get(index);
            }
            System.err.println("Unable to find parent '" + parent + "', bailing out and sending root back instead.");
            return root;
        }

        @Override
        public int getChildCount(final Object parent) {
            return ((Node<?>) parent).getValue() instanceof MethodDesc ? 0 : ((Node<?>) parent).getChildren().size();
        }

        @Override
        public boolean isLeaf(final Object node) {
            return !(node instanceof Node<?>);
        }

        @Override
        public void valueForPathChanged(final TreePath path, final Object newValue) {

        }

        @SuppressWarnings("SuspiciousMethodCalls")
        @Override
        public int getIndexOfChild(final Object parent, final Object child) {
            return ((Node<?>) parent).getChildren().indexOf(child);
        }

        @Override
        public void addTreeModelListener(final TreeModelListener l) {
            if (l != null && !listeners.contains(l)) {
                listeners.add(l);
            }
        }

        @Override
        public void removeTreeModelListener(final TreeModelListener l) {
            if (l != null && listeners.contains(l)) {
                listeners.remove(l);
            }
        }
    }
}

