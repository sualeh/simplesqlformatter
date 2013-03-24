/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package simplesqlformatter.formatter;


import org.apache.commons.lang3.StringUtils;

/**
 * Represents a token, either a SQL token, a string literal, or a
 * comment.
 * 
 * @author Sualeh Fatehi
 */
public abstract class Token
{

  /**
   * Set of values that can follow a token in formatted SQL.
   */
  protected final static class AfterToken
  {

    final static AfterToken NOTHING = new AfterToken("NOTHING", "");
    final static AfterToken SPACE = new AfterToken("SPACE", " ");
    final static AfterToken NEWLINE = new AfterToken("NEWLINE", "\n");

    private final String name;
    private final String value;

    private AfterToken(final String name, final String value)
    {
      this.name = name;
      this.value = value;
    }

    /**
     * Returns a string representation.
     * 
     * @return A string representation
     */
    @Override
    public String toString()
    {
      return value;
    }

    String getName()
    {
      return name;
    }

  }

  final static int INDENT_CONTINUATION = -1;
  /**
   * Token value.
   */
  protected String token = "";

  /**
   * After the token value.
   */
  protected AfterToken afterToken = AfterToken.NOTHING;

  private int indentLevel = INDENT_CONTINUATION;

  /**
   * Builds a string for use in the final formatted SQL statement.
   * 
   * @param indent
   *        The indent to use (tabs or spaces)
   * @return Built string for this token
   */
  public final String build(final String indent)
  {

    final StringBuffer buffer = new StringBuffer();

    buffer.append(StringUtils.repeat(indent, indentLevel)).append(token)
      .append(afterToken);

    return buffer.toString();

  }

  /**
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj
   *        the reference object with which to compare.
   * @return <code>true</code> if this object is the same as the obj
   *         argument; <code>false</code> otherwise.
   * @see java.lang.Boolean#hashCode()
   * @see java.util.Hashtable
   */
  @Override
  public final boolean equals(final Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (!(obj instanceof Token))
    {
      return false;
    }

    final Token token = (SQLToken) obj;

    if (indentLevel != token.indentLevel)
    {
      return false;
    }
    if (!afterToken.equals(token.afterToken))
    {
      return false;
    }
    if (!this.token.equals(token.token))
    {
      return false;
    }

    return true;
  }

  /**
   * Get the token value.
   * 
   * @return Token value
   */
  public final String getToken()
  {
    return token;
  }

  /**
   * Returns a hash code value for the object. This method is supported
   * for the benefit of hashtables such as those provided by
   * <code>java.util.Hashtable</code>.
   * 
   * @return a hash code value for this object.
   * @see java.lang.Object#equals(java.lang.Object)
   * @see java.util.Hashtable
   */
  @Override
  public final int hashCode()
  {
    int result;
    result = indentLevel;
    result = 29 * result + token.hashCode();
    result = 29 * result + afterToken.hashCode();
    return result;
  }

  /**
   * Returns a string representation for debugging.
   * 
   * @return A string representation
   */
  @Override
  public final String toString()
  {

    final StringBuffer buffer = new StringBuffer();

    if (indentLevel == INDENT_CONTINUATION)
    {
      buffer.append("same");
    }
    else
    {
      buffer.append(indentLevel);
    }

    buffer.append(" ").append(" [").append(token).append("] ").append(" ")
      .append(afterToken.getName().toLowerCase());

    final String className = this.getClass().getName();
    buffer
      .append(" <")
      .append(className.substring(className.lastIndexOf('.') + 1).toLowerCase())
      .append("> ");

    return buffer.toString();

  }

  /**
   * Gets what follows the after token.
   * 
   * @return After the token
   */
  final AfterToken getAfterToken()
  {
    return afterToken;
  }

  final void nextIndentLevel()
  {
    indentLevel++;
  }

  final void setAfterToken(final AfterToken afterToken)
  {
    this.afterToken = afterToken;
  }

  final void setIndentLevel(final int indentLevel)
  {
    this.indentLevel = indentLevel;
  }

}
