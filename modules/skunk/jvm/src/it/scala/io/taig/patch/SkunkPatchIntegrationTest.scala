package io.taig.patch

import cats.data.NonEmptyList
import cats.effect.IO
import cats.syntax.all._
import io.taig.patch.util.Sessions
import munit.CatsEffectSuite
import skunk.codec.all._
import skunk.implicits._
import skunk.{~, AppliedFragment, Command, Query, Void}

final class SkunkPatchIntegrationTest extends CatsEffectSuite {
  val createTable: Command[Void] =
    sql"""
    CREATE TABLE "member" (
      "identifier" SERIAL PRIMARY KEY,
      "name" TEXT NOT NULL,
      "age" INT2 NULL,
      "address" TEXT NULL
    );
    """.command

  val insertMember: Query[Void, Int] =
    sql"""
    INSERT INTO "member" ("name", "age", "address")
    VALUES ('Angelo Merge', 55, 'Willy-Brandt-Straße 1, 10557 Berlin')
    RETURNING "identifier";
    """.query(int4)

  def updateMember(patch: AppliedFragment): Command[Int] =
    sql"""
    UPDATE "member"
    SET ${patch.fragment}
    WHERE "identifier" = $int4;
    """.command.contramap(patch.argument ~ _)

  val selectMember: Query[Int, String ~ Option[Short] ~ Option[String]] =
    sql"""
    SELECT "name", "age", "address"
    FROM "member"
    WHERE "identifier" = $int4;
    """.query(text ~ int2.opt ~ text.opt)

  test("updateFragment") {
    Sessions.embedded[IO].use { session =>
      session.transaction.surround {
        for {
          _ <- session.execute(createTable)
          identifier <- session.prepare(insertMember).use(_.unique(Void))
          patches = NonEmptyList.of(PersonSkunkPatch.Name("Angela Merkel"), PersonSkunkPatch.Age(None))
          encoder = new SkunkPatchEncoder[PersonSkunkPatch] {
            override def encode(person: PersonSkunkPatch): SkunkPatchEncoder.Result = person match {
              case PersonSkunkPatch.Name(value)    => SkunkPatchEncoder.Result.from("name", value, text)
              case PersonSkunkPatch.Age(value)     => SkunkPatchEncoder.Result.from("age", value, int2.opt)
              case PersonSkunkPatch.Address(value) => SkunkPatchEncoder.Result.from("address", value, text.opt)
            }
          }
          fragment = SkunkPatches.updateFragment(patches, encoder)
          _ <- session.prepare(updateMember(fragment)).use(_.execute(identifier))
          obtained <- session.prepare(selectMember).use(_.option(identifier))
        } yield assertEquals(
          obtained,
          expected = Some("Angela Merkel" ~ none[Short] ~ Some("Willy-Brandt-Straße 1, 10557 Berlin"))
        )
      }
    }
  }

  test("updateFragment: duplicate") {
    Sessions.embedded[IO].use { session =>
      session.transaction.surround {
        for {
          _ <- session.execute(createTable)
          identifier <- session.prepare(insertMember).use(_.unique(Void))
          patches = NonEmptyList.of(PersonSkunkPatch.Name("Angela Merte"), PersonSkunkPatch.Name("Angela Merkel"))
          encoder = SkunkPatchEncoder[PersonSkunkPatch] {
            case PersonSkunkPatch.Name(value)    => SkunkPatchEncoder.Result.from("name", value, text)
            case PersonSkunkPatch.Age(value)     => SkunkPatchEncoder.Result.from("age", value, int2.opt)
            case PersonSkunkPatch.Address(value) => SkunkPatchEncoder.Result.from("address", value, text.opt)
          }
          fragment = SkunkPatches.updateFragment(patches, encoder)
          _ <- session.prepare(updateMember(fragment)).use(_.execute(identifier))
          obtained <- session.prepare(selectMember).use(_.option(identifier))
        } yield assertEquals(
          obtained,
          expected = Some("Angela Merkel" ~ 55.toShort.some ~ Some("Willy-Brandt-Straße 1, 10557 Berlin"))
        )
      }
    }
  }
}
