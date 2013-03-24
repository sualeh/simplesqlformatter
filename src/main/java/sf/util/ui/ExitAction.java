/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package sf.util.ui;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

/**
 * Exits an application.
 * 
 * @author sfatehi
 */
public final class ExitAction
  extends GuiAction
{

  private static final long serialVersionUID = 5749903957626188378L;

  /**
   * Exits an application
   * 
   * @param frame
   *        Main window
   * @param text
   *        Text for the action
   */
  public ExitAction(final JFrame frame, String text)
  {
    super(text, "/icons/exit.gif");
    addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent actionevent)
      {
        frame.dispose();
        System.exit(0);
      }
    });
  }

}
