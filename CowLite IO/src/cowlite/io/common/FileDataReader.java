/* 
 * Copyright (C) 2018 Wessel Jelle Jongkind.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package cowlite.io.common;

import util.Match;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reading files from a file is something that commonly needs to be done in both 
 * large and small scale applications. This class offers an easy way to read files 
 * into different formats and to search through files by using regex codes. It
 * significantly simplifies the way that files are read and for most projects
 * it offers all the needed functionality.
 * <p>
 * <b>This class reads the entire file as a whole. Therefore it should not be
 * used for reading large files. When reading 500MB files, the RAM usage of the application
 * will increase with at least that amount. {@code FileDataReader} was written for
 * reading and searching through small files from between 0 - 200MB (and 
 * depending on the available RAM, up to 750MB).</b>
 * 
 * <h1>Basic usage</h1>
 * 
 * When reading files with {@code FileDataReader}, the path to the file has to 
 * be set first by using the {@link #setPath(String)} and {@link #setPath(File)} methods. 
 * Then, the entire file (provided that it exists) is immediately read into memory.
 * 
 * <h2>Data retrieval</h2>
 * 
 * The data can be obtained in multiple ways. The data can be retrieved in multiple
 * ways. The main methods for retrieving data are through the methods {@link #getDataBytes()}
 * which returns the bytes of the file, {@link #getDataString()} which returns the
 * content of the file in one {@code String}, {@link #getDataStringLines()} which
 * returns the content of the file in a {@code List} with each element in the {@code List}
 * representing a line of the file and {@link #getNumericDataLines()} which converts
 * each line to a numeric value (provided that the lines are numeric). The code
 * snippet below shows a small example of {@code FileDataReader} usage.
 * 
 * <pre><i>
 *      // The following won't work, as no path has been set yet...
 *      FileDataReader reader = new FileDataReader();
 *      try {
 *          reader.getDataStringLines();
 *      } catch(Exception e) {
 *          System.out.println("Oops, forget to set the path that has to be read...");
 *      }
 * 
 *      // Lets set a path and print all the lines of the file
 *      try {
 *          reader.setPath("C:\\Users\\SomeUser\\Desktop\\file.txt");
 *          for(String line : reader.getDataStringLines()) {
 *              System.out.println(line);
 *          }
 *      } catch(IOException e) {
 *          e.printStackTrace();
 *      }
 * </i></pre>
 * 
 * <h2>Searching through files</h2>
 * 
 * {@code FileDataReader} also offers some useful tools for searching through
 * files. It does this with the use of regex codes. The main methods for searching
 * through files are {@link #containsMatch(String)} which looks if a match for the
 * given regex code is found in the file and {@link #getRegexMatches(String, boolean)} 
 * which obtains all matches in the file for the given regex code. {@link #getRegexMatches(String, boolean)}
 * makes use of the {@code Match} class in which matches are conveniently stored
 * and made accessible. The code snippet below shows an example of searching through
 * a file with the following text:
 * 
 * <pre><i>
 * the quick brown fox
 * the quick brown
 *  fox
 * the quick brown
 * fox
 * the quick bro
 * wn
 *  f
 * ox
 * </i></pre>
 * 
 * In the following code we search through the text above for matches with "brown fox".
 * Multi-line matches are also accepted.
 * 
 * <pre><i>{@code
 *      // We asume that we already have made a FileDataReader for the text above
 *      List<Match> matches = reader.getRegexMatches("brown fox", true);
 *      
 *      // Print all matches 
 *      for(Match m : matches) {
 *          System.out.println("Startline: " + m.getStartLine() + ", Endline: " + m.getEndLine());
 *      }
 * }</i></pre>
 * 
 * This code should give the following output:
 * <pre><i>
 * Startline: 0, Endline: 0
 * Startline: 1, Endline: 2
 * Startline: 5, Endline: 8
 * </i></pre>
 * 
 * @author Wessel Jelle Jongkind
 * @version 2018-03-16 (yyyy-mm-dd)
 */
public class FileDataReader
{
    /**
     * The path to the file that is being read.
     */
    private String path;
    
