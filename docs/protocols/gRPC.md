### alloy.proto#grpc

This protocol represents the GRPC protocol as defined at [grpc.io](https://grpc.io/).

The following traits should be taken into consideration by implementors of the protocol :

- alloy.proto#protoIndex
- alloy.proto#protoNumType

Additionally, the following traits can be taken into consideration by other tooling to translate smithy specifications to proto specifications.

- alloy.proto#protoEnabled
- alloy.proto#protoReservedFields

#### ⚠️ Out-of-the-box support for `protobuf` or `gRPC` is not provided in smithy4s smithy4s.

In order to make them work you will need to:
1. Translate `*.smithy` files to `*.proto`, see [the example](https://github.com/disneystreaming/smithy-translate/blob/1701223018c4a7372633ede81bd64f1edb0390a4/modules/proto/examples/src/smithyproto/scalapb/demo/HelloServer.scala#L23-L26).
1. Use the generated `*.proto` schemas to create your services and clients.

The `alloy.proto#grpc` shape exists for the future implementation of out-of-the-box gRPC (PRs welcomed :slightly_smiling_face:)

See the minimal example below:
```scala
ThisBuild / scalaVersion := "2.13.9"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .enablePlugins(Smithy4sCodegenPlugin)
  .dependsOn(`proto-hints`)
  .settings(
    name := "foo",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s-swagger" % smithy4sVersion.value,
      "org.http4s" %% "http4s-ember-server" % "0.23.16"
    ),
    Compile / run / fork := true,
    Compile / run / connectInput := true
  )

// Generate the hints from alloy.proto in a separate project. They are not pre-packaged in smithy4s,
// and you need to explicitly allow for the generation of `alloy.proto` hints as we protect against the
// generation of any namespace prefixed with `alloy` by default
lazy val `proto-hints` = (project in file("proto-hints"))
  .enablePlugins(Smithy4sCodegenPlugin)
  .settings(
    name := "proto-hints",
    libraryDependencies ++= Seq(
      "com.disneystreaming.alloy" % "alloy-core" % "0.2.8" % Smithy4s,
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion.value
    ),
    Compile / smithy4sAllowedNamespaces := List("alloy.proto")
  )
```
