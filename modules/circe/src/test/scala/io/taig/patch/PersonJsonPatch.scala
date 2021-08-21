package io.taig.patch

sealed abstract class PersonJsonPatch extends Product with Serializable

object PersonJsonPatch {
  final case class Name(value: String) extends PersonJsonPatch
  final case class Age(value: Int) extends PersonJsonPatch
  final case class Address(value: Option[String]) extends PersonJsonPatch
}
