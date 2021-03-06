/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package sf.util;


import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Command-line options parser.
 * 
 * @author Steve Purcell, Sualeh Fatehi
 */
public final class CommandLineParser
{
  /**
   * Representation of a command-line option.
   * 
   * @author Sualeh Fatehi
   * @param <T>
   *        Option type
   */
  public abstract static class BaseOption<T>
    implements Option<T>
  {

    protected String shortForm;
    protected String longForm;
    protected boolean hasShortForm;
    protected boolean hasLongForm;
    protected T value;
    protected T defaultValue;

    protected BaseOption(final char shortForm,
                         final String longForm,
                         final T defaultValue)
    {
      setShortForm(shortForm);
      setLongForm(longForm);
      if (!hasLongForm && !hasShortForm)
      {
        throw new IllegalArgumentException("Option cannot be defined");
      }
      this.defaultValue = defaultValue;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Option#getDefaultValue()
     */
    public T getDefaultValue()
    {
      return defaultValue;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Option#getLongForm()
     */
    public String getLongForm()
    {
      return longForm;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Option#getShortForm()
     */
    public String getShortForm()
    {
      return shortForm;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Option#getValue()
     */
    public T getValue()
    {
      T returnValue;
      if (!isFound())
      {
        returnValue = defaultValue;
      }
      else
      {
        returnValue = value;
      }
      return returnValue;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Option#hasLongForm()
     */
    public boolean hasLongForm()
    {
      return hasLongForm;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Option#hasShortForm()
     */
    public boolean hasShortForm()
    {
      return hasShortForm;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Option#isFound()
     */
    public boolean isFound()
    {
      return value != null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
      final String optionString = (hasLongForm? longForm: "") +
                                  (hasShortForm? " (" + shortForm + ")": "") +
                                  "=" + value + " (" + defaultValue + ")";
      return optionString;
    }

    /**
     * Override to extract and convert an option value passed on the
     * command-line.
     * 
     * @param valueString
     * @return Parsed value
     */
    protected abstract T parseValue(final String valueString);

    void reset()
    {
      value = null;
    }

    void setValue(final String valueString)
    {
      value = parseValue(valueString);
    }

    private void setLongForm(final String longForm)
    {
      if (longForm == NO_LONG_FORM)
      {
        return;
      }
      if (longForm.length() == 0)
      {
        throw new IllegalArgumentException("Long form for option not specified");
      }
      this.longForm = longForm;
      hasLongForm = true;
    }

    private void setShortForm(final char shortForm)
    {
      if (shortForm == NO_SHORT_FORM)
      {
        return;
      }
      if (!Character.isLetterOrDigit(shortForm))
      {
        throw new IllegalArgumentException("Illegal short form for option specified: " +
                                           shortForm);
      }
      this.shortForm = new String(new char[] {
        shortForm
      });
      hasShortForm = true;
    }
  }

  /**
   * An option that expects a boolean value.
   */
  public static final class BooleanOption
    extends BaseOption<Boolean>
  {

    /**
     * Constructor that takes the short form and long form of the
     * switch.
     * 
     * @param shortForm
     *        Short form of the switch
     * @param longForm
     *        Long form of the switch
     */
    public BooleanOption(final char shortForm, final String longForm)
    {
      super(shortForm, longForm, Boolean.FALSE);
      value = Boolean.FALSE;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Option#isFound()
     */
    @Override
    public boolean isFound()
    {
      if (value != null)
      {
        return value.booleanValue();
      }
      else
      {
        return Boolean.FALSE;
      }
    }

    @Override
    protected Boolean parseValue(final String valueString)
    {
      if (valueString == null || valueString.length() == 0)
      {
        return Boolean.FALSE;
      }
      return Boolean.valueOf(valueString);
    }

  }

  /**
   * An option that expects a floating-point value.
   * 
   * @param <N>
   *        Option type
   */
  public static final class NumberOption<N extends Number>
    extends BaseOption<N>
  {

    /**
     * Constructor that takes the short form and long form of the
     * switch.
     * 
     * @param shortForm
     *        Short form of the switch
     * @param longForm
     *        Long form of the switch
     * @param defaultValue
     *        Default option value.
     */
    public NumberOption(final char shortForm,
                        final String longForm,
                        final N defaultValue)
    {
      super(shortForm, longForm, defaultValue);
    }

    @Override
    protected N parseValue(final String arg)
    {
      try
      {
        return (N) NumberFormat.getNumberInstance().parse(arg);
      }
      catch (final ParseException e)
      {
        return null;
      }
    }
  }

  /**
   * Representation of a command-line option.
   * 
   * @author Sualeh Fatehi
   * @param <T>
   *        Option type
   */
  public static interface Option<T>
  {
    /** Special value when no short form is defined */
    final char NO_SHORT_FORM = (char) 0;
    /** Special value when no long form is defined */
    final String NO_LONG_FORM = null;

    /**
     * Gets the default value for the option.
     * 
     * @return Default value.
     */
    T getDefaultValue();

    /**
     * Gets the long form of the switch for the option.
     * 
     * @return Long form of the switch
     */
    String getLongForm();

    /**
     * Gets the short form of the switch for the option.
     * 
     * @return Short form of the switch
     */
    String getShortForm();

    /**
     * Gets the value for the option.
     * 
     * @return Option value
     */
    T getValue();

    /**
     * Whether the option has the long form of the switch.
     * 
     * @return Whether the option has the long form of the switch
     */
    boolean hasLongForm();

    /**
     * Whether the option has the short form of the switch.
     * 
     * @return Whether the option has the short form of the switch
     */
    boolean hasShortForm();

    /**
     * Whether the option was found.
     * 
     * @return Whether the option was found
     */
    boolean isFound();

  }

  /**
   * An option that expects a string value.
   */
  public static final class StringOption
    extends BaseOption<String>
  {

    /**
     * Constructor that takes the short form and long form of the
     * switch.
     * 
     * @param shortForm
     *        Short form of the switch
     * @param longForm
     *        Long form of the switch
     * @param defaultValue
     *        Default option value.
     */
    public StringOption(final char shortForm,
                        final String longForm,
                        final String defaultValue)
    {
      super(shortForm, longForm, defaultValue);
    }

    @Override
    protected String parseValue(final String arg)
    {
      return arg;
    }
  }

  private static final String DASH = "-";

  private String[] remainingArgs;

  private final Map<String, Option<?>> optionsMap = new HashMap<String, Option<?>>();

  /**
   * Add the specified Option to the list of accepted options.
   * 
   * @param option
   *        Option to add
   */
  public void addOption(final Option<?> option)
  {
    if (option.hasShortForm())
    {
      optionsMap.put(DASH + option.getShortForm(), option);
    }
    if (option.hasLongForm())
    {
      optionsMap.put(DASH + option.getLongForm(), option);
    }
  }

  /**
   * Get an option value by name.
   * 
   * @param optionName
   *        Name of the option
   * @return Option
   */
  public boolean getBooleanOptionValue(final String optionName)
  {
    boolean optionValue = false;
    final Option<?> option = getOption(optionName);
    if (option == null)
    {
      optionValue = false;
    }
    else
    {
      if (option instanceof BooleanOption)
      {
        optionValue = ((BooleanOption) option).getValue();
      }
    }
    return optionValue;
  }

  /**
   * Get an option by name.
   * 
   * @param optionName
   *        Name of the option
   * @return Option
   */
  public Option<?> getOption(final String optionName)
  {
    return optionsMap.get(DASH + optionName);
  }

  /**
   * Get all the command line options.
   * 
   * @return Command line options
   */
  public Option<?>[] getOptions()
  {
    final Collection<Option<?>> options = optionsMap.values();
    final Collection<Option<?>> uniqueOptions = new HashSet<Option<?>>();

    for (final Option<?> option: options)
    {
      if (!uniqueOptions.contains(option))
      {
        uniqueOptions.add(option);
      }
    }
    return uniqueOptions.toArray(new Option[0]);
  }

  /**
   * Remaining arguments, that are not parsed.
   * 
   * @return The non-option arguments
   */
  public String[] getRemainingArgs()
  {
    final String[] remainingArgsCopy = new String[remainingArgs.length];
    System.arraycopy(remainingArgs,
                     0,
                     remainingArgsCopy,
                     0,
                     remainingArgs.length);
    return remainingArgsCopy;
  }

  /**
   * Get an option value by name.
   * 
   * @param optionName
   *        Name of the option
   * @return Option
   */
  public String getStringOptionValue(final String optionName)
  {
    final Option<?> option = getOption(optionName);
    if (option == null || option.getValue() == null)
    {
      return null;
    }
    return option.getValue().toString();
  }

  /**
   * Extract the options and non-option arguments from the given list of
   * command-line arguments. The default locale is used for parsing
   * options whose values might be locale-specific.
   * 
   * @param args
   *        Command line arguments
   */
  public void parse(final String[] args)
  {
    // Reset all options
    final Option<?>[] options = getOptions();
    for (final Option<?> element: options)
    {
      ((BaseOption<?>) element).reset();
    }

    final List<String> otherArgs = new ArrayList<String>();
    int position = 0;
    while (position < args.length)
    {
      String currentArg = args[position];
      String valueArg = null;

      // handle -arg=value
      final int equalsPos = currentArg.indexOf("=");

      if (equalsPos != -1)
      {
        valueArg = currentArg.substring(equalsPos + 1);
        currentArg = currentArg.substring(0, equalsPos);
      }

      final BaseOption<?> option = (BaseOption<?>) optionsMap.get(currentArg);
      if (option == null)
      {
        otherArgs.add(currentArg);
        position++;
        continue;
      }

      // // Check if a value is needed
      // final boolean wantsValue = !(option instanceof BooleanOption);
      // if (wantsValue)
      // {
      // if (valueArg == null)
      // {
      // // The next argument is the value argument
      // position++;
      // if (position < args.length)
      // {
      // valueArg = args[position];
      // }
      // }
      // }
      // else
      // {
      // valueArg = Boolean.TRUE.toString();
      // }
      // Check if a value is needed

      if (valueArg == null)
      {
        // The next argument is the value argument
        position++;
        if (position < args.length)
        {
          valueArg = args[position];
          // If this is not an argument, backtrack
          if (valueArg.startsWith(DASH))
          {
            position--;
            valueArg = null;
          }
        }
      }
      // Booleans are true, even if they have no value
      if (valueArg == null && option instanceof BooleanOption)
      {
        valueArg = Boolean.TRUE.toString();
      }

      option.setValue(valueArg);
      position++;
    }

    remainingArgs = otherArgs.toArray(new String[otherArgs.size()]);
  }

}
