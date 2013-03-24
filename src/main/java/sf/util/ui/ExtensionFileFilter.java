/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package sf.util.ui;


import java.io.File;
import java.util.Locale;

/**
 * Filters files by extension.
 * 
 * @author sfatehi
 */
public class ExtensionFileFilter
  extends javax.swing.filechooser.FileFilter
  implements java.io.FileFilter
{

  /**
   * Gets the extension for the given file.
   * 
   * @param file
   *        File
   * @return Extension
   */
  public static String getExtension(final File file)
  {
    String extension = "";
    final String fileName = file.getName();
    final int i = fileName.lastIndexOf('.');
    if (i > 0 && i < fileName.length() - 1)
    {
      extension = fileName.substring(i + 1).toLowerCase(Locale.ENGLISH);
    }
    return "." + extension;
  }

  /** A description for the file type. */
  private final String description;

  /** File extensions that are accepted. */
  private final String extension;

  /**
   * Constructor.
   * 
   * @param description
   *        A description of the file type;
   * @param extension
   *        The file extension;
   */
  public ExtensionFileFilter(final String description, final String extension)
  {
    this.description = description;

    String fileExtension = extension;
    if (fileExtension == null)
    {
      fileExtension = "";
    }
    if (!fileExtension.startsWith("."))
    {
      fileExtension = "." + fileExtension;
    }
    this.extension = fileExtension;

  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(final File file)
  {
    boolean accept = false;
    if (file.isDirectory())
    {
      accept = true;
    }
    else if (file.isHidden())
    {
      accept = false;
    }
    else
    {
      final String extension = getExtension(file);
      accept = extension != null && extension.equals(this.extension);
    }
    return accept;
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.swing.filechooser.FileFilter#getDescription()
   */
  @Override
  public String getDescription()
  {
    return description;
  }

  /**
   * Gets the file extension.
   * 
   * @return File extension.
   */
  public String getExtension()
  {
    return extension;
  }

}
