import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by mtumilowicz on 2018-11-13.
 */
public class LambdaCompilationTest {

    @Test
    public void thisTest() {
        assertThat(new LambdaCompilation().return_this(), 
                containsString("LambdaCompilation"));
    }

    @Test
    public void lambdaClass() {
        assertThat(new LambdaCompilation().return_lambda_class(), 
                containsString("LambdaCompilation$$Lambda$40"));
    }

    @Test
    public void exception() {
        LambdaCompilation lambdaCompilation = new LambdaCompilation();
        
        try {
            lambdaCompilation.lambda_exception();
        } catch (RuntimeException ex) {
            assertThat(ex.getStackTrace()[0].toString(), containsString("LambdaCompilation.lambda$lambda_exception$0"));
            assertThat(ex.getStackTrace()[1].toString(), containsString("LambdaCompilation.lambda_exception"));
            assertThat(ex.getStackTrace()[2].toString(), containsString("LambdaCompilationTest.exception"));
            ex.printStackTrace();
        }
    }
}
