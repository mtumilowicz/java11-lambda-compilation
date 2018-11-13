import org.junit.Test;

import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by mtumilowicz on 2018-11-13.
 */
public class LambdaCompilationTest {
    
    @Test
    public void thisTest() {
        Supplier<String> supplier = this::toString;
        
        assertThat(supplier.get(), containsString("LambdaCompilationTest"));
    }

    @Test
    public void lambdaClass() {
        Supplier<String> supplier = () -> "empty";

        assertThat(supplier.getClass().toString(), containsString("LambdaCompilationTest$$Lambda$40"));
    }

    @Test
    public void exception() {
        Supplier<String> supplier = () -> {throw new RuntimeException();};

        try {
            supplier.get();
        } catch (RuntimeException ex) {
            System.out.println(ex.getStackTrace()[0]);
        }
    }
}
