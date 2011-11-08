package kg.apc.jmeter.config;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TestCsvFileAction implements ActionListener {
    private final JTextField filename;
    private final JTextField prefix;
    private final JTextField separator;
    private final JTextArea infoArea;

    public TestCsvFileAction(JTextField fileName, JTextField prefix, JTextField separator, JTextArea infoArea) {
        this.filename = fileName;
        this.prefix = prefix;
        this.separator = separator;
        this.infoArea = infoArea;
    }

   @Override
    public void actionPerformed(ActionEvent e) {
        infoArea.setText("");
        infoArea.setForeground(Color.black);

        File f = new File(filename.getText());
        if (!f.exists()) {
            reportError("File '" + filename.getText() + "' was not found...");
            return;
        } else {
            VariableFromCsvFileReader reader = new VariableFromCsvFileReader(filename.getText());
            Map<String,String> vars = reader.getDataAsMap(prefix.getText(), separator.getText());
            reportOk("File successfuly parsed, " + vars.size() + " variable(s) found:");
            Iterator<String> iter = vars.keySet().iterator();
            while(iter.hasNext()) {
               String var = iter.next();
               reportOk("${" + var + "} = " + vars.get(var));
            }
        }
    }

    private void reportError(String msg) {
        infoArea.setText(infoArea.getText() + "Problem detected: " + msg+"\n");
        infoArea.setForeground(Color.red);
    }

    private void reportOk(String string) {
        infoArea.setText(infoArea.getText() + string+ "\n" );
    }
}
