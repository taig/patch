package io.taig.patch

import io.circe.{Decoder, DecodingFailure, Encoder, JsonObject}
import cats.syntax.all._

object circe {
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
