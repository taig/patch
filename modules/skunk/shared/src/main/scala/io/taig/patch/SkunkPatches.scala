package io.taig.patch

import scala.collection.immutable.HashMap
import scala.util.chaining._
import cats.data.{NonEmptyList, State}
import cats.syntax.all._
import skunk.data.Type
import skunk.util.Origin
import skunk.{AppliedFragment, Encoder, Fragment}

object SkunkPatches {
  def unsafeUpdateFragment[A](patches: NonEmptyList[A], encoder: SkunkPatchEncoder[A]): Fragment[NonEmptyList[A]] = {
    val encodedPatches = patches.toList
      .map(patch => (patch, encoder.encode(patch)))
      .reverse
      .distinctBy { case (_, result) => result.field }
      .reverse

    val lookup = encodedPatches.to(HashMap)

    encodedPatches.map { case (patch, _) => patch }.pipe { patches =>
      val parts = patches.mapWithIndex { (patch, index) =>
        val prefix = if (index == 0) "" else ", "
        val sql = prefix + s"""\"${lookup(patch).field}\" = """
        val state = State[Int, String](index => (index + 1, s"$$$index"))
        List(Left(sql), Right(state))
      }.flatten

      val values = new Encoder[NonEmptyList[A]] {
        override def sql: State[Int, String] = patches.foldLeft(State.empty[Int, String]) { (state, patch) =>
          (state, lookup(patch).state).mapN((a, b) => s"$a, $b")
        }

        override def encode(a: NonEmptyList[A]): List[Option[String]] = patches.flatMap(lookup(_).values)

        override def types: List[Type] = patches.flatMap(lookup(_).types)
      }

      Fragment(parts, values, Origin.unknown)
    }
  }

  def updateFragment[A](patches: NonEmptyList[A], encoder: SkunkPatchEncoder[A]): AppliedFragment =
    AppliedFragment(unsafeUpdateFragment(patches, encoder), patches)
}
