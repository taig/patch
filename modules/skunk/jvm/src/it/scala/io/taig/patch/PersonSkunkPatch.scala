package io.taig.patch

sealed abstract class PersonSkunkPatch extends Product with Serializable

object PersonSkunkPatch {
  final case class Name(value: String) extends PersonSkunkPatch
  final case class Age(value: Option[Short]) extends PersonSkunkPatch
  final case class Address(value: Option[String]) extends PersonSkunkPatch
}

sealed abstract class CombinedPersonSkunkPatch extends Product with Serializable

object CombinedPersonSkunkPatch {
  final case class NameAndAge(value: (String, Option[Short])) extends CombinedPersonSkunkPatch
  final case class Address(value: Option[String]) extends CombinedPersonSkunkPatch
}
