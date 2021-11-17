package io.taig.patch

import skunk.Encoder
import skunk.data.Type

trait SkunkPatchEncoder[A] {
  def encode(value: A): SkunkPatchEncoder.Result
}

object SkunkPatchEncoder {
  final case class Result(field: String, value: Option[String], tpe: Type)

  object Result {
    def apply[A](field: String, value: A, encoder: Encoder[A]): Option[Result] = for {
      value <- encoder.encode(value).headOption
      tpe <- encoder.types.headOption
    } yield Result(field, value, tpe)

    def unsafe[A](field: String, value: A, encoder: Encoder[A]): Result = apply(field, value, encoder).get
  }

  def apply[A](f: A => Result): SkunkPatchEncoder[A] = new SkunkPatchEncoder[A] {
    override def encode(value: A): Result = f(value)
  }
}
