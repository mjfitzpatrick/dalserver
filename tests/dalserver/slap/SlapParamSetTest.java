package dalserver.slap;

import dalserver.DalServerException;
import dalserver.InvalidDateException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class SlapParamSetTest {

    SlapParamSet pset = null;

    @Before 
    public void setup() throws DalServerException, InvalidDateException {
        pset = new SlapParamSet();
    }

    @After 
    public void teardown() {
        pset = null;
    }

    @Test 
    public void testSize() {
        assertEquals(14, pset.size());
    }

    @Test 
    public void testIsSet() {
        assertTrue(pset.isDefined("VERSION"));
        assertTrue(pset.isDefined("REQUEST"));
        assertTrue(pset.isDefined("WAVELENGTH"));
        assertTrue(pset.isDefined("TEMPERATURE"));
        assertTrue(pset.isDefined("RunID"));
    }
}
