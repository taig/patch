package io.taig.patch

trait JsonPatchCodec[A] extends JsonPatchDecoder[A] with JsonPatchEncoder[A]
