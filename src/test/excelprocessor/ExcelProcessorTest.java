package test.excelprocessor;

import diff.ArrayDiff;
import diff.Diff;
import excelprocessor.workbook.WorkbookWrapper;
import junit.framework.TestCase;
import kk.Utils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

            WorkbookWrapper w1 = new WorkbookWrapper(filePath1);
            WorkbookWrapper w2 = new WorkbookWrapper(filePath2);

            String[] arr1 = w1.getArrayCellValuesAtSheet(0).getCellRenderValues();
            String[] arr2 = w2.getArrayCellValuesAtSheet(0).getCellRenderValues();

            ArrayDiff diffProcessor = new ArrayDiff();
            List<Diff<String>> diffs = diffProcessor.diff_main(arr1, arr2);

            logger.info("diff result {}", diffs);


        }catch (Exception e) {
            e.printStackTrace();
        }
    }



}
