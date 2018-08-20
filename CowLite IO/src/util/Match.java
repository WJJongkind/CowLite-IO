package util;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Match {
        /**
          * List containing all of the groups of the match. Group 0 represents the
          * entire String that matched with the regex codes.
          */
         private final List<Group> groups;

         /**
          * Instantiates a new {@code Match} object without storing the start and end 
          * lines of each match or group. 
          * <p>
          * This constructor should be used when one single line was matched or when 
          * storing the indices of the lines at which the match started or ended is 
          * not necessary. 
          * 
          * @param matcher The {@code Matcher} of which the current match should be
          *                stored.
          * @see #Match(Matcher, int)
          * @see #Match(Matcher, int[])
          */
         public Match(Matcher matcher) {
             groups = new ArrayList<>();

             for(int i = 0; i <= matcher.groupCount(); i++) {
                 groups.add(new Group(matcher.group(i),
                                      -1,
                                      -1,
                                      matcher.start(i),
                                      matcher.end(i)
                 ));
             }
         }

         /**
          * Instantiates a new {@code Match} object and saves the line at which the
          * match was found along with it. 
          * <p>
          * This constructor should be used when a regex code was used to match 
          * multiple lines or multiple objects in a list and it is important to save
          * the line number or list index. 
          * 
          * @param matcher The {@code Matcher} of which the current match should be
          *                stored.
          * @param line The line or list index at which the match was found.
          * @see #Match(Matcher) 
          * @see #Match(Matcher, int[]) 
          */
         public Match(Matcher matcher, int line) {
             groups = new ArrayList<>();

             for(int i = 0; i <= matcher.groupCount(); i++) {
                 groups.add(new Group(matcher.group(i),
                                      line,
                                      line,
                                      matcher.start(i),
                                      matcher.end(i)
                 ));
             }
         }

         /**
          * Instantiates a new {@code Match} object and stores the indices or line numbers
          * where the matches and groups started and ended. 
          * <p>
          * This constructor should be used when a regex code was used for multi-line 
          * matching across  multiple lines or multiple objects in a list and it is 
          * important to save the line number or list index at which the match and 
          * it's groups started and ended. 
          * <p>
          * For this constructor to work properly, it is important that the {@code Matcher}
          * contains the cumulative start and end indices of the match and groups. In 
          * the case of lists of {@code String}s, the elements in that list should be
          * concatenated. In the case of a 'multi-line' {@code String} where
          * the lines are split by {@code \n}, all occurrences of {@code \n} should be
          * replaced with an empty {@code String} (""). 
          * 
          * @param matcher The {@code Matcher} of which the current match should be
          *                stored.
          * @param cumulativeLineSizes The line or list index at which the match was found.
          * @see #Match(Matcher) 
          * @see #Match(Matcher, int) 
          */
         public Match(Matcher matcher, int[] cumulativeLineSizes) {
             groups = new ArrayList<>();

             for(int i = 0; i <= matcher.groupCount(); i++) {
                 int startLine = getLineNumber(matcher.start(i), cumulativeLineSizes);
                 int endLine = getLineNumber(matcher.end(i), cumulativeLineSizes);
                 int startIndex;
                 int endIndex;

                 /*
                     Obtain the index at which the start line starts, in the scenario 
                     where all lines are pasted in one String. This is required to
                     calculate the index of the character in the line where the match 
                     starts.
                 */
                 if(startLine == 0) {
                     startIndex = 0;
                 } else {
                     startIndex = cumulativeLineSizes[startLine - 1];
                 }

                 // Same reason as startIndex, but for the index of the character in 
                 // the line where the match ends.
                 if(endLine == 0) {
                     endIndex = 0;
                 } else {
                     endIndex = cumulativeLineSizes[endLine - 1];
                 }

                 groups.add(new Group(matcher.group(i),
                                      startLine,
                                      endLine,
                                      matcher.start(i) - startIndex,
                                      matcher.end(i) - endIndex
                 ));
             }
         }

         /**
          * Obtains the line number or list index which contains the given index.
          * 
          * @param i The index of which the list index or line number should be obtained.
          * @param cumulativeLineSizes The cumulative sizes of the lines or list elements.
          * @return The line number or list index in which the given index is found.
          */
         private int getLineNumber(int i, int[] cumulativeLineSizes) {
             int line = 0;
             while(i > cumulativeLineSizes[line]) {
                 line++;
             }

             return line;
         }

         /**
          * Returns the index of the line at which the match starts, or -1 if no line was
          * stored. Starts with line 0 as the first line.
          * 
          * @return The line at which the match starts, or -1 if no line was stored.
          */
         public int getStartLine() {
             return groups.get(0).getStartLine();
         }

         /**
          * Returns the index of the line at which the match ends, or -1 if no line was
          * stored. Starts with line 0 as the first line.
          * 
          * @return The line at which the match ends, or -1 if no line was stored.
          */
         public int getEndLine() {
             return groups.get(0).getEndLine();
         }

         /**
          * Returns the index of the first matched character in the line where the match starts.
          * @return  The index of the first matched character in the line where the match starts.
          */
         public int getStartIndex() {
             return groups.get(0).getStartIndex();
         }

         /**
          * Returns the index of the last matched character in the line where the match ends.
          * @return  The index of the last matched character in the line where the match ends.
          */
         public int getEndIndex() {
             return groups.get(0).getEndIndex();
         }

         /**
          * Returns the amount of groups in the match.
          * @return The amount of groups in the match.
          */
         public int getGroupCount() {
             return groups.size();
         }

         /**
          * Returns the matched group at the given index, or null if the group does not exist.

          * @param group The group which needs to be obtained.
          * @return The group at the given index, or null if the group does not exist.
          */
         public Group group(int group) {
             if(group < groups.size() && group > -1) {
                 return groups.get(group);
             } else {
                 return null;
             }
         }

         /**
          * Returns all the groups in the match, including the fully matched String itself.
          * @return All the groups in the match, including the fully matched String itself.
          */
         public List<Group> getGroups() {
             return groups;
         }

         /**
          * Objects of this class represent groups in regex matches. For each group,
          * the matched String is stored along with some usefull additional information
          * such as the line at which the match started, the line at which te match ended,
          * and for both lines the index at which the match started or ended. 
          * 
          * @author Wessel Jelle Jongkind
          * @version 2018-03-22
          */
         public class Group {
             /**
              * The line at which the matched group starts.
              */
             private final int startLine;

             /**
              * The line at which the matched group ends.
              */
             private final int endLine;

             /**
              * The index of the character in the line at which the matched group starts.
              */
             private final int startIndex;

             /**
              * The index of the character in the line at which the matched group ends.
              */
             private final int endIndex;

             /**
              * The String that represents the group that was matched.
              */
             private final String match;

             /**
              * Instantiates a new Group object.
              * 
              * @param match The String that represents the group that was matched.
              * @param startLine The line at which the matched group starts.
              * @param endLine The line at which the matched group ends.
              * @param startIndex The index of the character in the line at which the matched group starts.
              * @param endIndex The index of the character in the line at which the matched group ends.
              */
             public Group(String match, int startLine, int endLine, int startIndex, int endIndex) {
                 this.startLine = startLine;
                 this.endLine = endLine;
                 this.startIndex = startIndex;
                 this.endIndex = endIndex;
                 this.match = match;
             }

             /**
              * Returns the index of the line at which the matched group starts, or -1 if no line was
              * stored. Starts with line 0 as the first line.
              * 
              * @return The line at which the match starts, or -1 if no line was stored.
              */
             public int getStartLine() {
                 return startLine;
             }

             /**
              * Returns the index of the line at which the matched group starts, or -1 if no line was
              * stored. Starts with line 0 as the first line.
              * 
              * @return The line at which the match starts, or -1 if no line was stored.
              */
             public int getEndLine() {
                 return endLine;
             }

             /**
              * Returns the index of the first matched character in the line where the matched group starts.
              * @return  The index of the first matched character in the line where the matched group starts.
              */
             public int getStartIndex() {
                 return startIndex;
             }

             /**
              * Returns the index of the first matched character in the line where the matched group ends.
              * @return  The index of the first matched character in the line where the matched group ends.
              */
             public int getEndIndex() {
                 return endIndex;
             }

             /**
              * Returns the {@code String} which was matched.
              * @return The {@code String} which was matched.
              */
             public String getMatch() {
                 return match;
             }
         }
    }