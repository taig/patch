package io.taig.patch

import cats.data.NonEmptyList
import cats.syntax.all._
import skunk.Encoder
import skunk.data.Type

import scala.util.chaining._

trait SkunkPatchEncoder[A] {
  def encode(value: A): NonEmptyList[SkunkPatchEncoder.Result]
}

object SkunkPatchEncoder {
  final case class Result(field: String, value: Option[String], tpe: Type)

  object Result {
    def apply[A](field: String, fields: String*)(value: A, encoder: Encoder[A]): Option[NonEmptyList[Result]] =
      NonEmptyList.of(field, fields: _*).pipe { fields =>
        val values = encoder.encode(value).toVector
        val types = encoder.types.toVector

        Option.when(values.length == fields.length && types.length == fields.length) {
          fields.mapWithIndex((field, index) => Result(field, values(index), types(index)))
        }
      }

    def unsafe[A](field: String, fields: String*)(value: A, encoder: Encoder[A]): NonEmptyList[Result] =
      apply(field, fields: _*)(value, encoder)
        .getOrElse(throw new IllegalArgumentException("Encoder shape does not match fields"))
  }

  def apply[A](f: A => NonEmptyList[Result]): SkunkPatchEncoder[A] = new SkunkPatchEncoder[A] {
    override def encode(value: A): NonEmptyList[Result] = f(value)
  }
}
