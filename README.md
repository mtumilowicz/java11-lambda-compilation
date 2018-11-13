# java11-lambda-compilation

_Reference_: https://www.logicbig.com/tutorials/core-java-tutorial/java-8-enhancements/java-lambda-functional-aspect.html  
_Reference_: https://www.infoq.com/articles/Java-8-Lambdas-A-Peek-Under-the-Hood

# preface
* Lambda expressions let you express instances of single-method classes 
more compactly.
* Lambda expressions enable you to treat functionality as method argument.

# introduction
Lambda Expressions are not completely Syntactic Sugar, meaning compiler 
doesn't translate them into something which is already understood by JVM.  

_Remark_: An example of syntactic sugar in Java is enum.

Lambda syntax written by the developer is desugared into JVM level 
instructions generated during compilation, meaning the actual 
responsibility of constructing lambda is bootstrapped to runtime.

The term "bootstrapping" in this context means that it prepares 
everything necessary to actually execute the job later.

# compile time
Instead of generating direct bytecode for lambda (like proposed anonymous 
class syntactic sugar approach), compiler declares a recipe 
(via `invokeDynamic` instructions) and delegates the real construction 
approach to runtime.

## phases
1. Generate an `invokedynamic` call site (called lambda factory) - this 
is needed for JVM to construct object representation of lambda during 
runtime.
1. Lambda body code is generated within an instance or static desugared 
private method which has the same parameters and return type as the 
lambda's functional interface abstract method.
	* **Capturing** - the lambda doesn’t access any variables defined outside its body.
		```
		Function<String, Integer> f = s -> Integer.parseInt(s);
		
		static Integer lambda$1(String s) {
			return Integer.parseInt(s);
		}
		```
	* **Non-capturing** - the lambda accesses variables defined outside its body.
		```
		int offset = 100;
		Function<String, Integer> f = s -> Integer.parseInt(s) + offset;
		
		static Integer lambda$1(int offset, String s) {
			return Integer.parseInt(s) + offset;
		}
		```

_Remark_: The linkage to this method is done via `invokespecial` or 
`invokestatic` instructions by the compiler.  
_Remark_: However this translation strategy is not set in stone because 
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

JVM chooses a strategy to construct lambda. That strategy may involve 
anonymous inner class generation or `MethodHandle` (another dynamic 
language feature added in Java 7, similar to C function pointer that 
points to an executable code) or dynamic proxies or whatever is good 
in performance. `invokedynamic` turns that choice into pure JVM 
implementation details, hence separating that decision from compile 
time bytecode.

* Runtime class type of a lambda: it depends on the above mentioned 
strategy specific to JVM implementation.
* `this` will print the enclosing class - because unlike anonymous 
classes they are just functional methods and have no meanings of 
`this` for themselves.

# example
```
public class LambdaTest {

   public void aTestMethod() {
       Runnable runnable = () -> {
           System.out.println("this " + this); // 1
           throw new RuntimeException(); // 2
       };
       System.out.println("class:  " + runnable.getClass()); // 3
       runnable.run();
   }

   public static void main(String[] args) {
       LambdaTest lambdaTest = new LambdaTest();
       lambdaTest.aTestMethod();
   }
}
```

1. Enclosing class - `LambdaTest`
1. Exception:
```
Exception in thread "main" java.lang.RuntimeException
    at LambdaTest.lambda$aTestMethod$0(LambdaTest.java:6)
    at LambdaTest.aTestMethod(LambdaTest.java:9)
    at LambdaTest.main(LambdaTest.java:14)
```
1. It's not `Runnable`, it looks like: `com.logicbig.tests.LambdaTest$$Lambda/381259350`