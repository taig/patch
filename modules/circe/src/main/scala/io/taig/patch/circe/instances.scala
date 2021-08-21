package io.taig.patch.circe

import cats.syntax.all._
import io.circe.{Decoder, DecodingFailure, Encoder, JsonObject}
import io.taig.patch.{JsonPatchDecoder, JsonPatchEncoder}

trait instances {
  implicit def decoderListPatcher[A](implicit decoder: JsonPatchDecoder[A]): Decoder[List[A]] =
    Decoder.instance { cursor =>
      cursor.as[JsonObject].flatMap { json =>
        json.toList.traverse { case (key, json) =>
          decoder.decode(key, json).getOrElse(DecodingFailure(s"Unknown update field: '$key'", cursor.history).asLeft)
        }
      }
    }

  implicit def encoderPatches[A](implicit encoder: JsonPatchEncoder[A]): Encoder.AsObject[List[A]] =
    Encoder.AsObject.instance { updates =>
      updates.foldLeft(JsonObject.empty)((json, value) => json.+:(encoder.encode(value)))
    }
}
