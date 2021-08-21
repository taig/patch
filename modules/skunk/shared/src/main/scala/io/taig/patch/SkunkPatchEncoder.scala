package io.taig.patch

import cats.data.State
import skunk.Encoder
import skunk.data.Type

trait SkunkPatchEncoder[A] {
  def encode(value: A): SkunkPatchEncoder.Result
}

object SkunkPatchEncoder {
  final case class Result(field: String, state: State[Int, String], values: List[Option[String]], types: List[Type])

  object Result {
    def from[A](field: String, value: A, encoder: Encoder[A]): Result =
      Result(field, encoder.sql, encoder.encode(value), encoder.types)
  }

  def apply[A](f: A => Result): SkunkPatchEncoder[A] = new SkunkPatchEncoder[A] {
    override def encode(value: A): Result = f(value)
  }
}
