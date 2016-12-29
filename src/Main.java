import com.jniwrapper.win32.jexcel.Application;
import com.jniwrapper.win32.jexcel.ExcelException;
import diff.ArrayDiff;
import diff.Diff;
import diff.SearchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by apple on 12/29/16.
 */
public class Main {
    final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] agrs) {
        logger.info(SearchUtils.join(",", new Integer[]{1,2,3,4}));
    }
}
