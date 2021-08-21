package io.taig.patch

trait JsonPatchCodec[A] extends JsonPatchDecoder[A] with JsonPatchEncoder[A]

object JsonPatchCodec {
  def apply[A](implicit codec: JsonPatchCodec[A]): JsonPatchCodec[A] = codec
}
