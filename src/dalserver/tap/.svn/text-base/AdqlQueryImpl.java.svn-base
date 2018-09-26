/*
 * AdqlQueryRunner.java
 * $ID*
 */

package dalserver.tap;

import ca.nrc.cadc.tap.AdqlQuery;
import ca.nrc.cadc.tap.parser.converter.TopConverter;
import ca.nrc.cadc.tap.parser.navigator.ExpressionNavigator;
import ca.nrc.cadc.tap.parser.navigator.FromItemNavigator;
import ca.nrc.cadc.tap.parser.navigator.ReferenceNavigator;
import org.apache.log4j.Logger;

/**
 * DALServer implementation of the OpenCADC AdqlQuery class, used to translate
 * an ADQL query to native SQL for the back-end, and verify it in the process.
 *
 * According to the OpenCADC framework: "TAP service implementors must implement
 * this class and add customisations of the navigatorlist as shown below.
 * Custom query visitors can be used to validate or modify the query; the base
 * class runs all the visitors in the navigatorlist once before converting the
 * result into SQL for execution."
 *
 * For our initial implementation here, the generic ADQL translation is used
 * without extension.
 *
 * @author DTody (based upon OpenCADC template by PDowler)
 */
public class AdqlQueryImpl extends AdqlQuery {
    private static Logger log = Logger.getLogger(AdqlQueryImpl.class);

    public AdqlQueryImpl() {
        super();
    }

    @Override
    protected void init() {
        super.init();

        // Example: for postgresql we have to convert TOP to LIMIT
        super.navigatorList.add(
	    new TopConverter(new ExpressionNavigator(), new ReferenceNavigator(), new FromItemNavigator()));

        // TODO: add more custom query visitors here
    }
}
