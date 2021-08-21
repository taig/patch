package io.taig.patch

import io.circe.{Decoder, Json}

/** Decode a `Json` object by its field name */
trait JsonPatchDecoder[A] {
  def decode(name: String, json: Json): Option[Decoder.Result[A]]
}
