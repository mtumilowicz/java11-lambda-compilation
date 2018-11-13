import java.util.function.Supplier;

/**
 * Created by mtumilowicz on 2018-11-13.
 */
public class LambdaCompilation {
    public String return_this() {
        Supplier<String> supplier = this::toString;

        return supplier.get();
    }
}
