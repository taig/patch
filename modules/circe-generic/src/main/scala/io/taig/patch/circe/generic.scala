package io.taig.patch.circe

import io.taig.patch._

object generic {
  def deriveDecoder[A](implicit decoder: DerivedJsonPatchDecoder[A]): JsonPatchDecoder[A] = decoder

  def deriveEncoder[A](implicit encoder: DerivedJsonPatchEncoder[A]): JsonPatchEncoder[A] = encoder
}
