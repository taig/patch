package io.taig.patch

import scala.reflect.ClassTag

import io.circe.{Decoder, Json}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HNil, Inl, Inr, Lazy}

trait DerivedJsonPatchDecoder[A] extends JsonPatchDecoder[A]

object DerivedJsonPatchDecoder {
  implicit def patch[A, B](implicit
      naming: Naming = Naming.camelCase,
      tag: ClassTag[A],
      generic: Lazy[Generic.Aux[A, B :: HNil]],
      decoder: Decoder[B]
  ): DerivedJsonPatchDecoder[A] = new DerivedJsonPatchDecoder[A] {
    override def decode(name: String, json: Json): Option[Decoder.Result[A]] =
      Option.when(name == naming(tag.runtimeClass.getName)) {
        decoder.decodeJson(json).map(b => generic.value.from(b :: HNil))
      }
  }

  implicit val cnil: DerivedJsonPatchDecoder[CNil] = new DerivedJsonPatchDecoder[CNil] {
    override def decode(name: String, json: Json): Option[Decoder.Result[CNil]] = None
  }

  implicit def coproduct[A, B <: Coproduct](implicit
      head: DerivedJsonPatchDecoder[A],
      tail: DerivedJsonPatchDecoder[B]
  ): DerivedJsonPatchDecoder[A :+: B] = new DerivedJsonPatchDecoder[A :+: B] {
    override def decode(name: String, json: Json): Option[Decoder.Result[A :+: B]] =
      head.decode(name, json).map(_.map(Inl(_))).orElse(tail.decode(name, json).map(_.map(Inr(_))))
  }

  implicit def generic[A, B <: Coproduct](implicit
      generic: Generic.Aux[A, B],
      decoder: DerivedJsonPatchDecoder[B]
  ): DerivedJsonPatchDecoder[A] = new DerivedJsonPatchDecoder[A] {
    override def decode(name: String, json: Json): Option[Decoder.Result[A]] =
      decoder.decode(name, json).map(_.map(generic.from))
  }
}
