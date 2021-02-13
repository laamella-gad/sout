# sout: a minimal Java template library

## Why? There are so many already!

Because this one can be learned in five minutes. Here we go:

Delimiters are fully configurable. Let's say we have... `<` `|` `>` `\\` for opening brace, separator, closing brace,
and escape character.

A template would then look
like [this sample](https://github.com/matozoid/sout/blob/master/src/test/resources/templates/hello.sout).

`<abc>` takes the value named "abc" from the model and toString()'s it.

`<abc.def>` takes the value named "def" from the value named "abc" from the model.

`<abc|<def>>` takes the value "abc" from the model, and the value "def" from that value. If the value of "abc" is a
collection of some kind, <def> will be repeated for each element of the collection.

`Hello <people|<name>, >` will output "Hello "

TODO 2 and 4 arguments. TODO empty name.

## How to run it

[Here are two samples.](src/test/java/com/laamella/examples/ExamplesTest.java)

## Dependency

TODO