package alloy.proto

final class ProtoReservedFieldsTraitValueSpec extends munit.FunSuite {

  test("cannot instantiate invalid ProtoReservedFieldsTraitValue") {
    intercept[IllegalArgumentException](
      ProtoReservedFieldsTraitValue.builder().build()
    )
  }
}
