package io.taig.patch

import io.circe.Json

/** Encode a value to its `Json` representation and its field name */
trait JsonPatchEncoder[A] { self =>
  def encode(value: A): (String, Json)

  final def contramap[B](f: B => A): JsonPatchEncoder[B] = new JsonPatchEncoder[B] {
    override def encode(value: B): (String, Json) = self.encode(f(value))
  }
}

object JsonPatchEncoder {
  def apply[A](implicit encoder: JsonPatchEncoder[A]): JsonPatchEncoder[A] = encoder
}
