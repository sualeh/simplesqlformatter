/*
 * Copyright 2004-2013, Sualeh Fatehi <sualeh@hotmail.com>
 * This work is licensed under the Creative Commons Attribution-Noncommercial-No Derivative Works 3.0 License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ 
 * or send a letter to Creative Commons, 543 Howard Street, 5th Floor, San Francisco, California, 94105, USA.
 */
package simplesqlformatter.formatter;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Formats SQL following a series of well-defined steps.
 * 
 * @author Sualeh Fatehi.
 */
public final class SQLFormatter
{

  /**
   * Temporary storage of intermediate parsing results.
   */
  private final static class PartialParseResult
  {

    private final List<Token> tokens;
    private final String text;

    PartialParseResult(final List<Token> tokens, final String text)
    {
      this.tokens = tokens;
      this.text = text;
    }

    String getText()
    {
      return text;
    }

    List<Token> getTokens()
    {
      return tokens;
    }

  }

  private final static String TAB = "\t";
  private final static char PLACEHOLDER_TOKEN = '\001';

  private final static int MAX_INDENTS = 32;

  /**
   * Replace all literals (string literals and comments) with a
   * place-holder. Maintain a list of literals substituted in this way.
   * 
   * @param parseInformation
   *        the text to process
   */
  private static PartialParseResult processLiterals(final PartialParseResult parseInformation)
  {

    String workingText = parseInformation.getText();

    final List<Token> tokens = new ArrayList<Token>();
    for (int i = 0; i < workingText.length(); i++)
    {
      for (final LiteralDelimiter delimiter: LiteralDelimiter.ALLDELIMITERS)
      {
        if (workingText.regionMatches(i, delimiter.getStart(), 0, delimiter
          .getStart().length()))
        {
          final int beforeStart = i;
          final int afterStart = beforeStart + delimiter.getStart().length();
          int afterEnd = workingText.indexOf(delimiter.getEnd(), afterStart);
          if (afterEnd == -1)
          {
            afterEnd = workingText.indexOf("\n", afterStart);
            if (afterEnd == -1)
            {
              afterEnd = workingText.indexOf("\r", afterStart);
              if (afterEnd == -1)
              {
                afterEnd = workingText.length() - 1;
              }
            }
            afterEnd++;
          }
          else
          {
            afterEnd += delimiter.getEnd().length();
          }
          String escapedText = workingText.substring(beforeStart, afterEnd);
          if (delimiter == LiteralDelimiter.SQLCOMMENT)
          {
            // Convert SQL comment to C-style comment.
            escapedText = LiteralDelimiter.CSTYLECOMMENT.getStart() +
                          escapedText.trim().substring(2) +
                          LiteralDelimiter.CSTYLECOMMENT.getEnd();
          }
          tokens.add(new LiteralToken(escapedText, delimiter));

          final String preEscape = workingText.substring(0, beforeStart);
          String postEscape = "";
          if (afterEnd < workingText.length())
          {
            postEscape = workingText.substring(afterEnd);
          }
          String escapePlaceholder = String.valueOf(PLACEHOLDER_TOKEN);
          if (delimiter != LiteralDelimiter.DOUBLEQUOTEDSTRING &&
              delimiter != LiteralDelimiter.SINGLEQUOTEDSTRING)
          {
            if (!preEscape.endsWith(" "))
            {
              escapePlaceholder = " " + escapePlaceholder;
            }
            if (!postEscape.startsWith(" "))
            {
              escapePlaceholder += " ";
            }
          }
          workingText = preEscape + escapePlaceholder + postEscape;
          break;
        }
      }
    }

    return new PartialParseResult(tokens, workingText);

  }

  private static List<Token> splitLiterals(final List<Token> tokens,
                                           final List<Token> literals)
  {

    final List<Token> allTokens = new ArrayList<Token>();

    int literalIndex = 0;

    for (Token token: tokens)
    {
      final String tokenValue = token.getToken();

      if (tokenValue.equals(String.valueOf(PLACEHOLDER_TOKEN)))
      {
        allTokens.add(literals.get(literalIndex));
        literalIndex++;
        continue;
      }

      if (tokenValue.lastIndexOf(PLACEHOLDER_TOKEN) > 0)
      {
        final StringTokenizer tokenizer = new StringTokenizer(tokenValue,
                                                              String
                                                                .valueOf(PLACEHOLDER_TOKEN));
        final int numTokens = tokenizer.countTokens();
        for (int i = 0; i < numTokens; i++)
        {
          final String subToken = tokenizer.nextToken();
          allTokens.add(new SQLToken(subToken));
          if (i < numTokens - 1)
          {
            allTokens.add(literals.get(literalIndex));
            literalIndex++;
          }
        }
        if (tokenValue.endsWith(String.valueOf(PLACEHOLDER_TOKEN)))
        {
          allTokens.add(literals.get(literalIndex));
          literalIndex++;
        }
      }
      else
      {
        allTokens.add(token);
      }
    }

    return allTokens;

  }

