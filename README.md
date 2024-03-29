# sout: a minimal Java template library

This one can be learned in five minutes. Here we go:

1. create a configuration with `new SoutConfiguration(...)` (
   see [samples](src/test/java/com/laamella/examples/ExamplesTest.java))
2. create a new template with `new SoutTemplate(template, configuration)`
3. render the template by giving it your model: `template.render(model, output)`

Delimiters are fully configurable. Let's say we have... `<` `|` `>` for opening brace, separator, and closing brace.
With these delimiters, a template would look
like [this sample](https://github.com/matozoid/sout/blob/master/src/test/resources/templates/hello.sout).

Choose characters for delimiters that do not occur frequently in your template. If you need to use the character
directly in the output, you can use the (fully configurable) escape character.

## simple values

`<abc>` takes the value named "abc" from the model and renders it to a string.

`<abc.def>` is a nice shortcut that takes the value named "def" from the value named "abc" from the model.

`<>` converts the model itself to a string.

"Taking the value named abc" means looking at the model, and...

- if it is a map, check for key "abc"
- if it is a function, apply "abc" to it
- if it is an object, try accessing field "abc"
- ... or try accessing "getAbc()"
- ... or try accessing "isAbc()"
- ... or try accessing "abc()"

## nesting

Anything that nests is written like this:

Opening brace, name, separator, part, [separator, part], closing brace

`<name|part1|part2>`

Which of the parts gets rendered depends on the type of `name`.

### conditional rendering

For this, the name needs to resolve to a boolean in the model.

`<foodReady|Yay!>` If the food is ready, `Yay!` will be rendered, else nothing is.

`<foodReady|Yay!|Awww>` If the food is ready, `Yay!` will be rendered, else `Awww`.

### looping over a collection

For this, the name needs to resolve to some kind of collection in the model.

`<abc|<def>>` takes the value "abc" from the model, and the value "def" from that value. If the value of "abc" is a
collection of some kind, `<def>` will be repeated for each element of the collection.

With a model of...

```
people -> [
    { name -> "Piet" },
    { name -> "Klaas" }
]
jobs -> [ "janitor", "cleaner" ]
company -> "Laamella Gad"
```

`Hello <people|<name>, >` will output `Hello Piet, Klaas, `. Note the comma at the end.

`Hello <people|<name>|, >` will output `Hello Piet, Klaas`. Note how the comma is now a correct separator.

`Available jobs: <jobs|<>|, >.` will output `Available jobs: janitor, cleaner.` Since the strings we want to render are
directly contained in the jobs list, we can use `<>` to say: the direct list element please, no need to look for a value
on it.

`<people|Hello |<name>|, |! Welcome to <company>!>` will output `Hello Piet, Klaas! Welcome to Laamella Gad!`. If there
were no people in the list, Hello and the welcome part wouldn't be rendered.

As you can see, putting the separator character after the name creates a nesting where we can loop. The amount of
separated parts indicates what the separated parts mean:

| Number | Meaning |
| --- |  --- |
| 1 | the name of the collection in the model, and the main part, the part that gets repeated for every element in the collection |
| 2 | the name, the main part, and the separator part that comes between the main parts |
| 4 | the name, lead in, main part, separator part, lead out. The lead in and lead out are only rendered when the collection is not empty |

## extension

Template rendering can be extended at these points:

1. with a CustomNameRenderer you can look at every name before it is evaluated, and take over rendering if you like. The
   samples contain a template inclusion extension and a counter extension.
2. with a CustomTypeRenderer you can look at every value that is about to be rendered, and take over if you like. The
   samples contain a custom date formatter, and a formatter that handles null values by rendering an empty string,
   preventing the default behaviour of throwing an exception.
3. with a CustomIteratorFactory you can make loops over types of collections that are not known to sout. The samples
   contain an iterator factory for a Tuple class.

# Samples

[Various complete samples.](src/test/java/com/laamella/examples/ExamplesTest.java)

# Dependency

TODO release

# Design decisions

- make it do only what it needs to do. Therefore, no template loading infrastructure, no library of formatters, no
  caching mechanics, etc. All of these can be built, but it would make the library opinionated, and a template library will probably need to work in an environment with a different opinion.
- Only the bare minimum API is `public` to improve discoverability.
- make everything straightforward. No builders, no hidden static factory methods, no accidentally public methods, etc.
- If there is something in the JDK that is good enough, don't reinvent it.
- avoid complexity in the template language - special formatting is done in code. We have great tooling for code!
- open for extension. Rendering can be completely customized at various points. New functionality can be added in several ways.
- strict by default: a template can't be created from an invalid template text, nulls will abort template rendering,
  etc. Don't accidentally render `Good afternoon insert name here`, `You have clicked this button NaN times` or `Yours, null`.