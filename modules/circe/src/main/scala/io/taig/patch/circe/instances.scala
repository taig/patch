package io.taig.patch.circe

import cats.Foldable
import cats.data.{NonEmptyList, NonEmptySeq}
import cats.syntax.all._
import io.circe.{Decoder, DecodingFailure, Encoder, JsonObject}
import io.taig.patch.{JsonPatchDecoder, JsonPatchEncoder}

trait instances {
  implicit def decoderListPatches[A](implicit decoder: JsonPatchDecoder[A]): Decoder[List[A]] =
    Decoder.instance { cursor =>
      cursor.as[JsonObject].flatMap { json =>
        json.toList.traverse { case (key, json) =>
          decoder.decode(key, json).getOrElse(DecodingFailure(s"Unknown update field: '$key'", cursor.history).asLeft)
        }
      }
    }

  implicit def decoderSeqPatches[A](implicit decoder: JsonPatchDecoder[A]): Decoder[Seq[A]] =
    decoderListPatches[A].map(_.toSeq)

  implicit def decoderSetPatches[A](implicit decoder: JsonPatchDecoder[A]): Decoder[Set[A]] =
    decoderListPatches[A].map(_.toSet)

  implicit def decoderNonEmptyListPatches[A](implicit decoder: JsonPatchDecoder[A]): Decoder[NonEmptyList[A]] =
    decoderListPatches[A].emap(_.toNel.toRight("empty"))

  implicit def decoderNonEmptySeqPatches[A](implicit decoder: JsonPatchDecoder[A]): Decoder[NonEmptySeq[A]] =
    decoderListPatches[A].emap(_.toNeSeq.toRight("empty"))

  implicit def encoderPatches[F[_]: Foldable, A](implicit encoder: JsonPatchEncoder[A]): Encoder.AsObject[F[A]] =
    Encoder.AsObject.instance { updates =>
      updates.foldl(JsonObject.empty)((json, value) => json.+:(encoder.encode(value)))
    }
}
