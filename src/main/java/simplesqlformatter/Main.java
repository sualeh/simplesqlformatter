/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package simplesqlformatter;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import org.apache.commons.io.IOUtils;

import sf.util.CommandLineParser;
import simplesqlformatter.formatter.SQLFormatter;
import simplesqlformatter.formatter.SQLFormatterEditor;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.LightGray;

/**
 * Command line interface to the Simple SQL formatter.
 * 
 * @author Sualeh Fatehi
 */
public final class Main
{

  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  /**
   * Internal storage for information. Read from text file.
   */
  private static String info;

  static
  {
    // load about information
    try
    {
      final StringWriter writer = new StringWriter();
      IOUtils.copy(Main.class.getResourceAsStream("/sqlformatter-readme.txt"),
                   writer);
      info = writer.toString();
    }
    catch (final IOException e)
    {
      LOGGER.log(Level.FINE, e.getMessage(), e);
    }

  }

  /**
   * Reads stdin for a SQL statement, formats it, and prints it to
   * stdout.
   * 
   * @param args
   *        Ignored
   * @throws IOException
   *         In case of i/o error
   */
  public static void main(final String[] args)
    throws IOException
  {

    // parse command line
    final CommandLineParser parser = new CommandLineParser();
    parser.addOption(new CommandLineParser.BooleanOption('h', "?"));
    parser.addOption(new CommandLineParser.BooleanOption('c', "console"));
    parser.addOption(new CommandLineParser.BooleanOption('d', "debug"));
    parser.parse(args);

    final boolean help = parser.getOption("h").isFound();
    if (help)
    {
      printUsage();
      return;
    }

    final boolean debug = parser.getOption("d").isFound();
    final boolean console = parser.getOption("c").isFound();

    if (!console)
    {
      doWindow(debug);
    }
    else
    {
      doConsole();
    }

  }

  private static void doConsole()
    throws IOException
  {
    final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    final Writer out = new OutputStreamWriter(System.out);

    final StringBuffer input = new StringBuffer();
    String line;

    // read input
    while ((line = in.readLine()) != null)
    {
      input.append(line + "\n");
    }
    in.close();

    // format, and write output
    out.write(new SQLFormatter().format(new String(input)));
    out.flush();
    out.close();
  }

  private static void doWindow(final boolean debug)
  {
    try
    {
      PlasticLookAndFeel.setPlasticTheme(new LightGray());
      UIManager.setLookAndFeel(new PlasticLookAndFeel());
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING, "Cannot set look and feel");
    }

    new SQLFormatterEditor(debug).setVisible(true);
  }

  private static void printUsage()
  {
    System.out.println(Version.about());
    System.out.println(info);
  }

  private Main()
  {

  }

}
