package test.excelprocessor;

import controller.MainController;
import diff.ArrayDiff;
import diff.Diff;
import excelprocessor.workbook.WorkbookWrapper;
import junit.framework.TestCase;
import kk.Utils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.applet.Main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by apple on 1/2/17.
 */
public class ExcelProcessorTest extends TestCase {
    private Logger logger = LoggerFactory.getLogger(ExcelProcessorTest.class);

    public void testLoadWordbook() {
        try {

            String filePath1 = Utils.getCurrentWorkingDir() + File.separator + "resources"
                    + File.separator + "HeroConfig.xlsx";

            String filePath2 = Utils.getCurrentWorkingDir() + File.separator + "resources"
                    + File.separator + "HeroConfig2.xlsx";

            WorkbookWrapper w1 = new WorkbookWrapper(filePath1, MainController.OLD_FILE_INDEX);
            WorkbookWrapper w2 = new WorkbookWrapper(filePath2, MainController.NEW_FILE_INDEX);

            String[] arr1 = w1.getDataForDiffAtSheet(1);
            String[] arr2 = w2.getDataForDiffAtSheet(1);

            ArrayDiff diffProcessor = new ArrayDiff(String.class);
            List<Diff<String>> diffs = diffProcessor.diff_main(arr1, arr2);

//            logger.info("diff result {}", diffs);


        }catch (Exception e) {
            e.printStackTrace();
        }
    }



}
