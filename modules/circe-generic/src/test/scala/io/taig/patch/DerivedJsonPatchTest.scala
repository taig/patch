package io.taig.patch

import io.circe.Json
import munit.FunSuite

final class DerivedJsonPatchTest extends FunSuite {
  test("deriveDecoder") {
    val decoder = circe.generic.deriveDecoder[PersonJsonPatch]

    assertEquals(
      obtained = decoder.decode("name", Json.fromString("foo")),
      expected = Some(Right(PersonJsonPatch.Name("foo")))
    )
  }

  test("deriveEncoder") {
    val encoder = circe.generic.deriveEncoder[PersonJsonPatch]

    assertEquals(
      obtained = encoder.encode(PersonJsonPatch.Name("foo")),
      expected = "name" -> Json.fromString("foo")
    )
  }
}