    /**
     * The data of the file that is being read.
     */
    private ArrayList<String> data;
    
    /**
     * Sets the file which has to be read. All content of the file is immediately
     * loaded into memory. 
     * @param f The file which has to be read.
     * @throws IOException When the file is not accessible.
     */
    public void setPath(File f) throws IOException {
        setPath(f.getAbsolutePath(), "UTF-8");
    }
    
    public  void setPath(File f, String charset) throws IOException {
        setPath(f.getAbsolutePath(), charset);
    }
    
    public void setPath(String path) throws IOException {
        setPath(path, "UTF-8");
    }
    
    /**
     * Set the path to an existing file which has to be read. All contents of the
     * file are immediately loaded into memory.
     * @param path Path to the file which has to be read.
     * @param charset The CharSet of the file that is being used (usually UTF-8 or UTF-16).
     * @throws IOException When the file is not accessible.
     */
    public void setPath(String path, String charset) throws IOException
    {
        this.path = path;
        readData(charset);
    }
    
    /**
     * Read the content of the file to an {@code ArrayList}. The data is stored
     * in {@link #data}. 
     * @param charset The charset to be used (such as UTF-8).
     * @throws IOException When the file is not accessible.
     */
    private void readData(String charset) throws IOException
    {
        data = new ArrayList<>();
        try (FileInputStream is = new FileInputStream(new File(path));
             InputStreamReader red = new InputStreamReader(is, charset);
             BufferedReader bufred = new BufferedReader(red)) {
            
            String dataline;
            while((dataline = bufred.readLine()) != null)
                data.add(dataline);
        }
    }
    
    /**
     * Returns a {@code String} representation of the path to the file that is
     * being read.
     * @return A {@code String} representation of the path to the file that is
     * being read.
     */
    public String getPath()
    {
        return path;
    }
    
    /**
     * Returns the {@code File} object that denotes the file that is being read.
     * @return The {@code File} object that denotes the file that is being read.
     */
    public File getFile()
    {
        return new File(path);
    }
    
    /**
     * Returns all of the contents of the file that is being read in a single String. 
     * If the file contains multiple lines, then these lines are pasted together 
     * without a separator between them.
     * @return All of the contents of the file that is being read in a single String.
     */
    public String getDataString()
    {
        String allData = "";
        for(String datapart: data)
            allData = allData + datapart;
        
        return allData;
    }
    
    /**
     * Returns the contents of the file that is being read in a {@code List}. Each
     * line of the file is a separate String in the list that is returned by this
     * method.
     * 
     * @return The contents of the file that is being read in a {@code List}.
     */
    public List<String> getDataStringLines()
    {
       return new ArrayList<>(data); 
    }
    
    /**
     * Returns the contents of the file that is being read represented as a numeric
     * {@code List}. Each line of the file is parsed to a double and added 
     * to the list. One of the requirements for this method to work, is that each 
     * line of the file which is being read, represents a numeric value. 
     * 
     * @return The contents of the file that is being read represented as a numeric
     * {@code List}.
     * 
     * @throws NumberFormatException When one or more of the lines in the file could
     *                               not be parsed to a double.
     */
    public List<Double> getNumericDataLines() throws NumberFormatException
    {
        ArrayList<Double> numeric = new ArrayList<>();
        
        try{
            for(String datapart: data)
                numeric.add(Double.parseDouble(datapart));
        }catch(NumberFormatException e){
            throw new NumberFormatException("Line could not be parsed to double.");
        }
        
        return numeric;
    }
    
    /**
     * Returns the content of the file that is being read represented as an array
     * of bytes.
     * @return The content of the file that is being read represented as an array
     * of bytes.
     * @throws Exception When the file is not accessible.
     */
    public byte[] getDataBytes() throws Exception
    {
        return Files.readAllBytes(Paths.get(path));
    }
    
    /**
     * Returns the name of the file that is being read.
     * @return The name of the file that is being read.
     */
    public String getFileName()
    {
        return new File(path).getName();
    }
    
