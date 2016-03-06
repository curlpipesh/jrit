package me.curlpipesh.jritMain;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sun.jvmstat.monitor.*;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * The main frame for the attacher. Presents a list of VMs to the user so that
 * one can be chosen to attach
 *
 * @author audrey
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JRITFrame extends JFrame {
    private JButton jButton1;
    private JComboBox<String> jComboBox1;
    private JLabel jLabel1;

    public JRITFrame() {
        super("jRIT - Java Runtime Instrumentation Tool");
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        // Get all VMs running on localhost
        final MonitoredHost localhost;
        try {
            localhost = MonitoredHost.getMonitoredHost("localhost");
        } catch (final MonitorException | URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        final Collection<String> vms = new ArrayList<>();
        try {
            final Set<Integer> pids = localhost.activeVms();

            for (final int pid : pids) {
                final MonitoredVm vm = localhost.getMonitoredVm(new VmIdentifier("//" + pid));
                final String processName = MonitoredVmUtil.mainClass(vm, true);
                vms.add(pid + ": " + processName);
            }
        } catch (final MonitorException | URISyntaxException e) {
            e.printStackTrace();
        }


        jComboBox1 = new JComboBox<>();
        jLabel1 = new JLabel();
        jButton1 = new JButton();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // Add PIDs and whatnot to combobox
        jComboBox1.setModel(new DefaultComboBoxModel<>(vms.stream().toArray(String[]::new)));

        jLabel1.setText("Select a VM to attach:");

        jButton1.setText("Attach");
        jButton1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    // TODO: Grab and attach
                    final String identifier = (String) jComboBox1.getSelectedItem();
                    final String pid = identifier.split(": ")[0];
                    try {
                        // Set invisible if we can successfully attach, but return
                        // to visibility otherwise
                        setVisible(false);
                        final VirtualMachine vm = VirtualMachine.attach(pid);
                        vm.loadAgent(new File(JRIT.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile() + File.separator + "caching-agent-0.1.0.jar");
                        System.out.println("Agent loaded!");
                        vm.detach();
                        EventQueue.invokeLater(() -> System.exit(0));
                    } catch (AgentLoadException | AgentInitializationException | IOException | AttachNotSupportedException e) {
                        e.printStackTrace();
                        setVisible(true);
                    }
                }
            }
        });

        final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jComboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(jButton1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(jComboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton1))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }
}
