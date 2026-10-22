package rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RulesOf6005Test {

    // --- tests that mirror the starter test ---------------------------------

    @Test
    public void testWrittenByYourself() {
        assertTrue("Expected true: you wrote it yourself",
                RulesOf6005.mayUseCodeInAssignment(true, false, true, false, false));
    }

    @Test
    public void testCitedPublicCode() {
        assertTrue("Expected true: public code, cited, not coursework",
                RulesOf6005.mayUseCodeInAssignment(false, true, false, true, false));
    }

    // --- additional unit tests (step 9) -------------------------------------

    @Test
    public void testOtherStudentsCourseWorkNeverAllowed() {
        assertFalse("Expected false: another student's coursework",
                RulesOf6005.mayUseCodeInAssignment(false, true, true, true, false));
    }

    @Test
    public void testExternalCodeWithoutCitationNotAllowed() {
        assertFalse("Expected false: public code but source not cited",
                RulesOf6005.mayUseCodeInAssignment(false, true, false, false, false));
    }

    @Test
    public void testImplementationRequiredBlocksExternalCode() {
        assertFalse("Expected false: assignment requires your own implementation",
                RulesOf6005.mayUseCodeInAssignment(false, true, false, true, true));
    }

    @Test
    public void testCodeNotAvailableToOthersNotAllowed() {
        assertFalse("Expected false: code not available to the whole class",
                RulesOf6005.mayUseCodeInAssignment(false, false, false, true, false));
    }
}