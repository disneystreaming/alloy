## JSON serialisation

`alloy` defines a number of traits that can be taken into consideration by protocols to express additional constraints and encodings typically found in the industry.

### Unions

Unions in this protocol can be encoded in three different ways: tagged, discriminated, and untagged.

By default, the specification of the Smithy language hints that the `tagged-union` encoding should be used. This is arguably the best encoding for unions, as it works with members of any type (not just structures), and does not require backtracking during parsing, which makes it more efficient.

However, `alloy#simpleRestJson` supports two additional encodings: `discriminated` and `untagged`, which users can opt-in via the `alloy#discriminated` and `alloy#untagged` trait, respectively. These are mostly offered as a way to retrofit existing APIs in Smithy.


#### Tagged union

This is the default behavior, and happens to visually match how Smithy unions are declared. In this encoding, the union is encoded as a JSON object with a single key-value pair, the key signalling which alternative has been encoded.

```smithy
union Tagged {
  first: String
  second: IntWrapper
}

structure IntWrapper {
  int: Integer
}
```

The following instances of `Tagged`

```scala
Tagged.FirstCase("alloy")
Tagged.SecondCase(IntWrapper(42)))
```

are encoded as such :

```json
{ "first": "alloy" }
{ "second": { "int": 42 } }
```

#### Untagged union

Untagged unions are supported via an annotation: `@untagged`. Despite the smaller payload size this encoding produces, it is arguably the worst way of encoding unions, as it may require backtracking multiple times on the parsing side. Use this carefully, preferably only when you need to retrofit an existing API into Smithy.

```smithy
use alloy#untagged

@untagged
union Untagged {
  first: String
  second: IntWrapper
}

structure IntWrapper {
  int: Integer
}
```

The following instances of `Untagged`

```scala
Untagged.FirstCase("alloy")
Untagged.SecondCase(Two(42)))
```

are encoded as such :

```json
"alloy"
{ "int": 42 }
```

#### Discriminated union

Discriminated union are supported via an annotation: `@discriminated("tpe")`, and work only when all members of the union are structures.
In this encoding, the discriminator is inlined as a JSON field within JSON object resulting from the encoding of the member.

Despite the JSON payload exhibiting less nesting than in the `tagged union` encoding, this encoding often leads to bigger payloads, and requires backtracking once during parsing.

```smithy
use alloy#discriminated

@discriminated("tpe")
union Discriminated {
  first: StringWrapper
  second: IntWrapper
}

structure StringWrapper {
  myString: String
}

structure IntWrapper {
  myInt: Integer
}
```

The following instances of `Discriminated`

```scala
Discriminated.FirstCase(StringWrapper("alloy"))
Discriminated.SecondCase(IntWrapper(42)))
```

are encoded as such

```json
{ "tpe": "first", "myString": "alloy" }
{ "tpe": "second", "myInt": 42 }
```
