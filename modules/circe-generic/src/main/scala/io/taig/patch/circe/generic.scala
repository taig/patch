package io.taig.patch.circe

import io.taig.patch.{DerivedJsonPatchEncoder, JsonPatchEncoder}

object generic {
  def deriveEncoder[A](names: A => String)(implicit encoder: DerivedJsonPatchEncoder[A]): JsonPatchEncoder[A] =
    encoder.toEncoder(names)
}
