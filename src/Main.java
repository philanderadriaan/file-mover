import java.io.File;
import java.io.FileInputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;

public class Main
{

  /**
   * Config file name.
   */
  private static final String CFG_FILE_NM = "config.properties";

  /**
   * Source folder key in config.
   */
  private static final String SRC_KEY = "src";

  /**
   * Destination folder key in config.
   */
  private static final String DEST_KEY = "dest";

  /**
   * Delete regex key in config.
   */
  private static final String DEL_KEY = "del";

  /**
   * Config object.
   */
  private static final Properties CFG = new Properties();

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    try
    {
      // Load config.
      CFG.load(new FileInputStream(CFG_FILE_NM));

      // Get source/destination folders from config.
      File srcFolder = new File(CFG.getProperty(SRC_KEY));
      File destFolder = new File(CFG.getProperty(DEST_KEY));

      // Move files/folders from source to destination.
      for (File file : srcFolder.listFiles())
      {
        String destFileNm = destFolder.getAbsolutePath() + "\\" + file.getName();
        try
        {
          // Move file to destination.
          log("Moving " + file.getAbsolutePath() + " -> " + destFileNm);
          Files.move(file.toPath(), Paths.get(destFileNm),
                     StandardCopyOption.REPLACE_EXISTING);
        }
        catch (DirectoryNotEmptyException e)
        {
          // Delete non-empty folder from destination and move again.
          log("Deleting " + destFileNm);
          FileUtils.deleteDirectory(new File(destFileNm));
          log("Moving " + file.getAbsolutePath() + " -> " + destFileNm);
          Files.move(file.toPath(), Paths.get(destFileNm),
                     StandardCopyOption.REPLACE_EXISTING);
        }
        catch (AccessDeniedException e)
        {
          // Log and ignore other exceptions.
          log(e.getClass().getName());
        }
      }

      // Delete files defined by regex in config.
      for (File file : FileUtils.listFiles(destFolder, null, true))
      {
        if (match(file, DEL_KEY))
        {
          log("Deleting " + file.getAbsolutePath());
          file.delete();
        }
      }
    }
    catch (Exception e)
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().toString(),
                                    JOptionPane.ERROR_MESSAGE);
      throw e;
    }

  }

  /**
   * 
   * @param file
   * @param key
   * @return
   */
  private static boolean match(File file, String key)
  {
    String regex = CFG.getProperty(key);
    return regex != null &&
           Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(file.getName()).find();
  }

  /**
   * Output to log.
   * 
   * @param msg
   */
  private static void log(String msg)
  {
    System.out.println("[" + new Date() + "]\t" + msg);
  }

}
