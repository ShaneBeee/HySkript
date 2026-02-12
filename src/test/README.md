# TESTS
> [!IMPORTANT]  
> This page is a work in progress.

## Running:

To run tests, there are a few steps:

1) In the build.gradle.kts file, you will need to change this line:  
   `val assetLocation = "/Users/ShaneBee/Desktop/Server/Hytale/Assets/Assets.zip"`  
   This will need to be changed to the location of your Assets.zip file on your computer.
2) After that you can run tests with `./gradlew testRunner`  
   The server will start up, the tests will run and you will see the outcome.

## Making Tests:

### Location of Tests:

Tests are in this package under `skript/tests`.  
Please make sure to add your test in a matching package to their respective Java packages.

### Naming Tests:

The file should match the name of the class you are testing for.  
Ex: `ExprSomeThing.java` -> `ExprSomeThing.sk`

In the test file, the test name should match the class/file name.  
Ex:

```applescript
test "ExprSomeThing":
    some test of some sort
```

### Available Syntaxes:

#### Test Event:

The test event is the top level structure used for your tests to run.  
See above for naming conventions.  
You can use commands/functions in your tests, but other events will not be called.

#### Test Event Context Values:

- `event-world` = The world the tests happen in (which is "default")

#### Assert Effect:

The assert effect is used to test conditions and make sure the test can pass.

Pattern: `assert %=boolean% with %string%`
Example: `assert 1 = 1 with "1 should equal 1"`

If the test fails, your message and the condition will be sent to the TestResults class, which the TestRunner will use
to decide the outcome of all tests.

#### Others:

For the most part, all other syntaxes will be available.  
Some notes:

- Do not use async effects/sections (They'll most likely cause issues)
- Use wait/delay effect with extreme caution   
  - Delays must be less than a second, or the server is going to stop before your test finishes
