package io.taig.patch

import io.circe.{Encoder, Json}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HNil, Inl, Inr, Lazy}

trait DerivedJsonPatchEncoder[A] { self =>
  def encode(value: A): Json

  def toEncoder(names: A => String): JsonPatchEncoder[A] = new JsonPatchEncoder[A] {
    override def encode(value: A): (String, Json) = names(value) -> self.encode(value)
  }
}

object DerivedJsonPatchEncoder {
  implicit def patch[A, B](implicit
      generic: Lazy[Generic.Aux[A, B :: HNil]],
      encoder: Encoder[B]
  ): DerivedJsonPatchEncoder[A] = new DerivedJsonPatchEncoder[A] {
    override def encode(value: A): Json = encoder.apply(generic.value.to(value).head)
  }

  implicit val cnil: DerivedJsonPatchEncoder[CNil] = new DerivedJsonPatchEncoder[CNil] {
    override def encode(value: CNil): Json = value.impossible
  }

  implicit def coproduct[A, B <: Coproduct](implicit
      head: DerivedJsonPatchEncoder[A],
      tail: DerivedJsonPatchEncoder[B]
  ): DerivedJsonPatchEncoder[A :+: B] = new DerivedJsonPatchEncoder[A :+: B] {
    override def encode(value: A :+: B): Json = value match {
      case Inl(value) => head.encode(value)
      case Inr(value) => tail.encode(value)
    }
  }

  implicit def generic[A, B <: Coproduct](implicit
      generic: Generic.Aux[A, B],
      encoder: DerivedJsonPatchEncoder[B]
  ): DerivedJsonPatchEncoder[A] =
    new DerivedJsonPatchEncoder[A] {
      override def encode(value: A): Json = encoder.encode(generic.to(value))
    }
}
