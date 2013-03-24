package simplesqlformatter.test;


import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import simplesqlformatter.formatter.SQLFormatter;

public class TestSQLFormatter
  extends TestCase
{

  private final static int MAXFILES = 8;

  private static String[] sqlOriginal = new String[MAXFILES];
  private static String[] sqlFormatted = new String[MAXFILES];

  /**
   * System specific file separator character.
   */
  public final static char FILE_SEPARATOR = System
    .getProperty("file.separator").charAt(0);
  public final static String NEWLINE = System.getProperty("line.separator");
  public final static int MAX_LINE_LENGTH = 70;

  public static String compare(String text1, String text2)
  {

    char[] characters1 = new char[text1.length()];
    char[] characters2 = new char[text2.length()];

    StringBuffer compare = new StringBuffer();

    text1.getChars(0, text1.length(), characters1, 0);
    text2.getChars(0, text2.length(), characters2, 0);
    for (int i = 0; i < characters1.length; i++)
    {
      char character1 = 0, character2 = 0;
      try
      {
        character1 = characters1[i];
        character2 = characters2[i];
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
        compare.append(character1);
        continue;
      }
      if (character1 == character2)
      {
        compare.append(character1);
      }
      else
      {
        compare.append("~" + character1 + "=" + character2 + "~");
      }
    }

    return compare.toString();

  }

  public static String stripNewlines(String text)
  {

    char[] characters = new char[text.length()];
    StringBuffer strippedText = new StringBuffer();

    text.getChars(0, text.length(), characters, 0);
    for (int i = 0; i < characters.length; i++)
    {
      char character = characters[i];
      if (character != '\r')
      {
        strippedText.append(character);
      }
    }

    return strippedText.toString();

  }

  public static String stripWhitespace(String text)
  {

    char[] characters = new char[text.length()];
    StringBuffer strippedText = new StringBuffer();

    text.getChars(0, text.length(), characters, 0);
    for (int i = 0; i < characters.length; i++)
    {
      char character = characters[i];
      if (!Character.isWhitespace(character))
      {
        strippedText.append(character);
      }
    }

    return strippedText.toString();

  }

  private static String readFully(InputStream stream)
    throws IOException
  {
    final StringWriter writer = new StringWriter();
    IOUtils.copy(stream, writer);
    return writer.toString();
  }

  public TestSQLFormatter(String name)
  {
    super(name);
  }

  public void setUp()
  {

    // read files
    for (int i = 0; i < MAXFILES; i++)
    {
      try
      {
        InputStream stream;
        final String sqlOriginalFile = "/original/" + (i + 1) + ".sql";
        final String sqlFormattedFile = "/formatted/~" + (i + 1) + ".sql";

        stream = this.getClass().getResourceAsStream(sqlOriginalFile);
        sqlOriginal[i] = readFully(stream);

        stream = this.getClass().getResourceAsStream(sqlFormattedFile);
        sqlFormatted[i] = readFully(stream);
      }
      catch (IOException e)
      {
        fail();
      }
    }

  }

  public void testDoubleFormatting()
  {

    for (int i = 0; i < sqlOriginal.length; i++)
    {
      String sqlStatement = sqlOriginal[i];
      String formattedSQL = sqlStatement;

      formattedSQL = new SQLFormatter().format(formattedSQL);
      formattedSQL = new SQLFormatter().format(formattedSQL);

      if (!stripWhitespace(sqlStatement)
        .equalsIgnoreCase(stripWhitespace(formattedSQL)))
      {
        System.out
          .println(compare(stripWhitespace(sqlStatement).toLowerCase(),
                           stripWhitespace(formattedSQL).toLowerCase()));
      }

      assertEquals(stripWhitespace(sqlStatement).toLowerCase(),
                   stripWhitespace(formattedSQL).toLowerCase());
    }

  }

  public void testFormat()
  {

    for (int i = 0; i < sqlOriginal.length; i++)
    {
      String formattedSQL = new SQLFormatter().format(sqlOriginal[i]);
      assertEquals("Error formatting - " + (i + 1) + ".sql",
                   stripNewlines(sqlFormatted[i].trim()),
                   stripNewlines(formattedSQL.trim()));
    }

  }

  public void testFormatting()
  {

    for (int i = 0; i < sqlOriginal.length; i++)
    {
      String sqlStatement = sqlOriginal[i];
      String formattedSQL = new SQLFormatter().format(sqlStatement);

      if (!stripWhitespace(sqlStatement)
        .equalsIgnoreCase(stripWhitespace(formattedSQL)))
      {
        System.out
          .println(compare(stripWhitespace(sqlStatement).toLowerCase(),
                           stripWhitespace(formattedSQL).toLowerCase()));
      }

      assertEquals(stripWhitespace(sqlStatement).toLowerCase(),
                   stripWhitespace(formattedSQL).toLowerCase());
    }

  }

}