  private static PartialParseResult tokenize(final PartialParseResult substitutedLiterals)
  {

    final String substitutedSql = substitutedLiterals.getText();
    final List<Token> literals = substitutedLiterals.getTokens();

    final List<Token> tokens = new ArrayList<Token>();

    // Tokenize the SQL by whitespace delimiters, as well as SQL
    // delimiters
    final StringTokenizer tokenizer = new StringTokenizer(substitutedSql);
    while (tokenizer.hasMoreTokens())
    {
      final String token = tokenizer.nextToken();
      final StringTokenizer subTokenizer = new StringTokenizer(token,
                                                               "(),",
                                                               true);
      while (subTokenizer.hasMoreTokens())
      {
        final String subToken = subTokenizer.nextToken();
        tokens.add(new SQLToken(subToken));
      }
    }

    // Search for keywords that are two tokens long, such as GROUP BY,
    // and treat them as
    // a single token.
    for (int i = 0; i < tokens.size() - 1; i++)
    {
      final String token1 = (tokens.get(i)).getToken();
      final String token2 = (tokens.get(i + 1)).getToken();
      final SQLToken twoWordToken = new SQLToken(token1 + " " + token2);
      if (twoWordToken.isKeyword())
      {
        tokens.set(i, twoWordToken);
        tokens.remove(i + 1);
      }
    }

    // Convert tokens vector into an array with natural (1-based)
    // numbering.
    tokens.add(0, new SQLToken(""));
    tokens.add(new SQLToken(""));

    return new PartialParseResult(splitLiterals(tokens, literals), "");

  }

  private String indent = "  ";

  /**
   * Formats a SQL statement.
   * 
   * @param sql
   *        SQL statement to format
   * @return Formatted SQL statement
   */
  public String format(final String sql)
  {

    final String cleanedSql = cleanString(sql);
    final Token[] tokens = parse(cleanedSql);
    if (!isSQL(tokens))
    {
      return cleanedSql;
    }

    final StringBuffer sqlBuffer = new StringBuffer();
    for (int j = 1; j < tokens.length - 1; j++)
    {
      sqlBuffer.append(tokens[j].build(indent));
    }

    return sqlBuffer.toString().trim();

  }

  private String cleanString(String sql)
  {
    String cleanedSql = StringUtils.trimToEmpty(sql);
    if (sql.startsWith("\"") && (sql.endsWith("\"") || sql.endsWith("\";")))
    {
      cleanedSql = cleanedSql.replaceAll("\\r|\\n|\\t", " ");
      cleanedSql = cleanedSql.replaceAll("\"[ ]+\\+[ ]+\"", " ");
      cleanedSql = cleanedSql.replaceAll("^\"", "");
      if (sql.endsWith("\""))
      {
        cleanedSql = cleanedSql.replaceAll("\"$", "");
      }
      if (sql.endsWith("\";"))
      {
        cleanedSql = cleanedSql.replaceAll("\";$", "");
      }
      StringEscapeUtils.unescapeJava(cleanedSql);
    }
    return cleanedSql;
  }

  /**
   * Gets the current indent size. A negative number indicates a tab
   * indent.
   * 
   * @return Indent size
   */
  public int getIndent()
  {
    if (indent.equals(TAB))
    {
      return -1;
    }
    else
    {
      return indent.length();
    }
  }

  /**
   * Checks if the provided string is a SQL statement by checking the
   * first SQL token.
   * 
   * @param sql
   *        Checks if the string is sql
   * @return True if the statement is a SQL statement
   */
  public boolean isSQL(final String sql)
  {
    return isSQL(parse(sql));
  }

  /**
   * Parses a SQL statement into an array of tokens. The tokens array
   * can be used ot build the formatted SQL statement.
   * 
   * @param sql
   *        SQL statement to parse
   * @return Parsed SQL as an array of tokens
   */
  public Token[] parse(final String sql)
  {

    if (sql == null || sql.length() == 0)
    {
      return new Token[0];
    }

    PartialParseResult result = new PartialParseResult(new ArrayList<Token>(),
                                                       sql);

    // step 1 - substitute all literals
    result = processLiterals(result);
    // step 2 - tokenize
    result = tokenize(result);
    // step 3 - sequence tokens
    determineAfterTokens(result);
    determineIndents(result);
    determineContinuationIndents(result);

    return (Token[]) result.getTokens().toArray(new Token[0]);

  }

  /**
   * Sets an indent, in number of spaces. Any negative value indeciates
   * indentation by tab characters.
   * 
   * @param i
   *        Indent size
   */
  public void setIndent(final int i)
  {
    if (i < 0)
    {
      indent = TAB;
    }
    else
    {
      indent = StringUtils.repeat(" ", i);
    }
  }

