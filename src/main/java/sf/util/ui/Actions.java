/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package sf.util.ui;


import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * GUI helper methods.
 * 
 * @author sfatehi
 */
public class Actions
{

  /**
   * Shows an open file dialog, and returns the selected file. Checks if
   * the file is readable.
   * 
   * @param parent
   * @param dialogTitle
   *        Dialog title
   * @param fileFilters
   *        File filters
   * @param suggestedFile
   *        Suggested file name
   * @param cannotReadMessage
   *        Message if the file cannot be read
   * @return Selected file to open
   */
  public static File showOpenDialog(final Component parent,
                                    final String dialogTitle,
                                    final List<FileFilter> fileFilters,
                                    final File suggestedFile,
                                    final String cannotReadMessage)
  {
    final JFileChooser fileDialog = new JFileChooser();
    fileDialog.setDialogTitle(dialogTitle);
    fileDialog.setSelectedFile(suggestedFile);
    if (fileFilters != null && fileFilters.size() > 0)
    {
      fileDialog.setAcceptAllFileFilterUsed(false);
      for (final FileFilter fileFilter: fileFilters)
      {
        fileDialog.addChoosableFileFilter(fileFilter);
      }
    }
    fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
    final int dialogReturnValue = fileDialog.showOpenDialog(parent);

    if (dialogReturnValue != JFileChooser.APPROVE_OPTION)
    {
      return null;
    }

    File selectedFile = fileDialog.getSelectedFile();
    selectedFile = addExtension(fileDialog, selectedFile);
    if (selectedFile == null || !selectedFile.exists() ||
        !selectedFile.canRead())
    {
      JOptionPane.showMessageDialog(parent, selectedFile + "\n" +
                                            cannotReadMessage);
      return null;
    }
    return selectedFile;
  }

  /**
   * Shows the save dialog.
   * 
   * @param parent
   *        Main GUI window.
   * @param dialogTitle
   *        Dialog title.
   * @param suggestedFile
   *        Suggested file name.
   * @param fileFilters
   *        File filters for the dialog
   * @param overwriteMessage
   *        Message to confirm overwrite
   * @return Selected file, or null if no file is selected.
   */
  public static File showSaveDialog(final Component parent,
                                    final String dialogTitle,
                                    final List<FileFilter> fileFilters,
                                    final File suggestedFile,
                                    final String overwriteMessage)
  {
    final JFileChooser fileDialog = new JFileChooser();
    fileDialog.setDialogTitle(dialogTitle);
    fileDialog.setSelectedFile(suggestedFile);
    if (fileFilters != null && fileFilters.size() > 0)
    {
      fileDialog.setAcceptAllFileFilterUsed(false);
      for (final FileFilter fileFilter: fileFilters)
      {
        fileDialog.addChoosableFileFilter(fileFilter);
      }
    }
    fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
    final int dialogReturnValue = fileDialog.showSaveDialog(parent);

    if (dialogReturnValue != JFileChooser.APPROVE_OPTION)
    {
      return null;
    }

    File selectedFile = fileDialog.getSelectedFile();
    if (selectedFile != null)
    {
      selectedFile = addExtension(fileDialog, selectedFile);

      if (selectedFile.exists())
      {
        final int confirm = JOptionPane
          .showConfirmDialog(parent,
                             selectedFile + "\n" + overwriteMessage,
                             dialogTitle,
                             JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
        {
          selectedFile = null;
        }
      }
    }
    return selectedFile;
  }

  private static File addExtension(final JFileChooser fileDialog,
                                   File selectedFile)
  {
    // Add extension, if it is not provided
    final FileFilter fileFilter = fileDialog.getFileFilter();
    if (fileFilter instanceof ExtensionFileFilter)
    {
      final ExtensionFileFilter extFileFilter = (ExtensionFileFilter) fileFilter;
      final String selectedExtension = extFileFilter.getExtension();
      if (!ExtensionFileFilter.getExtension(selectedFile)
        .equals(selectedExtension))
      {
        selectedFile = new File(selectedFile.getAbsoluteFile() +
                                selectedExtension);
      }
    }
    return selectedFile;
  }

  private Actions()
  {
    // Prevent instantiation
  }

}
