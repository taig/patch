package io.taig.patch.util

import cats.effect.Resource
import cats.effect.kernel.Async
import cats.effect.std.Console
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import natchez.Trace.Implicits.noop
import skunk.Session

object Sessions {
  def embedded[F[_]: Console](implicit F: Async[F]): Resource[F, Session[F]] = {
    val builder = EmbeddedPostgres.builder().setPort(0)

    Resource.fromAutoCloseable(F.blocking(builder.start())).flatMap { postgres =>
      Session.single[F](
        host = "localhost",
        postgres.getPort,
        user = "postgres",
        database = "postgres",
        password = Some("postgres")
      )
    }
  }
}
