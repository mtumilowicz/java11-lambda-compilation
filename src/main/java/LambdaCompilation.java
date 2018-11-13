import java.util.function.Supplier;

/**
 * Created by mtumilowicz on 2018-11-13.
 */
class LambdaCompilation {
    
    String return_this() {
        Supplier<String> supplier = this::toString;

        return supplier.get();
    }

    String return_lambda_class() {
        Supplier<String> supplier = this::toString;

        return supplier.getClass().toString();
    }
    
    void lambda_exception() {
        Supplier<String> supplier = () -> {throw new RuntimeException();};
        
        supplier.get();
    }
}
