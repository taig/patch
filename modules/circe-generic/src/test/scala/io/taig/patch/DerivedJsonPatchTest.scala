package io.taig.patch

import munit.FunSuite

final class DerivedJsonPatchTest extends FunSuite {
  test("deriveEncoder") {
    circe.generic.deriveEncoder[PersonJsonPatch] {
      case _: PersonJsonPatch.Name    => "name"
      case _: PersonJsonPatch.Age     => "age"
      case _: PersonJsonPatch.Address => "address"
    }
  }
}
