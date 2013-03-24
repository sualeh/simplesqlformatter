/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package simplesqlformatter.formatter;


/**
 * Set of delimiters for literal expressions in SQL.
 * 
 * @author Sualeh Fatehi
 */
final class LiteralDelimiter
{

  final static LiteralDelimiter DOUBLEQUOTEDSTRING = new LiteralDelimiter("\"",
                                                                          "\"");
  final static LiteralDelimiter SINGLEQUOTEDSTRING = new LiteralDelimiter("'",
                                                                          "'");
  final static LiteralDelimiter CSTYLECOMMENT = new LiteralDelimiter("/*", "*/");
  final static LiteralDelimiter SQLCOMMENT = new LiteralDelimiter("--", "\n");

  final static LiteralDelimiter[] ALLDELIMITERS = {
      DOUBLEQUOTEDSTRING, SINGLEQUOTEDSTRING, SQLCOMMENT, CSTYLECOMMENT,
  };

  private final String start;
  private final String end;

  private LiteralDelimiter(final String start, final String end)
  {
    this.start = start;
    this.end = end;
  }

  String getEnd()
  {
    return end;
  }

  String getStart()
  {
    return start;
  }

}
