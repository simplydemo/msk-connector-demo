package io.github.simplydemo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {

    @Test
    public void test_assertTrue() {
        assertTrue("APPLE".toLowerCase().equals("apple"));
    }

    /**

     @Mock private Calculator calculator;

     @InjectMocks private CalculationService calculationService;

     @BeforeEach void setUp() {
     MockitoAnnotations.openMocks(this);
     }

     @Test void testPerformAddition() {
     when(calculator.add(2, 3)).thenReturn(5);

     int result = calculationService.performAddition(2, 3);
     assertEquals(5, result);
     }
     */

}