/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package simplesqlformatter.formatter;


/**
 * Represents a literal token - that is, a quoted string or a comment.
 * 
 * @author Sualeh Fatehi
 */
final class LiteralToken
  extends Token
{

  LiteralToken(final String token, final LiteralDelimiter delimiter)
  {
    this.token = token;
    if (delimiter == LiteralDelimiter.CSTYLECOMMENT)
    {
      afterToken = AfterToken.NEWLINE;
    }
    else if (delimiter == LiteralDelimiter.DOUBLEQUOTEDSTRING ||
             delimiter == LiteralDelimiter.SINGLEQUOTEDSTRING)
    {
      afterToken = AfterToken.SPACE;
    }
  }

}
