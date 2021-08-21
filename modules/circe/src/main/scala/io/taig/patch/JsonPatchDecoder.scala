package io.taig.patch

import io.circe.{Decoder, DecodingFailure, Json}

/** Decode a `Json` object by its field name */
trait JsonPatchDecoder[A] { self =>
  def decode(name: String, json: Json): Option[Decoder.Result[A]]

  final def map[B](f: A => B): JsonPatchDecoder[B] = new JsonPatchDecoder[B] {
    override def decode(name: String, json: Json): Option[Decoder.Result[B]] = self.decode(name, json).map(_.map(f))
  }

  final def emap[B](f: A => Either[DecodingFailure, B]): JsonPatchDecoder[B] = new JsonPatchDecoder[B] {
    override def decode(name: String, json: Json): Option[Decoder.Result[B]] = self.decode(name, json).map(_.flatMap(f))
  }
}

object JsonPatchDecoder {
  def apply[A](implicit decoder: JsonPatchDecoder[A]): JsonPatchDecoder[A] = decoder
}