  private void determineAfterTokens(final PartialParseResult tokenizedResult)
  {
    final List<Token> tokens = tokenizedResult.getTokens();
    final int numTokens = tokens.size();
    int i;
    SQLToken previousToken, currentToken;

    i = 0;
    while (tokens.get(i) instanceof LiteralToken)
    {
      i++;
    }
    currentToken = (SQLToken) tokens.get(i);
    for (; i < numTokens - 1; i++)
    {
      previousToken = currentToken;
      while (tokens.get(i) instanceof LiteralToken)
      {
        i++;
      }
      currentToken = (SQLToken) tokens.get(i);

      currentToken.setAfterToken(Token.AfterToken.SPACE);
      if (currentToken.isSignificantKeyword())
      {
        // significant keywords appear on a line by themselves
        previousToken.setAfterToken(Token.AfterToken.NEWLINE);
        currentToken.setAfterToken(Token.AfterToken.NEWLINE);
      }
      else if (currentToken.isSeparator())
      {
        // , is NOT preceded by a space, and followed by a new line
        previousToken.setAfterToken(Token.AfterToken.NOTHING);
        currentToken.setAfterToken(Token.AfterToken.NEWLINE);
      }
      else if (currentToken.isOpenParenthesis())
      {
        // text follows an open parenthesis on the same line
        currentToken.setAfterToken(Token.AfterToken.NOTHING);
        // if you have a function or keyword, then the open parenthesis
        // should have no leading space
        if (previousToken.isFunction() || !previousToken.isKeyword())
        {
          previousToken.setAfterToken(Token.AfterToken.NOTHING);
        }
      }
      else if (currentToken.isCloseParenthesis())
      {
        previousToken.setAfterToken(Token.AfterToken.NOTHING);
      }
      else if (currentToken.isConditional())
      {
        currentToken.setAfterToken(Token.AfterToken.NEWLINE);
      }
    }

  }

  private void determineContinuationIndents(final PartialParseResult tokenizedResult)
  {

    final List<Token> tokens = tokenizedResult.getTokens();
    final int numTokens = tokens.size();
    int i;
    Token currentToken;
    Token previousToken;

    // adjust for continuation indent levels
    i = 0;
    currentToken = tokens.get(i);
    for (i = 1; i < numTokens - 1; i++)
    {
      previousToken = currentToken;
      currentToken = tokens.get(i);
      if (previousToken.getAfterToken() != Token.AfterToken.NEWLINE)
      {
        currentToken.setIndentLevel(Token.INDENT_CONTINUATION);
      }
    }

  }

  private void determineIndents(final PartialParseResult tokenizedResult)
  {

    final List<Token> tokens = tokenizedResult.getTokens();
    int numSQLTokens = tokens.size();
    for (int i = numSQLTokens - 1; i > 0; i--)
    {
      final Token token = tokens.get(i);
      if (token instanceof SQLToken && token.getToken().length() > 0)
      {
        break;
      }
      else
      {
        numSQLTokens--;
      }
    }
    // last SQL token always ends with a space
    ((SQLToken) tokens.get(numSQLTokens - 1))
      .setAfterToken(Token.AfterToken.SPACE);

    int i;
    SQLToken currentToken;
    SQLToken previousToken;

    int indentLevel = 0;
    final int[] indents = new int[MAX_INDENTS];

    i = 0;
    while (tokens.get(i) instanceof LiteralToken)
    {
      i++;
    }
    currentToken = (SQLToken) tokens.get(i);
    for (i++; i < numSQLTokens; i++)
    {
      previousToken = currentToken;
      while (!(tokens.get(i) instanceof SQLToken))
      {
        i++;
      }
      currentToken = (SQLToken) tokens.get(i);
      final Token nextToken = tokens.get(i + 1);

      if (currentToken.isCloseParenthesis())
      {
        if (indents[indentLevel] == 0)
        {
          indentLevel--;
          if (i > 0)
          {
            previousToken.setAfterToken(Token.AfterToken.NEWLINE);
          }
        }
        else
        {
          indents[indentLevel]--;
        }
      }
      currentToken.setIndentLevel(indentLevel * 2);
      if (!currentToken.isSignificantKeyword())
      {
        currentToken.nextIndentLevel();
      }
      if (currentToken.isMinorKeyword())
      {
        previousToken.setAfterToken(Token.AfterToken.NEWLINE);
      }
      if (currentToken.isOpenParenthesis())
      {
        if (nextToken instanceof SQLToken && ((SQLToken) nextToken).isSelect())
        {
          previousToken.setAfterToken(Token.AfterToken.NEWLINE);
          currentToken.setAfterToken(Token.AfterToken.NOTHING);
          if (indentLevel < MAX_INDENTS)
          {
            indentLevel++;
          }
          indents[indentLevel] = 0;
        }
        else
        {
          indents[indentLevel]++;
        }
      }
    }

  }

  private boolean isSQL(final Token[] tokens)
  {

    if (tokens == null || tokens.length == 0)
    {
      return false;
    }

    int i = 1;
    while (tokens[i] instanceof LiteralToken)
    {
      i++;
    }
    final SQLToken firstToken = (SQLToken) tokens[i];

    final boolean isSQL = firstToken.isSelect() || firstToken.isInsert() ||
                          firstToken.isUpdate() || firstToken.isDelete();

    return isSQL;

  }

}
