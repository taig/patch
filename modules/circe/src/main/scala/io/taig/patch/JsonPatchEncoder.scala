package io.taig.patch

import io.circe.Json

/** Encode a value to its `Json` representation and its field name */
trait JsonPatchEncoder[A] {
  def encode(value: A): (String, Json)
}