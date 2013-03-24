/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package simplesqlformatter.formatter;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import sf.util.ui.Actions;
import sf.util.ui.ExitAction;
import sf.util.ui.ExtensionFileFilter;
import sf.util.ui.GuiAction;
import simplesqlformatter.Version;

/**
 * Provides an editor and debugger for formatting SQL statements.
 * 
 * @author Sualeh Fatehi
 */
public final class SQLFormatterEditor
  extends JFrame
{

  private static final int MAX_JAVA_STRING_LINE_LENGTH = 100;
  private static final String KEY_SQLFILE = "simplesqlformatter.sqlfile";

  private final static long serialVersionUID = 3760840181833283637L;

  private static final Logger LOGGER = Logger
    .getLogger(SQLFormatterEditor.class.getName());

  private final static int ROWS = 30;
  private final static int COLUMNS = 60;
  private final static double SPLITPANEWEIGHT = 0.8;

  private final Container panel;
  private final JTextArea textArea = new JTextArea(ROWS, COLUMNS);
  private final JTextArea debugArea = new JTextArea(ROWS, COLUMNS / 2);
  private final JLabel statusBar = new JLabel("Ready");

  private static final Preferences preferences = Preferences
    .userNodeForPackage(SQLFormatterEditor.class);

  public static String newline = System.getProperty("line.separator");

  /**
   * Creates a new instance of a Simple SQL Formatter editor main
   * window.
   * 
   * @param debug
   *        Whether to show the debug window
   */
  public SQLFormatterEditor(final boolean debug)
  {

    setTitle("Simple SQL Formatter");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setIconImage(new ImageIcon(SQLFormatterEditor.class.getResource("/sf.png")) //$NON-NLS-1$
      .getImage());

    panel = getContentPane();
    panel.setLayout(new BorderLayout());

    // Create menus and toolbars
    final JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    final JToolBar toolBar = new JToolBar();
    toolBar.setRollover(true);
    add(toolBar, BorderLayout.NORTH);

    createFileMenu(menuBar, toolBar);
    createEditMenu(menuBar, toolBar);
    createHelpMenu(menuBar, toolBar);

    final Font font = new Font("Monospaced", Font.PLAIN, 12);

    // create text area
    textArea.setFont(font);

    // create debug area
    debugArea.setFont(font);
    debugArea.setEditable(false);
    final JLabel debugTitle = new JLabel("Simple SQL Formatter Debug");
    debugTitle.setBorder(BorderFactory.createEtchedBorder());
    final JPanel debugPanel = new JPanel(new BorderLayout());
    debugPanel.add(debugTitle, BorderLayout.NORTH);
    debugPanel.add(new JScrollPane(debugArea), BorderLayout.CENTER);

    if (debug)
    {
      // create split pane
      final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                  new JScrollPane(textArea),
                                                  debugPanel);
      splitPane.setOneTouchExpandable(true);
      splitPane.setResizeWeight(SPLITPANEWEIGHT);

      panel.add(splitPane, BorderLayout.CENTER);
    }
    else
    {
      panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    statusBar.setBorder(BorderFactory.createEtchedBorder());
    panel.add(statusBar, BorderLayout.SOUTH);

    pack();

  }

  private void clear()
  {
    textArea.setText("");
    debugArea.setText("");
    statusBar.setText("Cleared edit buffer");
  }

  private void copy()
  {
    copyToClipboard(textArea.getText());
    statusBar.setText("Copied the SQL stament to the clipboard");
  }

  private void copyAsJavaString()
  {
    final StringBuilder builder = new StringBuilder();

    final List<String> lines;
    try
    {
      lines = IOUtils.readLines(new StringReader(textArea.getText()));
    }
    catch (IOException e)
    {
      LOGGER.log(Level.FINE, e.getMessage(), e);
      return;
    }

    builder.append("\"")
      .append(StringUtils.repeat(" ", MAX_JAVA_STRING_LINE_LENGTH))
      .append("\"");
    for (String javaLine: lines)
    {
      javaLine = StringEscapeUtils.escapeJava(javaLine);
      javaLine = StringUtils.rightPad(javaLine, 100);
      builder.append("+ \" ").append(javaLine).append("\"");

      builder.append(newline);
    }

    copyToClipboard(builder.toString());
    statusBar.setText("Copied the SQL stament to the clipboard as Java string");
  }

  private void copyToClipboard(final String copiedSQL)
  {
    final StringSelection sql = new StringSelection(copiedSQL);
    final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
    clip.setContents(sql, sql);
  }

  private void createEditMenu(JMenuBar menuBar, JToolBar toolBar)
  {
    final JMenu menuEdit = new JMenu("Edit");

    final GuiAction copy = new GuiAction("Copy All", "/icons/edit_copy.gif");
    copy.setShortcutKey(KeyStroke.getKeyStroke("control C"));
    copy.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent actionevent)
      {
        copy();
      }
    });
    menuEdit.add(copy);

    final GuiAction paste = new GuiAction("Paste Over", "/icons/edit_paste.gif");
    paste.setShortcutKey(KeyStroke.getKeyStroke("control shift V"));
    paste.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent actionevent)
      {
        clear();
        paste();
      }
    });
    menuEdit.add(paste);

    final GuiAction copyAsJavaString = new GuiAction("Copy as Java String",
                                                     "/icons/edit_copy.gif");
    copyAsJavaString.setShortcutKey(KeyStroke.getKeyStroke("control J"));
    copyAsJavaString.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent actionevent)
      {
        copyAsJavaString();
      }
    });
    menuEdit.add(copyAsJavaString);

    menuEdit.addSeparator();

    final GuiAction format = new GuiAction("Format", "/icons/edit_format.gif");
    format.setShortcutKey(KeyStroke.getKeyStroke("control F"));
    format.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent actionevent)
      {
        format();
      }
    });
    menuEdit.add(format);
    toolBar.add(format);

    final GuiAction formatFromClipboard = new GuiAction("Format From Clipboard",
                                                        "/icons/edit_format_from_clipboard.gif");
    formatFromClipboard.setShortcutKey(KeyStroke
      .getKeyStroke("control shift F"));
    formatFromClipboard.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent actionevent)
      {
        formatFromClipboard();
      }
    });
    menuEdit.add(formatFromClipboard);
    toolBar.add(formatFromClipboard);

    menuBar.add(menuEdit);
  }

  private void createFileMenu(JMenuBar menuBar, JToolBar toolBar)
  {
    final JMenu menuFile = new JMenu("File");
    final GuiAction newFile = new GuiAction("New", "/icons/file_new.gif");
    newFile.setShortcutKey(KeyStroke.getKeyStroke("control N"));
    newFile.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent actionevent)
      {
        clear();
      }
    });
    menuFile.add(newFile);
    toolBar.add(newFile);

    final GuiAction openFile = new GuiAction("Open", "/icons/file_open.gif");
    openFile.setShortcutKey(KeyStroke.getKeyStroke("control O"));
    openFile.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent actionevent)
      {
        openFile();
      }
    });
    menuFile.add(openFile);
    toolBar.add(openFile);

    final GuiAction saveFile = new GuiAction("Save", "/icons/file_save.gif");
    saveFile.setShortcutKey(KeyStroke.getKeyStroke("control S"));
    saveFile.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent actionevent)
      {
        saveFile();
      }
    });
    menuFile.add(saveFile);
    toolBar.add(saveFile);

    menuFile.addSeparator();
    toolBar.addSeparator();

    final ExitAction exit = new ExitAction(this, "Exit");
    exit.setShortcutKey(KeyStroke.getKeyStroke("control Q"));
    menuFile.add(exit);

    menuBar.add(menuFile);
  }

  private void createHelpMenu(JMenuBar menuBar, JToolBar toolBar)
  {
    final JMenu menuHelp = new JMenu("Help");
    final GuiAction about = new GuiAction("About", "/icons/help_about.gif");
    about.setShortcutKey(KeyStroke.getKeyStroke("control H"));
    about.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent actionevent)
      {
        JOptionPane.showMessageDialog(SQLFormatterEditor.this,
                                      Version.about(),
                                      Version.getProductName(),
                                      JOptionPane.PLAIN_MESSAGE);
      }
    });
    menuHelp.add(about);

    menuBar.add(menuHelp);
  }

  /**
   * Debug at the second stage of processing - SQL is tokenized.
   * 
   * @param sql
   *        SQL statement
   * @return Debug message
   */
  private String debugGetTokens(final String sql)
  {

    final Token[] tokens = new SQLFormatter().parse(sql);

    final StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < tokens.length; i++)
    {
      final Token token = tokens[i];
      buffer.append("[").append(i).append("] ").append(token).append("\n");
    }

    return buffer.toString();

  }

  private void format()
  {
    final String originalSQL = textArea.getText();
    final String formattedSQL = new SQLFormatter().format(originalSQL);
    textArea.setText(formattedSQL);
    debugArea.setText(debugGetTokens(originalSQL));
    statusBar.setText("Formatted SQL statement");
  }

  private void formatFromClipboard()
  {
    paste();
    format();
    copy();
  }

  private void openFile()
  {

    final List<FileFilter> fileFilters = new ArrayList<FileFilter>();
    fileFilters.add(new ExtensionFileFilter("Text files", ".txt"));
    fileFilters.add(new ExtensionFileFilter("SQL files", ".sql"));

    final File selectedFile = Actions.showOpenDialog(this,
                                                     "Open SQL File",
                                                     fileFilters,
                                                     new File(preferences
                                                       .get(KEY_SQLFILE,
                                                            "./untitled.sql")),
                                                     "Could not read file.");
    if (selectedFile != null)
    {
      try
      {
        textArea.setText(FileUtils.readFileToString(selectedFile));
        debugArea.setText("");
        statusBar.setText("Opened file " + selectedFile);
        // remember selection
        preferences.put(KEY_SQLFILE, selectedFile.getAbsolutePath());
      }
      catch (final IOException e)
      {
        LOGGER.log(Level.FINE, e.getMessage(), e);
      }
    }
  }

  private void paste()
  {
    final Clipboard clipboard = Toolkit.getDefaultToolkit()
      .getSystemClipboard();
    final Transferable transferable = clipboard.getContents(null);
    try
    {
      textArea.setText((String) transferable
        .getTransferData(DataFlavor.stringFlavor));
    }
    catch (final IOException e)
    {
      LOGGER.log(Level.FINE, e.getMessage(), e);
    }
    catch (final UnsupportedFlavorException e)
    {
      LOGGER.log(Level.FINE, e.getMessage(), e);
    }
    statusBar.setText("Pasted new SQL statement over the old");
  }

  private void saveFile()
  {

    final List<FileFilter> fileFilters = new ArrayList<FileFilter>();
    fileFilters.add(new ExtensionFileFilter("Text files", ".txt"));
    fileFilters.add(new ExtensionFileFilter("SQL files", ".sql"));

    final File selectedFile = Actions.showSaveDialog(this,
                                                     "Save SQL File",
                                                     fileFilters,
                                                     new File(preferences
                                                       .get(KEY_SQLFILE,
                                                            "./untitled.sql")),
                                                     "Could not read file.");
    if (selectedFile != null)
    {
      try
      {
        final String formattedSQL = textArea.getText();
        final PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(selectedFile)));
        out.print(formattedSQL);
        out.flush();
        out.close();
      }
      catch (final IOException e)
      {
        LOGGER.log(Level.FINE, e.getMessage(), e);
      }
    }

    statusBar.setText("Saved file " + selectedFile);

  }

}
