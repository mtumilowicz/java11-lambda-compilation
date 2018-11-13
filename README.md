# java11-lambda-compilation

_Reference_: https://www.logicbig.com/tutorials/core-java-tutorial/java-8-enhancements/java-lambda-functional-aspect.html  
_Reference_: https://www.infoq.com/articles/Java-8-Lambdas-A-Peek-Under-the-Hood

# preface
* Lambda expressions let you express instances of single-method classes 
more compactly.
* Lambda expressions enable you to treat functionality as method argument.

# introduction
Lambda Expressions are not completely syntactic sugar - compiler 
doesn't translate them into something which is already understood by JVM.  

_Remark_: An example of syntactic sugar in Java is enum.

Lambda syntax written by the developer is desugared into JVM level 
instructions generated during compilation, meaning the actual 
responsibility of constructing lambda is bootstrapped to runtime.

The term "bootstrapping" means that it prepares 
everything necessary to actually execute the job later.

# compile time
Instead of generating direct bytecode for lambda - compiler declares 
a recipe (via `invokeDynamic` instructions) and delegates the 
real construction approach to runtime.

## phases
1. Generate an `invokedynamic` call site (called lambda factory) - this 
is needed for JVM to construct object representation of lambda during 
runtime.
1. Lambda body code is generated within an instance or static desugared 
private method which has the same parameters and return type as the 
lambda's functional interface abstract method.
	* **Capturing** - the lambda doesn’t access any variables defined 
	outside its body.
		```
		Function<String, Integer> f = s -> Integer.parseInt(s);
		
		static Integer lambda$1(String s) {
			return Integer.parseInt(s);
		}
		```
	* **Non-capturing** - the lambda accesses variables defined outside 
	its body.
		```
		int offset = 100;
		Function<String, Integer> f = s -> Integer.parseInt(s) + offset;
		
		static Integer lambda$1(int offset, String s) {
			return Integer.parseInt(s) + offset;
		}
		```

_Remark_: The linkage to this method is done via `invokespecial` or 
`invokestatic` instructions by the compiler.  
_Remark_: Any translation strategy is not set in stone because 
the use of the invokedynamic instruction gives the compiler the 
flexibility to choose different implementation strategies in the future. 
For instance, the captured values could be boxed in an array or, if the 
lambda expression reads some fields of the class where it is used, the 
generated method could be an instance one, instead of being declared 
static, thus avoiding the need to pass those fields as additional arguments.	

# naming
```
public static void main (String[] args) {
    Runnable r = () -> System.out.println();
}

private static void lambda$main$0();
```

# runtime
When JVM encounters `invokedynamic` instruction it makes bootstrap call. 
This is one time only call and necessary for lambda object construction 
during runtime. Bootstrap method also creates a `CallSite` instance 
that can be used at runtime to execute lambda method.

JVM chooses a strategy to construct lambda, `invokedynamic` turns 
that choice into pure JVM implementation details, hence separating 
that decision from compile time bytecode.

* `this` will print the enclosing class - because unlike anonymous 
classes they are just functional methods and have no meanings of 
`this` for themselves.

# project description
* We will be testing:
    * this of lambda
    * lambda class
    * exceptions thrown from lambda
    
* We provide base class `LambdaCompilation`
    ```
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
    ```
* And test class `LambdaCompilationTest`, where
    * this:
        ```
        @Test
        public void thisTest() {
            assertThat(new LambdaCompilation().return_this(), containsString("LambdaCompilation"));
        }    
        ```
    * lambda class
        ```
        @Test
        public void lambdaClass() {
            assertThat(new LambdaCompilation().return_lambda_class(), 
                    containsString("LambdaCompilation$$Lambda$40"));
        }    
        ```
    * exceptions thrown from lambda
        ```
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
        ```