// Created on 8-Feb-07

package ca.onfire.sample;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A simple multi-line, non-editable, scrollable text display widget.
 *
 * @version $Version$
 * @author pdowler
 */
public class JTextDisplay extends JPanel
{
    public JTextDisplay(String content)
    {
        super(new BorderLayout());
        init(content);
    }
    
    private void init(String content)
    {
        JTextArea textWidget = new JTextArea();
        textWidget.setEditable(false);
        this.add(new JScrollPane(textWidget), BorderLayout.CENTER);
        textWidget.setText(content);
    }
}
