Faraaz Ismail
Paul Glass
Brett Smith
Full Lisp Interpreter

To compile code:
  Run `make` from the command line

To run code:
  `java Parser -options < [test_file]`

  where:
    -options can include: --dynamic for dynamic scoping, --ast to show the abstract syntax tree. Static scoping is set by default.

    - [test_file] is the text file with the examples you want to run. example_scoping.txt for scoping tests, example_combinator.txt for combinator tests, example_recursion for recursion tests if you want to edit existing files.
