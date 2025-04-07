### Documentation trait

The traits described in this document are meant to capture information for documentation purposes. No protocol is expected to support them, but some tooling
may make use of them.

#### alloy#dataExamples

This trait allows you to provide concrete examples of what instances of a given shape will look like. There are three different formats that examples can be provided in: smithy, json, or string.

- Smithy format: Here you will define your examples to match the format of the shape the trait is on. The format must match exactly, and must not include protocol specific information such as `jsonName`. A validator is run on this format type to make sure the data matches the shape it is on.
- Json format: This format is similar to the smithy one, except you can put anything you want in the contents of the JSON. This means you can include protocol-specific pieces such as taking into account the `jsonName` trait.
- String format: This is just a string that is not validated. This format is most useful for protocols that do not support JSON.

```smithy
@dataExamples([
  {
    smithy: {
      name: "Emily",
      age: 64
    }
  },
  {
    json: {
      fullName: "Allison",
      age: 22
    }
  },
  {
    string: "{ fullName: \"Sarah\" }"
  }
])
structure User {
    @jsonName("fullName")
    name: String
    age: Integer
}
```

#### alloy.openapi#openapiExtensions

OpenAPI has support for [extensions](https://swagger.io/docs/specification/openapi-extensions). You can use this trait to capture similar bits of free-form information that may
not otherwise be represented in smithy-traits.

```smithy
@openapiExtensions(
  "x-foo": "bar"
)
list StringList {
  member: String
}
```
