/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package simplesqlformatter.formatter;


/**
 * Represents a SQL token.
 * 
 * @author Sualeh Fatehi
 */
final class SQLToken
  extends Token
{

  SQLToken(final String token)
  {
    this.token = token;

    if (isKeyword())
    {
      this.token = this.token.toUpperCase();
    }

  }

  boolean isCloseParenthesis()
  {
    return token.equals(")");
  }

  boolean isConditional()
  {
    return isIn("|AND|OR|XOR|NOT|BETWEEN|");
  }

  boolean isDelete()
  {
    return isIn("|DELETE|");
  }

  boolean isFunction()
  {
    return isIn("|COUNT|SUM|AVG|MIN|MAX|COALESCE|");
  }

  boolean isInsert()
  {
    return isIn("|INSERT|INSERT INTO");
  }

  boolean isKeyword()
  {
    return isSignificantKeyword() || isMinorKeyword() ||
           isIn("|ANY|LIKE|IN|EXISTS|IS|NULL|") || isConditional() ||
           isFunction();
  }

  boolean isMinorKeyword()
  {
    return isIn("|AS|INNER JOIN|OUTER JOIN|JOIN|ON|");
  }

  boolean isOpenParenthesis()
  {
    return token.equals("(");
  }

  boolean isSelect()
  {
    return isIn("|SELECT|SELECT DISTINCT|");
  }

  boolean isSeparator()
  {
    return token.equals(",");
  }

  boolean isSignificantKeyword()
  {
    return isSelect() ||
           isInsert() ||
           isUpdate() ||
           isDelete() ||
           isIn("|FROM|WHERE|ORDER BY|GROUP BY|HAVING|"
                + "|SET|INTO|VALUES|UNION|ALL|MINUS|");
  }

  boolean isUpdate()
  {
    return isIn("|UPDATE|");
  }

  /**
   * Checks whether a token is in a pipe-delimited word list.
   * 
   * @param wordsList
   *        Delimited list of words
   * @return Whether the token is in the word list
   */
  private boolean isIn(final String wordsList)
  {
    return wordsList.indexOf("|" + token.toUpperCase() + "|") > -1;
  }

}
