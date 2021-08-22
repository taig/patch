package io.taig.patch

import scala.collection.immutable.HashMap
import scala.util.chaining._

import cats.data.State
import cats.syntax.all._
import skunk.data.Type
import skunk.util.Origin
import skunk.{AppliedFragment, Encoder, Fragment}

object SkunkPatches {
  def unsafeUpdateFragment[A](patches: List[A], encoder: SkunkPatchEncoder[A]): Fragment[List[A]] = {
    val encodedPatches = patches
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

      val values = new Encoder[List[A]] {
        override def sql: State[Int, String] = patches.foldLeft(State.empty[Int, String]) { (state, patch) =>
          (state, lookup(patch).state).mapN((a, b) => s"$a, $b")
        }

        override def encode(a: List[A]): List[Option[String]] = patches.flatMap(lookup(_).values)

        override def types: List[Type] = patches.flatMap(lookup(_).types)
      }

      Fragment(parts, values, Origin.unknown)
    }
  }

  def updateFragment[A](patches: List[A], encoder: SkunkPatchEncoder[A]): Option[AppliedFragment] =
    Option.when(patches.nonEmpty)(AppliedFragment(unsafeUpdateFragment(patches, encoder), patches))
}
