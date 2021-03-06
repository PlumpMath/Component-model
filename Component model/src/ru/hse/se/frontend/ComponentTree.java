package ru.hse.se.frontend;

import ru.hse.se.nodes.Node;
import ru.hse.se.parsers.VRMLParser;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Assembles the UI. The UI consists of a JTreeTable and a menu.
 * The JTreeTable uses a ComponentsModel to visually represent a Components
 * file stored in the Netscape file format.
 *
 * @author Tim Akhmetgareev
 */
public class ComponentTree {
    /** Number of instances of ComponentTree. */
    private static int         ttCount;

    /** Used to represent the model. */
    private JTreeTable         treeTable;
    /** Frame containing everything. */
    private JFrame             frame;



    private JPanel panel;
    /** Path created for. */
    private String             path;
    JTextArea textArea;
     static ArrayList<Node> nodes;
    static String oldValue;
    static String newValue;
    JTextArea jTextArea;


    /**
     * Creates a ComponentTree, loading the Components from the file
     * at <code>path</code>.
     */
    public ComponentTree(String path) {
	this.path = path;
	ttCount++;
        jTextArea=new JTextArea();
        textArea=new JTextArea();
    redirectSystemStreams();
	frame = createFrame();
	Container       cPane = frame.getContentPane();
	JMenuBar        mb = createMenuBar();
	TreeTableModel  model = createModel(path);


        cPane.setLayout(new BorderLayout(5,5));
        Dimension dim=new Dimension(600, 600);
        cPane.setPreferredSize(dim);


        try {
            textArea.read(new FileReader(path),null);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        treeTable = createTreeTable(model);
        textArea.setEditable(false);

        JScrollPane tp =new JScrollPane(textArea,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane sp = new JScrollPane(treeTable,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane cp=new JScrollPane(jTextArea,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getViewport().setBackground(Color.white);

        Dimension scrDim=new Dimension(300,600);
        Dimension dime=new Dimension(300,400) ;
        Dimension dimc=new Dimension(300,200);
        sp.setLocation(0,0);
        sp.setPreferredSize(dime);
        tp.setLocation(300,0);
        tp.setPreferredSize(scrDim);
        cp.setLocation(0,400);
        cp.setPreferredSize(dimc);
	    cPane.add(sp, BorderLayout.LINE_START);
     cPane.add(tp, BorderLayout.CENTER);
        cPane.add(cp,BorderLayout.PAGE_END);
        cPane.setPreferredSize(dim);


    //frame.add(cPane);
    frame.setJMenuBar(mb);
	//frame.pack();

	frame.setVisible(true);
    }


    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jTextArea.append(text);
            }
        });
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    /**
     * Creates and returns the instanceof JTreeTable that will be used.
     */

    protected JTreeTable createTreeTable(TreeTableModel model) {
	final JTreeTable       treeTable = new JTreeTable(model);
        treeTable.getTree().getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
	treeTable.setDefaultRenderer(Object.class,
				     new ComponentsStringRenderer());
        treeTable.getTree().setCellRenderer(new VRMLComponentsCellRenderer());


       /* treeTable.getTree().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                textArea.select(textArea.getText().indexOf(e.getPath().getLastPathComponent().toString()),e.getPath().getLastPathComponent().toString().length()+textArea.getText().indexOf(e.getPath().getLastPathComponent().toString()));
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });  */


	return treeTable;
    }

    /**
     * Creates the ComponentsModel for the file at <code>path</code>.
     */
    protected TreeTableModel createModel(String path) {
	Components components = new Components(new VRMLParser(), path);
	return new ComponentsModel(components.getRoot());
    }

    /**
     * Creates the JFrame that will contain everything.
     */
    protected JFrame createFrame() {
	JFrame       retFrame = new JFrame("TreeTable III -- " + path);
        Dimension dimension = new Dimension(600, 400)          ;
        retFrame.setSize(600,600);
        retFrame.setMinimumSize(dimension);
	retFrame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent we) {
		frame.dispose();
		if (--ttCount == 0) {
		    System.exit(0);
		}
	    }
	});
	return retFrame;
    }

    /**
     * Creates a menu bar.
     */
    protected JMenuBar createMenuBar() {
        JMenu            fileMenu = new JMenu("File"); 
	JMenuItem        menuItem;

	menuItem = new JMenuItem("Open");
	menuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		JFileChooser      fc = new JFileChooser(path);
		int               result = fc.showOpenDialog(frame);

		if (result == JFileChooser.APPROVE_OPTION) {
		    String      newPath = fc.getSelectedFile().getPath();

		    new ComponentTree(newPath);

		}
	    }
	});
	fileMenu.add(menuItem);
	fileMenu.addSeparator();
	
	menuItem = new JMenuItem("Exit"); 
	menuItem.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent ae) {
		System.exit(0);
	    }
	});
	fileMenu.add(menuItem); 


	// Create a menu bar
	JMenuBar        menuBar = new JMenuBar();

	menuBar.add(fileMenu);

	// Menu for the look and feels (lafs).
	UIManager.LookAndFeelInfo[]        lafs = UIManager.
	                                    getInstalledLookAndFeels();
	ButtonGroup                        lafGroup = new ButtonGroup();

	JMenu          optionsMenu = new JMenu("Options");

	menuBar.add(optionsMenu);

	for(int i = 0; i < lafs.length; i++) {
	    JRadioButtonMenuItem rb = new JRadioButtonMenuItem(lafs[i].
							       getName()); 
	    optionsMenu.add(rb);
	    rb.setSelected(UIManager.getLookAndFeel().getName().equals
			   (lafs[i].getName()));
	    rb.putClientProperty("UIKey", lafs[i]);
	    rb.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent ae) {
	            JRadioButtonMenuItem rb2 = (JRadioButtonMenuItem)ae.
			                       getSource();
	            if(rb2.isSelected()) {
		        UIManager.LookAndFeelInfo       info =
                                      (UIManager.LookAndFeelInfo)
			               rb2.getClientProperty("UIKey");
		        try {
		            UIManager.setLookAndFeel(info.getClassName());
		            SwingUtilities.updateComponentTreeUI(frame);
			}
			catch (Exception e) {
		             System.err.println("unable to set UI " +
						e.getMessage());
			}
	            }
	        }
	    });
	    lafGroup.add(rb);
	}
	return menuBar;
    }





    /**
     * The renderer used for String in the TreeTable. The only thing it does,
     * is to format a null String as '---'.
     */
    static class ComponentsStringRenderer extends DefaultTableCellRenderer {
	public ComponentsStringRenderer() { super(); }

	public void setValue(Object value) { 
	    setText((value == null) ? "---" : value.toString());
	}
    }


    public static void main(String[] args) {
	if (args.length > 0) {
	    // User is specifying the Component file to show.
	    for (int counter = args.length - 1; counter >= 0; counter--) {
		new ComponentTree(args[counter]);
	    }
	}
	else {
	    // No file specified, see if the user has one in their home
	    // directory.
	    String path;

	    try {
		path = null;
	    }
	    catch (Throwable th) {
		path = null;
	    }
	    if (path == null) {
		// None available, use a default.
		path = "test/Example.wrl";
	    }
        new ComponentTree(path);
	}
    }
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ComponentTree.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }


    public boolean saveToFile(String namefile)
    {
        try
        {
            File file = new File (namefile);
            FileWriter out = new FileWriter(file);
            String text = textArea.getText();
            out.write(text);
            out.close();
            return true;
        }
        catch (IOException e)
        {
            System.out.println("Error saving file.");
        }
        return false;
    }
}