    /**
     * Checks whether there are any matches with the given regex code in the contents
     * of the file. 
     * 
     * @param regex The regex code with which the contents of the file have to be matched.
     * @param multiline {@code true} if the regex matching should be done over multiple
     *                  lines, false otherwise.
     * @return {@code true} if a part of the file matches the given regex code. 
     */
    public boolean containsMatch(String regex, boolean multiline)
    {
        if(multiline) {
            return containsMultilineMatch(regex);
        } else {
            return containsMatch(regex);
        }
    }
    
    /**
     * Checks whether there are any matches with the given regex code in the contents
     * of the file. This method matches across multiple lines, so if the last part
     * of line n and the first part of line n + 1 form a match together, it will
     * be registered as a match.
     * 
     * @param regex The regex code with which the contents of the file have to be matched.
     * 
     * @return {@code true} if a part of the file matches the given regex code. 
     */
    private boolean containsMultilineMatch(String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(getDataString());
        return matcher.find();
    }
    
    /**
     * Checks whether there are any matches with the given regex code in the contents
     * of the file. This method matches across single lines, so if the last part
     * of line n and the first part of line n + 1 form a match together, it will
     * not be registered as a match.
     * 
     * @param regex The regex code with which the contents of the file have to be matched.
     * 
     * @return {@code true} if a part of the file matches the given regex code. 
     */
    private boolean containsMatch(String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher;
        for(int i = 0; i < data.size(); i++)
        {
            matcher = pattern.matcher(data.get(i));
            if(matcher.find())
                return true;
        }
        return false;
    }
    
    /**
     * Finds  all matches with the given regex code in the contents of the file. 
     * 
     * @param regex The regex code with which the contents of the file have to be matched.
     * @param multiline {@code true} if the regex matching should be done over multiple
     *                  lines, false otherwise.
     * @return A {@code List} of {@code Match} objects which represent the matches
     *         that were found in the file. 
     */
    public List<Match> getRegexMatches(String regex, boolean multiline)
    {
        if(multiline) {
            return multilineMatches(regex);
        } else {
            return matches(regex);
        }
    }
    
    /**
     * Finds all matches with the given regex code in the contents
     * of the file. This method matches across multiple lines, so if the last part
     * of line n and the first part of line n + 1 form a match together, it will
     * be registered as a match.
     * 
     * @param regex The regex code with which the contents of the file have to be matched.
     * 
     * @return A {@code List} of {@code Match} objects which represent the matches
     *         that were found in the file. 
     */
    private List<Match> multilineMatches(String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(getDataString());
        List<Match> matches = new ArrayList<>();
        int[] cumulativeLengths = getCumulativeLengths();
        
        // Iterate over all the matches that were found
        while(matcher.find()) {
            matches.add(new Match(matcher, cumulativeLengths));
        }
        
        return matches;
    }
    
    /**
     * Creates and returns an array which contains the cumulative sizes of the lines
     * in the file that is being read.
     * 
     * @return An array which contains the cumulative sizes of the lines
     * in the file that is being read.
     */
    private int[] getCumulativeLengths() {
        int[] lengths = new int[data.size()];
        
        for(int i = 0; i < data.size(); i++) {
            if(i > 0) {
                lengths[i] = lengths[i - 1] + data.get(i).length();
            } else {
                lengths[i] = data.get(i).length();
            }
        }
        
        return lengths;
    }
        
    /**
     * Finds all matches with the given regex code in the contents
     * of the file. This method matches across single lines, so if the last part
     * of line n and the first part of line n + 1 form a match together, it will
     * not be registered as a match.
     * 
     * @param regex The regex code with which the contents of the file have to be matched.
     * 
     * @return A {@code List} of {@code Match} objects which represent the matches
     *         that were found in the file. 
     */
    private List<Match> matches(String regex) {
        //Prepare the  matcher and matchset
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher;
        List<Match> matches = new ArrayList<>();

        //Iterate over all lines of the file
        for(int i = 0; i < data.size(); i++)
        {
            matcher = pattern.matcher(data.get(i));
            while(matcher.find()) {
                matches.add(new Match(matcher, i));
            }
        }
        return matches;
    }
}