package io.taig.patch

import cats.data.{NonEmptyList, State}
import cats.syntax.all._
import skunk.data.Type
import skunk.util.Origin
import skunk.{AppliedFragment, Encoder, Fragment}

object SkunkPatches {
  def unsafeUpdateFragment[A](patches: NonEmptyList[A], encoder: SkunkPatchEncoder[A]): Fragment[NonEmptyList[A]] = {
    val results = patches.toList
      .flatMap(encoder.encode(_).toList)
      .reverse
      .distinctBy(_.field)
      .reverse

    val parts = results.mapWithIndex { (result, index) =>
      val prefix = if (index == 0) "" else ", "
      val sql = prefix + s"""\"${result.field}\" = """
      val state = State[Int, String](index => (index + 1, s"$$$index"))
      List(Left(sql), Right(state))
    }.flatten

    val values = new Encoder[NonEmptyList[A]] {
      override val sql: State[Int, String] = State.empty

      override def encode(a: NonEmptyList[A]): List[Option[String]] = results.map(_.value)

      override def types: List[Type] = results.map(_.tpe)
    }

    Fragment(parts, values, Origin.unknown)
  }

  def updateFragment[A](patches: NonEmptyList[A], encoder: SkunkPatchEncoder[A]): AppliedFragment =
    AppliedFragment(unsafeUpdateFragment(patches, encoder), patches)
}
