package io.taig.patch

import cats.data.NonEmptyList
import io.circe.Decoder.Result
import io.circe.Json
import io.circe.syntax._
import io.taig.patch.circe._
import munit.FunSuite

final class JsonPatchTest extends FunSuite {
  val patches: List[PersonJsonPatch] = List(PersonJsonPatch.Name("Angelo Merte"), PersonJsonPatch.Address(None))

  val json: Json = Json.obj("name" := "Angelo Merte", "address" := None)

  implicit val codec: JsonPatchCodec[PersonJsonPatch] = new JsonPatchCodec[PersonJsonPatch] {
    override def decode(name: String, json: Json): Option[Result[PersonJsonPatch]] = name match {
      case "name"    => Some(json.as[String].map(PersonJsonPatch.Name.apply))
      case "age"     => Some(json.as[Int].map(PersonJsonPatch.Age.apply))
      case "address" => Some(json.as[Option[String]].map(PersonJsonPatch.Address.apply))
      case _         => None
    }

    override def encode(person: PersonJsonPatch): (String, Json) = person match {
      case PersonJsonPatch.Name(value)    => "name" -> value.asJson
      case PersonJsonPatch.Age(value)     => "age" -> value.asJson
      case PersonJsonPatch.Address(value) => "address" -> value.asJson
    }
  }

  test("encode") {
    assertEquals(obtained = patches.asJson, expected = json)
  }

  test("encode: duplicate") {
    assertEquals(obtained = (PersonJsonPatch.Name("Angela Merte") :: patches).asJson, expected = json)
  }

  test("encode: NonEmptyList") {
    assertEquals(obtained = NonEmptyList.fromListUnsafe(patches).asJson, expected = json)
  }

  test("decode") {
    assertEquals(obtained = json.as[List[PersonJsonPatch]], expected = Right(patches))
  }

  test("decode: NonEmptyList") {
    assertEquals(obtained = json.as[NonEmptyList[PersonJsonPatch]].map(_.toList), expected = Right(patches))
  }
}
