# Contributing to HySkript

> [!IMPORTANT]  
> This document is a work in progress.

## Branches:
- `master` = Current release only
- `dev/patch` = Development branch for patch releases
- `dev/feature` = Development branch for feature releases

## Pull Requests:
- If you are adding a new feature, please provide a detailed explanation of the feature and its implementation.
- If you are fixing a bug, please provide a detailed explanation of the bug and how your fix addresses it.

## Before:
Before taking the time to make a big change and submitting a PR, we recommend posting a suggestion on the [Issue Tracker](https://github.com/SkriptDev/HySkript/issues) outlining what you'd like to do.   
This way the team can discuss with you whether or not we want this in HySkript.


## Dos and Don'ts:
### Do:
- PRs should be code-based (no string only PRs)
- Use descriptive commit messages
- Use descriptive PR titles
- Ensure you follow the code style of this project

### Don't:
- Don't commit directly to `master`
- Don't use reflection (Hytale is pretty open)
- We won't accept PRs that are just string-based changes. Your PR contribution should be code-based. (If you find a typo, report it and/or let one of the team members know.)
- If a class seems outdated (in terms of formatting) please do not reformat the entire class for small changes (it makes PRs really difficult to read).
- Don't break any current syntaxes (ie: removing/changing a pattern with a breaking change).

## Code Style:
### Formatting:
- Use 4 spaces for indentation. (editorconfig should take care of this)
- No wildcard imports.
- Imports should be grouped together by type (e.g. all `java.lang`... imports together)
- No trailing whitespace.
- 120-character line limit. (editorconfig should take care of this)
- Each class begins with an empty line
- Each class ends with an empty line
- Use empty lines liberally inside methods to improve readability

### Naming
- Class names are written in UpperCamelCase
  - The file name should match its primary class name (e.g. `MyClass` goes in `MyClass.java.`)
- Fields and methods named in `camelCase`.
  - Static constant fields should be named in `UPPER_SNAKE_CASE`
- Use prefixes only where their use has been already established (such as `ExprSomeRandomThing`)
  - Otherwise, use postfixes where necessary
  - Common occurrences include: Struct (Structure), Sec (Section),  Eff (Effect), Cond (Condition), Expr (Expression)
- Ensure variable/field names are descriptive.

### Syntax Docs:
- When using the since field, please use "INSERT VERISON" (this will be changed on release), ex:      
  `.since("INSERT VERSION")`
- Please provide detailed descriptions of what the syntax does.
- Please provide adequate yet simple examples.
    - For expressions, please provide an example of using the getter as well as each changer you have applied.
    - For all others, please provide at least one example per pattern.
    - Please see other examples in HySkript for further inspiration.
