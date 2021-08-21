package io.taig.patch

import scala.reflect.ClassTag

import io.circe.{Encoder, Json}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HNil, Inl, Inr, Lazy}

trait DerivedJsonPatchEncoder[A] extends JsonPatchEncoder[A]

object DerivedJsonPatchEncoder {
  implicit def patch[A, B](implicit
      naming: Naming = Naming.camelCase,
      tag: ClassTag[A],
      generic: Lazy[Generic.Aux[A, B :: HNil]],
      encoder: Encoder[B]
  ): DerivedJsonPatchEncoder[A] = new DerivedJsonPatchEncoder[A] {
    override def encode(value: A): (String, Json) =
      naming(tag.runtimeClass.getName) -> encoder.apply(generic.value.to(value).head)
  }

  implicit val cnil: DerivedJsonPatchEncoder[CNil] = new DerivedJsonPatchEncoder[CNil] {
    override def encode(value: CNil): (String, Json) = value.impossible
  }

  implicit def coproduct[A, B <: Coproduct](implicit
      head: DerivedJsonPatchEncoder[A],
      tail: DerivedJsonPatchEncoder[B]
  ): DerivedJsonPatchEncoder[A :+: B] = new DerivedJsonPatchEncoder[A :+: B] {
    override def encode(value: A :+: B): (String, Json) = value match {
      case Inl(value) => head.encode(value)
      case Inr(value) => tail.encode(value)
    }
  }

  implicit def generic[A, B <: Coproduct](implicit
      generic: Generic.Aux[A, B],
      encoder: DerivedJsonPatchEncoder[B]
  ): DerivedJsonPatchEncoder[A] = new DerivedJsonPatchEncoder[A] {
    override def encode(value: A): (String, Json) = encoder.encode(generic.to(value))
  }
}
