package generators.Http4s.RoundTrip

import cats.effect.IO
import form.client.http4sV022.foo.FooClient
import form.client.{ http4sV022 => cdefs }
import form.server.http4sV022.foo._
import form.server.http4sV022.foo.FooResource._
import form.server.{ http4sV022 => sdefs }
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.{ Method, Request, Status, Uri, UrlForm }
import org.scalatest.EitherValues
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class Http4sFormDataTest extends AnyFunSuite with Matchers with EitherValues {

  test("present required form param") {
    val fooClient = FooClient.httpClient(
      Client.fromHttpApp(
        new FooResource[IO]()
          .routes(new FooHandler[IO] {
            def doFoo(respond: DoFooResponse.type)(status: sdefs.definitions.Status, description: String): IO[DoFooResponse] =
              if (status == sdefs.definitions.Status.Ok) {
                IO.pure(respond.Ok)
              } else {
                IO.pure(respond.NotAcceptable)
              }
            def doBar(respond: DoBarResponse.type)(status: Option[sdefs.definitions.Status], description: Option[String]): IO[DoBarResponse] = ???
            def doBaz(respond: DoBazResponse.type)(status: Iterable[String], description: Option[Iterable[String]]): IO[DoBazResponse]       = ???
          })
          .orNotFound
      )
    )
    fooClient.doFoo(cdefs.definitions.Status.Ok, "Description").attempt.unsafeRunSync().value shouldBe cdefs.foo.DoFooResponse.Ok
  }

  test("missing required form param") {
    val client = Client.fromHttpApp(
      new FooResource[IO]()
        .routes(new FooHandler[IO] {
          def doFoo(respond: DoFooResponse.type)(status: sdefs.definitions.Status, description: String): IO[DoFooResponse] =
            if (status == sdefs.definitions.Status.Ok) {
              IO.pure(respond.Ok)
            } else {
              IO.pure(respond.NotAcceptable)
            }
          def doBar(respond: DoBarResponse.type)(status: Option[sdefs.definitions.Status], description: Option[String]): IO[DoBarResponse] = ???
          def doBaz(respond: DoBazResponse.type)(status: Iterable[String], description: Option[Iterable[String]]): IO[DoBazResponse]       = ???
        })
        .orNotFound
    )
    val req = Request[IO](method = Method.POST, uri = Uri.unsafeFromString("http://localhost:1234/foo")).withEntity(UrlForm.empty)
    client.run(req).use(r => IO(r.status)).unsafeRunSync() shouldBe Status.BadRequest
  }

  test("present optional form param") {
    val fooClient = FooClient.httpClient(
      Client.fromHttpApp(
        new FooResource[IO]()
          .routes(new FooHandler[IO] {
            def doFoo(respond: DoFooResponse.type)(status: sdefs.definitions.Status, description: String): IO[DoFooResponse] = ???
            def doBar(respond: DoBarResponse.type)(status: Option[sdefs.definitions.Status], description: Option[String]): IO[DoBarResponse] =
              if (status.contains(sdefs.definitions.Status.Ok)) {
                IO.pure(respond.Ok)
              } else {
                IO.pure(respond.NotAcceptable)
              }
            def doBaz(respond: DoBazResponse.type)(status: Iterable[String], description: Option[Iterable[String]]): IO[DoBazResponse] = ???
          })
          .orNotFound
      )
    )
    fooClient.doBar(Some(cdefs.definitions.Status.Ok)).attempt.unsafeRunSync().value shouldBe cdefs.foo.DoBarResponse.Ok
  }

  test("missing optional form param") {
    val fooClient = FooClient.httpClient(
      Client.fromHttpApp(
        new FooResource[IO]()
          .routes(new FooHandler[IO] {
            def doFoo(respond: DoFooResponse.type)(status: sdefs.definitions.Status, description: String): IO[DoFooResponse] = ???

            def doBar(respond: DoBarResponse.type)(status: Option[sdefs.definitions.Status], description: Option[String]): IO[DoBarResponse] =
              if (status.isEmpty) {
                IO.pure(respond.Ok)
              } else {
                IO.pure(respond.NotAcceptable)
              }
            def doBaz(respond: DoBazResponse.type)(status: Iterable[String], description: Option[Iterable[String]]): IO[DoBazResponse] = ???
          })
          .orNotFound
      )
    )
    fooClient.doBar(None).attempt.unsafeRunSync().value shouldBe cdefs.foo.DoBarResponse.Ok
  }

  test("present required multi form String param") {
    val fooClient = FooClient.httpClient(
      Client.fromHttpApp(
        new FooResource[IO]()
          .routes(new FooHandler[IO] {
            def doFoo(respond: DoFooResponse.type)(status: sdefs.definitions.Status, description: String): IO[DoFooResponse]                 = ???
            def doBar(respond: DoBarResponse.type)(status: Option[sdefs.definitions.Status], description: Option[String]): IO[DoBarResponse] = ???
            def doBaz(respond: DoBazResponse.type)(status: Iterable[String], description: Option[Iterable[String]]): IO[DoBazResponse] =
              if (status.size == 1 && status.iterator.next() == sdefs.definitions.Status.Ok.toString) {
                IO.pure(respond.Ok)
              } else {
                IO.pure(respond.NotAcceptable)
              }
          })
          .orNotFound
      )
    )
    fooClient.doBaz(Seq(cdefs.definitions.Status.Ok.toString)).attempt.unsafeRunSync().value shouldBe cdefs.foo.DoBazResponse.Ok
  }
}
