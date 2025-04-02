#### URL Form serialisation

URL Form serialisation is used by a few AWS protocols, including :

- [AWS query protocol](https://smithy.io/2.0/aws/protocols/aws-query-protocol.html)
- [AWS EC2 query protoco](https://smithy.io/2.0/aws/protocols/aws-ec2-query-protocol.html)

However these protocols make use of XML-related traits to custom URL Form serialisation logic. We think it's valuable to have dedicated traits to the same purpose, that
other protocols can use.

#### alloy#urlFormFlattened

url-form data equivalent of `xmlFlattened`. Unwraps the values of a list, set, or map into the containing structure/union when serialized as url-form data.

For instance, serialising a piece of data described by the following structure ...

```smithy
structure User {
    name: String
    @urlFormFlattened
    aliases: StringList
}

list StringList {
  member: String
}
```

... would lead to the following payload :

```
name=foo
aliases.1=A
aliases.2=B
```

instead of

```
name=foo
aliases.member.1=A
aliases.member.2=B
```

#### alloy#urlFormName

url-form data equivalent of `xmlName`. Changes the serialized url-form data key of a structure, union, or member.


For instance, serialising a piece of data described by the following structure ...


```smithy
structure User {
    @urlFormName("nickName")
    name: String
    age: Integer
}
```

... would lead to the following payload :

```
nickName=John
age=23
```

instead of

```
name=John
age=23
```
