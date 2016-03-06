package me.curlpipesh.jritMain;

import java.awt.*;

/**
 * <tt>JRIT</tt> is short for "Java Runtime Instrumentation Tool."
 * <p>
 * Main class. Used for finding running JVM instances and attaching the
 * instrumentation caching as needed.
 *
 * @author audrey
 * @since 3/5/16
 */
@SuppressWarnings("unused")
public final class JRIT {
    private JRIT() {
    }

    public static void main(final String[] args) {
        EventQueue.invokeLater(() -> new JRITFrame().setVisible(true));
    }
}
