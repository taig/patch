package io.taig.patch

import io.circe.Decoder.Result
import io.circe.Json

trait JsonPatchCodec[A] extends JsonPatchDecoder[A] with JsonPatchEncoder[A]

object JsonPatchCodec {
  def apply[A](implicit codec: JsonPatchCodec[A]): JsonPatchCodec[A] = codec

  def from[A](decoder: JsonPatchDecoder[A], encoder: JsonPatchEncoder[A]): JsonPatchCodec[A] = new JsonPatchCodec[A] {
    override def decode(name: String, json: Json): Option[Result[A]] = decoder.decode(name, json)

    override def encode(value: A): (String, Json) = encoder.encode(value)
  }
}
